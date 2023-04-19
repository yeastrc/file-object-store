package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response from 
 *
 */
@XmlRootElement(name="uploadFile_AddFileIn_S3_BucketName_ObjectName_Response")
@XmlAccessorType(XmlAccessType.FIELD)
public class UploadFile_AddFileIn_S3_BucketName_ObjectName_Response {
	
	// Properties as XML elements
	
	@XmlAttribute // attribute name is property name
	private boolean statusSuccess;

	@XmlAttribute // attribute name is property name
	private String apiKey_Assigned;
	
	@XmlAttribute // attribute name is property name
	private boolean uploadFileS3BucketOrObjectKey_NotFound;
	
	/**
	 * S3 returned an permission error when trying to determine if object exists or unable to determine the size
	 */
	@XmlAttribute // attribute name is property name
	private boolean uploadFileS3BucketOrObjectKey_PermissionError;
		
	
	//  These are populated for upload filesize exceeds allowed max
	@XmlAttribute // attribute name is property name
	private Boolean fileSizeLimitExceeded;
	@XmlAttribute // attribute name is property name
	private Long maxSize;
	@XmlAttribute // attribute name is property name
	private String maxSizeFormatted;
	
	@Override
	public String toString() {
		return "UploadFile_AddFileInS3Bucket_Response [statusSuccess=" + statusSuccess + ", apiKey_Assigned="
				+ apiKey_Assigned + ", uploadFileS3BucketOrObjectKey_NotFound=" + uploadFileS3BucketOrObjectKey_NotFound
				+ ", uploadFileS3BucketOrObjectKey_PermissionError=" + uploadFileS3BucketOrObjectKey_PermissionError
				+ ", fileSizeLimitExceeded=" + fileSizeLimitExceeded + ", maxSize=" + maxSize + ", maxSizeFormatted="
				+ maxSizeFormatted + "]";
	}	

	
	public boolean isStatusSuccess() {
		return statusSuccess;
	}
	public void setStatusSuccess(boolean statusSuccess) {
		this.statusSuccess = statusSuccess;
	}
	public String getApiKey_Assigned() {
		return apiKey_Assigned;
	}
	public void setApiKey_Assigned(String apiKey_Assigned) {
		this.apiKey_Assigned = apiKey_Assigned;
	}
	public boolean isUploadFileS3BucketOrObjectKey_NotFound() {
		return uploadFileS3BucketOrObjectKey_NotFound;
	}
	public void setUploadFileS3BucketOrObjectKey_NotFound(boolean uploadFileS3BucketOrObjectKey_NotFound) {
		this.uploadFileS3BucketOrObjectKey_NotFound = uploadFileS3BucketOrObjectKey_NotFound;
	}
	public boolean isUploadFileS3BucketOrObjectKey_PermissionError() {
		return uploadFileS3BucketOrObjectKey_PermissionError;
	}
	public void setUploadFileS3BucketOrObjectKey_PermissionError(boolean uploadFileS3BucketOrObjectKey_PermissionError) {
		this.uploadFileS3BucketOrObjectKey_PermissionError = uploadFileS3BucketOrObjectKey_PermissionError;
	}
	public Boolean getFileSizeLimitExceeded() {
		return fileSizeLimitExceeded;
	}
	public void setFileSizeLimitExceeded(Boolean fileSizeLimitExceeded) {
		this.fileSizeLimitExceeded = fileSizeLimitExceeded;
	}
	public Long getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(Long maxSize) {
		this.maxSize = maxSize;
	}
	public String getMaxSizeFormatted() {
		return maxSizeFormatted;
	}
	public void setMaxSizeFormatted(String maxSizeFormatted) {
		this.maxSizeFormatted = maxSizeFormatted;
	}
}
