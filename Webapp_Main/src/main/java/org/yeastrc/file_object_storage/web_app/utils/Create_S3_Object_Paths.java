package org.yeastrc.file_object_storage.web_app.utils;

import org.yeastrc.file_object_storage.web_app.constants_enums.S3_ObjectKey_Prefixes_UploadedFile;

public class Create_S3_Object_Paths {

	//  private constructor
	private Create_S3_Object_Paths() { }
	
	/**
	 * @return instance
	 */
	public static Create_S3_Object_Paths getInstance() { 
		return new Create_S3_Object_Paths(); 
	}
	
	//  Change to have a single S3 object path for uploaded scan files 
	//  since cannot change the S3 object key for a S3 object
	

	/**
	 * @param uploadScanFileTempKey_Dir_Name - All scan files will retain the path based on the uploadScanFileTempKey_Dir_Name
	 * @param scanFilenameToProcess
	 * @return
	 */
	public String get_ScanFile_Uploaded_S3ObjectPath( String uploadScanFileTempKey_Dir_Name, String scanFilenameToProcess ) {
		
		return  S3_ObjectKey_Prefixes_UploadedFile.UPLOADED_FILE 
				+ S3_ObjectKey_Prefixes_UploadedFile.S3_PATH_SEPARATOR
				+ uploadScanFileTempKey_Dir_Name
				+ S3_ObjectKey_Prefixes_UploadedFile.S3_PATH_SEPARATOR
				+ scanFilenameToProcess;
	}
	
//	/**
//	 * @param uploadScanFileTempKey_Dir_Name
//	 * @param scanFilenameToProcess
//	 * @return
//	 */
//	public String get_ScanFile_TempUpload_S3ObjectPath( String uploadScanFileTempKey_Dir_Name, String scanFilenameToProcess ) {
//		
//		return  S3_ObjectKey_Prefixes_UploadedScanFile.TEMP_UPLOADED_SCAN_FILE 
//				+ S3_ObjectKey_Prefixes_UploadedScanFile.S3_PATH_SEPARATOR
//				+ uploadScanFileTempKey_Dir_Name
//				+ S3_ObjectKey_Prefixes_UploadedScanFile.S3_PATH_SEPARATOR
//				+ scanFilenameToProcess;
//	}

//	/**
//	 * @param uploadScanFileTempKey_Dir_Name
//	 * @param scanFilenameToProcess
//	 * @return
//	 */
//	public String get_ScanFile_ToImport_S3ObjectPath( String dirToProcessScanFile_Name, String scanFilenameToProcess ) {
//		
//		return  S3_ObjectKey_Prefixes_UploadedScanFile.SCAN_FILE_TO_IMPORT_PREFIX 
//				+ S3_ObjectKey_Prefixes_UploadedScanFile.S3_PATH_SEPARATOR
//				+ dirToProcessScanFile_Name
//				+ S3_ObjectKey_Prefixes_UploadedScanFile.S3_PATH_SEPARATOR
//				+ scanFilenameToProcess;
//	}
	
}
