package org.yeastrc.file_object_storage.web_app.utils;

import org.yeastrc.file_object_storage.web_app.hashes_compute.Hashes_Compute;
import org.yeastrc.file_object_storage.web_app.hashes_compute.Hashes_Compute.Hashes_Compute_Result;

public class FileAPIKey_ComputeFromScanFileContentHashes {

	/**
	 * private constructor
	 */
	private FileAPIKey_ComputeFromScanFileContentHashes(){}
	public static FileAPIKey_ComputeFromScanFileContentHashes getInstance( ) throws Exception {
		FileAPIKey_ComputeFromScanFileContentHashes instance = new FileAPIKey_ComputeFromScanFileContentHashes();
		return instance;
	}
	
	/**
	 * Compute the File API from the File Content Hashes
	 * 
	 * @return API Key as String
	 * @throws Exception 
	 */
	public String fileAPIKey_ComputeFromScanFileContentHashes( Hashes_Compute hashes_Compute ) throws Exception {

		Hashes_Compute_Result hashes_Compute_Result = hashes_Compute.compute_Hashes();

		byte[] hash_sha384_Bytes = hashes_Compute_Result.getSha_384_Hash();
		
		String hash_sha384_String = Get_HashBytes_As_HexString.getInstance().get_HashBytes_As_HexString( hash_sha384_Bytes );
		
		return hash_sha384_String;
	}
}
