package apiutilities.apihelpers;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apiutilities.apicommon.APIMaster;
import apiutilities.models.APIModel;
import apiutilities.models.APIReportModel;
import apiutilities.testsettings.APISessionData;
import apiutilities.testsettings.APITestSettings;
import io.restassured.response.Response;

public class APIController {
	private static final Logger logger =LoggerFactory.getLogger(APIController.class.getName());

	public List<APIReportModel> executeAPI(String testcase,String moduleName, String interfaceName, String iteration,String apiFilePath)
	{
		APIMaster interfaceMaster = new APIMaster();
		List<APIReportModel> apiReportModels = new ArrayList<>();
		Response response = null;
		try {
			APIModel iModel=	interfaceMaster.fetchInterfaceRepository(APITestSettings.getApiTestSettings(),testcase,moduleName,interfaceName,iteration,apiFilePath);

			RESTHelper apiREST = new RESTHelper();

			String apiType=iModel.getInterfaceType();
			String methodType=iModel.getMethodType();
			DBValidation validateDB = new DBValidation();
			String request=iModel.gettRequestData();
			String expectedResponse=iModel.getResponseData();
			String url= iModel.getServiceUrl();
			String dbQuery = iModel.getDbQuery();
			
			if(apiType.equalsIgnoreCase("REST"))
			{
				response=apiREST.executRESTAPI(iModel,methodType);	
				APIReportModel apiReportModel = new APIReportModel();
				apiReportModel.setUrl(url);
				apiReportModel.setRequest(request);
				apiReportModel.setResponse(response.getBody().asString());
				apiReportModel.setStatusCode("Status Code: =>"+response.getStatusCode()+"; Status Line: =>"+response.getStatusLine());


				if(response.statusCode()==200)
				{
					apiReportModels = extracted(testcase, moduleName,  iteration,  response,
							iModel,  expectedResponse, apiReportModel);
				}
				else
				{
					apiReportModels.add(apiReportModel);
					apiReportModels.get(0).setTestStepResult("FAIL");
					apiReportModels.get(0).setAdditionalDetails(iModel.getInterfaceName() + " Failed. Response Code : +" +response.statusCode() + "Error Details : " + response.asString());
					
				}
				//DB VALIDATION
				if (APITestSettings.DBVALIDATION.equalsIgnoreCase("Yes") && !dbQuery.equalsIgnoreCase("N//A")) {
						apiReportModel.setDbActualValue(validateDB.validateDBStatus(iModel));
						apiReportModel.setDbExpectedValue(iModel.getDBExpectedValue());
						apiReportModel.setDbValidation(iModel.getDbQuery());
						if(iModel.getDBExpectedValue().equals(apiReportModel.getDbActualValue())) {
							apiReportModels.get(0).setTestStepResult("PASS");
							apiReportModels.get(0).setAdditionalDetails(apiReportModel.getAdditionalDetails()+":DB Validation was successful.");
						}
						else
						{
							apiReportModels.get(0).setTestStepResult("FAIL");
							apiReportModels.get(0).setAdditionalDetails(apiReportModel.getAdditionalDetails()+":Expected value mismatch in DB validation");
						}

					}
				

			}
			else if(apiType.equalsIgnoreCase("SOAP"))
			{

				extracted(testcase, moduleName,  iteration,  iModel, 
						 request, expectedResponse, url);
			}
			
		} catch (Exception e) {
		
			e.printStackTrace();
		}

		return apiReportModels;

	}

	private List<APIReportModel> extracted(String testcase, String moduleName,  String iteration,
			 Response response, APIModel iModel,String expectedResponse, APIReportModel apiReportModel) {
		
		List<APIReportModel> apiReportModels= new ArrayList<>();
		RESTValidation validateREST= new RESTValidation();
		
		if(!iModel.getResponsePath().equals(("")))
		{
			apiReportModels=validateREST.validateAPIPostResponse(apiReportModel, expectedResponse);

		}
		else if(!iModel.getResponseValidationModel().getxPathJsonPath().isEmpty())
		{
			apiReportModels=validateREST.validateJSONPath(apiReportModel,iModel.getResponseValidationModel().getxPathJsonPath());


		}
 
		else {
		Map<String, String> sessionData = validateREST.storeJSONPath(response.getBody().asString(), iModel.getStoreResponseModel().getXpathJsonPath());


		APISessionData.setSessionDataCollection(testcase, moduleName, "", iteration, sessionData);

		apiReportModel.setTestStepResult("PASS");
		apiReportModel.setAdditionalDetails(iModel.getInterfaceName() + " executed successfully.");
		apiReportModels.add(apiReportModel);
		}
		return apiReportModels;
	}

	private List<APIReportModel> extracted(String testcase, String moduleName, String iteration,
			 APIModel iModel, 
			  String request, String expectedResponse, String url){
		
		List<APIReportModel> apiReportModels= new ArrayList<>();
		SOAPHelper apiSOAP= new SOAPHelper();
		SOAPValidations validateSOAP= new SOAPValidations();
		Response response;
		response=apiSOAP.executeSOAPAPI(iModel);	
		APIReportModel apiReportModel = new APIReportModel();
		apiReportModel.setUrl(url);
		apiReportModel.setRequest(request);
		apiReportModel.setResponse(response.getBody().asString());	
		if(response.statusCode()==200)
		{
			if(!iModel.getResponsePath().equals(("")))
			{
				apiReportModels=validateSOAP.validateAPIPostResponse(apiReportModel, expectedResponse);
			}
			else if(!iModel.getResponseValidationModel().getxPathJsonPath().isEmpty())
			{

				apiReportModels=validateSOAP.validateXPath(apiReportModel,iModel.getResponseValidationModel().getxPathJsonPath());
			}

			else {
			Map<String, String> sessionData = validateSOAP.storeXPath(response.getBody().asString(), iModel.getStoreResponseModel().getXpathJsonPath());


			APISessionData.setSessionDataCollection(testcase, moduleName, "", iteration, sessionData);
			apiReportModel.setTestStepResult("PASS");
			apiReportModel.setAdditionalDetails(iModel.getInterfaceName() + " executed successfully.");
			apiReportModels.add(apiReportModel);
			}

		}
		else
		{
			apiReportModels.add(apiReportModel);
			apiReportModels.get(0).setTestStepResult("FAIL");
			apiReportModels.get(0).setAdditionalDetails(iModel.getInterfaceName() + " Failed. Response Code : +" +response.statusCode() + "Error Details : " + response.asString());
			
		}
		return apiReportModels;

	}

	/**
	 * Executes the API for the given module and interface.
	 *
	 * @param moduleName   the name of the module
	 * @param interfaceName the name of the interface
	 * @throws UnsupportedOperationException if this method is not overridden and implemented
	 */
	public void executeAPI(String moduleName, String interfaceName) {
	    /*
	     * This method is intended to be overridden by subclasses or other classes
	     * to provide the actual implementation for executing the API based on the
	     * given module and interface names. The default implementation throws an
	     * UnsupportedOperationException to indicate that it should not be called
	     * directly.
	     */
	    throw new UnsupportedOperationException("This method should be overridden and implemented.");
	}



	public void storeRESTResponse(String responseLocation, Response responsePost) {
		
		Path path = Paths.get(responseLocation).normalize();
        if (!Files.isWritable(path.getParent())) {
            throw new SecurityException("Invalid file path or insufficient permissions");
        }
	    try (FileWriter file = new FileWriter(path.toFile())) {
	        file.write(responsePost.prettyPrint());
	        file.flush();
	    } catch (IOException e) {
	        logger.error("Exception while storing REST response: {}", e.getMessage(), e);
	    }
	}

}