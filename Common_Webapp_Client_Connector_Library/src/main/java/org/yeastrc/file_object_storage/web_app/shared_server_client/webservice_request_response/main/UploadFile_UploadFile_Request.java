package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;

import java.io.File;

/**
 * Request to UploadScanFile_UploadScanFile_Servlet via CallSpectralStorageWebservice
 * 
 * Not serialized to bytes (XML, JSON, ?)
 *
 * Used only in call to CallSpectralStorageWebservice.call_UploadScanFile_Service(...)
 */
public class UploadFile_UploadFile_Request {
	
	private File file;
	private boolean gzipCompressContents;

	@Override
	public String toString() {
		return "UploadFile_UploadFile_Request [file=" + file + ", gzipCompressContents=" + gzipCompressContents + "]";
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File scanFile) {
		this.file = scanFile;
	}

	public boolean isGzipCompressContents() {
		return gzipCompressContents;
	}

	public void setGzipCompressContents(boolean gzipCompressContents) {
		this.gzipCompressContents = gzipCompressContents;
	}

	
}
