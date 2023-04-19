package org.yeastrc.file_object_storage.web_app.servlets_get_file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.config.ConfigData_Directories_ProcessUploadInfo_InWorkDirectory;
import org.yeastrc.file_object_storage.web_app.constants_enums.FileUploadConstants;
import org.yeastrc.file_object_storage.web_app.constants_enums.ServetResponseFormatEnum;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageBadRequestToServletException;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageDeserializeRequestException;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageSerializeRequestException;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageWebappConfigException;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageWebappInternalException;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn.StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents_ReadFromInputStream;
import org.yeastrc.file_object_storage.web_app.servlets_common.GetRequestObjectFromInputStream;
import org.yeastrc.file_object_storage.web_app.servlets_common.Get_ServletResultDataFormat_FromServletInitParam;
import org.yeastrc.file_object_storage.web_app.servlets_common.WriteResponseStringToOutputStream;
import org.yeastrc.file_object_storage.web_app.shared_server_client.constants_enums.WebserviceFileObjectStorage_HttpHeaderParamsConstants;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.Get_StoredFileObjectContents_Request;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.Get_StoredFileObjectContents_Response_InHeader;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create.Storage_Object_Name__Create__Result__MainName;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;


/**
 * Returns one of:
 *   * The file contents for the API key in the stored GZIP format.
 *   * A 404 status with data in HTTP Header ServletResponse_HeaderKeyStrings.GET_FILE_CONTENTS_RESPONSE_INFO
 *   * A 400 status with data in HTTP Header ServletResponse_HeaderKeyStrings.GET_FILE_CONTENTS_RESPONSE_INFO
 *   * A 500 status
 * 
 */
public class Get_StoredFileObjectContents_Servlet extends HttpServlet {

	private static final Logger log = LoggerFactory.getLogger( Get_StoredFileObjectContents_Servlet.class );

	private static final long serialVersionUID = 1L;

	public static final int COPY_FILE_ARRAY_SIZE = 32 * 1024; // 32 KB
	
	private ServetResponseFormatEnum servetResponseFormat;
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config)
	          throws ServletException {
		
		super.init(config); //  Must call this first

		servetResponseFormat = 
				Get_ServletResultDataFormat_FromServletInitParam.getInstance()
				.get_ServletResultDataFormat_FromServletInitParam( config );

		log.warn( "INFO: servetResponseFormat: " + servetResponseFormat );
		
	}
	
	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		log.info( "INFO:  doPost called");
		

		Get_StoredFileObjectContents_Request get_StoredFileObjectContents_Request = null;

		try {
			Object requestObj = null;

			try {
				requestObj = GetRequestObjectFromInputStream.getSingletonInstance().getRequestObjectFromStream( request );
			} catch ( FileObjectStorageDeserializeRequestException e ) {
				throw e;
			} catch (Exception e) {
				String msg = "Failed to deserialize request";
				log.error( msg, e );
				throw new FileObjectStorageBadRequestToServletException( e );
			}

			try {
				get_StoredFileObjectContents_Request = (Get_StoredFileObjectContents_Request) requestObj;
			} catch (Exception e) {
				String msg = "Incoming request not deserialize to expected Java class.  Failed to cast requestObj to Get_StoredFileObjectContents_Request";
				log.error( msg, e );
				throw new FileObjectStorageBadRequestToServletException( "Incoming request not deserialize to expected Java class" );
			}
		} catch (FileObjectStorageBadRequestToServletException e) {

			response.setStatus( HttpServletResponse.SC_BAD_REQUEST /* 400  */ );
			
//			response.setHeader( , value);

			if ( StringUtils.isNotEmpty( e.getMessage() ) ) {
				WriteResponseStringToOutputStream.getInstance()
				.writeResponseStringToOutputStream( e.getMessage(), response);
			}

			return;  // EARLY EXIT

		} catch (Throwable e) {
			String msg = "Failed to process request";
			log.error( msg, e );
			response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR /* 500  */ );
			
			return;  // EARLY EXIT
		}
		
		if ( StringUtils.isEmpty( get_StoredFileObjectContents_Request.getFileAPIKey() ) ) {
			
			String msg = "'fileAPIKey' not set in request";
			
			log.warn(msg);

			WriteResponseStringToOutputStream.getInstance()
			.writeResponseStringToOutputStream( msg, response);

			return;  // EARLY EXIT
		}
		
		try {

			if ( StringUtils.isNotEmpty( ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage() ) ) {
				
				processRequest_DataIn_AWS_S3( get_StoredFileObjectContents_Request, request, response );
				
			} else {
			
				processRequest_DataOn_LocalFileSystem( get_StoredFileObjectContents_Request, request, response );
			
			}
			
		} catch (Throwable e) {

			String msg = "Failed to process request";
			log.error( msg, e );
			response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR /* 500  */ );
			
		} finally {
			
			
		}
	}
	
	/**
	 * @param get_StoredFileObjectContents_Request
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @throws Exception 
	 */
	private void processRequest_DataOn_LocalFileSystem( 
			
			Get_StoredFileObjectContents_Request get_StoredFileObjectContents_Request,
			HttpServletRequest httpServletRequest, 
			HttpServletResponse httpServletResponse) throws Exception {
		
		try {

			//////

			Storage_Object_Name__Create__Result__MainName storage_Object_Name__Create__Result =
					Storage_Object_Name__Create.create_Main_Storage_Object_Name( get_StoredFileObjectContents_Request.getFileAPIKey() );

			String[] storage_Object_Name_AllPossible = { storage_Object_Name__Create__Result.getMainObjectname(), storage_Object_Name__Create__Result.getGzipObjectname() };
			
			File mainStorage_SubDirBasedOnName =
					StorageDir_OnLocalFileSystem_CreateToStoreFileIn.getInstance().createToStoreFileIn(
							storage_Object_Name_AllPossible, StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM.NO );
			
			if ( mainStorage_SubDirBasedOnName == null ) {

				//  Subdir path not exist so the file to return not exist


				httpServletResponse.setStatus( HttpServletResponse.SC_NOT_FOUND /* 404  */ );
				
				Get_StoredFileObjectContents_Response_InHeader responseInHeader = new Get_StoredFileObjectContents_Response_InHeader();
				
				responseInHeader.setFileAPIKey_NOT_FOUND(true);

				this.addHeader_ToResponse( responseInHeader, httpServletResponse );

				
				return;  // EARLY EXIT

			}
			
			File storage_Object_Name_File_ToUse = null;

			long responseContentLength = 0;
			
			
			boolean have_GZIP_File = false;

			Get_StoredFileObjectContents_Response_InHeader responseInHeader = new Get_StoredFileObjectContents_Response_InHeader();
			
			{

				File storage_Object_Name_File_GzipObjectname = new File( mainStorage_SubDirBasedOnName, storage_Object_Name__Create__Result.getGzipObjectname() );

				if ( storage_Object_Name_File_GzipObjectname.exists() ) {

					//  Exists

					storage_Object_Name_File_ToUse = storage_Object_Name_File_GzipObjectname;
					
					responseContentLength = storage_Object_Name_File_ToUse.length();
					
					have_GZIP_File = true;
					
					if ( get_StoredFileObjectContents_Request.getReturnAs_GZIP_IfAvailable() != null
							&& get_StoredFileObjectContents_Request.getReturnAs_GZIP_IfAvailable().booleanValue()
							) {
						
						responseInHeader.setResponse_Is_GZIP( true );
					}

					//  Get Meta Data to return original file length
					
					String metaFilename = Storage_Object_Name__Create.create_GZIP_MetaData_Storage_Object_Name( get_StoredFileObjectContents_Request.getFileAPIKey() );
					
					File metaFile = new File(mainStorage_SubDirBasedOnName, metaFilename);
					
					if ( metaFile.exists() ) {

						JAXBContext jaxbContext = JAXBContext.newInstance( MetaFileContents.class );
						
						try ( FileInputStream fis = new FileInputStream(metaFile) ) {
							
							Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
							
							Object object = unmarshaller.unmarshal(fis);
							
							if ( ! ( object instanceof MetaFileContents )  ) {
								String msg = "Unmarshal of metadata file not result in object of type MetaFileContents ";
								log.error(msg);
								throw new FileObjectStorageDeserializeRequestException(msg);
							}
							
							MetaFileContents metaFileContents = (MetaFileContents) object;
							
							responseInHeader.setFileLength_NonGZIP( metaFileContents.getOriginalFileSize() );

							if ( get_StoredFileObjectContents_Request.getReturnAs_GZIP_IfAvailable() != null
									&& get_StoredFileObjectContents_Request.getReturnAs_GZIP_IfAvailable().booleanValue()
									) {
								
							} else {
								
								//  Returning NOT GZIP
								
								responseContentLength = metaFileContents.getOriginalFileSize();
							}
						}
					}
					
				} else {
					
					File storage_Object_Name_File_MainObjectname = new File( mainStorage_SubDirBasedOnName, storage_Object_Name__Create__Result.getMainObjectname() );

					if ( storage_Object_Name_File_MainObjectname.exists() ) {

						//  Exists

						storage_Object_Name_File_ToUse = storage_Object_Name_File_MainObjectname;

						responseContentLength = storage_Object_Name_File_ToUse.length();
						
					} else {

						//  NOT exists

						httpServletResponse.setStatus( HttpServletResponse.SC_NOT_FOUND /* 404  */ );
						
						Get_StoredFileObjectContents_Response_InHeader responseInHeader_NotFound = new Get_StoredFileObjectContents_Response_InHeader();
						
						responseInHeader_NotFound.setFileAPIKey_NOT_FOUND(true);

						this.addHeader_ToResponse( responseInHeader_NotFound, httpServletResponse );
						
						return;  // EARLY EXIT
					}
				}
			}

			this.addHeader_ToResponse( responseInHeader, httpServletResponse );

			//  Copy file to response
			
			
			httpServletResponse.setContentLengthLong(responseContentLength);

			this.addHeader_ToResponse( responseInHeader, httpServletResponse );
			
			InputStream inputStream = null;
			OutputStream outputStream = null;
			
			try {
				inputStream = new FileInputStream(storage_Object_Name_File_ToUse);
				outputStream = httpServletResponse.getOutputStream();
				
				if ( have_GZIP_File ) {
				
					if ( get_StoredFileObjectContents_Request.getReturnAs_GZIP_IfAvailable() != null 
							&& get_StoredFileObjectContents_Request.getReturnAs_GZIP_IfAvailable().booleanValue() ) { 

					} else {

						inputStream = new GZIPInputStream(inputStream);
					}
				}

				byte[] byteBuffer = new byte[ COPY_FILE_ARRAY_SIZE ];
				int bytesRead;

				while ( ( bytesRead = inputStream.read( byteBuffer ) ) > 0 ){
					
					outputStream.write( byteBuffer, 0, bytesRead );
				}
			} catch ( Exception e ) {
				
				String msg = "Failed writing request to file: " + storage_Object_Name_File_ToUse.getAbsolutePath();
				log.error(msg, e);

				throw new FileObjectStorageWebappInternalException(msg);
			} finally {

				boolean closeOutputStreamFail = false;
				try {
					if ( outputStream != null ) {
						outputStream.close();
					}
				} catch(Exception e){
					closeOutputStreamFail = true;

					String msg = "Failed closing outputStream";
					log.error(msg, e);
					throw new FileObjectStorageWebappInternalException(msg);
				} finally {
					try {
						if ( inputStream != null ) {
							inputStream.close();
						}
					} catch(Exception e){ 
						if ( ! closeOutputStreamFail ) {
						}
						String msg = "Failed closing input stream for file: " + storage_Object_Name_File_ToUse.getAbsolutePath();
						log.error(msg, e);
						throw new FileObjectStorageWebappInternalException(msg);
					}
				}
			}
			
		} finally {
			
		}
	}
	

	/**
	 * @param get_StoredFileObjectContents_Request
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @throws Exception 
	 */
	private void processRequest_DataIn_AWS_S3( 
			
			Get_StoredFileObjectContents_Request get_StoredFileObjectContents_Request,
			HttpServletRequest httpServletRequest, 
			HttpServletResponse httpServletResponse) throws Exception {
		
		try {
			boolean webservice_Request__ReturnAs_GZIP_IfAvailable = false;
			
			if ( get_StoredFileObjectContents_Request.getReturnAs_GZIP_IfAvailable() != null
					&& get_StoredFileObjectContents_Request.getReturnAs_GZIP_IfAvailable().booleanValue()
					) {	
		
				webservice_Request__ReturnAs_GZIP_IfAvailable = true;
			}
			
			S3Client amazonS3_Client_ForOutput = null;
			
			{  // Use Region from Config, otherwise SDK use from Environment Variable

				final String amazonS3_RegionName = 
						ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Region_MainStorage();

				if ( StringUtils.isNotEmpty( amazonS3_RegionName ) ) {
					
					Region aws_S3_Region = Region.of(amazonS3_RegionName);
					
					amazonS3_Client_ForOutput = 
							S3Client.builder()
							.region( aws_S3_Region )
							.httpClientBuilder(ApacheHttpClient.builder())
							.build();
				} else {
					//  SDK use Region from Environment Variable
					
					amazonS3_Client_ForOutput = 
							S3Client.builder()
							.httpClientBuilder(ApacheHttpClient.builder())
							.build(); 
				}
			}
			
			boolean have_GZIP_MainContents_S3_Object = false;
			
			Internal__AWS_S3_GetObjectResponse_Class internal__AWS_S3_GetObjectResponse_Class = null;
			
			try {

				{	//  Get the AWS S3 Response for the main Object.

					Storage_Object_Name__Create__Result__MainName storage_Object_Name__Create__Result =
							Storage_Object_Name__Create.create_Main_Storage_Object_Name( get_StoredFileObjectContents_Request.getFileAPIKey() );

					if ( webservice_Request__ReturnAs_GZIP_IfAvailable ) {

						//  Prefer GZIP response so look for GZIP first

						try {  // Get using GZIP object name

							internal__AWS_S3_GetObjectResponse_Class = 
									this.get__Internal__AWS_S3_GetObjectResponse_Class( 
											storage_Object_Name__Create__Result.getGzipObjectname(), amazonS3_Client_ForOutput
											);

							have_GZIP_MainContents_S3_Object = true;

						} catch ( NoSuchKeyException e ) {
							//  EAT Exception
						}

						if ( internal__AWS_S3_GetObjectResponse_Class == null ) {

							//  NOT FOUND

							try {  // Get using NNOT GZIP object name

								internal__AWS_S3_GetObjectResponse_Class = 
										this.get__Internal__AWS_S3_GetObjectResponse_Class( 
												storage_Object_Name__Create__Result.getMainObjectname(), amazonS3_Client_ForOutput
												);


							} catch ( NoSuchKeyException e ) {
								//  EAT Exception
							}
						}

					} else {

						//  NOT Prefer GZIP response so look for Not GZIP first

						try {  // Get using NNOT GZIP object name

							internal__AWS_S3_GetObjectResponse_Class = 
									this.get__Internal__AWS_S3_GetObjectResponse_Class( 
											storage_Object_Name__Create__Result.getMainObjectname(), amazonS3_Client_ForOutput
											);


						} catch ( NoSuchKeyException e ) {
							//  EAT Exception
						}

						if ( internal__AWS_S3_GetObjectResponse_Class == null ) {

							//  NOT FOUND

							try {  // Get using GZIP object name

								internal__AWS_S3_GetObjectResponse_Class = 
										this.get__Internal__AWS_S3_GetObjectResponse_Class( 
												storage_Object_Name__Create__Result.getGzipObjectname(), amazonS3_Client_ForOutput
												);

								have_GZIP_MainContents_S3_Object = true;

							} catch ( NoSuchKeyException e ) {
								//  EAT Exception
							}
						}

					}

					if ( internal__AWS_S3_GetObjectResponse_Class == null ) {

						//  Neither is found so return the 404 NOT FOUND

						httpServletResponse.setStatus( HttpServletResponse.SC_NOT_FOUND /* 404  */ );

						Get_StoredFileObjectContents_Response_InHeader responseInHeader = new Get_StoredFileObjectContents_Response_InHeader();
						responseInHeader.setFileAPIKey_NOT_FOUND(true);
						this.addHeader_ToResponse( responseInHeader, httpServletResponse );

						return;  // EARLY EXIT
					}
				}

				long responseContentLength = 0;

				{
					GetObjectResponse getObjectResponse = internal__AWS_S3_GetObjectResponse_Class.getObjectResponseMainObject_UsableAs_InputStream.response();
					if ( getObjectResponse == null ) {
						String msg = "internal__AWS_S3_GetObjectResponse_Class.getObjectResponseMainObject_UsableAs_InputStream.response() returned null";
						log.error(msg);
						throw new FileObjectStorageWebappConfigException(msg);
					}

					Long contentLength = getObjectResponse.contentLength();

					if ( contentLength == null ) {
						String msg = "internal__AWS_S3_GetObjectResponse_Class.getObjectResponseMainObject_UsableAs_InputStream.response().contentLength() returned null";
						log.error(msg);
						throw new FileObjectStorageWebappConfigException(msg); 
					}

					responseContentLength = contentLength.longValue();
				}

				Get_StoredFileObjectContents_Response_InHeader responseInHeader = new Get_StoredFileObjectContents_Response_InHeader();

				if ( webservice_Request__ReturnAs_GZIP_IfAvailable && have_GZIP_MainContents_S3_Object ) {
					responseInHeader.setResponse_Is_GZIP( true );
				}


				if ( have_GZIP_MainContents_S3_Object ) {

					//  Get Meta Data to return original file length since have GZIP file and will return NOT GZIP data

					String metaFilename = Storage_Object_Name__Create.create_GZIP_MetaData_Storage_Object_Name( get_StoredFileObjectContents_Request.getFileAPIKey() );

					try {
						String s3_Object_Key__metaFilename = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + metaFilename;

						GetObjectRequest getObjectRequest = GetObjectRequest.builder()
								.bucket(ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage())
								.key(s3_Object_Key__metaFilename)
								.build();

						ResponseInputStream<GetObjectResponse> getObjectResponse__CanUseAs_InputStream =
								amazonS3_Client_ForOutput.getObject(getObjectRequest );

						MetaFileContents metaFileContents = MetaFileContents_ReadFromInputStream.readFromInputStream(getObjectResponse__CanUseAs_InputStream);

						if ( metaFileContents.getOriginalFileSize() == null ) {

							//  No Original File Size in MetaFile and need original file size

							String msg = "No Original File Size in MetaFile and need original file size.  File API Key: " + get_StoredFileObjectContents_Request.getFileAPIKey();
							log.error(msg);
							throw new FileObjectStorageWebappInternalException(msg);
						}

						responseInHeader.setFileLength_NonGZIP( metaFileContents.getOriginalFileSize() );

						if ( ( ! webservice_Request__ReturnAs_GZIP_IfAvailable ) && have_GZIP_MainContents_S3_Object ) {

							responseContentLength = metaFileContents.getOriginalFileSize();
						}

					} catch ( NoSuchKeyException e ) {

						//  No MetaFile and need original file size

						String msg = "No MetaFile and need original file size.  File API Key: " + get_StoredFileObjectContents_Request.getFileAPIKey();
						log.error(msg);
						throw new FileObjectStorageWebappInternalException(msg);
					}
				}

				//  Copy file to response

				httpServletResponse.setContentLengthLong(responseContentLength);

				this.addHeader_ToResponse( responseInHeader, httpServletResponse );

				try ( OutputStream outputStream = httpServletResponse.getOutputStream() ) {

					InputStream inputStream = internal__AWS_S3_GetObjectResponse_Class.getObjectResponseMainObject_UsableAs_InputStream;
							
					if ( ( ! webservice_Request__ReturnAs_GZIP_IfAvailable ) && have_GZIP_MainContents_S3_Object ) {

						inputStream = new GZIPInputStream(inputStream);
					}

					byte[] byteBuffer = new byte[ COPY_FILE_ARRAY_SIZE ];
					int bytesRead;

					while ( ( bytesRead = inputStream.read( byteBuffer ) ) > 0 ){

						outputStream.write( byteBuffer, 0, bytesRead );
					}
				} catch ( Exception e ) {

					String msg = "Failed writing request to API Key: " + get_StoredFileObjectContents_Request.getFileAPIKey();
					log.error(msg, e);

					throw new FileObjectStorageWebappInternalException(msg);
				}
			} finally { 
				
				if ( internal__AWS_S3_GetObjectResponse_Class != null ) {
					
					internal__AWS_S3_GetObjectResponse_Class.getObjectResponseMainObject_UsableAs_InputStream.close();
				}
				
			}
			
		} finally {
			
		}
	}
	
	/**
	 * Response for Get Object
	 *
	 */
	private static class Internal__AWS_S3_GetObjectResponse_Class {
		
		ResponseInputStream<GetObjectResponse> getObjectResponseMainObject_UsableAs_InputStream;
		
	}
	
	private Internal__AWS_S3_GetObjectResponse_Class get__Internal__AWS_S3_GetObjectResponse_Class(
			
			String objectName_WithoutPrefix,
			S3Client amazonS3_Client_ForOutput
			) {

		String s3_Object_Key = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + objectName_WithoutPrefix;

		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage())
				.key(s3_Object_Key)
				.build();

		ResponseInputStream<GetObjectResponse> getObjectResponse__CanUseAs_InputStream =
				amazonS3_Client_ForOutput.getObject(getObjectRequest );


		Internal__AWS_S3_GetObjectResponse_Class response = new Internal__AWS_S3_GetObjectResponse_Class();
		response.getObjectResponseMainObject_UsableAs_InputStream = getObjectResponse__CanUseAs_InputStream;
		
		return response;
	}
	
	
	
	
	////////////////////////////////////////////////
	////////////////////////////////////////////////
	////////////////////////////////////////////////
	
	/**
	 * @param responseInHeader
	 * @param httpServletResponse
	 * @throws FileObjectStorageSerializeRequestException 
	 * @throws JAXBException 
	 * @throws FileObjectStorageWebappConfigException 
	 */
	private void addHeader_ToResponse( Get_StoredFileObjectContents_Response_InHeader responseInHeader, HttpServletResponse httpServletResponse ) throws FileObjectStorageSerializeRequestException, JAXBException, FileObjectStorageWebappConfigException {
		
		if ( servetResponseFormat == ServetResponseFormatEnum.XML ) {

			JAXBContext jaxbContext = JAXBContext.newInstance( Get_StoredFileObjectContents_Response_InHeader.class );
			
			// Marshal Java object into XML
			try {
				Marshaller marshaller = jaxbContext.createMarshaller();

				StringWriter stringWriter = new StringWriter();
				
				marshaller.setProperty( Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.toString() );
				
				marshaller.marshal( responseInHeader, stringWriter );

				httpServletResponse.setHeader( 
						WebserviceFileObjectStorage_HttpHeaderParamsConstants.GET_FILE_OBJECT_HTTP_RESPONSE_HEADER_PARAM,
						stringWriter.toString()
						);

			} catch ( JAXBException e2 ) {
				String msg = "Failed to serialize response object";
				log.error( msg, e2 );
				throw new FileObjectStorageSerializeRequestException( msg, e2 );
			}
			
		} else if ( servetResponseFormat == ServetResponseFormatEnum.JSON ) {

			String msg = "JSON not currently supported.  Add in Jackson jars for support";
			log.error( msg );
			throw new IllegalArgumentException( msg );
			
//			// send the JSON response 
//			ObjectMapper mapper = new ObjectMapper();  //  Jackson JSON library object
//			mapper.writeValue( outputStreamBufferOfServerResponse, webserviceResponseAsObject ); // where first param can be File, OutputStream or Writer

//			httpServletResponse.setHeader( , value);
//
//			if ( StringUtils.isNotEmpty( e.getMessage() ) ) {
//				WriteResponseStringToOutputStream.getInstance()
//				.writeResponseStringToOutputStream( e.getMessage(), response);
//			}

		} else {
			String msg = "Unknown value for servetResponseFormat: " + servetResponseFormat;
			log.error( msg );
			throw new FileObjectStorageWebappConfigException( msg );
		}
		
	}


}
