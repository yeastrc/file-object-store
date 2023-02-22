package org.yeastrc.file_object_storage.web_app.config;

import org.slf4j.LoggerFactory;  import org.slf4j.Logger;

/**
 * 
 *
 */
public class A_Load_Config {

	private static final Logger log = LoggerFactory.getLogger(A_Load_Config.class);

	//  private constructor
	private A_Load_Config() { }
	
	/**
	 * @return newly created instance
	 */
	public static A_Load_Config getInstance() { 
		return new A_Load_Config(); 
	}
	
	/**
	 * @throws Exception
	 */
	public void load_Config() throws Exception {
		
		ConfigDataInWebApp_Reader.getInstance().readConfigDataInWebApp();
		ConfigData_Allowed_Remotes_InWorkDirectory_Reader.getInstance().readConfigDataInWebApp();
		
		ConfigData_Directories_ProcessUploadInfo_InWorkDirectory_Reader.getInstance().readConfigDataInWebApp();
	}
}
