package org.yeastrc.file_object_storage.web_app.constants_enums;

/**
 * Object prefixes used for S3 objects for uploaded files
 *
 */
public class S3_ObjectKey_Prefixes_UploadedFile {

	public static final String S3_PATH_SEPARATOR = "/"; 

	//  Change to have a single S3 object path for uploaded scan files 
	//  since cannot change the S3 object key for a S3 object

	/**
	 * When file initially uploaded
	 */
	public static final String UPLOADED_FILE = "file_object_storage_file_uploaded";

}
