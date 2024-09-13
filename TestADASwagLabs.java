package adautilities;

import com.deque.html.axecore.results.Node;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestADASwagLabs {
	private static final Logger logger =LoggerFactory.getLogger(TestADASwagLabs.class.getName());
	
	static List<String> tags = Arrays.asList("best-practice"); 

	public static void main(String[] args) throws InterruptedException {

		System.setProperty("webdriver.gecko.driver", "C:\\Users\\ronashah\\Desktop\\project\\GPS\\Workspace\\GPSCommon\\GPSAutomation\\GPSCommon\\src\\main\\resources\\BrowserDrivers\\geckodriver.exe");
             
        WebDriver driver = new FirefoxDriver();
        
        driver.get("https://www.saucedemo.com/v1/index.html");
        adaScan(driver,"Login");
        
        driver.findElement(By.xpath("//input[@name='user-name']")).sendKeys("standard_user");
        driver.findElement(By.xpath("//input[@name='password']")).sendKeys("secret_sauce");
        driver.findElement(By.xpath("//input[@id='login-button']")).click();
      
        Thread.sleep(10000);
        adaScan(driver,"Cart");
   
	    driver.quit();
	}

	public static void adaScan(WebDriver driver,String screenName)
	{
		  String strHelp = "";
	        String strImpact = "";
	        String strDescription = "";
	        String strHelpUrl = "";
	        String strId = "";
	        String strTags = "";
	     
	        StringBuilder axeResults = new StringBuilder();
	     
	               AxeBuilder builder = new AxeBuilder();
	               axeResults.append("Screen Name,Help,Impact,Description,Help Url,Id,Tags");
	               axeResults.append(System.lineSeparator());
	               try {
	            	   
	                     Results result = builder.analyze(driver);
	                     List<Rule> violations = result.getViolations();
	                     
	                     String url=driver.getCurrentUrl();
	                     String pageID = url.substring(url.lastIndexOf("/") + 1, url.length());
	  
	                     logger.info("Violation of the Screen {} : {} ",screenName,violations.size());

	                     checkViolation(screenName, violations, pageID);

	                     for (Rule element : violations) {
	                            strHelp = element.getHelp();
	                            strImpact = element.getImpact();
	                            strDescription = "\"" + element.getDescription() + "\"";
	                            strHelpUrl = element.getHelpUrl();
	                            strId = element.getId();
	                            strTags = "\"" + String.join(",", element.getTags()) + "\"";

	                            axeResults.append(screenName + "," + strHelp + "," + strImpact + "," + strDescription + ","
	                                         + strHelpUrl + "," + strId + "," + strTags);
	                            axeResults.append(System.lineSeparator());

	                            if (element.getNodes() != null && !element.getNodes().isEmpty()) {
	                                  for (Node item : element.getNodes()) {
	                                         if (item.getHtml().trim().length() > 0 && item.getTarget().toString().trim().length() > 0) {
	                                               String htmlContent = item.getHtml();
	                                              htmlContent = htmlContent.replace(",", "_");   
	                                              htmlContent = htmlContent.replaceAll("\\s", "");   
	                                               axeResults.append(screenName + "," + strHelp + "," + strImpact + "," + strDescription
	                                                             + "," + strHelpUrl + "," + "\"" + htmlContent + "\"" + "," + "\""
	                                                             + item.getTarget() + "\"");
	                                                axeResults.append(System.lineSeparator());
	                                         }
	                                  }
	                            }
	                     }

	                     BufferedWriter writer = null;
	                     
	                     File file = new File("C:\\ADATestReports" + "\\AccessibilityReport_"
	                                               + screenName + ".csv");
	                     writeAxeResult(axeResults, writer, file);
	               } catch (Exception e) {

	                     e.printStackTrace();
	               }

	}

	private static void writeAxeResult(StringBuilder axeResults, BufferedWriter writer, File file) {
		
		Path path = file.toPath().normalize();
        if (!Files.isWritable(path.getParent())) {
            throw new SecurityException("Invalid file path or insufficient permissions");
        }
		try (BufferedWriter writer1 = new BufferedWriter(new FileWriter(path.toFile()))){
		        writer1.write(axeResults.toString());
		 } catch (Exception e1) {
			 e1.printStackTrace();

		 } finally {
		        writerValidation(writer);
		 }
	}

	private static void writerValidation(BufferedWriter writer) {
		if (writer != null) {
		      try {
		             writer.close();
		      } catch (IOException e) {
		            
		             e.printStackTrace();
		      }
		}
	}

	private static void checkViolation(String screenName, List<Rule> violations, String pageID) {
		if (violations.isEmpty()) {
		     
		        logger.info("No violations found:{} with PageID:{} No violations found: {}" ,screenName, pageID, screenName);
		                    
		 } 
		 else {
			 logger.info("ADA violations exists on the page: {} with PageID: {} Violations found",screenName,pageID);
		             
		 }
	}
}	
