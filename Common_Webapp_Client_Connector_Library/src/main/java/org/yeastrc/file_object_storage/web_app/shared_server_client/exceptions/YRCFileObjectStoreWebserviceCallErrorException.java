package org.yeastrc.file_object_storage.web_app.shared_server_client.exceptions;

/**
 * Error calling User Management Central Webservice
 *
 */
public class YRCFileObjectStoreWebserviceCallErrorException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private boolean callInterfaceInternalError;
	private String callInterfaceInternalErrorMessage;
	
	private boolean scanFilenameError;
	private String scanFilenameErrorMessage;
	
	private boolean badHTTPStatusCode;
	private boolean serverURLError;
	private boolean serverSendReceiveDataError;
	private boolean connectToServerError;
	private boolean failToEncodeDataToSendToServer;
	private boolean failToDecodeDataReceivedFromServer;
	
	private Integer httpStatusCode;
	private String webserviceURL;
	
	private byte[] errorStreamContents;


	public YRCFileObjectStoreWebserviceCallErrorException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public YRCFileObjectStoreWebserviceCallErrorException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public YRCFileObjectStoreWebserviceCallErrorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public YRCFileObjectStoreWebserviceCallErrorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public YRCFileObjectStoreWebserviceCallErrorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	
	public boolean isBadHTTPStatusCode() {
		return badHTTPStatusCode;
	}

	public void setBadHTTPStatusCode(boolean badHTTPStatusCode) {
		this.badHTTPStatusCode = badHTTPStatusCode;
	}

	public boolean isServerURLError() {
		return serverURLError;
	}

	public void setServerURLError(boolean serverURLError) {
		this.serverURLError = serverURLError;
	}

	public boolean isServerSendReceiveDataError() {
		return serverSendReceiveDataError;
	}

	public void setServerSendReceiveDataError(boolean serverSendReceiveDataError) {
		this.serverSendReceiveDataError = serverSendReceiveDataError;
	}

	public boolean isConnectToServerError() {
		return connectToServerError;
	}

	public void setConnectToServerError(boolean connectToServerError) {
		this.connectToServerError = connectToServerError;
	}

	public boolean isFailToEncodeDataToSendToServer() {
		return failToEncodeDataToSendToServer;
	}

	public void setFailToEncodeDataToSendToServer(boolean failToEncodeDataToSendToServer) {
		this.failToEncodeDataToSendToServer = failToEncodeDataToSendToServer;
	}

	public boolean isFailToDecodeDataReceivedFromServer() {
		return failToDecodeDataReceivedFromServer;
	}

	public void setFailToDecodeDataReceivedFromServer(boolean failToDecodeDataReceivedFromServer) {
		this.failToDecodeDataReceivedFromServer = failToDecodeDataReceivedFromServer;
	}

	public Integer getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(Integer httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	public String getWebserviceURL() {
		return webserviceURL;
	}

	public void setWebserviceURL(String webserviceURL) {
		this.webserviceURL = webserviceURL;
	}

	public byte[] getErrorStreamContents() {
		return errorStreamContents;
	}

	public void setErrorStreamContents(byte[] errorStreamContents) {
		this.errorStreamContents = errorStreamContents;
	}

	public boolean isCallInterfaceInternalError() {
		return callInterfaceInternalError;
	}

	public void setCallInterfaceInternalError(boolean callInterfaceInternalError) {
		this.callInterfaceInternalError = callInterfaceInternalError;
	}

	public String getCallInterfaceInternalErrorMessage() {
		return callInterfaceInternalErrorMessage;
	}

	public void setCallInterfaceInternalErrorMessage(String callInterfaceInternalErrorMessage) {
		this.callInterfaceInternalErrorMessage = callInterfaceInternalErrorMessage;
	}

	public boolean isScanFilenameError() {
		return scanFilenameError;
	}

	public void setScanFilenameError(boolean scanFilenameError) {
		this.scanFilenameError = scanFilenameError;
	}

	public String getScanFilenameErrorMessage() {
		return scanFilenameErrorMessage;
	}

	public void setScanFilenameErrorMessage(String scanFilenameErrorMessage) {
		this.scanFilenameErrorMessage = scanFilenameErrorMessage;
	}

}
