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
	private File webappWorkDirectory;

	/**
	 * The 'work' directory for the webapp
	 * @return
	 */
	public File getWebappWorkDirectory() {
		return webappWorkDirectory;
	}

	/**
	 * The 'work' directory for the webapp
	 * 
	 * @param webappWorkDirectory
	 */
	public void setWebappWorkDirectory(File webappWorkDirectory) {
		this.webappWorkDirectory = webappWorkDirectory;
	}


}
