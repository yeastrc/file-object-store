package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  
 *  
 *  
 * Request object for 
 *
 */
@XmlRootElement(name="uploadFile_AddFileIn_S3_BucketName_ObjectName_Request")
@XmlAccessorType(XmlAccessType.FIELD)
public class UploadFile_AddFileIn_S3_BucketName_ObjectName_Request extends BaseYRCFileObjectStoreWebserviceRequest {

	// Properties as XML attributes

	@XmlAttribute // attribute name is property name
	private String s3Bucket;

	@XmlAttribute // attribute name is property name
	private String s3ObjectKey;

	/**
	 * Optional.
	 * Used for evaluating filename suffix for valid file types.
	 * If not provided, the suffix of the s3ObjectKey string is evaluated
	 */
	@XmlAttribute // attribute name is property name
	private String filenameSuffix;
	
	/**
	 * Required if region other than default configured in Spectral Storage web app
	 */
	@XmlAttribute // attribute name is property name
	private String s3Region;

	@XmlAttribute // attribute name is property name
	private boolean gzipCompressTheStoredFile;


	@Override
	public String toString() {
		return "UploadFile_AddFileIn_S3_BucketName_ObjectName_Request [s3Bucket=" + s3Bucket + ", s3ObjectKey=" + s3ObjectKey + ", filenameSuffix="
				+ filenameSuffix + ", s3Region=" + s3Region + ", gzipCompressTheStoredFile="
				+ gzipCompressTheStoredFile + "]";
	}

	
	
	public String getS3Bucket() {
		return s3Bucket;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public String getS3ObjectKey() {
		return s3ObjectKey;
	}

	public void setS3ObjectKey(String s3ObjectKey) {
		this.s3ObjectKey = s3ObjectKey;
	}

	public String getS3Region() {
		return s3Region;
	}

	public void setS3Region(String s3Region) {
		this.s3Region = s3Region;
	}

	public String getFilenameSuffix() {
		return filenameSuffix;
	}

	public void setFilenameSuffix(String scanFilenameSuffix) {
		this.filenameSuffix = scanFilenameSuffix;
	}

	public boolean isGzipCompressTheStoredFile() {
		return gzipCompressTheStoredFile;
	}



	public void setGzipCompressTheStoredFile(boolean gzipCompressTheStoredFile) {
		this.gzipCompressTheStoredFile = gzipCompressTheStoredFile;
	}

}
