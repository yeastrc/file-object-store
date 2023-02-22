package org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main;


import java.io.InputStream;

/**
 * Not serialized to bytes (XML, JSON, ?)
 *
 * Used only in response from call to CallSpectralStorageWebservice.call_GetFile_Webservice(...)
 *
 */
public class Get_StoredFileObjectContents_Response_FromConnectionLibraryCall extends BaseGetDataWebserviceRequest {

	private Get_StoredFileObjectContents_Response_InHeader responseFromWebserviceInHeader;
	
	private Long returnedContentsLength;
	
	private InputStream inputStream_FileObjectContents;

	@Override
	public String toString() {
		return "Get_StoredFileObjectContents_Response_FromConnectionLibraryCall [responseFromWebserviceInHeader="
				+ responseFromWebserviceInHeader + ", returnedContentsLength=" + returnedContentsLength
				+ ", inputStream_FileObjectContents=" + inputStream_FileObjectContents + "]";
	}
	
	public Get_StoredFileObjectContents_Response_InHeader getResponseFromWebserviceInHeader() {
		return responseFromWebserviceInHeader;
	}

	public void setResponseFromWebserviceInHeader(
			Get_StoredFileObjectContents_Response_InHeader responseFromWebserviceInHeader) {
		this.responseFromWebserviceInHeader = responseFromWebserviceInHeader;
	}

	public InputStream getInputStream_FileObjectContents() {
		return inputStream_FileObjectContents;
	}

	public void setInputStream_FileObjectContents(InputStream inputStream_FileObjectContents) {
		this.inputStream_FileObjectContents = inputStream_FileObjectContents;
	}

	public Long getReturnedContentsLength() {
		return returnedContentsLength;
	}

	public void setReturnedContentsLength(Long returnedContentsLength) {
		this.returnedContentsLength = returnedContentsLength;
	}


}
