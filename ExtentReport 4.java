package apiutilities.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

import apiutilities.models.APIReportModel;
import apiutilities.testsettings.APITestSettings;

public class ExtentReport {


	
	private static ExtentReports extent;
	protected static final Map<Integer, ExtentTest> dictExtentTestScenario = new HashMap<>();
	protected static final Map<Integer, String> dictExtentTestCase = new HashMap<>();
	protected static final Map<String, ExtentTest> tcExtentMapping = new HashMap<>();	
	private static final Logger logger =LoggerFactory.getLogger(ExtentReport.class.getName());

	public static void startReport(String reportPath, String hostName, String environment, String userName)
	{
           ExtentHtmlReporter htmlReporter;
		try
		{
			reportPath = reportPath + "\\ExtentReport.html"; 
			htmlReporter = new ExtentHtmlReporter(reportPath);
			extent = new ExtentReports();
			extent.attachReporter(htmlReporter);
			extent.setSystemInfo("Host Name", hostName);
			extent.setSystemInfo("Environment", environment);
			extent.setSystemInfo("Username", userName);
		}
		catch(Exception e)
		{
			logger.info("Unable to initialize the Extent Report");
			logger.info(e.getMessage());
			logger.info(Arrays.toString(e.getStackTrace()));

		}
	}

	public void jenkinsReport(String reportPath) {
		reportPath = reportPath + "\\ExtentReport.html"; 
		String destinationPath=APITestSettings.getHomePath()+"\\JenkinsResult";
		String report = "\\ExtentReportAPI.html";

		try {
			Path source = Path.of(reportPath);
			Path destination = Path.of(destinationPath+report);

			Files.createDirectories(destination);

			File file = new File(destinationPath, report);
			if (file.exists()) {
				 Files.delete(destination);
			} 

			Files.copy(source, destination);
            logger.info("File copied successfully.");
		} catch (IOException e) {
			logger.info("An error occurred while copying the file: {} ", e.getMessage());
		}
	}


	public ExtentTest startTest(String testCase,String userStory, String module, String testCaseDescription)
	{
		try
		{
			ExtentTest tc = extent.createTest(testCase, testCaseDescription);
			ExtentTest test =tc.createNode("Iteration==>1");
			test.assignCategory(userStory);
			test.assignCategory(module);

			return test;

		}
		catch(Exception e)
		{
			logger.info("Unable to initialize the Extent Test Case ==> {} ", testCase );
			logger.info(e.getMessage());
			logger.info(Arrays.toString(e.getStackTrace()));
			

		}
		return null;
	}




	public void endReport()

	{

		extent.flush();

	}


	public void addTestStep(ExtentTest extentTest, String module, String apiName, String iteration,List<APIReportModel> reportData) throws InterruptedException
	{

		ExtentTest node=extentTest.createNode(module + "===>" + apiName +"==>" + iteration);
		ExtentTest requestNode=node.createNode("Request");
		if(reportData.get(0).getRequest().contains("<")) {
			reportData.get(0).setRequest("<textarea style='color:black'>"+reportData.get(0).getRequest()+"</textarea>");
		}
		requestNode.log(Status.INFO , MarkupHelper.createLabel(reportData.get(0).getRequest(),ExtentColor.GREY));
		if(reportData.get(0).getResponse().contains("<")) {
			reportData.get(0).setResponse("<textarea style='color:black'>"+reportData.get(0).getResponse()+"</textarea>");
		}
		ExtentTest responseNode=node.createNode("Response");
		responseNode.log(Status.INFO , MarkupHelper.createLabel( reportData.get(0).getResponse(),ExtentColor.GREY));

		ExtentTest resultNode=node.createNode("Results");

		
		for(APIReportModel apiReportModel:reportData)
		{
			
			Status extentStatus = Status.INFO;
			if(reportData.get(0).getTestStepResult().equalsIgnoreCase("PASS"))
			{
				extentStatus=Status.PASS;
			}
			else if(reportData.get(0).getTestStepResult().equalsIgnoreCase("FAIL"))
			{
				extentStatus=Status.FAIL;				
			}

			Thread.sleep(0);
			if(!(reportData.get(0).getActualResponse().equals(""))) {
				ExtentTest stepResult=resultNode.createNode("Result");
				stepResult.log(Status.INFO,"Status Info :  "+ reportData.get(0).getStatusCode());
				stepResult.log(Status.INFO,"Validation :  "+ apiReportModel.getXpathJsonKey());
				stepResult.log(Status.INFO,"<br> Expected Results <br>"+ apiReportModel.getExpectedResponse());
				stepResult.log(extentStatus,"Actual Results <br>"+ apiReportModel.getActualResponse());
			}
		}

		if(APITestSettings.DBVALIDATION.equalsIgnoreCase("Yes")) {
			Status extentStatus = Status.INFO;
			if(reportData.get(reportData.size()-1).getTestStepResult().equalsIgnoreCase("PASS"))
			{
				extentStatus=Status.PASS;
			}
			else if(reportData.get(reportData.size()-1).getTestStepResult().equalsIgnoreCase("FAIL"))
			{
				extentStatus=Status.FAIL;				
			}
			ExtentTest dbValidation=node.createNode("DB Validation Result");
			dbValidation.log(Status.INFO,"Query Validation :  "+ reportData.get(reportData.size()-1).getDbValidation());
			dbValidation.log(Status.INFO,"<br> Expected DB Results <br>"+ reportData.get(reportData.size()-1).getDbExpectedValue());
			dbValidation.log(extentStatus,"Actual DB Results <br>"+ reportData.get(reportData.size()-1).getDbActualValue());
		}
	}


	public String writeDataToTextFile(String filePath, String fileName,String fileContent,String fileFormat)
	{
		filePath = filePath + File.separator + fileName + fileFormat;
		Path path = Paths.get(filePath).normalize();
        if (!Files.isWritable(path.getParent())) {
            throw new SecurityException("Invalid file path or insufficient permissions");
        }

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile())))
		{
			writer.write(fileContent);

			filePath= filePath.replace("\\","/");

			filePath = "<a href = 'file:///"+ filePath + "'>"+ fileName + "</a>";
			return filePath;
		}

		catch (Exception e)
		{
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