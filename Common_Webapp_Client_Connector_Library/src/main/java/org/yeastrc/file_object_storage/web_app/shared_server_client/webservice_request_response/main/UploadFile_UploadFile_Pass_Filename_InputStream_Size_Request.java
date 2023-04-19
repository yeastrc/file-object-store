package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;

import java.io.InputStream;

/**
 * Request to UploadFile_UploadFile_Servlet via CallSpectralStorageWebservice
 * 
 * Not serialized to bytes (XML, JSON, ?)
 *
 * Used only in call to CallSpectralStorageWebservice.call_UploadFile_Pass_InputStream_Size_Service(...)
 */
public class UploadFile_UploadFile_Pass_Filename_InputStream_Size_Request {
	
	//  ALL must be populated
	private String filename;
	private InputStream file_InputStream;
	private Long file_Size;

	private boolean gzipCompressContents;

	@Override
	public String toString() {
		return "UploadFile_UploadFile_Pass_Filename_InputStream_Size_Request [filename=" + filename
				+ ", file_InputStream=" + file_InputStream + ", file_Size=" + file_Size + ", gzipCompressContents="
				+ gzipCompressContents + "]";
	}

	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public InputStream getFile_InputStream() {
		return file_InputStream;
	}
	public void setFile_InputStream(InputStream file_InputStream) {
		this.file_InputStream = file_InputStream;
	}
	public Long getFile_Size() {
		return file_Size;
	}
	public void setFile_Size(Long file_Size) {
		this.file_Size = file_Size;
	}

	public boolean isGzipCompressContents() {
		return gzipCompressContents;
	}

	public void setGzipCompressContents(boolean gzipCompressContents) {
		this.gzipCompressContents = gzipCompressContents;
	}
}
