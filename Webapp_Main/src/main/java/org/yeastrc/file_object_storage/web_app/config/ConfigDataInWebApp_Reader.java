package org.yeastrc.file_object_storage.web_app.config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageWebappConfigException;

/**
 * Update ConfigDataInWebApp with contents in config file
 *
 */
public class ConfigDataInWebApp_Reader {

	private static final Logger log = LoggerFactory.getLogger(ConfigDataInWebApp_Reader.class);
	

	/**
	 * String used to get value from Environment Variable or java -D parameter
	 */
	private static final String YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL = "YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL";
	
	
	private static String CONFIG_FILENAME = "file_object_storage_config_files_dir_config.properties";

	private static String PROPERTY_NAME__WEBAPP_CONFIG_FILES_DIRECTORY = "webapp.config.files.directory";
	
	//  private constructor
	private ConfigDataInWebApp_Reader() { }
	
	/**
	 * @return newly created instance
	 */
	public static ConfigDataInWebApp_Reader getInstance() { 
		return new ConfigDataInWebApp_Reader(); 
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void readConfigDataInWebApp() throws Exception {
		
		ConfigDataInWebApp configDataInWebApp = new ConfigDataInWebApp();
		
		File configFilesDirectory = null;
		
		{
			String webappConfigFilesDirectoryString = System.getenv(YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL);

			if ( StringUtils.isNotEmpty( webappConfigFilesDirectoryString ) ) {

				configFilesDirectory = new File( webappConfigFilesDirectoryString );
				if ( ! ( configFilesDirectory.exists() && configFilesDirectory.isDirectory() && configFilesDirectory.canRead() ) ) {
					String msg = "Config Files Directory does not exist or is not readable. Config Files Directory from environement variable '" + YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL 
							+ "' with value of: " + webappConfigFilesDirectoryString ;
					log.error( msg );
					throw new FileObjectStorageWebappConfigException( msg );
				}

				log.warn( "INFO::  Using value for Config Files Directory from environement variable '" + YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL 
						+ "' with value of: " + webappConfigFilesDirectoryString );
			}
		}

		if ( configFilesDirectory == null ) {
				
			//  Not in Environment Variable so get from JVM -D Property

			{
				Properties prop = System.getProperties();
				String webappConfigFilesDirectoryString = prop.getProperty(YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL);

				if ( StringUtils.isNotEmpty( webappConfigFilesDirectoryString ) ) {

					configFilesDirectory = new File( webappConfigFilesDirectoryString );
					if ( ! ( configFilesDirectory.exists() && configFilesDirectory.isDirectory() && configFilesDirectory.canRead() ) ) {
						String msg = "Config Files Directory does not exist or is not readable. Config Files Directory from JVM -D Property '" 
								+ YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL 
								+ "' with value of: " + webappConfigFilesDirectoryString ;
						log.error( msg );
						throw new FileObjectStorageWebappConfigException( msg );
					}

					log.warn( "INFO::  Using value for Config Files Directory from JVM -D Property '" + YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL 
							+ "' with value of: " + webappConfigFilesDirectoryString );
				}
			}

			if ( configFilesDirectory == null ) {

				boolean stopProcessing_PropertiesFile = false;
				
				InputStream propertiesFileAsStream = null;
				try {
					//  Get config file from class path
					ClassLoader thisClassLoader = this.getClass().getClassLoader();
					URL configPropFile = thisClassLoader.getResource( CONFIG_FILENAME );
					if ( configPropFile == null ) {
						//  No properties file
						String msg = "Properties file '" + CONFIG_FILENAME + "' not found in class path.";
						log.error( msg );
						
						stopProcessing_PropertiesFile = true;
						
					} else {
						log.info( "Properties file '" 
								+ CONFIG_FILENAME 
								+ "' found, load path = " 
								+ configPropFile.getFile() );
					}
					
					if ( ! stopProcessing_PropertiesFile ) {
						propertiesFileAsStream = thisClassLoader.getResourceAsStream( CONFIG_FILENAME );
						if ( propertiesFileAsStream == null ) {
							//  No properties file
					
							String msg = "Properties file '" 
									+ CONFIG_FILENAME 
									+ "' not found in base of class path.";
							log.error( msg );
							
							stopProcessing_PropertiesFile = true;
						} else {
						
							Properties configProps = new Properties();
							configProps.load(propertiesFileAsStream);
							String webappConfigFilesDirectoryString = configProps.getProperty( PROPERTY_NAME__WEBAPP_CONFIG_FILES_DIRECTORY );


							if ( StringUtils.isNotEmpty( webappConfigFilesDirectoryString ) ) {

								configFilesDirectory = new File( webappConfigFilesDirectoryString );
								if ( ! ( configFilesDirectory.exists() && configFilesDirectory.isDirectory() && configFilesDirectory.canRead() ) ) {
									String msg = "Property '" + PROPERTY_NAME__WEBAPP_CONFIG_FILES_DIRECTORY 
											+ "' in config does not exist, is not  a directory, or is not readable. Value: "
											+ webappConfigFilesDirectoryString;
									log.error( msg );
									throw new FileObjectStorageWebappConfigException( msg );
								}
							}
						}
					}

				} catch ( RuntimeException e ) {
					log.error( "Error processing Properties file '" 
							+ CONFIG_FILENAME 
							+ "', exception: " 
							+ e.toString(), e );
					throw e;
				} finally {
					if ( propertiesFileAsStream != null ) {
						propertiesFileAsStream.close();
					}
				}
			}

			if ( configFilesDirectory == null ) {

				String msg = "Config Files Directory value NOT FOUND in environement variable '" + YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL 
						+ "', JVM -D Property '" + YRC_FILE_OBJECT_STORAGE_CONFIG_FILES_DIR__ENV_LABEL 
						+ "', or in Property '" + PROPERTY_NAME__WEBAPP_CONFIG_FILES_DIRECTORY 
						+ "' in config file '"
						+ CONFIG_FILENAME
						+ "'.  Config file searched for using base of class path.";
				log.error( msg );
				throw new FileObjectStorageWebappConfigException( msg );
			}

			configDataInWebApp.setConfigFilesDirectory( configFilesDirectory );

			ConfigDataInWebApp.setInstance( configDataInWebApp );
		}
	}

}
