package org.yeastrc.file_object_storage.web_app.utils;

/**
 * 
 *
 */
public class Storage_Object_Name__Create {
	
	private static final String MAIN_DATA_FILE_NAME = ".data";
	
	private static final String GZIP_DATA_FILE_NAME = MAIN_DATA_FILE_NAME + ".gz";

	private static final String GZIP_META_FILE_NAME = GZIP_DATA_FILE_NAME + ".metafile";
	
	
	/**
	 * 
	 *
	 */
	public static class Storage_Object_Name__Create__Result__MainName {
		
		private String mainObjectname;
		private String gzipObjectname;
		
		public String getMainObjectname() {
			return mainObjectname;
		}
		public String getGzipObjectname() {
			return gzipObjectname;
		}
	}

	/**
	 * @param apiKey
	 * @return
	 */
	public static Storage_Object_Name__Create__Result__MainName create_Main_Storage_Object_Name( String apiKey ) {
		
		Storage_Object_Name__Create__Result__MainName result = new Storage_Object_Name__Create__Result__MainName();
		
		result.mainObjectname = apiKey + MAIN_DATA_FILE_NAME;
		result.gzipObjectname = apiKey + GZIP_DATA_FILE_NAME;
		
		return result;
	}

	/**
	 * @param apiKey
	 * @return
	 */
	public static String create_GZIP_MetaData_Storage_Object_Name( String apiKey ) {
		
		return apiKey + GZIP_META_FILE_NAME;
	}
}
