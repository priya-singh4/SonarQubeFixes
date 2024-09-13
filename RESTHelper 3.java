package apiutilities.apihelpers;



import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import java.util.Arrays;
import java.util.Map;

import apiutilities.models.APIModel;
import apiutilities.testsettings.APITestSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RESTHelper 
{

	
	    private final Logger logger = LoggerFactory.getLogger(RESTHelper.class.getName());

	    public Response executRESTAPI(APIModel iModel, String apiType) throws IOException  {
	        Response responsePost = null;

	        Map<String, String> defaultHeaders = iModel.getHeaderData();
	        String jsonDataInFile = iModel.gettRequestData();
	        String certificatePath = iModel.getCertificatePath();
	        String certificatePassword = iModel.getCertificatePassword();
	        String serviceURL = iModel.getServiceUrl();

	        if (APITestSettings.isKeyStoreConfigurationRequired()) {
	            loadKeyStore(certificatePath, certificatePassword);
	        }

	        if (iModel.isMockRequired()) {
	            createMockFile(iModel);
	        }

	        if (defaultHeaders.isEmpty()) {
	            responsePost = executeRequestWithoutHeaders( apiType, jsonDataInFile,   serviceURL);
	        } else {
	            responsePost = executeRequestWithHeaders(apiType, jsonDataInFile, serviceURL, defaultHeaders);
	        }

	        logRequestResponse(iModel, jsonDataInFile);

	        return responsePost;
	    }

	    private void loadKeyStore(String certificatePath, String certificatePassword) {
	        try (FileInputStream fis = new FileInputStream(certificatePath)) {
	            KeyStore keyStore = KeyStore.getInstance("PKCS12");
	            keyStore.load(fis, certificatePassword.toCharArray());
	        } catch (Exception ex) {
	            logger.info("Error while loading keystore >>>>>>>>>");
	            ex.printStackTrace();
	        }
	    }


	    private void createMockFile(APIModel iModel) throws IOException {
	    	
	    	Path mockFileLocation = Paths.get(iModel.getMockData().getMockLocation());
	        String fileContent = iModel.getMockData().getMockFileContent();
	    	
	        if (!Files.isWritable(mockFileLocation.getParent())) {
	            throw new SecurityException("Invalid file path or insufficient permissions");
	        }
	        try (FileWriter file = new FileWriter(mockFileLocation.toFile())) {
	            file.write(fileContent);
	        } catch (Exception e) {
	            logger.error("Unable to create the mock File===> {}", iModel.getMockData().getMockFileName());
	            logger.error(e.getMessage());
	            logger.error(Arrays.toString(e.getStackTrace()));
	        }
	    }

	    private Response executeRequestWithoutHeaders( String apiType, String jsonDataInFile,  String serviceURL) {
	        return executeRequest(apiType, jsonDataInFile, serviceURL, null);
	    }

	    private Response executeRequestWithHeaders( String apiType, String jsonDataInFile, String serviceURL, Map<String, String> defaultHeaders) {
	       
	        return executeRequest(apiType, jsonDataInFile, serviceURL, defaultHeaders);
	    }

	    private Response executeRequest(String apiType, String jsonDataInFile, String serviceURL, Map<String, String> headers) {
	        RequestSpecification request = RestAssured.given()
	                .urlEncodingEnabled(false)
	                .relaxedHTTPSValidation()
	                .contentType(ContentType.JSON)
	                .body(jsonDataInFile);

	        if (headers != null) {
	            request.headers(headers);
	        }

	        switch (apiType.toUpperCase()) {
	            case "GET":
	                return request.get(serviceURL);
	            case "POST":
	                return request.post(serviceURL);
	            case "PUT":
	                return request.put(serviceURL);
	            case "PATCH":
	                return request.patch(serviceURL);
	            case "DELETE":
	                return request.delete(serviceURL);
	            default:
	                throw new IllegalArgumentException("Invalid API type: " + apiType);
	        }
	    }

	    private void logRequestResponse(APIModel iModel, String jsonDataInFile) {
	        logger.info("-------{} ==> {} ------", iModel.getModule(), iModel.getInterfaceName() + "--------------");
	        logger.info("------------------------serviceURL----------------------------------------");
	        logger.info("------------------------APITestSettings.getTrustStoreLocation()-----------------------------------------");
	        logger.info("------------------------APITestSettings.getTrustStorePassword()-----------------------------------------");
	        logger.info("------------------------certificatePath-----------------------------------------");
	        logger.info("------------------------certificatePassword-----------------------------------------");
	        logger.info("--------------------------Request--------------------------");
	        logger.info(jsonDataInFile);
	        logger.info("--------------------------Response--------------------------");
	        logger.info("-------{} ==> {} ------", iModel.getModule(), iModel.getInterfaceName() + "--------------");
	        logger.info("------------------------serviceURL-----------------------------------------");
	        logger.info("--------------------------Request--------------------------");
	        logger.info(jsonDataInFile);
	        logger.info("--------------------------Response--------------------------");
	    }
	}

