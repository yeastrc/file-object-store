package org.yeastrc.file_object_storage.web_app.hashes_compute;

import java.security.MessageDigest;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Computes the Hash of the file contents used as the API key (and filename) 
 * 
 * 
 * Computes cumulative hashes from passed in byte[] and # bytes in array to use 
 * 
 * Holds Instance variables of data in progress
 *
 */
public class Hashes_Compute {

//	private static final Logger log = LoggerFactory.getLogger( Hashes_Compute.class );

	/**
	 * private constructor
	 */
	private Hashes_Compute(){}
	public static Hashes_Compute getNewInstance( ) throws Exception {
		
		Hashes_Compute instance = new Hashes_Compute();

		instance.md_SHA_384 = MessageDigest.getInstance( SHA_384_ALGORITHM );

		instance.md_SHA_512 = MessageDigest.getInstance( SHA_512_ALGORITHM );

		instance.md_SHA_1 = MessageDigest.getInstance( SHA_1_ALGORITHM );
		
		return instance;
	}


	private static final String SHA_384_ALGORITHM = "SHA-384";
	private static final String SHA_512_ALGORITHM = "SHA-512";
	private static final String SHA_1_ALGORITHM = "SHA1";

	/**
	 * Result from call to compute_File_Hashes
	 * 
	 * Use class ScanFileAPIKey_ComputeFromScanFileContentHashes if generating API Key from hash
	 *
	 */
	public static class Hashes_Compute_Result {
		
		private byte[] sha_384_Hash;
		private byte[] sha_512_Hash;
		private byte[] sha_1_Hash;
		
		/**
		 * Use class ScanFileAPIKey_ComputeFromScanFileContentHashes if generating API Key from hash
		 * 
		 * @return
		 */
		public byte[] getSha_384_Hash() {
			return sha_384_Hash;
		}
		public void setSha_384_Hash(byte[] sha_384_Hash) {
			this.sha_384_Hash = sha_384_Hash;
		}
		public byte[] getSha_512_Hash() {
			return sha_512_Hash;
		}
		public void setSha_512_Hash(byte[] sha_512_Hash) {
			this.sha_512_Hash = sha_512_Hash;
		}
		public byte[] getSha_1_Hash() {
			return sha_1_Hash;
		}
		public void setSha_1_Hash(byte[] sha_1_Hash) {
			this.sha_1_Hash = sha_1_Hash;
		}
	}
	
	private MessageDigest md_SHA_384;

	private MessageDigest md_SHA_512;

	private MessageDigest md_SHA_1;
	
	private Hashes_Compute_Result computedResult;
	
	
	/**
	 * @param dataBytes
	 * @param numberBytesToUse - Number of bytes in array dataBytes to use
	 */
	public void updateHashesComputing( byte[] dataBytes, int numberBytesToUse ) {
		
		md_SHA_384.update( dataBytes, 0, numberBytesToUse );
		md_SHA_512.update( dataBytes, 0, numberBytesToUse );
		md_SHA_1.update( dataBytes, 0, numberBytesToUse );
	}
	
	/**
	 * Use class ScanFileAPIKey_ComputeFromScanFileContentHashes if generating API Key from hash
	 * 
	 * @return object with computed hashes
	 */
	public Hashes_Compute_Result compute_Hashes() {
		
		if ( computedResult != null ) {
			//  Already computed result so return it
			return computedResult; // EARLY RETURN
		}

		Hashes_Compute_Result result = new Hashes_Compute_Result();
	
		result.setSha_384_Hash( md_SHA_384.digest() );
		result.setSha_512_Hash( md_SHA_512.digest() );
		result.setSha_1_Hash( md_SHA_1.digest() );
		
		computedResult = result;
	
		return result;
	}
	

}
