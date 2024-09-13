package apiutilities.apihelpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apiutilities.models.APIReportModel;
import apiutilities.testsettings.APITestSettings;
import reportutilities.common.ReportCommon;
import reportutilities.constants.ReportContants;
import reportutilities.model.TestCaseParam;

public class APIReport {

	private static final Logger logger =LoggerFactory.getLogger(APIReport.class.getName());
	public void addTestStep(TestCaseParam testCaseParam,List<APIReportModel> apiReportModels) 
	{
		
		ReportCommon reportCommon = new ReportCommon();
		String request = "Request";
		reportCommon.logAPIModule(testCaseParam, testCaseParam.getModuleName() + "===>" + testCaseParam.getApiName() +"==>" + testCaseParam.getIteration());
		

		reportCommon.logAPIModule(testCaseParam, request);
		if(apiReportModels.get(0).getRequest().contains("<")) {
			apiReportModels.get(0).setRequest("<textarea style='color:black'>"+apiReportModels.get(0).getRequest()+"</textarea>");
		}
		
		reportCommon.logAPITestStep(testCaseParam,request +apiReportModels.get(0).getRequest(),request +apiReportModels.get(0).getRequest(),LocalDateTime.now(),ReportContants.getStatusDone(),"","");
		if(apiReportModels.get(0).getResponse().contains("<")) {
			apiReportModels.get(0).setResponse("<textarea style='color:black'>"+apiReportModels.get(0).getResponse()+"</textarea>");
		}
		reportCommon.logAPITestStep(testCaseParam,"Response" +apiReportModels.get(0).getRequest(),"Response" +apiReportModels.get(0).getResponse(),LocalDateTime.now(),ReportContants.getStatusDone(),"","");

		
		
		reportCommon.logAPIModule(testCaseParam,"Results");

		for(APIReportModel apiReportModel:apiReportModels)
		{
			String apiStatus = ReportContants.getStatusDone();
			if(apiReportModels.get(0).getTestStepResult().equalsIgnoreCase("PASS"))
			{
				apiStatus=ReportContants.getStatusPass();
			}
			else if(apiReportModels.get(0).getTestStepResult().equalsIgnoreCase("FAIL"))
			{
				apiStatus=ReportContants.getStatusFail();				
			}

			if(!(apiReportModels.get(0).getActualResponse().equals(""))) {

				reportCommon.logAPITestStep(testCaseParam,"Status Info :  "+ apiReportModels.get(0).getStatusCode(),"Status Info :  "+ apiReportModels.get(0).getStatusCode(),LocalDateTime.now(),ReportContants.getStatusDone(),"","");
				reportCommon.logAPITestStep(testCaseParam,"Validation :  "+ apiReportModel.getXpathJsonKey(),"Validation :  "+ apiReportModel.getXpathJsonKey(),LocalDateTime.now(),ReportContants.getStatusDone(),"","");
				reportCommon.logAPITestStep(testCaseParam,"<br> Expected Results <br>"+ apiReportModel.getExpectedResponse(),"<br> Expected Results <br>"+ apiReportModel.getExpectedResponse(),LocalDateTime.now(),ReportContants.getStatusDone(),"","");
				reportCommon.logAPITestStep(testCaseParam,"Actual Results <br>"+ apiReportModel.getActualResponse(),"Actual Results <br>"+ apiReportModel.getActualResponse(),LocalDateTime.now(),apiStatus,"","");

			}
		}

		if(APITestSettings.DBVALIDATION.equalsIgnoreCase("Yes")) {
			String apiStatus = ReportContants.getStatusDone();
			if(apiReportModels.get(apiReportModels.size()-1).getTestStepResult().equalsIgnoreCase("PASS"))
			{
				apiStatus=ReportContants.getStatusPass();
			}
			else if(apiReportModels.get(apiReportModels.size()-1).getTestStepResult().equalsIgnoreCase("FAIL"))
			{
				apiStatus=ReportContants.getStatusFail();				
			}
			
			reportCommon.logAPIModule(testCaseParam,"DB Validation Result");

			reportCommon.logAPITestStep(testCaseParam,"Query Validation :  "+ apiReportModels.get(apiReportModels.size()-1).getDbValidation(),"Query Validation :  "+ apiReportModels.get(apiReportModels.size()-1).getDbValidation(),LocalDateTime.now(),ReportContants.getStatusDone(),"","");
			reportCommon.logAPITestStep(testCaseParam,"<br> Expected DB Results <br>"+ apiReportModels.get(apiReportModels.size()-1).getDbExpectedValue(),"<br> Expected DB Results <br>"+ apiReportModels.get(apiReportModels.size()-1).getDbExpectedValue(),LocalDateTime.now(),ReportContants.getStatusDone(),"","");
			reportCommon.logAPITestStep(testCaseParam,"Actual DB Results <br>"+ apiReportModels.get(apiReportModels.size()-1).getDbActualValue(),"Actual DB Results <br>"+ apiReportModels.get(apiReportModels.size()-1).getDbActualValue(),LocalDateTime.now(),apiStatus,"","");
			
		}
	}


	public String writeDataToTextFile(String filePath, String fileName, String fileContent, String fileFormat) {
	    String dele = "/";
		filePath = filePath + dele + fileName + fileFormat;
		
		Path path = Paths.get(filePath).normalize();

        if (!Files.isWritable(path.getParent())) {
            throw new SecurityException("Invalid file path or insufficient permissions");
        }
		
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
	        writer.write(fileContent);
	        filePath = filePath.replace("\\", "/");
	        filePath = "<a href = 'file:///" + filePath + "'>" + fileName + "</a>";
	        return filePath;
	    } catch (IOException e) {
	        logger.error("Exception while writing data to file: {}", e.getMessage(), e);
	        return filePath;
	    }
	}


	public String writeImageToReport(String filePath, String fileName)
	{

		try
		{
			filePath = filePath.replace("\\", "/");
			filePath = "<a href = 'file:///" + filePath + "'>" + fileName + "</a>";
			return filePath;
		}

		catch (Exception e)
		{
			return filePath;
		}
	}



}