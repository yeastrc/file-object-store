package org.yeastrc.file_object_storage.web_app.servlets_upload_file__utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.yeastrc.file_object_storage.web_app.constants_enums.YRC_FileObjectStorage__AWS_S3_Constants;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

/**
 * Output Stream that will automatically break the data passed to it 
 *   into parts to send to AWS S3 Upload Parts
 *
 */
public class UploadFile_Util__OutputStream__SendTo_AWS_S3 extends OutputStream {
	
	//  Properties from constructor

	long partSize_To_S3_Client;
	
	S3Client amazonS3_Client_ForOutput;

	String amazonS3_bucketName;
	
	String s3_Object_Key;
	
	String uploadId_S3Client_Client;
	
	List<CompletedPart> completedPart_List;
	
	//  Local properties
	
	byte[] bytes_ToSendTo_S3;
	int bytes_ToSendTo_S3_CurrentIndex;
	
	 //  initialize to starting part number.  Incremented after each part is sent
	int partNumber_To_S3Client_Client = YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MINIMUM_PART_NUMBER;
	
	boolean closeCalled = false;
	
	
	/**
	 * 'constructor'
	 * 
	 * 
	 * @param partSize_To_S3_Client
	 * @param amazonS3_Client_ForOutput
	 * @param amazonS3_bucketName
	 * @param s3_Object_Key
	 * @param uploadId_S3Client_Client
	 * @param completedPart_List
	 */
	public UploadFile_Util__OutputStream__SendTo_AWS_S3( 
			
			int partSize_To_S3_Client,

			S3Client amazonS3_Client_ForOutput,

			String amazonS3_bucketName,
			
			String s3_Object_Key,
			
			String uploadId_S3Client_Client,
			List<CompletedPart> completedPart_List
			) {
		this.partSize_To_S3_Client = partSize_To_S3_Client;
		this.amazonS3_Client_ForOutput = amazonS3_Client_ForOutput;
		this.amazonS3_bucketName = amazonS3_bucketName;
		this.s3_Object_Key = s3_Object_Key;
		this.uploadId_S3Client_Client = uploadId_S3Client_Client;
		this.completedPart_List = completedPart_List;
		

		this.bytes_ToSendTo_S3 = new byte[ partSize_To_S3_Client ];
		this.bytes_ToSendTo_S3_CurrentIndex = 0;
	}

	@Override
	public void write(int b) throws IOException {

		this.bytes_ToSendTo_S3[ this.bytes_ToSendTo_S3_CurrentIndex ] = (byte) b;
		
		this.bytes_ToSendTo_S3_CurrentIndex++;
		
		if ( this.bytes_ToSendTo_S3_CurrentIndex >= this.partSize_To_S3_Client ) {
			
			this.send_UploadPart_To_AWS_S3();
			
			//  this.bytes_ToSendTo_S3_CurrentIndex = 0;  // Moved to this.send_UploadPart_To_AWS_S3();
		}
	}

	@Override
	public void close() throws IOException {
//		super.close();
		
		if ( closeCalled ) {
			
			//  close() already called
			
			return; // EARLY RETURN
		}
		
		closeCalled = true;
		
		//  Send last block to AWS S3
		
		this.send_UploadPart_To_AWS_S3();
	}
	
	/**
	 * 
	 */
	private void send_UploadPart_To_AWS_S3() {
		
		if ( this.bytes_ToSendTo_S3_CurrentIndex == 0 ) {
			// Nothing to send so exit
			
			return; // EARLY RETURN
		}

	     UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
	                .bucket(amazonS3_bucketName)
	                .key(s3_Object_Key)
	                .uploadId(uploadId_S3Client_Client)
	                .partNumber( partNumber_To_S3Client_Client ).build();
	     
	     RequestBody amazonS3_RequestBody = null;
	     
	     if ( this.bytes_ToSendTo_S3_CurrentIndex >= this.partSize_To_S3_Client ) {
	    	 
	    	 amazonS3_RequestBody = RequestBody.fromBytes( this.bytes_ToSendTo_S3 );
	    	 
	     } else {
	    	 // Last block so sending part of the byte array so use ByteArrayInputStream
	    	 
	    	 ByteArrayInputStream bais_ToSendTo_S3 = new ByteArrayInputStream(bytes_ToSendTo_S3, 0, bytes_ToSendTo_S3_CurrentIndex);
	    	 
	    	 amazonS3_RequestBody = RequestBody.fromInputStream(bais_ToSendTo_S3, this.bytes_ToSendTo_S3_CurrentIndex);
	     }

	     String etag = amazonS3_Client_ForOutput.uploadPart(uploadPartRequest, amazonS3_RequestBody ).eTag();

	     CompletedPart completedPart = 
	    		 CompletedPart
	    		 .builder()
	    		 .partNumber( partNumber_To_S3Client_Client )
	    		 .eTag(etag)
	    		 .build();

	     completedPart_List.add( completedPart );

	     partNumber_To_S3Client_Client++;
	     
	     this.bytes_ToSendTo_S3_CurrentIndex = 0; // Reset
	}

	

	//  Not override the rest since the base class OutputStream calls void write(int b) from those methods

	//	@Override
	//	public void write(byte[] b, int off, int len) throws IOException {
	//		super.write(b, off, len);
	//	}
	//
	//	@Override
	//	public void write(byte[] b) throws IOException {
	//		super.write(b);
	//	}


	//  Not override 
	
	//	@OVERRIDE
	//	PUBLIC VOID FLUSH() THROWS IOEXCEPTION {
	//		SUPER.FLUSH();
	//	}
	//

}
