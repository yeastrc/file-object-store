package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response from UploadFile_AddFileFromFilenameAndPath_Servlet
 *
 */
@XmlRootElement(name="uploadFile_AddFileFromFilenameAndPath_Response")
@XmlAccessorType(XmlAccessType.FIELD)
public class UploadFile_AddFileFromFilenameAndPath_Response {
	
	// Properties as XML elements
	
	@XmlAttribute // attribute name is property name
	private boolean statusSuccess;
	
	@XmlAttribute // attribute name is property name
	private String apiKey_Assigned;
		
	@XmlAttribute // attribute name is property name
	private boolean uploadFileWithPath_FilePathsAllowedNotConfigured;
	
	@XmlAttribute // attribute name is property name
	private boolean uploadFileWithPath_FilePathNotAllowed;
	
	@XmlAttribute // attribute name is property name
	private boolean uploadFileWithPath_FileNotFound;

	@XmlAttribute // attribute name is property name
	private boolean uploadFileWithPath_NotMatch_SubmittedFileSize;
		
	//  These are populated for upload filesize exceeds allowed max
	@XmlAttribute // attribute name is property name
	private boolean fileSizeLimitExceeded;
	@XmlAttribute // attribute name is property name
	private Long maxSize;
	@XmlAttribute // attribute name is property name
	private String maxSizeFormatted;
	

	@Override
	public String toString() {
		return "UploadFile_AddFileFromFilenameAndPath_Response [statusSuccess=" + statusSuccess + ", apiKey_Assigned="
				+ apiKey_Assigned + ", uploadFileWithPath_FilePathsAllowedNotConfigured="
				+ uploadFileWithPath_FilePathsAllowedNotConfigured + ", uploadFileWithPath_FilePathNotAllowed="
				+ uploadFileWithPath_FilePathNotAllowed + ", uploadFileWithPath_FileNotFound="
				+ uploadFileWithPath_FileNotFound + ", uploadFileWithPath_NotMatch_SubmittedFileSize="
				+ uploadFileWithPath_NotMatch_SubmittedFileSize + ", fileSizeLimitExceeded=" + fileSizeLimitExceeded
				+ ", maxSize=" + maxSize + ", maxSizeFormatted=" + maxSizeFormatted + "]";
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
	public boolean isUploadFileWithPath_FilePathsAllowedNotConfigured() {
		return uploadFileWithPath_FilePathsAllowedNotConfigured;
	}
	public void setUploadFileWithPath_FilePathsAllowedNotConfigured(
			boolean uploadFileWithPath_FilePathsAllowedNotConfigured) {
		this.uploadFileWithPath_FilePathsAllowedNotConfigured = uploadFileWithPath_FilePathsAllowedNotConfigured;
	}
	public boolean isUploadFileWithPath_FilePathNotAllowed() {
		return uploadFileWithPath_FilePathNotAllowed;
	}
	public void setUploadFileWithPath_FilePathNotAllowed(boolean uploadFileWithPath_FilePathNotAllowed) {
		this.uploadFileWithPath_FilePathNotAllowed = uploadFileWithPath_FilePathNotAllowed;
	}
	public boolean isUploadFileWithPath_FileNotFound() {
		return uploadFileWithPath_FileNotFound;
	}
	public void setUploadFileWithPath_FileNotFound(boolean uploadFileWithPath_FileNotFound) {
		this.uploadFileWithPath_FileNotFound = uploadFileWithPath_FileNotFound;
	}
	public boolean isUploadFileWithPath_NotMatch_SubmittedFileSize() {
		return uploadFileWithPath_NotMatch_SubmittedFileSize;
	}
	public void setUploadFileWithPath_NotMatch_SubmittedFileSize(boolean uploadFileWithPath_NotMatch_SubmittedFileSize) {
		this.uploadFileWithPath_NotMatch_SubmittedFileSize = uploadFileWithPath_NotMatch_SubmittedFileSize;
	}
	public boolean isFileSizeLimitExceeded() {
		return fileSizeLimitExceeded;
	}
	public void setFileSizeLimitExceeded(boolean fileSizeLimitExceeded) {
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
