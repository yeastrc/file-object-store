package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response object for POST to Webservice Get_StoredFileObjectContents_Servlet, put in the response Header
 * 
 * See ServletResponse_HeaderKeyStrings.
 *
 */
@XmlRootElement(name="get_StoredFileObjectContents_Response_InHeader")
@XmlAccessorType(XmlAccessType.FIELD)
public class Get_StoredFileObjectContents_Response_InHeader extends BaseGetDataWebserviceRequest {

	@XmlAttribute // attribute name is property name
	private Long fileLength_NonGZIP;

	@XmlAttribute // attribute name is property name
	private Boolean response_Is_GZIP;  //  set to true when response is GZIP

	@XmlAttribute // attribute name is property name
	private Boolean fileAPIKey_NOT_FOUND;  //  set to true with return status code 404

	@Override
	public String toString() {
		return "Get_StoredFileObjectContents_Response_InHeader [fileLength_NonGZIP=" + fileLength_NonGZIP
				+ ", response_Is_GZIP=" + response_Is_GZIP + ", fileAPIKey_NOT_FOUND=" + fileAPIKey_NOT_FOUND + "]";
	}

	public Long getFileLength_NonGZIP() {
		return fileLength_NonGZIP;
	}

	public void setFileLength_NonGZIP(Long fileLength_NonGZIP) {
		this.fileLength_NonGZIP = fileLength_NonGZIP;
	}

	public Boolean getFileAPIKey_NOT_FOUND() {
		return fileAPIKey_NOT_FOUND;
	}

	public void setFileAPIKey_NOT_FOUND(Boolean fileAPIKey_NOT_FOUND) {
		this.fileAPIKey_NOT_FOUND = fileAPIKey_NOT_FOUND;
	}

	public Boolean getResponse_Is_GZIP() {
		return response_Is_GZIP;
	}

	public void setResponse_Is_GZIP(Boolean response_Is_GZIP) {
		this.response_Is_GZIP = response_Is_GZIP;
	}

}
