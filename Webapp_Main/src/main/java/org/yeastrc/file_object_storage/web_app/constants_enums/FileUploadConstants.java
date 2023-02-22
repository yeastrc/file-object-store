package org.yeastrc.file_object_storage.web_app.constants_enums;

import java.text.NumberFormat;

/**
 * 
 *
 */
public class FileUploadConstants {


	 public static final long MAX_FILE_UPLOAD_SIZE = ( 40L * 1000L * 1000L * 1000L ); // 40GB max

//	public static final long MAX_FILE_UPLOAD_SIZE = ( 2L * 10L * 1000L  ); // temp smaller max of 20KB

//	public static final long MAX_FILE_UPLOAD_SIZE = 5L; // temp smaller max of 5 bytes

	
	public static final String MAX_FILE_UPLOAD_SIZE_FORMATTED = NumberFormat.getInstance().format( MAX_FILE_UPLOAD_SIZE );
	
	
	public static long get_MAX_FILE_UPLOAD_SIZE() {
		
		return MAX_FILE_UPLOAD_SIZE;
	}
	
	
	public static String get_MAX_FILE_UPLOAD_SIZE_FORMATTED() {
		
		return MAX_FILE_UPLOAD_SIZE_FORMATTED;
	}
	

	/**
	 * MAIN storage subdir
	 */
	public static final String FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR = "file_object_storage_main_storage_base_dir";
	

	public static final String UPLOAD_FILE_TEMP_BASE_DIR = "upload_file_temp_base_dir";
	
	
	/**
	 * Prefix for temp subdir per request
	 */
	public static final String UPLOAD_FILE_TEMP_SUB_DIR_PREFIX = "up_tmp_";
	
	
}
