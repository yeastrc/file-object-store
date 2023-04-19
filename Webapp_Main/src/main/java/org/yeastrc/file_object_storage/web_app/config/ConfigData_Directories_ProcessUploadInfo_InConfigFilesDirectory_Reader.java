package org.yeastrc.file_object_storage.web_app.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
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

	// No Default file
//	private static String CONFIG_DEFAULTS_FILENAME = "spectral_server_accept_import_config_dirs_process_cmd_defaults.properties";

	private static String CONFIG_OVERRIDES_FILENAME = "file_object_storage_server_config_dirs_process_cmd.properties";

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
	private static String PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY = "file.object.storage.base.directory";

	private static String PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE = "s3.bucket.main.storage";
	private static String PROPERTY_NAME__S3_REGION_MAIN_STORAGE = "s3.region.main.storage";

	private static String PROPERTY_NAME__S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE = "s3.bucket.temporary.input.file.storage";


	private static String PROPERTY_NAME__SUBMITTED_FILE_PATH_RESTRICTIONS = "submitted.file.path.restrictions";

	private static enum IsDefaultPropertiesFile {
		YES, NO
	}

	private static enum AllowNoPropertiesFile {
		YES, NO
	}

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

		// Local Internal class

		InternalConfigDirectoryStrings internalConfigDirectoryStrings = new InternalConfigDirectoryStrings();

//		processPropertiesFilename( CONFIG_DEFAULTS_FILENAME, IsDefaultPropertiesFile.YES, AllowNoPropertiesFile.NO, configData_Directories_ProcessUploadCommand_InWorkDirectory );

		processPropertiesFilename(CONFIG_OVERRIDES_FILENAME, IsDefaultPropertiesFile.NO, AllowNoPropertiesFile.NO,
				configData_Directories_ProcessUploadCommand_InWorkDirectory, internalConfigDirectoryStrings);

		log.warn("Finished processing config file '" + CONFIG_OVERRIDES_FILENAME + "'.");

		if ( StringUtils.isNotEmpty( internalConfigDirectoryStrings.scanStorageBaseDirectory ) 
				&& StringUtils.isNotEmpty( configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Bucket_MainStorage() ) ) {
			String msg = "Cannot set both properties '"
				+ PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY 
				+ "' and '"
				+ PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE
				+ "' to a value in config.";
			log.error( msg );
			throw new FileObjectStorageWebappConfigException( msg );
		}

		if (StringUtils.isNotEmpty(internalConfigDirectoryStrings.scanStorageBaseDirectory)) {

			File fileObjectStorage_BaseDirectory = new File(internalConfigDirectoryStrings.scanStorageBaseDirectory);

			if (!(fileObjectStorage_BaseDirectory.exists() && fileObjectStorage_BaseDirectory.isDirectory()
					&& fileObjectStorage_BaseDirectory.canRead())) {
				String msg = "!!Property '" + PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY
						+ "' in config does not exist or is not a directory or is not readable.  Value:  "
						+ internalConfigDirectoryStrings.scanStorageBaseDirectory;
				log.error(msg);
				throw new FileObjectStorageWebappConfigException(msg);
			}

			configData_Directories_ProcessUploadCommand_InWorkDirectory
					.setFileObjectStorage_BaseDirectory(fileObjectStorage_BaseDirectory);

			log.warn("INFO: '" + PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY + "' has value: "
					+ internalConfigDirectoryStrings.scanStorageBaseDirectory);

//		} else {
//
//			String msg = "Must set property '" + PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY
//					+ "' to a value in config.";
//			log.error(msg);
//			throw new FileObjectStorageWebappConfigException(msg);
		}

		if ( 
				StringUtils.isEmpty( internalConfigDirectoryStrings.scanStorageBaseDirectory )
				&& StringUtils.isEmpty( configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Bucket_MainStorage() ) ) {
			
			String msg = "Must set One of properties '"
					+ PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY 
					+ "' and '"
					+ PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE
					+ "' to a value in config.";
			log.error( msg );
			throw new FileObjectStorageWebappConfigException( msg );
		}

		log.warn( "INFO: '" + PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE + "' has value: " 
				+ configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Bucket_MainStorage() );

		if ( StringUtils.isNotEmpty( configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Region_MainStorage() ) ) {
			log.warn( "INFO: '" + PROPERTY_NAME__S3_REGION_MAIN_STORAGE + "' has value: " 
					+ configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Region_MainStorage() );
		}

		if ( StringUtils.isNotEmpty( configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Bucket_TemporaryInputFileStorage() ) ) {
			log.warn( "INFO: '" + PROPERTY_NAME__S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE + "' has value: " 
					+ configData_Directories_ProcessUploadCommand_InWorkDirectory.getS3Bucket_TemporaryInputFileStorage() );
		}

		if (configData_Directories_ProcessUploadCommand_InWorkDirectory.getSubmittedFilePathRestrictions() != null
				&& (!configData_Directories_ProcessUploadCommand_InWorkDirectory.getSubmittedFilePathRestrictions()
						.isEmpty())) {
			log.warn("INFO: '" + PROPERTY_NAME__SUBMITTED_FILE_PATH_RESTRICTIONS + "' has value(s) (comma delim): "
					+ StringUtils.join(configData_Directories_ProcessUploadCommand_InWorkDirectory
							.getSubmittedFilePathRestrictions(), ","));
		} else {
			log.warn("INFO: '" + PROPERTY_NAME__SUBMITTED_FILE_PATH_RESTRICTIONS
					+ "' has NO values or is missing.  All requests with scan filename and path will be rejected with the appropriate flag in the response.  ");
		}

		ConfigData_Directories_ProcessUploadInfo_InWorkDirectory
				.setInstance(configData_Directories_ProcessUploadCommand_InWorkDirectory);
	}

	/**
	 * @param propertiesFilename
	 * @param configDataInWebApp
	 * @throws IOException
	 * @throws FileObjectStorageWebappConfigException
	 */
	private void processPropertiesFilename(String propertiesFilename, IsDefaultPropertiesFile isDefaultPropertiesFile,
			AllowNoPropertiesFile allowNoPropertiesFile,
			ConfigData_Directories_ProcessUploadInfo_InWorkDirectory configData_Directories_ProcessUploadCommand_InWorkDirectory,
			InternalConfigDirectoryStrings internalConfigDirectoryStrings) throws Exception {

		InputStream propertiesFileAsStream = null;
		try {
			if (isDefaultPropertiesFile == IsDefaultPropertiesFile.YES) {

				// Get config file from class path
				ClassLoader thisClassLoader = this.getClass().getClassLoader();
				URL configPropFile = thisClassLoader.getResource(propertiesFilename);
				if (configPropFile == null) {
					// No properties file
					if (allowNoPropertiesFile == AllowNoPropertiesFile.NO) {
						return; // EARLY EXIT
					}
					// String msg = "Properties file '" + DB_CONFIG_FILENAME + "' not found in class
					// path.";
					// log.error( msg );
					// throw new Exception( msg );
				} else {
					log.info("Properties file '" + propertiesFilename + "' found, load path = "
							+ configPropFile.getFile());
				}
				propertiesFileAsStream = thisClassLoader.getResourceAsStream(propertiesFilename);
				if (propertiesFileAsStream == null) {
					// No properties file
					if (allowNoPropertiesFile == AllowNoPropertiesFile.NO) {
						return; // EARLY EXIT
					}
					String msg = "Properties file '" + propertiesFilename + "' not found in class path.";
					log.error(msg);
					throw new FileObjectStorageWebappConfigException(msg);
				}

			} else {

				// Get config file from Config File Directory

				File configFilesDirectory = ConfigDataInWebApp.getSingletonInstance().getConfigFilesDirectory();

				// Already tested but test here to be extra safe
				if (configFilesDirectory == null) {
					String msg = "Config Files directory in config is empty or missing";
					log.error(msg);
					throw new FileObjectStorageWebappConfigException(msg);
				}

				File configFile = new File(configFilesDirectory, propertiesFilename);
				if (!(configFile.exists() && configFile.isFile() && configFile.canRead())) {

					if (allowNoPropertiesFile == AllowNoPropertiesFile.YES) {
						return; // EARLY EXIT
					}

					String msg = "Config file '" + propertiesFilename
							+ "' does not exist, is not  a file, or is not readable." + "  Config file with path: "
							+ configFile.getCanonicalPath();
					log.error(msg);
					throw new FileObjectStorageWebappConfigException(msg);
				}

				propertiesFileAsStream = new FileInputStream(configFile);

			}

			Properties configProps = new Properties();
			configProps.load(propertiesFileAsStream);
			String propertyValue = null;

			propertyValue = configProps.getProperty(PROPERTY_NAME__FILE_OBJECT_STORAGE_BASE_DIRECTORY);
			if (StringUtils.isNotEmpty(propertyValue)) {
				internalConfigDirectoryStrings.scanStorageBaseDirectory = propertyValue.trim();
			}

			propertyValue = configProps.getProperty( PROPERTY_NAME__S3_BUCKET_MAIN_STORAGE );
			if ( StringUtils.isNotEmpty( propertyValue ) ) {

				 configData_Directories_ProcessUploadCommand_InWorkDirectory.setS3Bucket_MainStorage( propertyValue.trim() );
			}

			propertyValue = configProps.getProperty( PROPERTY_NAME__S3_REGION_MAIN_STORAGE );
			if ( StringUtils.isNotEmpty( propertyValue ) ) {

				 configData_Directories_ProcessUploadCommand_InWorkDirectory.setS3Region_MainStorage( propertyValue.trim() );
			}

			propertyValue = configProps.getProperty( PROPERTY_NAME__S3_BUCKET_TEMPORARY_INPUT_FILE_STORAGE );
			if ( StringUtils.isNotEmpty( propertyValue ) ) {

				 configData_Directories_ProcessUploadCommand_InWorkDirectory.setS3Bucket_TemporaryInputFileStorage( propertyValue.trim() );
			}

			propertyValue = configProps.getProperty(PROPERTY_NAME__SUBMITTED_FILE_PATH_RESTRICTIONS);
			if (StringUtils.isNotEmpty(propertyValue)) {
				String[] valuesArray = propertyValue.split(",");
				List<String> values = new ArrayList<>(valuesArray.length);
				for (String value : valuesArray) {
					if (StringUtils.isNotEmpty(value)) {
						values.add(value.trim());
					}
				}
				if (!values.isEmpty()) {
					configData_Directories_ProcessUploadCommand_InWorkDirectory
							.setSubmittedFilePathRestrictions(values);
				}
			}

		} catch (RuntimeException e) {
			log.error("Error processing Properties file '" + propertiesFilename + "', exception: " + e.toString(), e);
			throw e;

		} catch (Exception e) {
			log.error("Error processing Properties file '" + propertiesFilename + "', exception: " + e.toString(), e);
			throw e;
		} finally {
			if (propertiesFileAsStream != null) {
				propertiesFileAsStream.close();
			}
		}
	}

	/**
	 * 
	 *
	 */
	private static class InternalConfigDirectoryStrings {

		/**
		 * The Base Directory that the scans are written to for perm storage
		 */
		private String scanStorageBaseDirectory;

	}

}
