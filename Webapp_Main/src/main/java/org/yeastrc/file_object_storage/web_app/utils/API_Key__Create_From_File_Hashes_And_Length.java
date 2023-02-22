package org.yeastrc.file_object_storage.web_app.utils;

import org.yeastrc.file_object_storage.web_app.hashes_compute.Hashes_Compute;

/**
 * API Key - Create from File Hashes and Length
 *
 */
public class API_Key__Create_From_File_Hashes_And_Length {

	/**
	 * private constructor
	 */
	private API_Key__Create_From_File_Hashes_And_Length(){}
	public static API_Key__Create_From_File_Hashes_And_Length getNewInstance( ) throws Exception {
		API_Key__Create_From_File_Hashes_And_Length instance = new API_Key__Create_From_File_Hashes_And_Length();
		return instance;
	}

	
	/**
	 * @param compute_Hashes
	 * @param fileLength
	 * @throws Exception 
	 */
	public String getAPI_Key__Create_From_File_Hashes_And_Length( Hashes_Compute hashes_Compute, long fileLength ) throws Exception {
		
		String apiKey_String = 
				
				FileAPIKey_ComputeFromScanFileContentHashes.getInstance()
				.fileAPIKey_ComputeFromScanFileContentHashes( hashes_Compute )
				
				+ "_l_" + fileLength;
			;
			
			return apiKey_String;
	}
	
}
