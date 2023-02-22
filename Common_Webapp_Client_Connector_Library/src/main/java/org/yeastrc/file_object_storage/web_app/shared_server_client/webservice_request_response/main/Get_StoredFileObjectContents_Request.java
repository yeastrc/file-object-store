package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Request object for POST to Webservice Get_StoredFileObjectContents_Servlet
 *
 */
@XmlRootElement(name="get_StoredFileObjectContents_Request")
@XmlAccessorType(XmlAccessType.FIELD)
public class Get_StoredFileObjectContents_Request extends BaseGetDataWebserviceRequest {

	@XmlAttribute // attribute name is property name
	private String fileAPIKey;

	@XmlAttribute // attribute name is property name
	private Boolean returnAs_GZIP_IfAvailable;

	@Override
	public String toString() {
		return "Get_StoredFileObjectContents_Request [fileAPIKey=" + fileAPIKey + ", returnAs_GZIP_IfAvailable="
				+ returnAs_GZIP_IfAvailable + "]";
	}
	
	public String getFileAPIKey() {
		return fileAPIKey;
	}

	public void setFileAPIKey(String fileAPIKey) {
		this.fileAPIKey = fileAPIKey;
	}

	public Boolean getReturnAs_GZIP_IfAvailable() {
		return returnAs_GZIP_IfAvailable;
	}

	public void setReturnAs_GZIP_IfAvailable(Boolean returnAs_GZIP_IfAvailable) {
		this.returnAs_GZIP_IfAvailable = returnAs_GZIP_IfAvailable;
	}

}
