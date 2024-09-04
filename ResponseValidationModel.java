package api_utilities.models;

import java.util.ArrayList;
import java.util.HashMap;

public class ResponseValidationModel {

	public String responseType="";
	public String expectedResponseFilePath="";
	public String expectedResponse="";
	public String actualResponseFilePath="";
	public String actualResponse="";
	public HashMap<String,String> xPathJsonPath= new HashMap<>();
	public ArrayList<String> dataElements= new ArrayList<>();
	
}
