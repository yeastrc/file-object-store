package org.yeastrc.file_object_storage.web_app.shared_server_client.constants_enums;

/**
 * URL Paths to the servlets
 *
 */
public class WebserviceFileObjectStoragePathConstants {

	//  Servlet - HealthCheck - Only validates that webapp is up and responding
	
	public static final String HEALTH_CHECK = "/health-check";
	
	//  Servlets - Processing  File Upload and Returning Status and API Key
	
	public static final String UPLOAD_FILE_ADD_FILE_IN_S3_BUCKET_NAME_OBJECT_NAME_SERVLET_XML = "/update/uploadFile_add_S3_BucketName_ObjectName_XML";

	public static final String UPLOAD_FILE_ADD_FILENAME_WITH_PATH_SERVLET_XML = "/update/uploadFile_addFilenameWithPath_XML";
	
	public static final String UPLOAD_FILE_UPLOAD_FILE_SERVLET_XML = "/update/uploadFile_uploadFile_XML";
		

	public static final String GET_FILE_SERVLET_XML = "/get/get_FileContents_XML";
		
		
}
