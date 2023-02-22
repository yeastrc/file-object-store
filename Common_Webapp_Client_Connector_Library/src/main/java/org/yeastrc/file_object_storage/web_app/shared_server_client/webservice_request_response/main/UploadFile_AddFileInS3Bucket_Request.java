package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

//  AWS S3 Support commented out.  See file ZZ__AWS_S3_Support_CommentedOut.txt in GIT repo root.

/**
 *  AWS S3 Support commented out.  See file ZZ__AWS_S3_Support_CommentedOut.txt in GIT repo root.
 *  
 *  
 * Request object for POST to Webservice UploadFile_Init_Servlet
 *
 */
@XmlRootElement(name="uploadFile_AddFileInS3Bucket_Request")
@XmlAccessorType(XmlAccessType.FIELD)
public class UploadFile_AddFileInS3Bucket_Request extends BaseYRCFileObjectStoreWebserviceRequest {

	//  AWS S3 Support commented out.  See file ZZ__AWS_S3_Support_CommentedOut.txt in GIT repo root.
	
	
//	// Properties as XML attributes
//
//	@XmlAttribute
//	private String uploadFileTempKey; // assigned temp key for rest of Upload  File process
//
//	@XmlAttribute // attribute name is property name
//	private String s3Bucket;
//
//	@XmlAttribute // attribute name is property name
//	private String s3ObjectKey;
//
//	/**
//	 * Optional.
//	 * Used for evaluating filename suffix for valid file types.
//	 * If not provided, the suffix of the s3ObjectKey string is evaluated
//	 */
//	@XmlAttribute // attribute name is property name
//	private String scanFilenameSuffix;
//	
//	/**
//	 * Required if region other than default configured in Spectral Storage web app
//	 */
//	@XmlAttribute // attribute name is property name
//	private String s3Region;
//
//	@Override
//	public String toString() {
//		return "UploadFile_AddFileInS3Bucket_Request [uploadFileTempKey=" + uploadFileTempKey + ", s3Bucket=" + s3Bucket
//				+ ", s3ObjectKey=" + s3ObjectKey + ", scanFilenameSuffix=" + scanFilenameSuffix + ", s3Region="
//				+ s3Region + "]";
//	}
//
//
//	
//	
//	public String getS3Bucket() {
//		return s3Bucket;
//	}
//
//	public void setS3Bucket(String s3Bucket) {
//		this.s3Bucket = s3Bucket;
//	}
//
//	public String getS3ObjectKey() {
//		return s3ObjectKey;
//	}
//
//	public void setS3ObjectKey(String s3ObjectKey) {
//		this.s3ObjectKey = s3ObjectKey;
//	}
//
//	public String getS3Region() {
//		return s3Region;
//	}
//
//	public void setS3Region(String s3Region) {
//		this.s3Region = s3Region;
//	}
//
//	public String getUploadFileTempKey() {
//		return uploadFileTempKey;
//	}
//
//	public void setUploadFileTempKey(String uploadFileTempKey) {
//		this.uploadFileTempKey = uploadFileTempKey;
//	}
//
//	public String getFilenameSuffix() {
//		return scanFilenameSuffix;
//	}
//
//	public void setFilenameSuffix(String scanFilenameSuffix) {
//		this.scanFilenameSuffix = scanFilenameSuffix;
//	}

}
