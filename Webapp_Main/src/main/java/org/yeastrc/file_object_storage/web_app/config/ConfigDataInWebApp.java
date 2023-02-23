package org.yeastrc.file_object_storage.web_app.config;

import java.io.File;

/**
 * Config data for config file in web app
 * 
 * The Primary configuration for the web app
 * 
 * 
 * Singleton Instance
 *
 */
public class ConfigDataInWebApp {
	
	private static volatile ConfigDataInWebApp instance;

	// package private constructor
	ConfigDataInWebApp() { }
	
	/**
	 * @return Singleton instance
	 */
	public static ConfigDataInWebApp getSingletonInstance() { 
		return instance; 
	}
	/**
	 * package private
	 * @param instance
	 */
	static void setInstance(ConfigDataInWebApp instance) {
		ConfigDataInWebApp.instance = instance;
	}

	/**
	 * The 'work' directory for the webapp
	 */
	private File configFilesDirectory;

	/**
	 * The 'work' directory for the webapp
	 * @return
	 */
	public File getConfigFilesDirectory() {
		return configFilesDirectory;
	}

	/**
	 * The 'Config Files' directory for the webapp
	 * 
	 * @param configFilesDirectory
	 */
	public void setConfigFilesDirectory(File configFilesDirectory) {
		this.configFilesDirectory = configFilesDirectory;
	}


}
