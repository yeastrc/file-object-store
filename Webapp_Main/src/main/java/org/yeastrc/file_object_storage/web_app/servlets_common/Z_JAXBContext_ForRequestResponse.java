package org.yeastrc.file_object_storage.web_app.servlets_common;

import javax.xml.bind.JAXBContext;

import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.*;

/**
 * JAXB Context for requests and responses
 *
 */
public class Z_JAXBContext_ForRequestResponse {

	private static Z_JAXBContext_ForRequestResponse instance = null;

	//  private constructor
	private Z_JAXBContext_ForRequestResponse() { }
	
	/**
	 * @return Singleton instance
	 */
	public synchronized static Z_JAXBContext_ForRequestResponse getSingletonInstance() throws Exception {
		if ( instance == null ) {
			instance = new Z_JAXBContext_ForRequestResponse();
			instance.init();
		}
		return instance; 
	}
	
	
	private JAXBContext jaxbContext;

	
	/**
	 * @throws Exception
	 */
	private void init() throws Exception {

		jaxbContext = 
				JAXBContext.newInstance( 
						UploadFile_AddFileInS3Bucket_Request.class,
						UploadFile_AddFileInS3Bucket_Response.class,
						UploadFile_AddFileFromFilenameAndPath_Request.class,
						UploadFile_AddFileFromFilenameAndPath_Response.class,
						UploadFile_UploadFile_Response.class,
						Get_StoredFileObjectContents_Request.class
						);
	}
	
	/**
	 * @return
	 */
	public JAXBContext getJAXBContext() {
		return jaxbContext;
	}
}
