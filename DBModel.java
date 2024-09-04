package api_utilities.models;

import java.util.HashMap;

public class DBModel {
	public String loggingConnectionString = "";
	public String loggingTableName = "";


	public HashMap<String,EnvModel> env = new HashMap<String,EnvModel>();
}
