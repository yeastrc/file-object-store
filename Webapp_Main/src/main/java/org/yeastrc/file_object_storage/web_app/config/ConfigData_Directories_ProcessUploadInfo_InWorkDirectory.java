package org.yeastrc.file_object_storage.web_app.config;

import java.io.File;
import java.util.List;

/**
 * Config data for config file in web app Work Directory
 * 
 * The Secondary configuration for the web app
 * 
 * 
 * Singleton Instance
 *
 */
public class ConfigData_Directories_ProcessUploadInfo_InWorkDirectory {
	
	private static volatile ConfigData_Directories_ProcessUploadInfo_InWorkDirectory instance;

	//  package private constructor
	ConfigData_Directories_ProcessUploadInfo_InWorkDirectory() { }
	
	/**
	 * @return Singleton instance
	 */
	public synchronized static ConfigData_Directories_ProcessUploadInfo_InWorkDirectory getSingletonInstance() { 
		return instance; 
	}
	public static void setInstance(ConfigData_Directories_ProcessUploadInfo_InWorkDirectory instanceNew) {
		instance = instanceNew;
	}

	/**
	 * The Base Directory used by File Object Storage.  
	 * 
	 * This is required if sending files to S3 for temp hold file contents streamed to this webapp via webservice call.
	 *   **  Another solution is to store the file contents in a different S3 bucket temporarily and then copy info perm S3 bucket.
	 * 
	 * * The Perm file storage will be under a sub dir
	 * 
	 * * Temp sub dirs will be created under this dir as needed and named starting with 'temp' 
	 *  
	 */
	private File fileObjectStorage_BaseDirectory;

	/**
	 * The S3 bucket that the file data is written to for perm storage
	 */
	private String s3Bucket_MainStorage;

	/**
	 * The S3 region that the file data is written to for perm storage
	 */
	private String s3Region_MainStorage;

	/**
	 * The S3 bucket that the incoming file data is written to for temporary storage
	 */
	private String s3Bucket_TemporaryInputFileStorage;


	
	/**
	 * Path for passed in Filename must start with one of these values
	 */
	private List<String> submittedFilePathRestrictions;

	public File getFileObjectStorage_BaseDirectory() {
		return fileObjectStorage_BaseDirectory;
	}

	public void setFileObjectStorage_BaseDirectory(File fileObjectStorage_BaseDirectory) {
		this.fileObjectStorage_BaseDirectory = fileObjectStorage_BaseDirectory;
	}

	public List<String> getSubmittedFilePathRestrictions() {
		return submittedFilePathRestrictions;
	}

	public void setSubmittedFilePathRestrictions(List<String> submittedFilePathRestrictions) {
		this.submittedFilePathRestrictions = submittedFilePathRestrictions;
	}

	public String getS3Bucket_MainStorage() {
		return s3Bucket_MainStorage;
	}

	public void setS3Bucket_MainStorage(String s3Bucket_MainStorage) {
		this.s3Bucket_MainStorage = s3Bucket_MainStorage;
	}

	public String getS3Region_MainStorage() {
		return s3Region_MainStorage;
	}

	public void setS3Region_MainStorage(String s3Region_MainStorage) {
		this.s3Region_MainStorage = s3Region_MainStorage;
	}

	public String getS3Bucket_TemporaryInputFileStorage() {
		return s3Bucket_TemporaryInputFileStorage;
	}

	public void setS3Bucket_TemporaryInputFileStorage(String s3Bucket_TemporaryInputFileStorage) {
		this.s3Bucket_TemporaryInputFileStorage = s3Bucket_TemporaryInputFileStorage;
	}
	
}
