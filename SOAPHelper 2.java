package apiutilities.apihelpers;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apiutilities.models.APIModel;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class SOAPHelper 
{
	
	final Logger logger = LoggerFactory.getLogger(SOAPHelper.class.getName());

	public Response executeSOAPAPI(APIModel iModel)
	{
		Response responsePost ;

		Map<String, String>  defaultHeaders = iModel.getHeaderData();
		String jsonDataInFile="";
		 jsonDataInFile=iModel.gettRequestData();
		String certificatePath=iModel.getCertificatePath();
		String certificatePassword=iModel.getCertificatePassword();		
		KeyStore keyStore = null;
		Path mockFileLocation = Paths.get(iModel.getMockData().getMockLocation());
        String fileContent = iModel.getMockData().getMockFileContent();
    	
        
		try (FileInputStream fileInput  =  new FileInputStream(certificatePath)){
		        keyStore = KeyStore.getInstance("PKCS12");
		        keyStore.load(fileInput,certificatePassword.toCharArray());

		    } catch (Exception ex) {
		        logger.error("Error while loading keystore >>>>>>>>>");
		        ex.printStackTrace();
		    }																					  
		if (iModel.isMockRequired())
		{
			if (!Files.isWritable(mockFileLocation.getParent())) {
	            throw new SecurityException("Invalid file path or insufficient permissions");
	        }
			try (FileWriter file = new FileWriter(mockFileLocation.toFile())) {
			    file.write(fileContent);
			    file.flush();
			} 
			catch (Exception e)
			{
				logger.error("Unable to create the mock File===> {}",iModel.getMockData().getMockFileName());
				logger.error(e.getMessage());
				logger.error(Arrays.toString(e.getStackTrace()));
				
			
			}
		}

		if(defaultHeaders.isEmpty())
		{
			if(iModel.isCertificateRequired())
			{
							responsePost =		
			RestAssured.given()
            .urlEncodingEnabled(false)
            .relaxedHTTPSValidation()
            .body(jsonDataInFile)
            .config(RestAssured.config())
            .contentType(ContentType.XML)
            .expect()
            .when()
			.post(iModel.getServiceUrl());
			}
			else
			{
				responsePost =		
						RestAssured.given()
			            .urlEncodingEnabled(false)
			            .relaxedHTTPSValidation()
			            .body(jsonDataInFile)
			            .contentType(ContentType.XML)
			            .expect()
			            .when()
						.post(iModel.getServiceUrl());	
			}
		}
			
		else
		{
				responsePost =
						RestAssured.given()
			            .urlEncodingEnabled(false)
			            .relaxedHTTPSValidation()
			            .body(jsonDataInFile)
			            .config(RestAssured.config())
			            .contentType(ContentType.XML)
								.headers(defaultHeaders)
					            .expect()
					            .when()
								.post(iModel.getServiceUrl());
				
		}
		
		logger.info(iModel.getModule(),  " {}-------------- ==> {}", iModel.getInterfaceName());
		
		logger.info("--------------------------Request--------------------------");
		
		logger.info(jsonDataInFile);
		
		logger.info("--------------------------Response--------------------------");
		
		logger.info(iModel.getModule(),  " {}-------------- ==> {}", iModel.getInterfaceName());
		
		logger.info("--------------------------Request--------------------------");
		
		logger.info(jsonDataInFile);
		
		logger.info("--------------------------Response--------------------------");

		return responsePost;
		
	}	 
}