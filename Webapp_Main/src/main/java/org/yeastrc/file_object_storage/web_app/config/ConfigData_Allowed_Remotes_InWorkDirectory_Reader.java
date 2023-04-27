package org.yeastrc.file_object_storage.web_app.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageWebappConfigException;

/**
 * Update ConfigData_Allowed_Remotes_InWorkDirectory with contents in config file
 *
 */
public class ConfigData_Allowed_Remotes_InWorkDirectory_Reader {

	private static final Logger log = LoggerFactory.getLogger(ConfigData_Allowed_Remotes_InWorkDirectory_Reader.class);
	

	private static String CONFIG_DEFAULTS_FILENAME = "file_object_storage_config_allowed_remotes_defaults.properties";
	private static String CONFIG_OVERRIDES_FILENAME = "file_object_storage_config_allowed_remotes.properties";

	private static String PROPERTY_NAME__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER = "access.allowed.all.remote.ips.update.importer";
	private static String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER = "YRC_FILE_OBJECT_STORAGE_ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER";
	private static String PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER = "true";
	
	private static String PROPERTY_NAME__ALLOWED_REMOTE_IPS_OVERALL = "allowed.remote.ips.overall";
	private static String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_OVERALL = "YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_OVERALL";
	
	private static String PROPERTY_NAME__ALLOWED_REMOTE_IPS_ADMIN = "allowed.remote.ips.admin";
	private static String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_ADMIN = "YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_ADMIN";
	
	private static String PROPERTY_NAME__ALLOWED_REMOTE_IPS_UPDATE = "allowed.remote.ips.update";
	private static String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_UPDATE = "YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_UPDATE";
	
	
	private static String PROPERTY_NAME__ALLOWED_REMOTE_IPS_DELIMITER = ",";
	
	//  private constructor
	private ConfigData_Allowed_Remotes_InWorkDirectory_Reader() { }
	
	/**
	 * @return newly created instance
	 */
	public static ConfigData_Allowed_Remotes_InWorkDirectory_Reader getInstance() { 
		return new ConfigData_Allowed_Remotes_InWorkDirectory_Reader(); 
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void readConfigDataInWebApp() throws Exception {
		
		ConfigData_Allowed_Remotes_InWorkDirectory configData_Allowed_Remotes_InWorkDirectory = new ConfigData_Allowed_Remotes_InWorkDirectory();

		try {
			Properties propertiesFile_Properties = null;
			Properties propertiesFile_DEFAULT_Properties = null;

			{ //  Main Config File (Override)
				File workDirectory = ConfigDataInWebApp.getSingletonInstance().getConfigFilesDirectory();

				//  Already tested but test here to be extra safe
				if ( workDirectory == null ) {
					String msg = "work directory in config is empty or missing so NOT reading the config file " + CONFIG_OVERRIDES_FILENAME;
					log.warn( msg );

				} else {
	
					File configFile = new File( workDirectory, CONFIG_OVERRIDES_FILENAME );
	
					if ( ( configFile.exists() && configFile.isFile() && configFile.canRead() ) ) {
	
						log.warn( "INFO:: Processing Config file '" + CONFIG_OVERRIDES_FILENAME
								+ "'."
								+ "  Config file with path: " + configFile.getCanonicalPath() );
						//  Get config file from Work Directory
	
						try ( InputStream propertiesFileAsStream = new FileInputStream( configFile ); ){
	
							propertiesFile_Properties = new Properties();
							propertiesFile_Properties.load( propertiesFileAsStream );
	
						} catch ( Exception e ) {
							log.error( "Error processing Properties file '" + CONFIG_OVERRIDES_FILENAME + "', exception: " + e.toString(), e );
							throw e;
						}
	
					}
				}
			}			

			{ //  Default Config File

				//  Get config file from class path
				ClassLoader thisClassLoader = this.getClass().getClassLoader();
				URL configPropFile = thisClassLoader.getResource( CONFIG_DEFAULTS_FILENAME );
				if ( configPropFile == null ) {
					//  No properties file
					String msg = "Properties file '" + CONFIG_DEFAULTS_FILENAME + "' not found in class path.";
					log.error( msg );
					throw new Exception( msg );
				} else {
					log.info( "DEFAULT Properties file '" + CONFIG_DEFAULTS_FILENAME + "' found, load path = " + configPropFile.getFile() );
				}
				
				InputStream propertiesFileAsStream = null;
				
				try {
					propertiesFileAsStream = thisClassLoader.getResourceAsStream( CONFIG_DEFAULTS_FILENAME );
					if ( propertiesFileAsStream == null ) {
						//  No properties file
						String msg = "DEFAULT Properties file '" + CONFIG_DEFAULTS_FILENAME + "' not found in class path.";
						log.error( msg );
						throw new FileObjectStorageWebappConfigException( msg );
					}
					
					propertiesFile_DEFAULT_Properties = new Properties();
					propertiesFile_DEFAULT_Properties.load( propertiesFileAsStream );
	
				} catch ( Exception e ) {
					log.error( "Error processing DEFAULT Properties file '" + CONFIG_DEFAULTS_FILENAME + "', exception: " + e.toString(), e );
					throw e;
				} finally {
					
					if ( propertiesFileAsStream != null ) {
						try {
							propertiesFileAsStream.close();

						} catch ( Exception e ) {
							log.error( "Error Closing DEFAULT Properties file '" + CONFIG_DEFAULTS_FILENAME + "', exception: " + e.toString(), e );
							throw e;
						}
					}
				}
			}
			

			{  //   Access Allowed for all remote IP Addresses, for Update and Importer
				
				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
					if ( PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER.equals( valueFoundInLabel_String ) ) {
						
						configData_Allowed_Remotes_InWorkDirectory.setAccessAllowed_allRemoteIps_update_importer(true);

						log.warn( "INFO: Environment Variable '" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
								+ "' has value '" + PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
								+ "' so setting flag to true to allow connections from any IP address for Update and Importer" );
					} else {

						log.warn( "INFO: Environment Variable '" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
								+ "' NOT HAVE value '" + PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
								+ "' so NOT setting flag to true to allow connections from any IP address for Update and Importer."
								+ "  Value found for that environment variable: " + valueFoundInLabel_String );
					}
				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
						
						if ( PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER.equals( valueFoundInLabel_String ) ) {
							
							configData_Allowed_Remotes_InWorkDirectory.setAccessAllowed_allRemoteIps_update_importer(true);

							log.warn( "INFO: JVM param: '-D" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
									+ "' has value '" + PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
									+ "' so setting flag to true to allow connections from any IP address for Update and Importer" );
						} else {

							log.warn( "INFO: JVM param: '-D" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
									+ "' NOT HAVE value '" + PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
									+ "' so NOT setting flag to true to allow connections from any IP address for Update and Importer."
									+ "  Value found for that JVM param: " + valueFoundInLabel_String );
						}
					} else {

						//  check in Properties File

						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER );
	
							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
								
								if ( PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER.equals( valueFoundInLabel_String ) ) {
									
									configData_Allowed_Remotes_InWorkDirectory.setAccessAllowed_allRemoteIps_update_importer(true);

									log.warn( "INFO: Properties File has value for Property '" + PROPERTY_NAME__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
											+ "' has value '" + PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
											+ "' so setting flag to true to allow connections from any IP address for Update and Importer" );
								} else {

									log.warn( "INFO: Properties File has value for Property '" + PROPERTY_NAME__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
											+ "' NOT HAVE value '" + PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
											+ "' so NOT setting flag to true to allow connections from any IP address for Update and Importer."
											+ "  Value found for that JVM param: " + valueFoundInLabel_String );
								}
							} else {

								//  Last place to check is Default Properties File

								if ( propertiesFile_Properties != null ) {
									valueFoundInLabel_String = propertiesFile_DEFAULT_Properties.getProperty( PROPERTY_NAME__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER );
			
									if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
										
										if ( PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER.equals( valueFoundInLabel_String ) ) {
											
											configData_Allowed_Remotes_InWorkDirectory.setAccessAllowed_allRemoteIps_update_importer(true);

											log.warn( "INFO: Default Properties File has value for Property '" + PROPERTY_NAME__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
													+ "' has value '" + PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
													+ "' so setting flag to true to allow connections from any IP address for Update and Importer" );
										} else {

											log.warn( "INFO: Default Properties File has value for Property '" + PROPERTY_NAME__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
													+ "' NOT HAVE value '" + PROPERTY_VALUE__TRUE__ACCESS_ALLOWED_ALL_REMOTE_IPS_UPDATE_IMPORTER 
													+ "' so NOT setting flag to true to allow connections from any IP address for Update and Importer."
													+ "  Value found for that JVM param: " + valueFoundInLabel_String );
										}
									}
								}
							}
						}
					}
				}
			}


			{  //   Access Allowed for remote IP Addresses, for All Requests

				String finalValue_String = null;
				
				String message_Label = null;
				
				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_OVERALL );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

					finalValue_String = valueFoundInLabel_String;
					
					message_Label = "Environment Variable: '" 
							+ ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_OVERALL 
							+ "'";
				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_OVERALL);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

						finalValue_String = valueFoundInLabel_String;
						
						message_Label = "JVM param: '-D" 
								+ ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_OVERALL 
								+ "'";
					} else {

						//  Check in Properties File

						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__ALLOWED_REMOTE_IPS_OVERALL );
	
							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

								finalValue_String = valueFoundInLabel_String;
								
								message_Label = "Properties file key: '" 
										+ PROPERTY_NAME__ALLOWED_REMOTE_IPS_OVERALL 
										+ "'";
							}
						} else {

							//  Last place to check is Default Properties File

							if ( propertiesFile_DEFAULT_Properties != null ) {
								valueFoundInLabel_String = propertiesFile_DEFAULT_Properties.getProperty( PROPERTY_NAME__ALLOWED_REMOTE_IPS_OVERALL );
		
								if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

									finalValue_String = valueFoundInLabel_String;
									
									message_Label = "Default Properties file key: '" 
											+ PROPERTY_NAME__ALLOWED_REMOTE_IPS_OVERALL 
											+ "'";
								}
							}
						}
						
					}
				}
				if ( StringUtils.isNotEmpty( finalValue_String ) ) {
					
					String[] allowedRemoteIPs = finalValue_String.split( PROPERTY_NAME__ALLOWED_REMOTE_IPS_DELIMITER );

					for ( String allowedRemoteIP : allowedRemoteIPs ) {
						configData_Allowed_Remotes_InWorkDirectory.addAllowedRemoteIP_Overall( allowedRemoteIP );
					}

					log.warn( "INFO: " + message_Label + " has value: " 
							+ StringUtils.join( configData_Allowed_Remotes_InWorkDirectory.getAllowedRemoteIPs_Overall(), " , "  ) );
				}
			}


			{  //   Access Allowed for remote IP Addresses, for Admin Requests

				String finalValue_String = null;

				String message_Label = null;

				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_ADMIN );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

					finalValue_String = valueFoundInLabel_String;

					message_Label = "Environment Variable: '" 
							+ ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_ADMIN 
							+ "'";
				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_ADMIN);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

						finalValue_String = valueFoundInLabel_String;

						message_Label = "JVM param: '-D" 
								+ ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_ADMIN 
								+ "'";
					} else {

						//  Check in Properties File

						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__ALLOWED_REMOTE_IPS_ADMIN );

							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

								finalValue_String = valueFoundInLabel_String;

								message_Label = "Properties file key: '" 
										+ PROPERTY_NAME__ALLOWED_REMOTE_IPS_ADMIN 
										+ "'";
							}
						} else {

							//  Last place to check is Default Properties File

							if ( propertiesFile_DEFAULT_Properties != null ) {
								valueFoundInLabel_String = propertiesFile_DEFAULT_Properties.getProperty( PROPERTY_NAME__ALLOWED_REMOTE_IPS_ADMIN );

								if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

									finalValue_String = valueFoundInLabel_String;

									message_Label = "Default Properties file key: '" 
											+ PROPERTY_NAME__ALLOWED_REMOTE_IPS_ADMIN 
											+ "'";
								}
							}
						}

					}
				}
				if ( StringUtils.isNotEmpty( finalValue_String ) ) {

					String[] allowedRemoteIPs = finalValue_String.split( PROPERTY_NAME__ALLOWED_REMOTE_IPS_DELIMITER );

					for ( String allowedRemoteIP : allowedRemoteIPs ) {
						configData_Allowed_Remotes_InWorkDirectory.addAllowedRemoteIP_Admin( allowedRemoteIP );
					}

					log.warn( "INFO: " + message_Label + " has value: " 
							+ StringUtils.join( configData_Allowed_Remotes_InWorkDirectory.getAllowedRemoteIPs_Overall(), " , "  ) );
				}
			}



			{  //   Access Allowed for remote IP Addresses, for Update Requests

				String finalValue_String = null;

				String message_Label = null;

				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_UPDATE );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

					finalValue_String = valueFoundInLabel_String;

					message_Label = "Environment Variable: '" 
							+ ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_UPDATE 
							+ "'";
				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_UPDATE);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

						finalValue_String = valueFoundInLabel_String;

						message_Label = "JVM param: '-D" 
								+ ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE___ALLOWED_REMOTE_IPS_UPDATE 
								+ "'";
					} else {

						//  Check in Properties File

						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__ALLOWED_REMOTE_IPS_UPDATE );

							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

								finalValue_String = valueFoundInLabel_String;

								message_Label = "Properties file key: '" 
										+ PROPERTY_NAME__ALLOWED_REMOTE_IPS_UPDATE 
										+ "'";
							}
						} else {

							//  Last place to check is Default Properties File

							if ( propertiesFile_DEFAULT_Properties != null ) {
								valueFoundInLabel_String = propertiesFile_DEFAULT_Properties.getProperty( PROPERTY_NAME__ALLOWED_REMOTE_IPS_UPDATE );

								if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

									finalValue_String = valueFoundInLabel_String;

									message_Label = "Default Properties file key: '" 
											+ PROPERTY_NAME__ALLOWED_REMOTE_IPS_UPDATE 
											+ "'";
								}
							}
						}

					}
				}
				if ( StringUtils.isNotEmpty( finalValue_String ) ) {

					String[] allowedRemoteIPs = finalValue_String.split( PROPERTY_NAME__ALLOWED_REMOTE_IPS_DELIMITER );

					for ( String allowedRemoteIP : allowedRemoteIPs ) {
						configData_Allowed_Remotes_InWorkDirectory.addAllowedRemoteIP_Update( allowedRemoteIP );
					}

					log.warn( "INFO: " + message_Label + " has value: " 
							+ StringUtils.join( configData_Allowed_Remotes_InWorkDirectory.getAllowedRemoteIPs_Overall(), " , "  ) );
				}
			}

		} catch ( Exception e ) {
			log.error( "Error processing Properties file '" + CONFIG_OVERRIDES_FILENAME + "', default properties file '" + CONFIG_DEFAULTS_FILENAME + "', and associated Environment Variables and JVM '-D' parameters.  Exception: " + e.toString(), e );
			throw e;
		}
		
		log.warn( "Finished processing Confiration in config file '" 
				+ CONFIG_OVERRIDES_FILENAME
				 + "', default properties file '" + CONFIG_DEFAULTS_FILENAME
				+ "' and associated Environment Variables and JVM '-D' parameters." );
		

//		if ( configData_Allowed_Remotes_InWorkDirectory.getAllowedRemoteIPs_Overall() == null 
//				|| configData_Allowed_Remotes_InWorkDirectory.getAllowedRemoteIPs_Overall().isEmpty() ) {
//			String msg = "Property '" + PROPERTY_NAME__ALLOWED_REMOTE_IPS_OVERALL + "' in config is empty or missing";
//			log.error( msg );
//			throw new SpectralFileWebappConfigException( msg );
//		}
//
//		if ( configData_Allowed_Remotes_InWorkDirectory.getAllowedRemoteIPs_Admin() == null 
//				|| configData_Allowed_Remotes_InWorkDirectory.getAllowedRemoteIPs_Admin().isEmpty() ) {
//			String msg = "Property '" + PROPERTY_NAME__ALLOWED_REMOTE_IPS_ADMIN + "' in config is empty or missing";
//			log.error( msg );
//			throw new SpectralFileWebappConfigException( msg );
//		}
		

		ConfigData_Allowed_Remotes_InWorkDirectory.setInstance( configData_Allowed_Remotes_InWorkDirectory );
	}
	
	
}
