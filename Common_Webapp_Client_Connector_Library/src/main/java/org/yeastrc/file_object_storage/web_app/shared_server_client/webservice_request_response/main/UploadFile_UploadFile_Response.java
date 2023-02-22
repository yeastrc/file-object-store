package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response from UploadScanFile_UploadScanFile_Servlet
 *
 */
@XmlRootElement(name="UploadFile_UploadFile_Response")
@XmlAccessorType(XmlAccessType.FIELD)
public class UploadFile_UploadFile_Response {
	
	// Properties as XML elements

	@XmlAttribute // attribute name is property name
	private boolean statusSuccess;

	@XmlAttribute // attribute name is property name
	private String apiKey_Assigned;
	
	//  These are populated for upload filesize exceeds allowed max

	@XmlAttribute // attribute name is property name
	private Boolean fileSizeLimitExceeded;

	@XmlAttribute // attribute name is property name
	private Long maxSize;

	@XmlAttribute // attribute name is property name
	private String maxSizeFormatted;
	

	@Override
	public String toString() {
		return "UploadFile_UploadFile_Response [statusSuccess=" + statusSuccess + ", apiKey_Assigned=" + apiKey_Assigned
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