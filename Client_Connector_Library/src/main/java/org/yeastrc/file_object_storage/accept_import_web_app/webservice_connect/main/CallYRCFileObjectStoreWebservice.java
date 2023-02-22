package org.yeastrc.file_object_storage.accept_import_web_app.webservice_connect.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.yeastrc.file_object_storage.web_app.shared_server_client.constants_enums.WebserviceFileObjectStorage_QueryParamsConstants;
import org.yeastrc.file_object_storage.web_app.shared_server_client.constants_enums.WebserviceFileObjectStoragePathConstants;
import org.yeastrc.file_object_storage.web_app.shared_server_client.constants_enums.WebserviceFileObjectStorage_HttpHeaderParamsConstants;
import org.yeastrc.file_object_storage.web_app.shared_server_client.exceptions.YRCFileObjectStoreWebserviceCallErrorException;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.Get_StoredFileObjectContents_Request;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.Get_StoredFileObjectContents_Response_FromConnectionLibraryCall;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.Get_StoredFileObjectContents_Response_InHeader;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_AddFileFromFilenameAndPath_Request;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_AddFileFromFilenameAndPath_Response;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_AddFileInS3Bucket_Request;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_AddFileInS3Bucket_Response;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_UploadFile_Request;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_UploadFile_Response;

/**
 * 
 *
 */
public class CallYRCFileObjectStoreWebservice {

	private static final String XML_ENCODING_CHARACTER_SET = StandardCharsets.UTF_8.toString();
	private static final int SUCCESS_HTTP_RETURN_CODE = 200;
	private static final int NOT_FOUND_HTTP_RETURN_CODE = 404;
	private static final String CONTENT_TYPE_SEND_RECEIVE = "application/xml";

	private static final String CONTENT_TYPE_RECEIVE_GET_FILE = "application/octet-stream";

	private static final int CONNECTION__TIMEOUT_LIMIT__HEALTH_CHECK__MILLISECONDS = 200;
	private static final int READ_FROM_CONNECTION__TIMEOUT_LIMIT__HEALTH_CHECK___MILLISECONDS = 200;

	//////
	
	private String spectralStorageServerBaseURL;
	private JAXBContext jaxbContext;
	private boolean instanceInitialized;
	
	//  private constructor
	private CallYRCFileObjectStoreWebservice() { }
	/**
	 * @return newly created instance
	 */
	public static CallYRCFileObjectStoreWebservice getInstance() { 
		return new CallYRCFileObjectStoreWebservice(); 
	}
	
	/**
	 * Must be called before any other methods are called
	 * 
	 * @param spectralStorageServerBaseURL - excludes "/services..."
	 * @param requestingWebappIdentifier - identifier of the requesting web app
	 * @param requestingWebappKey - key for the requesting web app - null if none
	 * @throws Throwable
	 */
	public synchronized void init( CallYRCFileObjectStoreWebserviceInitParameters initParameters ) throws Exception {
		
		if ( initParameters.getFileObjectStorageServerBaseURL() == null || initParameters.getFileObjectStorageServerBaseURL().length() == 0 ) {
			throw new IllegalArgumentException( "spectralStorageServerBaseURL cannot be empty");
		}
		this.spectralStorageServerBaseURL = initParameters.getFileObjectStorageServerBaseURL();

		jaxbContext = 
				JAXBContext.newInstance( 
						UploadFile_AddFileInS3Bucket_Request.class,
						UploadFile_AddFileInS3Bucket_Response.class,
						UploadFile_AddFileFromFilenameAndPath_Request.class,
						UploadFile_AddFileFromFilenameAndPath_Response.class,
						UploadFile_UploadFile_Response.class,
						Get_StoredFileObjectContents_Request.class
						);
		instanceInitialized = true;
	}
	
	/////////////////////////////

	/**
	 * @param webserviceRequest
	 * @return
	 * @throws Exception - YRCSpectralStorageAcceptImportWebserviceCallErrorException thrown if get HTTP status code other than 200 ok
	 */
	public void call_HealthCheck_Webservice() throws Exception {
		if ( ! instanceInitialized ) {
			throw new IllegalStateException( "Not initialized" );
		}

		String webserviceURL = spectralStorageServerBaseURL
				+ WebserviceFileObjectStoragePathConstants.HEALTH_CHECK;

		getRequestFromServer_Return_ByteArray(webserviceURL, CONNECTION__TIMEOUT_LIMIT__HEALTH_CHECK__MILLISECONDS, READ_FROM_CONNECTION__TIMEOUT_LIMIT__HEALTH_CHECK___MILLISECONDS );
	}

	/**
	 * HTTP GET request - Used by Health Check ONLY
	 * 
	 * @param webserviceURL
	 * @return
	 * @throws Exception 
	 */
	private byte[] getRequestFromServer_Return_ByteArray(
			
			String webserviceURL,

			Integer httpURLConnection__ConnectTimeout__Milliseconds, // In Milliseconds
			Integer httpURLConnection__ReadTimeout__Milliseconds // In Milliseconds
			) throws Exception {
		
		byte[] serverResponseByteArray = null;
		
		//   Create object for connecting to server
		URL urlObject;
		try {
			urlObject = new URL( webserviceURL );
		} catch (MalformedURLException e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception creating URL object to connect to server.  URL: " + webserviceURL, e );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		
		//   Open connection to server
		URLConnection urlConnection;
		try {
			urlConnection = urlObject.openConnection();
		} catch (IOException e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception calling openConnection() on URL object to connect to server.  URL: " + webserviceURL, e );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		// Downcast URLConnection to HttpURLConnection to allow setting of HTTP parameters 
		if ( ! ( urlConnection instanceof HttpURLConnection ) ) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Processing Error: Cannot cast URLConnection to HttpURLConnection" );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		HttpURLConnection httpURLConnection = null;
		try {
			httpURLConnection = (HttpURLConnection) urlConnection;
		} catch (Exception e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Processing Error: Cannot cast URLConnection to HttpURLConnection" );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}

		if ( httpURLConnection__ConnectTimeout__Milliseconds != null ) {
			httpURLConnection.setConnectTimeout( httpURLConnection__ConnectTimeout__Milliseconds ); // In Milliseconds
		}
		
		if ( httpURLConnection__ReadTimeout__Milliseconds != null ) {
			httpURLConnection.setReadTimeout( httpURLConnection__ReadTimeout__Milliseconds ); // In Milliseconds
		}
		
		//  Set HttpURLConnection properties

		//   HTTP GET so skip httpURLConnection.setFixedLengthStreamingMode
		
		//   Set Number of bytes to send, can be int or long
		//     ( Calling setFixedLengthStreamingMode(...) allows > 2GB to be sent 
		//       and HttpURLConnection does NOT buffer the sent bytes using ByteArrayOutputStream )

		//		httpURLConnection.setFixedLengthStreamingMode( numberOfBytesToSend );
		
		httpURLConnection.setRequestProperty( "Accept", CONTENT_TYPE_SEND_RECEIVE );
		httpURLConnection.setRequestProperty( "Content-Type", CONTENT_TYPE_SEND_RECEIVE );

		// Send GET request to server
		
		try {  //  Overall try/catch block to put "httpURLConnection.disconnect();" in the finally block

			try {
				httpURLConnection.connect();
			} catch ( IOException e ) {
				YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception connecting to server at URL: " + webserviceURL, e );
				wcee.setServerURLError(true);
				wcee.setWebserviceURL( webserviceURL );
				throw wcee;
			}

			//   HTTP GET so skip "Send bytes to server"
			
			try {
				int httpResponseCode = httpURLConnection.getResponseCode();
				if ( httpResponseCode != SUCCESS_HTTP_RETURN_CODE ) {
					byte[] errorStreamContents = null;
					try {
						errorStreamContents= getErrorStreamContents( httpURLConnection );
					} catch ( Exception ex ) {
					}
					YRCFileObjectStoreWebserviceCallErrorException wcee = 
							new YRCFileObjectStoreWebserviceCallErrorException( "Unsuccessful HTTP response code of " + httpResponseCode
									+ " connecting to server at URL: " + webserviceURL );
					wcee.setBadHTTPStatusCode(true);
					wcee.setHttpStatusCode( httpResponseCode );
					wcee.setWebserviceURL( webserviceURL );
					wcee.setErrorStreamContents( errorStreamContents );
					throw wcee;
				}
			} catch ( IOException e ) {
				byte[] errorStreamContents = null;
				try {
					errorStreamContents= getErrorStreamContents( httpURLConnection );
				} catch ( Exception ex ) {
				}
				YRCFileObjectStoreWebserviceCallErrorException wcee = 
						new YRCFileObjectStoreWebserviceCallErrorException( "IOException getting HTTP response code from server at URL: " + webserviceURL, e );
				wcee.setServerSendReceiveDataError(true);
				wcee.setWebserviceURL( webserviceURL );
				wcee.setErrorStreamContents( errorStreamContents );
				throw wcee;
			}
			//  Get response from server
			ByteArrayOutputStream outputStreamBufferOfServerResponse = new ByteArrayOutputStream( 1000000 );
			InputStream inputStream = null;
			try {
				inputStream = httpURLConnection.getInputStream();
				int nRead;
				byte[] data = new byte[ 16384 ];
				while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
					outputStreamBufferOfServerResponse.write(data, 0, nRead);
				}
			} catch ( IOException e ) {
				byte[] errorStreamContents = null;
				try {
					errorStreamContents= getErrorStreamContents( httpURLConnection );
				} catch ( Exception ex ) {
				}
				YRCFileObjectStoreWebserviceCallErrorException wcee = 
						new YRCFileObjectStoreWebserviceCallErrorException( "IOException receiving response from server at URL: " + webserviceURL, e );
				wcee.setServerSendReceiveDataError(true);
				wcee.setWebserviceURL( webserviceURL );
				wcee.setErrorStreamContents( errorStreamContents );
				throw wcee;
			} finally {
				if ( inputStream != null ) {
					try {
						inputStream.close();
					} catch ( IOException e ) {
						byte[] errorStreamContents = null;
						try {
							errorStreamContents= getErrorStreamContents( httpURLConnection );
						} catch ( Exception ex ) {
						}
						YRCFileObjectStoreWebserviceCallErrorException wcee = 
								new YRCFileObjectStoreWebserviceCallErrorException( "IOException closing input Stream from server at URL: " + webserviceURL, e );
						wcee.setServerSendReceiveDataError(true);
						wcee.setWebserviceURL( webserviceURL );
						wcee.setErrorStreamContents( errorStreamContents );
						throw wcee;
					}
				}
			}
			
			// Response ignored
			
			serverResponseByteArray = outputStreamBufferOfServerResponse.toByteArray();

			
		} finally {
//			httpURLConnection.disconnect();
		}
		
		return serverResponseByteArray;
	}
	
	////////////////////////////////
	
	///////////////
	///////////////
	
	//  Main Call for Get  File

	/**
	 * @param webserviceRequest
	 * @return
	 * @throws Exception 
	 */
	public Get_StoredFileObjectContents_Response_FromConnectionLibraryCall call_GetFile_Webservice( Get_StoredFileObjectContents_Request webserviceRequest ) throws Exception {
		if ( ! instanceInitialized ) {
			throw new IllegalStateException( "Not initialized" );
		}
		if ( webserviceRequest == null ) {
			throw new IllegalArgumentException( "webserviceRequest param must not be null in call to call_UploadFile_AddFileFromFilenameAndPath_Webservice(...)" );
		}

		String webserviceURL = spectralStorageServerBaseURL
				+ WebserviceFileObjectStoragePathConstants.GET_FILE_SERVLET_XML;
		
		return call_GetFile_Webservice__callActualWebserviceOnServerSendObject(webserviceRequest, webserviceURL);
	}
	
	/**
	 * @param webserviceRequest
	 * @param webserviceURL
	 * @return
	 * @throws YRCFileObjectStoreWebserviceCallErrorException 
	 */
	private Get_StoredFileObjectContents_Response_FromConnectionLibraryCall call_GetFile_Webservice__callActualWebserviceOnServerSendObject(
			Get_StoredFileObjectContents_Request webserviceRequest,
			String webserviceURL
			) throws YRCFileObjectStoreWebserviceCallErrorException {

		Get_StoredFileObjectContents_Response_FromConnectionLibraryCall method_Response = new Get_StoredFileObjectContents_Response_FromConnectionLibraryCall();


		ByteArrayOutputStream byteArrayOutputStream_ToSend = new ByteArrayOutputStream(100000);
		try {
			//  Jackson JSON code for JSON testing
			//  JSON using Jackson
//			ObjectMapper mapper = new ObjectMapper();  //  Jackson JSON library object
//			requestXMLToSend = mapper.writeValueAsBytes( webserviceRequest );
			
			//  Marshal (write) the object to the byte array as XML
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			marshaller.setProperty( Marshaller.JAXB_ENCODING, XML_ENCODING_CHARACTER_SET );
			try {
				marshaller.marshal( webserviceRequest, byteArrayOutputStream_ToSend );
			} catch ( Exception e ) {
				throw e;
			} finally {
				if ( byteArrayOutputStream_ToSend != null ) {
					byteArrayOutputStream_ToSend.close();
				}
			}
			//  Confirm that the generated XML can be parsed.
//			ByteArrayInputStream bais = new ByteArrayInputStream( byteArrayOutputStream_ToSend.toByteArray() );
//			XMLInputFactory xmlInputFactory = create_XMLInputFactory_XXE_Safe();
//			XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader( new StreamSource( bais ) );
//			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//			@SuppressWarnings("unused")
//			Object unmarshalledObject = unmarshaller.unmarshal( xmlStreamReader );

		} catch ( Exception e ) {
			String msg = "Error. Fail to encode request to send to server: "
					+ e.toString();
			YRCFileObjectStoreWebserviceCallErrorException exception = new YRCFileObjectStoreWebserviceCallErrorException( msg, e );
			exception.setFailToEncodeDataToSendToServer(true);
			throw exception;
		}
		

		//   Create object for connecting to server
		URL urlObject;
		try {
			urlObject = new URL( webserviceURL );
		} catch (MalformedURLException e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception creating URL object to connect to server.  URL: " + webserviceURL, e );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		//   Open connection to server
		URLConnection urlConnection;
		try {
			urlConnection = urlObject.openConnection();
		} catch (IOException e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception calling openConnection() on URL object to connect to server.  URL: " + webserviceURL, e );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		// Downcast URLConnection to HttpURLConnection to allow setting of HTTP parameters 
		if ( ! ( urlConnection instanceof HttpURLConnection ) ) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Processing Error: Cannot cast URLConnection to HttpURLConnection" );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		HttpURLConnection httpURLConnection = null;
		try {
			httpURLConnection = (HttpURLConnection) urlConnection;
		} catch (Exception e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Processing Error: Cannot cast URLConnection to HttpURLConnection" );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		//  Set HttpURLConnection properties

		//   Set Number of bytes to send, can be int or long
		//     ( Calling setFixedLengthStreamingMode(...) allows > 2GB to be sent 
		//       and HttpURLConnection does NOT buffer the sent bytes using ByteArrayOutputStream )
		httpURLConnection.setFixedLengthStreamingMode( byteArrayOutputStream_ToSend.size() );
		
		httpURLConnection.setRequestProperty( "Accept", CONTENT_TYPE_RECEIVE_GET_FILE );
		httpURLConnection.setRequestProperty( "Content-Type", CONTENT_TYPE_SEND_RECEIVE );
		httpURLConnection.setDoOutput(true);

		// Send post request to server, get response
		
		try {  //  Overall try/catch block to put "httpURLConnection.disconnect();" in the finally block

			try {
				httpURLConnection.connect();
			} catch ( IOException e ) {
				YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception connecting to server at URL: " + webserviceURL, e );
				wcee.setServerURLError(true);
				wcee.setWebserviceURL( webserviceURL );
				throw wcee;
			}
			
			//  Send bytes to server
			OutputStream outputStream = null;
			try {
				outputStream = httpURLConnection.getOutputStream();
				//  Send bytes to server
				byteArrayOutputStream_ToSend.writeTo( outputStream );

			} catch ( IOException e ) {
				byte[] errorStreamContents = null;
				try {
					errorStreamContents= getErrorStreamContents( httpURLConnection );
				} catch ( Exception ex ) {
				}
				YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "IOException sending XML to server at URL: " + webserviceURL, e );
				wcee.setServerURLError(true);
				wcee.setWebserviceURL( webserviceURL );
				wcee.setErrorStreamContents( errorStreamContents );
				throw wcee;
			} finally {
				if ( outputStream != null ) {
					boolean closeOutputStreamFail = false;
					try {
						outputStream.close();
					} catch ( IOException e ) {
						closeOutputStreamFail = true;
						byte[] errorStreamContents = null;
						try {
							errorStreamContents= getErrorStreamContents( httpURLConnection );
						} catch ( Exception ex ) {
						}
						YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "IOException closing output Stream to server at URL: " + webserviceURL, e );
						wcee.setServerURLError(true);
						wcee.setWebserviceURL( webserviceURL );
						wcee.setErrorStreamContents( errorStreamContents );
						throw wcee;
					}
				}
			}
			//  Get Response
			
			//  Get HTTP Status Code
			try {
				int httpResponseCode = httpURLConnection.getResponseCode();
				if ( httpResponseCode != SUCCESS_HTTP_RETURN_CODE ) {
					byte[] errorStreamContents = null;
					try {
						errorStreamContents= getErrorStreamContents( httpURLConnection );
					} catch ( Exception ex ) {
					}
					
					if ( httpResponseCode == NOT_FOUND_HTTP_RETURN_CODE ) {
						
						String responseHeader_ForGetFileObject =
								httpURLConnection.getHeaderField( WebserviceFileObjectStorage_HttpHeaderParamsConstants.GET_FILE_OBJECT_HTTP_RESPONSE_HEADER_PARAM );
						
						if ( responseHeader_ForGetFileObject != null && responseHeader_ForGetFileObject.length() > 0 ) {
							
							//  Have response header so deserialize it and return it
							
							Object responseHeader_Deserialized_Object = null;
							
							try {
								JAXBContext jaxbContext = JAXBContext.newInstance( Get_StoredFileObjectContents_Response_InHeader.class );
								
								responseHeader_Deserialized_Object = jaxbContext.createUnmarshaller().unmarshal( new StringReader(responseHeader_ForGetFileObject) );
							} catch ( Throwable t ) {

								YRCFileObjectStoreWebserviceCallErrorException wcee = 
										new YRCFileObjectStoreWebserviceCallErrorException( 
												"Fail Deserialize Response Header '" 
												+ WebserviceFileObjectStorage_HttpHeaderParamsConstants.GET_FILE_OBJECT_HTTP_RESPONSE_HEADER_PARAM
												+ "'.  contents: " + responseHeader_ForGetFileObject, t
												);
								wcee.setWebserviceURL( webserviceURL );
								wcee.setErrorStreamContents( errorStreamContents );
								throw wcee;
							}
							
							if ( ! ( responseHeader_Deserialized_Object instanceof Get_StoredFileObjectContents_Response_InHeader ) ) {

								YRCFileObjectStoreWebserviceCallErrorException wcee = 
										new YRCFileObjectStoreWebserviceCallErrorException( 
												"Fail Deserialize Response Header '"  
														+ WebserviceFileObjectStorage_HttpHeaderParamsConstants.GET_FILE_OBJECT_HTTP_RESPONSE_HEADER_PARAM
														+ "'.  Deserialized Object of wrong class.  contents: " + responseHeader_ForGetFileObject
														);
								wcee.setWebserviceURL( webserviceURL );
								wcee.setErrorStreamContents( errorStreamContents );
								throw wcee;
							}
							
							Get_StoredFileObjectContents_Response_InHeader get_StoredFileObjectContents_Response_InHeader =
									(Get_StoredFileObjectContents_Response_InHeader) responseHeader_Deserialized_Object;

							Get_StoredFileObjectContents_Response_FromConnectionLibraryCall response = new Get_StoredFileObjectContents_Response_FromConnectionLibraryCall();

							response.setInputStream_FileObjectContents( null );

							response.setResponseFromWebserviceInHeader( get_StoredFileObjectContents_Response_InHeader );

							return response;  //  EARLY RETURN
						}
					}
					
					YRCFileObjectStoreWebserviceCallErrorException wcee = 
							new YRCFileObjectStoreWebserviceCallErrorException( "Unsuccessful HTTP response code of " + httpResponseCode
									+ " connecting to server at URL: " + webserviceURL );
					wcee.setBadHTTPStatusCode(true);
					wcee.setHttpStatusCode( httpResponseCode );
					wcee.setWebserviceURL( webserviceURL );
					wcee.setErrorStreamContents( errorStreamContents );
					throw wcee;
				}
			} catch ( IOException e ) {
				byte[] errorStreamContents = null;
				try {
					errorStreamContents= getErrorStreamContents( httpURLConnection );
				} catch ( Exception ex ) {
				}
				YRCFileObjectStoreWebserviceCallErrorException wcee = 
						new YRCFileObjectStoreWebserviceCallErrorException( "IOException getting HTTP response code from server at URL: " + webserviceURL, e );
				wcee.setServerSendReceiveDataError(true);
				wcee.setWebserviceURL( webserviceURL );
				wcee.setErrorStreamContents( errorStreamContents );
				throw wcee;
			}
			
			//  Get response from server

			//  Get Header with data if present

			String responseHeader_ForGetFileObject =
					httpURLConnection.getHeaderField( WebserviceFileObjectStorage_HttpHeaderParamsConstants.GET_FILE_OBJECT_HTTP_RESPONSE_HEADER_PARAM );
			
			if ( responseHeader_ForGetFileObject != null && responseHeader_ForGetFileObject.length() > 0 ) {
				
				//  Have response header so deserialize it and return it
				
				Object responseHeader_Deserialized_Object = null;
				
				try {
					JAXBContext jaxbContext = JAXBContext.newInstance( Get_StoredFileObjectContents_Response_InHeader.class );
					
					responseHeader_Deserialized_Object = jaxbContext.createUnmarshaller().unmarshal( new StringReader(responseHeader_ForGetFileObject) );
				} catch ( Throwable t ) {

					YRCFileObjectStoreWebserviceCallErrorException wcee = 
							new YRCFileObjectStoreWebserviceCallErrorException( 
									"Fail Deserialize Response Header '" 
									+ WebserviceFileObjectStorage_HttpHeaderParamsConstants.GET_FILE_OBJECT_HTTP_RESPONSE_HEADER_PARAM
									+ "'.  contents: " + responseHeader_ForGetFileObject, t
									);
					wcee.setWebserviceURL( webserviceURL );
					throw wcee;
				}
				
				if ( ! ( responseHeader_Deserialized_Object instanceof Get_StoredFileObjectContents_Response_InHeader ) ) {

					YRCFileObjectStoreWebserviceCallErrorException wcee = 
							new YRCFileObjectStoreWebserviceCallErrorException( 
									"Fail Deserialize Response Header '"  
											+ WebserviceFileObjectStorage_HttpHeaderParamsConstants.GET_FILE_OBJECT_HTTP_RESPONSE_HEADER_PARAM
											+ "'.  Deserialized Object of wrong class.  contents: " + responseHeader_ForGetFileObject
											);
					wcee.setWebserviceURL( webserviceURL );
					throw wcee;
				}
				
				Get_StoredFileObjectContents_Response_InHeader get_StoredFileObjectContents_Response_InHeader =
						(Get_StoredFileObjectContents_Response_InHeader) responseHeader_Deserialized_Object;

				method_Response.setResponseFromWebserviceInHeader( get_StoredFileObjectContents_Response_InHeader );
			}
			
			method_Response.setReturnedContentsLength( httpURLConnection.getContentLengthLong() );
		
			InputStream inputStream_FileObjectContents = null;
			
			try {
				inputStream_FileObjectContents = httpURLConnection.getInputStream();
			} catch ( Throwable t ) {

				YRCFileObjectStoreWebserviceCallErrorException wcee = 
						new YRCFileObjectStoreWebserviceCallErrorException( 
								"Fail get Input Stream", t
								);
				wcee.setWebserviceURL( webserviceURL );
				throw wcee;
			}
			
			method_Response.setInputStream_FileObjectContents(inputStream_FileObjectContents);
			
			
		} finally {
//			httpURLConnection.disconnect();
		}
		return method_Response;
	}
	
	
	////////////////////////////////
	
	///////////////
	///////////////
	
	//  Main Calls for Send  File (or  filename with path) to Add

	/////////////
	
	/**
	 * @param webserviceRequest
	 * @return
	 * @throws Exception 
	 */
//	public UploadFile_AddFileInS3Bucket_Response call_UploadFile_AddFileInS3Bucket_Webservice( UploadFile_AddFileInS3Bucket_Request webserviceRequest ) throws Exception {
//		if ( ! instanceInitialized ) {
//			throw new IllegalStateException( "Not initialized" );
//		}
//		if ( webserviceRequest == null ) {
//			throw new IllegalArgumentException( "webserviceRequest param must not be null in call to call_UploadFile_AddFileInS3Bucket_Webservice(...)" );
//		}
//
//		String webserviceURL = spectralStorageServerBaseURL
//				+ WebserviceSpectralStorageAcceptImportPathConstants.UPLOAD_SCAN_FILE_ADD_SCAN_FILE_IN_S3_BUCKET_SERVLET_XML;
//		Object webserviceResponseAsObject = callActualWebserviceOnServerSendObject( webserviceRequest, webserviceURL );
//		if ( ! ( webserviceResponseAsObject instanceof UploadFile_AddFileInS3Bucket_Response ) ) {
//			String msg = "Response unmarshaled to class other than UploadFile_AddFileInS3Bucket_Response.  "
//					+ " Unmarshaled Class: " + webserviceResponseAsObject.getClass();
//			YRCSpectralStorageAcceptImportWebserviceCallErrorException exception = new YRCSpectralStorageAcceptImportWebserviceCallErrorException( msg );
//			exception.setFailToDecodeDataReceivedFromServer(true);
//			throw exception;
//		}
//		UploadFile_AddFileInS3Bucket_Response webserviceResponse = null;
//		try {
//			webserviceResponse = (UploadFile_AddFileInS3Bucket_Response) webserviceResponseAsObject;
//		} catch ( Exception e ) {
//			String msg = "Error. Fail to cast response as UploadFile_AddFileInS3Bucket_Response: "
//					+ e.toString();
//			YRCSpectralStorageAcceptImportWebserviceCallErrorException exception = new YRCSpectralStorageAcceptImportWebserviceCallErrorException( msg );
//			exception.setFailToDecodeDataReceivedFromServer(true);
//			throw exception;
//		}
//		return webserviceResponse;
//	}

	/**
	 * @param webserviceRequest
	 * @return
	 * @throws Exception 
	 */
	public UploadFile_AddFileFromFilenameAndPath_Response call_UploadFile_AddFileFromFilenameAndPath_Webservice( UploadFile_AddFileFromFilenameAndPath_Request webserviceRequest ) throws Exception {
		if ( ! instanceInitialized ) {
			throw new IllegalStateException( "Not initialized" );
		}
		if ( webserviceRequest == null ) {
			throw new IllegalArgumentException( "webserviceRequest param must not be null in call to call_UploadFile_AddFileInS3Bucket_Webservice(...)" );
		}

		String webserviceURL = spectralStorageServerBaseURL
				+ WebserviceFileObjectStoragePathConstants.UPLOAD_FILE_ADD_FILENAME_WITH_PATH_SERVLET_XML;
		Object webserviceResponseAsObject = callActualWebserviceOnServerSendObject( webserviceRequest, webserviceURL );
		if ( ! ( webserviceResponseAsObject instanceof UploadFile_AddFileFromFilenameAndPath_Response ) ) {
			String msg = "Response unmarshaled to class other than UploadFile_AddFileFromFilenameAndPath_Response.  "
					+ " Unmarshaled Class: " + webserviceResponseAsObject.getClass();
			YRCFileObjectStoreWebserviceCallErrorException exception = new YRCFileObjectStoreWebserviceCallErrorException( msg );
			exception.setFailToDecodeDataReceivedFromServer(true);
			throw exception;
		}
		UploadFile_AddFileFromFilenameAndPath_Response webserviceResponse = null;
		try {
			webserviceResponse = (UploadFile_AddFileFromFilenameAndPath_Response) webserviceResponseAsObject;
		} catch ( Exception e ) {
			String msg = "Error. Fail to cast response as UploadFile_AddFileFromFilenameAndPath_Response: "
					+ e.toString();
			YRCFileObjectStoreWebserviceCallErrorException exception = new YRCFileObjectStoreWebserviceCallErrorException( msg );
			exception.setFailToDecodeDataReceivedFromServer(true);
			throw exception;
		}
		return webserviceResponse;
	}

	/**
	 * @param scanFile
	 * @return
	 * @throws Exception 
	 */
	public UploadFile_UploadFile_Response call_UploadFile_UploadFile_Service( UploadFile_UploadFile_Request webserviceRequest ) throws Exception {
		
		
		if ( ! instanceInitialized ) {
			throw new IllegalStateException( "Not initialized" );
		}
		
		if ( webserviceRequest == null ) {
			throw new IllegalArgumentException( "webserviceRequest param must not be null in call to call_UploadFile_UploadFile_Service(...)" );
		}
		
		/////
		
		File uploadFile = webserviceRequest.getFile();
		if ( uploadFile == null ) {
			throw new IllegalArgumentException( "file property in webserviceRequest param must not be null in call to call_UploadFile_UploadFile_Service(...)" );
		}
		if ( ! uploadFile.exists() ) {
			throw new IllegalArgumentException( "File in file property in webserviceRequest param must exist in call to call_UploadFile_UploadFile_Service(...)" );
		}
		
		String webserviceURL_Additions = "";
		
		if ( webserviceRequest.isGzipCompressContents() ) {
			
			webserviceURL_Additions =
					"?"
					+ WebserviceFileObjectStorage_QueryParamsConstants.UPLOAD_FILE_GZIP_COMPRESS_CONTENTS_QUERY_PARAM
					+ "="
					+ WebserviceFileObjectStorage_QueryParamsConstants.UPLOAD_FILE_GZIP_COMPRESS_CONTENTS_QUERY_PARAM__VALUE_TRUE;
		}
		
		String webserviceURL = spectralStorageServerBaseURL
				+ WebserviceFileObjectStoragePathConstants.UPLOAD_FILE_UPLOAD_FILE_SERVLET_XML
				+ webserviceURL_Additions;
		
		Object webserviceResponseAsObject = 
				callActualWebserviceOnServerSendByteArrayOrFileAsStreamReturnObject(
						null /* bytesToSend */, uploadFile, webserviceURL );
		if ( ! ( webserviceResponseAsObject instanceof UploadFile_UploadFile_Response ) ) {
			String msg = "Response unmarshaled to class other than UploadFile_UploadFile_Response.  "
					+ " Unmarshaled Class: " + webserviceResponseAsObject.getClass();
			YRCFileObjectStoreWebserviceCallErrorException exception = new YRCFileObjectStoreWebserviceCallErrorException( msg );
			exception.setFailToDecodeDataReceivedFromServer(true);
			throw exception;
		}
		UploadFile_UploadFile_Response webserviceResponse = null;
		try {
			webserviceResponse = (UploadFile_UploadFile_Response) webserviceResponseAsObject;
		} catch ( Exception e ) {
			String msg = "Error. Fail to cast response as UploadFile_UploadFile_Response: "
					+ e.toString();
			YRCFileObjectStoreWebserviceCallErrorException exception = new YRCFileObjectStoreWebserviceCallErrorException( msg );
			exception.setFailToDecodeDataReceivedFromServer(true);
			throw exception;
		}
		return webserviceResponse;
	}
	
	//////////////////////////////////////////////////////////////////
	//    Internal Methods
	
	/**
	 * @param webserviceRequest
	 * @param webserviceURL
	 * @return
	 * @throws Exception
	 */
	private Object callActualWebserviceOnServerSendObject( 
			Object webserviceRequest,
			String webserviceURL ) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream_ToSend = new ByteArrayOutputStream(100000);
		try {
			//  Jackson JSON code for JSON testing
			//  JSON using Jackson
//			ObjectMapper mapper = new ObjectMapper();  //  Jackson JSON library object
//			requestXMLToSend = mapper.writeValueAsBytes( webserviceRequest );
			
			//  Marshal (write) the object to the byte array as XML
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			marshaller.setProperty( Marshaller.JAXB_ENCODING, XML_ENCODING_CHARACTER_SET );
			try {
				marshaller.marshal( webserviceRequest, byteArrayOutputStream_ToSend );
			} catch ( Exception e ) {
				throw e;
			} finally {
				if ( byteArrayOutputStream_ToSend != null ) {
					byteArrayOutputStream_ToSend.close();
				}
			}
			//  Confirm that the generated XML can be parsed.
//			ByteArrayInputStream bais = new ByteArrayInputStream( byteArrayOutputStream_ToSend.toByteArray() );
//			XMLInputFactory xmlInputFactory = create_XMLInputFactory_XXE_Safe();
//			XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader( new StreamSource( bais ) );
//			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//			@SuppressWarnings("unused")
//			Object unmarshalledObject = unmarshaller.unmarshal( xmlStreamReader );

		} catch ( Exception e ) {
			String msg = "Error. Fail to encode request to send to server: "
					+ e.toString();
			YRCFileObjectStoreWebserviceCallErrorException exception = new YRCFileObjectStoreWebserviceCallErrorException( msg, e );
			exception.setFailToEncodeDataToSendToServer(true);
			throw exception;
		}
		
		return callActualWebserviceOnServerSendByteArrayOrFileAsStreamReturnObject( 
				byteArrayOutputStream_ToSend, null /* fileToSendAsStream */, webserviceURL );
	}
	
	/**
	 * Send byte array or File to server as stream
	 * 
	 * bytesToSend or fileToSendAsStream must not be null and both cannot be not null
	 * 
	 * @param bytesToSend
	 * @param fileToSendAsStream
	 * @param webserviceURL
	 * @return
	 * @throws Exception
	 */
	private Object callActualWebserviceOnServerSendByteArrayOrFileAsStreamReturnObject( 
			ByteArrayOutputStream byteArrayOutputStream_ToSend,
			File fileToSendAsStream,
			String webserviceURL ) throws Exception {

		byte[] serverResponseByteArray = 
				sendToServerSendByteArrayOrFileAsStream_GetByteArrayResponseFromServer(
						byteArrayOutputStream_ToSend,
						fileToSendAsStream, 
						webserviceURL );
		
		return parse_ServerResponseBytesArray_Return_Object(serverResponseByteArray, webserviceURL);
	}
	
	/**
	 * @param byteArrayOutputStream_ToSend
	 * @param fileToSendAsStream
	 * @param webserviceURL
	 * @return
	 * @throws YRCFileObjectStoreWebserviceCallErrorException
	 */
	private byte[] sendToServerSendByteArrayOrFileAsStream_GetByteArrayResponseFromServer(
			ByteArrayOutputStream byteArrayOutputStream_ToSend,
			File fileToSendAsStream, 
			String webserviceURL) throws YRCFileObjectStoreWebserviceCallErrorException {
		
		byte[] serverResponseByteArray = null;
		
		if ( ( ! ( byteArrayOutputStream_ToSend != null || fileToSendAsStream != null ) )
				|| (  byteArrayOutputStream_ToSend != null && fileToSendAsStream != null)) {
			String msg = "Exactly one of either byteArrayOutputStream_ToSend or fileToSendAsStream must be not null";
			YRCFileObjectStoreWebserviceCallErrorException exception = new YRCFileObjectStoreWebserviceCallErrorException( msg );
			exception.setCallInterfaceInternalError(true);
			exception.setCallInterfaceInternalErrorMessage(msg);
			throw exception;
		}
		
		//  Get number of bytes to send to specify in httpURLConnection.setFixedLengthStreamingMode(...)
		//  (This causes httpURLConnection to not buffer the sent data to get the length,
		//   allowing > 2GB to be sent and also no memory is needed for the buffering)
		long numberOfBytesToSend = -1;
		
		if ( byteArrayOutputStream_ToSend != null ) {
			numberOfBytesToSend = byteArrayOutputStream_ToSend.size();
		} else {
			numberOfBytesToSend = fileToSendAsStream.length();
		}
		
		//   Create object for connecting to server
		URL urlObject;
		try {
			urlObject = new URL( webserviceURL );
		} catch (MalformedURLException e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception creating URL object to connect to server.  URL: " + webserviceURL, e );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		//   Open connection to server
		URLConnection urlConnection;
		try {
			urlConnection = urlObject.openConnection();
		} catch (IOException e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception calling openConnection() on URL object to connect to server.  URL: " + webserviceURL, e );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		// Downcast URLConnection to HttpURLConnection to allow setting of HTTP parameters 
		if ( ! ( urlConnection instanceof HttpURLConnection ) ) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Processing Error: Cannot cast URLConnection to HttpURLConnection" );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		HttpURLConnection httpURLConnection = null;
		try {
			httpURLConnection = (HttpURLConnection) urlConnection;
		} catch (Exception e) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Processing Error: Cannot cast URLConnection to HttpURLConnection" );
			wcee.setServerURLError(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		//  Set HttpURLConnection properties

		//   Set Number of bytes to send, can be int or long
		//     ( Calling setFixedLengthStreamingMode(...) allows > 2GB to be sent 
		//       and HttpURLConnection does NOT buffer the sent bytes using ByteArrayOutputStream )
		httpURLConnection.setFixedLengthStreamingMode( numberOfBytesToSend );
		
		httpURLConnection.setRequestProperty( "Accept", CONTENT_TYPE_SEND_RECEIVE );
		httpURLConnection.setRequestProperty( "Content-Type", CONTENT_TYPE_SEND_RECEIVE );
		httpURLConnection.setDoOutput(true);
		// Send post request to server
		try {  //  Overall try/catch block to put "httpURLConnection.disconnect();" in the finally block

			try {
				httpURLConnection.connect();
			} catch ( IOException e ) {
				YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception connecting to server at URL: " + webserviceURL, e );
				wcee.setServerURLError(true);
				wcee.setWebserviceURL( webserviceURL );
				throw wcee;
			}
			//  Send bytes to server
			OutputStream outputStream = null;
			FileInputStream fileInputStream = null; // for when send file
			try {
				outputStream = httpURLConnection.getOutputStream();
				if ( byteArrayOutputStream_ToSend != null ) {
					//  Send bytes to server
					byteArrayOutputStream_ToSend.writeTo( outputStream );
				} else {
					//  Send file contents to server
					fileInputStream = new FileInputStream( fileToSendAsStream );
					int byteArraySize = 5000;
					byte[] data = new byte[ byteArraySize ];
					while (true) {
						int bytesRead = fileInputStream.read( data );
						if ( bytesRead == -1 ) {  // end of input
							break;
						}
						if ( bytesRead > 0 ) {
							outputStream.write( data, 0, bytesRead );
						}
					}
				}
			} catch ( IOException e ) {
				byte[] errorStreamContents = null;
				try {
					errorStreamContents= getErrorStreamContents( httpURLConnection );
				} catch ( Exception ex ) {
				}
				YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "IOException sending XML to server at URL: " + webserviceURL, e );
				wcee.setServerURLError(true);
				wcee.setWebserviceURL( webserviceURL );
				wcee.setErrorStreamContents( errorStreamContents );
				throw wcee;
			} finally {
				if ( outputStream != null ) {
					boolean closeOutputStreamFail = false;
					try {
						outputStream.close();
					} catch ( IOException e ) {
						closeOutputStreamFail = true;
						byte[] errorStreamContents = null;
						try {
							errorStreamContents= getErrorStreamContents( httpURLConnection );
						} catch ( Exception ex ) {
						}
						YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "IOException closing output Stream to server at URL: " + webserviceURL, e );
						wcee.setServerURLError(true);
						wcee.setWebserviceURL( webserviceURL );
						wcee.setErrorStreamContents( errorStreamContents );
						throw wcee;
					} finally {
						if ( fileInputStream != null ) {
							try {
								fileInputStream.close();
							} catch ( Exception e ) {
								if ( ! closeOutputStreamFail ) {
									// Only throw exception if close of output stream successful
									byte[] errorStreamContents = null;
									try {
										errorStreamContents= getErrorStreamContents( httpURLConnection );
									} catch ( Exception ex ) {
									}
									YRCFileObjectStoreWebserviceCallErrorException wcee = new YRCFileObjectStoreWebserviceCallErrorException( "Exception closing output Stream to server at URL: " + webserviceURL, e );
									wcee.setServerURLError(true);
									wcee.setWebserviceURL( webserviceURL );
									wcee.setErrorStreamContents( errorStreamContents );
									throw wcee;
								}
							}
						}
					}
				}
			}
			try {
				int httpResponseCode = httpURLConnection.getResponseCode();
				if ( httpResponseCode != SUCCESS_HTTP_RETURN_CODE ) {
					byte[] errorStreamContents = null;
					try {
						errorStreamContents= getErrorStreamContents( httpURLConnection );
					} catch ( Exception ex ) {
					}
					YRCFileObjectStoreWebserviceCallErrorException wcee = 
							new YRCFileObjectStoreWebserviceCallErrorException( "Unsuccessful HTTP response code of " + httpResponseCode
									+ " connecting to server at URL: " + webserviceURL );
					wcee.setBadHTTPStatusCode(true);
					wcee.setHttpStatusCode( httpResponseCode );
					wcee.setWebserviceURL( webserviceURL );
					wcee.setErrorStreamContents( errorStreamContents );
					throw wcee;
				}
			} catch ( IOException e ) {
				byte[] errorStreamContents = null;
				try {
					errorStreamContents= getErrorStreamContents( httpURLConnection );
				} catch ( Exception ex ) {
				}
				YRCFileObjectStoreWebserviceCallErrorException wcee = 
						new YRCFileObjectStoreWebserviceCallErrorException( "IOException getting HTTP response code from server at URL: " + webserviceURL, e );
				wcee.setServerSendReceiveDataError(true);
				wcee.setWebserviceURL( webserviceURL );
				wcee.setErrorStreamContents( errorStreamContents );
				throw wcee;
			}
			//  Get response XML from server
			ByteArrayOutputStream outputStreamBufferOfServerResponse = new ByteArrayOutputStream( 1000000 );
			InputStream inputStream = null;
			try {
				inputStream = httpURLConnection.getInputStream();
				int nRead;
				byte[] data = new byte[ 16384 ];
				while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
					outputStreamBufferOfServerResponse.write(data, 0, nRead);
				}
			} catch ( IOException e ) {
				byte[] errorStreamContents = null;
				try {
					errorStreamContents= getErrorStreamContents( httpURLConnection );
				} catch ( Exception ex ) {
				}
				YRCFileObjectStoreWebserviceCallErrorException wcee = 
						new YRCFileObjectStoreWebserviceCallErrorException( "IOException receiving XML from server at URL: " + webserviceURL, e );
				wcee.setServerSendReceiveDataError(true);
				wcee.setWebserviceURL( webserviceURL );
				wcee.setErrorStreamContents( errorStreamContents );
				throw wcee;
			} finally {
				if ( inputStream != null ) {
					try {
						inputStream.close();
					} catch ( IOException e ) {
						byte[] errorStreamContents = null;
						try {
							errorStreamContents= getErrorStreamContents( httpURLConnection );
						} catch ( Exception ex ) {
						}
						YRCFileObjectStoreWebserviceCallErrorException wcee = 
								new YRCFileObjectStoreWebserviceCallErrorException( "IOException closing input Stream from server at URL: " + webserviceURL, e );
						wcee.setServerSendReceiveDataError(true);
						wcee.setWebserviceURL( webserviceURL );
						wcee.setErrorStreamContents( errorStreamContents );
						throw wcee;
					}
				}
			}
			serverResponseByteArray = outputStreamBufferOfServerResponse.toByteArray();

			
		} finally {
//			httpURLConnection.disconnect();
		}
		return serverResponseByteArray;
	}
	
	/**
	 * @param httpURLConnection
	 * @return
	 * @throws IOException
	 */
	private byte[] getErrorStreamContents(HttpURLConnection httpURLConnection) throws IOException {
		
		InputStream inputStream = httpURLConnection.getErrorStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int byteArraySize = 5000;
		byte[] data = new byte[ byteArraySize ];
		while (true) {
			int bytesRead = inputStream.read( data );
			if ( bytesRead == -1 ) {  // end of input
				break;
			}
			if ( bytesRead > 0 ) {
				baos.write( data, 0, bytesRead );
			}
		}
		return baos.toByteArray();
	}
	

	/**
	 * @param serverResponseByteArray
	 * @return
	 * @throws YRCFileObjectStoreWebserviceCallErrorException 
	 */
	private Object parse_ServerResponseBytesArray_Return_Object( byte[] serverResponseByteArray, String webserviceURL ) throws YRCFileObjectStoreWebserviceCallErrorException {

		Object webserviceResponseAsObject = null;
		
		ByteArrayInputStream inputStreamBufferOfServerResponse = 
				new ByteArrayInputStream( serverResponseByteArray );
		// Unmarshal received XML into Java objects
		try {
			XMLInputFactory xmlInputFactory = create_XMLInputFactory_XXE_Safe();
			XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader( new StreamSource( inputStreamBufferOfServerResponse ) );
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			webserviceResponseAsObject = unmarshaller.unmarshal( xmlStreamReader );
		} catch ( Exception e ) {
			YRCFileObjectStoreWebserviceCallErrorException wcee = 
					new YRCFileObjectStoreWebserviceCallErrorException( "JAXBException unmarshalling XML received from server at URL: " + webserviceURL, e );
			wcee.setFailToDecodeDataReceivedFromServer(true);
			wcee.setWebserviceURL( webserviceURL );
			throw wcee;
		}
		return webserviceResponseAsObject; 
	}
	
	////////////////////////////////

	/**
	 * Create XMLInputFactory that has the settings that make it safe from XXE
	 * 
	 * @return
	 */
	private XMLInputFactory create_XMLInputFactory_XXE_Safe() {

	    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	    //  XXE  Mitigation
	    //  prevents using external resources when parsing xml
	    xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

	    //  prevents using external document type definition when parsing xml
	    xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
	  
		return xmlInputFactory;
	}
	
	
}
