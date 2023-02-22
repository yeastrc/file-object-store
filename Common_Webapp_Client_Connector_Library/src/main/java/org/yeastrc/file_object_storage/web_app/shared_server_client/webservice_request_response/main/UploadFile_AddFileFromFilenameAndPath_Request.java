package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Request object for POST to Webservice UploadFile_AddFileFromFilenameAndPath_Servlet
 *
 */
@XmlRootElement(name="uploadFile_AddFileFromFilenameAndPath_Request")
@XmlAccessorType(XmlAccessType.FIELD)
public class UploadFile_AddFileFromFilenameAndPath_Request extends BaseYRCFileObjectStoreWebserviceRequest {

	// Properties as XML attributes

	@XmlAttribute // attribute name is property name
	private String filenameWithPath;

	@XmlAttribute // attribute name is property name
	private BigInteger fileSize;

	@XmlAttribute // attribute name is property name
	private boolean gzipCompressTheStoredFile;

	@Override
	public String toString() {
		return "UploadFile_AddFileFromFilenameAndPath_Request [filenameWithPath=" + filenameWithPath + ", fileSize="
				+ fileSize + ", gzipCompressTheStoredFile=" + gzipCompressTheStoredFile + "]";
	}

	public BigInteger getFileSize() {
		return fileSize;
	}

	public void setFileSize(BigInteger fileSize) {
		this.fileSize = fileSize;
	}

	public String getFilenameWithPath() {
		return filenameWithPath;
	}

	public void setFilenameWithPath(String filenameWithPath) {
		this.filenameWithPath = filenameWithPath;
	}

	public boolean isGzipCompressTheStoredFile() {
		return gzipCompressTheStoredFile;
	}

	public void setGzipCompressTheStoredFile(boolean gzipCompressTheStoredFile) {
		this.gzipCompressTheStoredFile = gzipCompressTheStoredFile;
	}

}
