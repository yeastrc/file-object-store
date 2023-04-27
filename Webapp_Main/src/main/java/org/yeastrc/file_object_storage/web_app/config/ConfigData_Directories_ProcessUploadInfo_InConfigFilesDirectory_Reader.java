package org.yeastrc.file_object_storage.web_app.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageWebappConfigException;

/**
 * Update ConfigData_Directories_ProcessUploadCommand_InWorkDirectory with
 * contents in config file
 *
 */
public class ConfigData_Directories_ProcessUploadInfo_InConfigFilesDirectory_Reader {

	private static final Logger log = LoggerFactory
			.getLogger(ConfigData_Directories_ProcessUploadInfo_InConfigFilesDirectory_Reader.class);

	private static String CONFIG_FILENAME = "file_object_storage_server_config_dirs_process_cmd.properties";

	//   S3_BUCKET OVERRIDES SCAN_STORAGE_BASE_DIRECTORY

	private static String PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE = "s3.bucket.main.storage";
	private static final String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_MAIN_STORAGE = "YRC_FILE_OBJECT_STORAGE_S3_BUCKET_MAIN_STORAGE";

	private static String PROPERTY_NAME__S3_REGION_MAIN_STORAGE = "s3.region.main.storage";
	private static final String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_REGION_MAIN_STORAGE = "YRC_FILE_OBJECT_STORAGE_S3_REGION_MAIN_STORAGE";

	/**
	 * The Base Directory used by File Object Storage.
	 * 
	 * This is required if sending files to S3 for temp hold file contents streamed
	 * to this webapp via webservice call. ** Another solution is to store the file
	 * contents in a different S3 bucket temporarily and then copy info perm S3
	 * bucket.
	 * 
	 * * The Perm file storage will be under a sub dir
	 * 
	 * * Temp sub dirs will be created under this dir as needed and named starting
	 * with 'temp'
	 * 
	 */
	private static final String PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY = "file.object.storage.base.directory";
	private static final String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY = "YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY";
	
	private static final String PROPERTY_NAME__S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE = "s3.bucket.temporary.input.file.storage";
	private static final String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE = "YRC_FILE_OBJECT_STORAGE_S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE";

	private static String PROPERTY_NAME__SUBMITTED_FILE_PATH_RESTRICTIONS = "submitted.file.path.restrictions";
	private static final String ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_SUBMITTED_FILE_PATH_RESTRICTIONS = "YRC_FILE_OBJECT_STORAGE_SUBMITTED_FILE_PATH_RESTRICTIONS";

	
	
	// private constructor
	private ConfigData_Directories_ProcessUploadInfo_InConfigFilesDirectory_Reader() {
	}

	/**
	 * @return newly created instance
	 */
	public static ConfigData_Directories_ProcessUploadInfo_InConfigFilesDirectory_Reader getInstance() {
		return new ConfigData_Directories_ProcessUploadInfo_InConfigFilesDirectory_Reader();
	}

	/**
	 * @throws Exception
	 * 
	 */
	public void readConfigDataInWebApp() throws Exception {

		ConfigData_Directories_ProcessUploadInfo_InWorkDirectory configData_Directories_ProcessUploadCommand_InWorkDirectory = new ConfigData_Directories_ProcessUploadInfo_InWorkDirectory();

		try {
			Properties propertiesFile_Properties = null;

			{
				File workDirectory = ConfigDataInWebApp.getSingletonInstance().getConfigFilesDirectory();

				if ( workDirectory == null ) {
					String msg = "work directory in config is empty or missing so NOT reading the config file " + CONFIG_FILENAME;
					log.warn( msg );

				} else {
	
					File configFile = new File( workDirectory, CONFIG_FILENAME );
	
					if ( ( configFile.exists() && configFile.isFile() && configFile.canRead() ) ) {
	
						log.warn( "INFO:: Processing Config file '" + CONFIG_FILENAME
								+ "'."
								+ "  Config file with path: " + configFile.getCanonicalPath() );
						//  Get config file from Work Directory
	
						try ( InputStream propertiesFileAsStream = new FileInputStream( configFile ); ){
	
							propertiesFile_Properties = new Properties();
							propertiesFile_Properties.load( propertiesFileAsStream );
	
						} catch ( Exception e ) {
							log.error( "Error processing Properties file '" + CONFIG_FILENAME + "', exception: " + e.toString(), e );
							throw e;
						}
	
					}
				}
			}			
			

			{  //   S3  Bucket
				
				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_MAIN_STORAGE );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

					log.warn( "INFO: S3 Bucket - Main Data Storage - to use: Value found in Environment Variable: '" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_MAIN_STORAGE + "' with value: " + valueFoundInLabel_String );
				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_MAIN_STORAGE);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

						log.warn( "INFO: S3 Bucket - Main Data Storage - to use: Value found in JVM param: '-D" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_MAIN_STORAGE + "' with value: " + valueFoundInLabel_String );
					} else {

						//  Last place to check is Properties File

						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE );
	
							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
	
								log.warn( "INFO: S3 Bucket to to use: Value found in Properties file with key: '" + PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE + "' with value: " + valueFoundInLabel_String );
							}
						}
					}
				}
				
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
					configData_Directories_ProcessUploadCommand_InWorkDirectory.setS3Bucket_MainStorage( valueFoundInLabel_String );
				}
			}

			{  //  S3 Region - Main Data Storage
				
				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_REGION_MAIN_STORAGE );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

					log.warn( "INFO: S3 Region - Main Data Storage - to use: Value found in Environment Variable: '" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_REGION_MAIN_STORAGE + "' with value: " + valueFoundInLabel_String );
				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_REGION_MAIN_STORAGE);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

						log.warn( "INFO: S3 Region to use: Value found in JVM param: '-D" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_REGION_MAIN_STORAGE + "' with value: " + valueFoundInLabel_String );
					} else {

						//  Last place to check is Properties File

						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__S3_REGION_MAIN_STORAGE );
	
							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
	
								log.warn( "INFO: S3 Region to to use: Value found in Properties file with key: '" + PROPERTY_NAME__S3_REGION_MAIN_STORAGE + "' with value: " + valueFoundInLabel_String );
							}
						}
					}
				}
				
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
					configData_Directories_ProcessUploadCommand_InWorkDirectory.setS3Region_MainStorage( valueFoundInLabel_String );
				}
			}


			{  //  S3 Bucket - Temporary Input File Storage
				
				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

					log.warn( "INFO: S3 Bucket - Temporary Input File Storage - to use: Value found in Environment Variable: '" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE + "' with value: " + valueFoundInLabel_String );
				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

						log.warn( "INFO: S3 Bucket - Temporary Input File Storage: Value found in JVM param: '-D" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE + "' with value: " + valueFoundInLabel_String );
					} else {

						//  Last place to check is Properties File

						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE );
	
							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
	
								log.warn( "INFO: S3 Bucket - Temporary Input File Storage: Value found in Properties file with key: '" + PROPERTY_NAME__S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE + "' with value: " + valueFoundInLabel_String );
							}
						}
					}
				}
				
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
					configData_Directories_ProcessUploadCommand_InWorkDirectory.setS3Bucket_TemporaryInputFileStorage( valueFoundInLabel_String );
				}
			}
			
				
			if ( StringUtils.isNotEmpty( configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Bucket_MainStorage() ) ) {
				
				log.warn( "INFO: Since Main Storage S3 Bucket is configured, the FileObjectStorage_BaseDirectory value is ignored" );
				
			} else {
				///   FileObjectStorage_BaseDirectory -- Store on Local Disk  in specified directory

				
				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

					File fileObjectStorage_BaseDirectory = new File( valueFoundInLabel_String );

					if ( ! ( fileObjectStorage_BaseDirectory.exists() && fileObjectStorage_BaseDirectory.isDirectory() && fileObjectStorage_BaseDirectory.canRead() ) ) {
						String msg = "!!Storage Base Directory to use: Value INVALID in Environment Variable: " + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY 
								+ "' does not exist or is not a directory or is not readable.  Value:  " 
								+ valueFoundInLabel_String;
						log.error( msg );
						throw new FileObjectStorageWebappConfigException( msg );
					}
					
					configData_Directories_ProcessUploadCommand_InWorkDirectory.setFileObjectStorage_BaseDirectory(fileObjectStorage_BaseDirectory);

					log.warn( "INFO: Storage Base Directory to use: Value found in Environment Variable: '" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY + "' with value: " + valueFoundInLabel_String );

				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

						File fileObjectStorage_BaseDirectory = new File( valueFoundInLabel_String );

						if ( ! ( fileObjectStorage_BaseDirectory.exists() && fileObjectStorage_BaseDirectory.isDirectory() && fileObjectStorage_BaseDirectory.canRead() ) ) {
							String msg = "!!Storage Base Directory to use: Value INVALID in JVM param: '-D '" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY 
									+ "' does not exist or is not a directory or is not readable.  Value:  " 
									+ valueFoundInLabel_String;
							log.error( msg );
							throw new FileObjectStorageWebappConfigException( msg );
						}
						
						configData_Directories_ProcessUploadCommand_InWorkDirectory.setFileObjectStorage_BaseDirectory(fileObjectStorage_BaseDirectory);

						log.warn( "INFO: Storage Base Directory to use: Value found in JVM param: '-D" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY + "' with value: " + valueFoundInLabel_String );

					} else {

						//  Last place to check is Properties File
						
						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY );
	
							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
	
								File fileObjectStorage_BaseDirectory = new File( valueFoundInLabel_String );

								if ( ! ( fileObjectStorage_BaseDirectory.exists() && fileObjectStorage_BaseDirectory.isDirectory() && fileObjectStorage_BaseDirectory.canRead() ) ) {
									String msg = "!!Property '" + PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY 
											+ "' in config does not exist or is not a directory or is not readable.  Value:  " 
											+ valueFoundInLabel_String;
									log.error( msg );
									throw new FileObjectStorageWebappConfigException( msg );
								}
								
								configData_Directories_ProcessUploadCommand_InWorkDirectory.setFileObjectStorage_BaseDirectory(fileObjectStorage_BaseDirectory);
	
								log.warn( "INFO: Storage Base Directory to to use: Value found in Properties file with key: '" + PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY + "' with value: " + valueFoundInLabel_String );
							}
						}
					}
				}
			}


			{
				///   submittedScanFilePathRestrictions -- 
				
				String finalValueToParse = null;

				String valueFoundInLabel_String = System.getenv( ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_SUBMITTED_FILE_PATH_RESTRICTIONS );

				if ( valueFoundInLabel_String != null ) {
					valueFoundInLabel_String = valueFoundInLabel_String.trim();
				}
				if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
					
					finalValueToParse = valueFoundInLabel_String;

					log.warn( "INFO: Submitted Scan File Path Restrictions to use: Value found in Environment Variable: '" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_SUBMITTED_FILE_PATH_RESTRICTIONS + "' with value: " + valueFoundInLabel_String );

				} else {

					//  Not in config file or Environment Variable so get from JVM -D Property

					Properties prop = System.getProperties();
					valueFoundInLabel_String = prop.getProperty(ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_SUBMITTED_FILE_PATH_RESTRICTIONS);

					if ( valueFoundInLabel_String != null ) {

						valueFoundInLabel_String = valueFoundInLabel_String.trim();
					}

					if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {

						finalValueToParse = valueFoundInLabel_String;

						log.warn( "INFO: Submitted Scan File Path Restrictions to use: Value found in JVM param: '-D" + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_SUBMITTED_FILE_PATH_RESTRICTIONS + "' with value: " + valueFoundInLabel_String );

					} else {

						//  Last place to check is Properties File

						if ( propertiesFile_Properties != null ) {
							valueFoundInLabel_String = propertiesFile_Properties.getProperty( PROPERTY_NAME__SUBMITTED_FILE_PATH_RESTRICTIONS );
	
							if ( StringUtils.isNotEmpty( valueFoundInLabel_String ) ) {
								
								finalValueToParse = valueFoundInLabel_String;
	
								log.warn( "INFO: Submitted Scan File Path Restrictions to to use: Value found in Properties file with key: '" + PROPERTY_NAME__SUBMITTED_FILE_PATH_RESTRICTIONS + "' with value: " + valueFoundInLabel_String );
							}
						}
					}
				}
				
				if ( finalValueToParse != null ) {
					
					String[] valuesArray = finalValueToParse.split( "," );
					List<String> values = new ArrayList<>( valuesArray.length );
					for ( String value : valuesArray ) {
						if ( StringUtils.isNotEmpty(value) ) {
							values.add( value.trim() );
						}
					}
					if ( ! values.isEmpty() ) {
						configData_Directories_ProcessUploadCommand_InWorkDirectory.setSubmittedFilePathRestrictions( values );
					}
				}
			}

			if ( StringUtils.isEmpty( configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Bucket_MainStorage() )
					&& configData_Directories_ProcessUploadCommand_InWorkDirectory.getFileObjectStorage_BaseDirectory() == null ) {
				
				log.error( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
				log.error( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
				log.error( "!!!" );
				log.error( "!!!  FATAL Webapp START ERROR:: Main Data Storage: Directory or S3 Bucket is REQUIRED" ); 
				log.error( "!!!" );
				log.error( "!!!  Main Storage Base Directory is configured using one of the following:  " );

				log.error( "!!!  Configuration file: "  + CONFIG_FILENAME + " and property: " + PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY );
				log.error( "!!!  Environment Variable: " + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY );
				log.error( "!!!  Passed to java command as '-D' parameter: " + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_BASE_DIRECTORY );

				log.error( "!!!" );
				log.error( "!!!  Main Storage S3 Bucket is configured using one of the following:  " );

				log.error( "!!!  Configuration file: "  + CONFIG_FILENAME + " and property: " + PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE );
				log.error( "!!!  Environment Variable: " + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_MAIN_STORAGE );
				log.error( "!!!  Passed to java command as '-D' parameter: " + ENVIRONMENT_VARIABLE__YRC_FILE_OBJECT_STORAGE_S3_BUCKET_MAIN_STORAGE );

				log.error( "!!!" );
				log.error( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
				log.error( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );

				throw new FileObjectStorageWebappConfigException( "FATAL Webapp START ERROR::  Main Data Storage: Directory or S3 Bucket is REQUIRED." );
			}


		} catch ( Exception e ) {
			log.error( "Error processing Properties file '" + CONFIG_FILENAME + "' and associated Environment Variables and JVM '-D' parameters.  Exception: " + e.toString(), e );
			throw e;
		}

		log.warn("Finished processing config file '" + CONFIG_FILENAME + "'.");


		ConfigData_Directories_ProcessUploadInfo_InWorkDirectory
				.setInstance(configData_Directories_ProcessUploadCommand_InWorkDirectory);
	}

}
