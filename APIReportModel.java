package api_utilities.models;


public class APIReportModel {

	public String testStepResult="";
	public boolean boolResponseStringValidation=false;
	public boolean boolXMLJSONValidation=false;
	public boolean dataElementsValidation=false;
	public String expectedResponse="";
	public String actualResponse="";
	public String  xpathJsonKey="";
	public String  dataElementKey="";
	public String  additionalDetails="";
	public String request;
	public String response;
	public String url;
	public String dbValidation;
	public String dbExpectedValue;
	public String dbActualValue;
	public String statusCode;
}
