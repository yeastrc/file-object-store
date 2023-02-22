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
import org.yeastrc.file_object_storage.web_app.constants_enums.ServetResponseFormatEnum;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileBadRequestToServletException;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileDeserializeRequestException;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileFileUploadInternalException;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileSerializeRequestException;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileWebappConfigException;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileWebappInternalException;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn.StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents;
import org.yeastrc.file_object_storage.web_app.servlets_common.GetRequestObjectFromInputStream;
import org.yeastrc.file_object_storage.web_app.servlets_common.Get_ServletResultDataFormat_FromServletInitParam;
import org.yeastrc.file_object_storage.web_app.servlets_common.WriteResponseStringToOutputStream;
import org.yeastrc.file_object_storage.web_app.shared_server_client.constants_enums.WebserviceFileObjectStorage_HttpHeaderParamsConstants;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.Get_StoredFileObjectContents_Request;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.Get_StoredFileObjectContents_Response_InHeader;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create.Storage_Object_Name__Create__Result__MainName;


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
			} catch ( SpectralFileDeserializeRequestException e ) {
				throw e;
			} catch (Exception e) {
				String msg = "Failed to deserialize request";
				log.error( msg, e );
				throw new SpectralFileBadRequestToServletException( e );
			}

			try {
				get_StoredFileObjectContents_Request = (Get_StoredFileObjectContents_Request) requestObj;
			} catch (Exception e) {
				String msg = "Incoming request not deserialize to expected Java class.  Failed to cast requestObj to Get_StoredFileObjectContents_Request";
				log.error( msg, e );
				throw new SpectralFileBadRequestToServletException( "Incoming request not deserialize to expected Java class" );
			}
		} catch (SpectralFileBadRequestToServletException e) {

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
		
			processRequest( get_StoredFileObjectContents_Request, request, response );
			
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
	private void processRequest( 
			
			Get_StoredFileObjectContents_Request get_StoredFileObjectContents_Request,
			HttpServletRequest httpServletRequest, 
			HttpServletResponse httpServletResponse) throws Exception {
		
		try {

			////////////////////
			

			//  ASSUMED:  Past this point only local storage is assumed.  NO support of S3 


			if ( StringUtils.isNotEmpty( ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket() ) ) {
				
				String msg = "S3 is NOT Supported at this time";
				log.error(msg);
				throw new SpectralFileFileUploadInternalException(msg);
			}
			
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
								throw new SpectralFileDeserializeRequestException(msg);
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

				throw new SpectralFileWebappInternalException(msg);
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
					throw new SpectralFileWebappInternalException(msg);
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
						throw new SpectralFileWebappInternalException(msg);
					}
				}
			}
			
		} finally {
			
		}
	}
	
	
	/**
	 * @param responseInHeader
	 * @param httpServletResponse
	 * @throws SpectralFileSerializeRequestException 
	 * @throws JAXBException 
	 * @throws SpectralFileWebappConfigException 
	 */
	private void addHeader_ToResponse( Get_StoredFileObjectContents_Response_InHeader responseInHeader, HttpServletResponse httpServletResponse ) throws SpectralFileSerializeRequestException, JAXBException, SpectralFileWebappConfigException {
		
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
				throw new SpectralFileSerializeRequestException( msg, e2 );
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
			throw new SpectralFileWebappConfigException( msg );
		}
		
	}


}
