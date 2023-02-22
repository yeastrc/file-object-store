package org.yeastrc.file_object_storage.web_app.servlets_upload_file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.config.ConfigData_Directories_ProcessUploadInfo_InWorkDirectory;
import org.yeastrc.file_object_storage.web_app.constants_enums.FileUploadConstants;
import org.yeastrc.file_object_storage.web_app.constants_enums.ServetResponseFormatEnum;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileFileUploadFileSystemException;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileFileUploadInternalException;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.TempDir_OnLocalFileSystem_CreateToUploadFileTo;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn.StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM;
import org.yeastrc.file_object_storage.web_app.hashes_compute.Hashes_Compute;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents_WriteToOutputStream;
import org.yeastrc.file_object_storage.web_app.servlets_common.Get_ServletResultDataFormat_FromServletInitParam;
import org.yeastrc.file_object_storage.web_app.servlets_common.WriteResponseObjectToOutputStream;
import org.yeastrc.file_object_storage.web_app.shared_server_client.constants_enums.WebserviceFileObjectStorage_QueryParamsConstants;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_UploadFile_Response;
import org.yeastrc.file_object_storage.web_app.utils.API_Key__Create_From_File_Hashes_And_Length;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create.Storage_Object_Name__Create__Result__MainName;

//  import com.amazonaws.services.s3.AmazonS3;
//  import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
//  import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
//  import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
//  import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
//  import com.amazonaws.services.s3.model.PartETag;
//  import com.amazonaws.services.s3.model.UploadPartRequest;
//  import com.amazonaws.services.s3.model.UploadPartResult;


/**
 * Upload Scan File Process - Actual upload of Scan File
 * 
 * Receives uploadScanFileTempKey as query parameter
 * 
 * Receives Uploaded scan file as a Stream
 * 
 * Reads the input stream from the "HttpServletRequest request" object
 * 
 *  This Servlet should be considered a webservice as it returns JSON or XML
 */
public class UploadFile_UploadFile_Servlet extends HttpServlet {

	private static final Logger log = LoggerFactory.getLogger( UploadFile_UploadFile_Servlet.class );

	private static final long serialVersionUID = 1L;
	
	public static final int COPY_FILE_ARRAY_SIZE = 32 * 1024; // 32 KB
	
	//  Large since can have at most 10,000 parts
	public static final int S3_MULIPART_UPLOAD_PART_SIZE = 40 * 1024 * 1024; // each part 40 MB

//	public static final int S3_MULIPART_UPLOAD_PART_SIZE = 5 * 1024 * 1024; // each part 5 MB - Min Size

//	private static final int UPLOAD_SCAN_FILE_INIT_UPLOAD_TO_S3_RETRY_COUNT_MAX = 3;
//	private static final int UPLOAD_SCAN_FILE_INIT_UPLOAD_TO_S3_RETRY_DELAY = 200; // in milliseconds
//
//	private static final int UPLOAD_SCAN_FILE_PART_TO_S3_RETRY_COUNT_MAX = 4;
//	private static final int UPLOAD_SCAN_FILE_PART_TO_S3_RETRY_DELAY = 300; // in milliseconds
//
//	private static final int UPLOAD_SCAN_FILE_COMPLETE_UPLOAD_TO_S3_RETRY_COUNT_MAX = 6;
//	private static final int UPLOAD_SCAN_FILE_COMPLETE_UPLOAD_TO_S3_RETRY_DELAY = 500; // in milliseconds
	
	
	private static final String STANDARD_UPLOAD_FILE_CONTENTS_TEMP_FILENAME = "temp_uploaded_file_contents";

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
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		log.info( "INFO:  doPost called");

		try {
			boolean gzipCompressContents = false;
			
			{
				String gzipCompressContents_String = request.getParameter( WebserviceFileObjectStorage_QueryParamsConstants.UPLOAD_FILE_GZIP_COMPRESS_CONTENTS_QUERY_PARAM );
				
				if ( WebserviceFileObjectStorage_QueryParamsConstants.UPLOAD_FILE_GZIP_COMPRESS_CONTENTS_QUERY_PARAM__VALUE_TRUE.equals(gzipCompressContents_String) ) {
					gzipCompressContents = true;
				}
			}

//			String requestURL = request.getRequestURL().toString();
			
			long postContentLength = request.getContentLengthLong();
			

		       // file upload size limit
			if ( postContentLength > FileUploadConstants.MAX_FILE_UPLOAD_SIZE ) {

				log.warn( "Upload File size Exceeded.  File size uploaded: " + postContentLength
						+ ", max upload file size: " + FileUploadConstants.MAX_FILE_UPLOAD_SIZE 
						+ ", max upload file size (formatted): " + FileUploadConstants.MAX_FILE_UPLOAD_SIZE_FORMATTED);
				
				response.setStatus( HttpServletResponse.SC_BAD_REQUEST /* 400  */ );
				
				UploadFile_UploadFile_Response uploadResponse = new UploadFile_UploadFile_Response();
				uploadResponse.setStatusSuccess(false);
				uploadResponse.setFileSizeLimitExceeded(true);
				uploadResponse.setMaxSize( FileUploadConstants.MAX_FILE_UPLOAD_SIZE );
				uploadResponse.setMaxSizeFormatted( FileUploadConstants.MAX_FILE_UPLOAD_SIZE_FORMATTED );

				WriteResponseObjectToOutputStream.getSingletonInstance()
				.writeResponseObjectToOutputStream( uploadResponse, servetResponseFormat, response );
				
				throw new FailResponseSentException();
			}

			File uploadFileTempDir = TempDir_OnLocalFileSystem_CreateToUploadFileTo.getInstance().createTempDirToUploadFileTo();
			
		//   AWS S3 Support commented out.  See file ZZ__AWS_S3_Support_CommentedOut.txt in GIT repo root.

//			if ( StringUtils.isNotEmpty( ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket() ) ) {
//				
//				//  Save uploaded scan file to S3 Object. Returns a response to client if fail
//				saveUploadedScanFileToS3Object( request, response, scanFilenameToProcess, uploadScanFileTempKey_Dir );
//
//			} else {
			
				//  Save uploaded scan file to local disk file. Returns a response to client if fail
					
			INTERNAL__saveUploadedFileToLocalDiskFile__Response saveUploadedFileToLocalDiskFile__Response =
					saveUploadedFileToLocalDiskFile( request, response, uploadFileTempDir, gzipCompressContents );
			
//			}
			

			File rootBaseDir = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getFileObjectStorage_BaseDirectory();
			
			File mainStorage_BaseDir = new File( rootBaseDir, FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR );
			if ( ! mainStorage_BaseDir.exists() ) {
//				boolean mkdirResult = 
				mainStorage_BaseDir.mkdir();
			}
			if ( ! mainStorage_BaseDir.exists() ) {
				String msg = "mainStorage_BaseDir does not exist after testing for it and attempting to create it.  mainStorage_BaseDir: " 
						+ mainStorage_BaseDir.getAbsolutePath();
				log.error( msg );
				throw new SpectralFileFileUploadFileSystemException(msg);
			}
			
			
			//  ASSUMED:  Past this point only local storage is assumed.  NO support of S3 


			if ( StringUtils.isNotEmpty( ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket() ) ) {
				
				String msg = "S3 is NOT Supported at this time";
				log.error(msg);
				throw new SpectralFileFileUploadInternalException(msg);
			}
				

			Storage_Object_Name__Create__Result__MainName storage_Object_Name__Create__Result =
					Storage_Object_Name__Create.create_Main_Storage_Object_Name( saveUploadedFileToLocalDiskFile__Response.API_Key );

			String[] storage_Object_Name_AllPossible = { storage_Object_Name__Create__Result.getMainObjectname(), storage_Object_Name__Create__Result.getGzipObjectname() };
			
			File mainStorage_SubDirBasedOnName =
					StorageDir_OnLocalFileSystem_CreateToStoreFileIn.getInstance().createToStoreFileIn(
							storage_Object_Name_AllPossible, StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM.YES );
			
			boolean storage_Object_Name_File_AlreadyExists = false;
			
			{
				//  Check if any of existing versions of object filename exists
				
				File storage_Object_Name_File_MainObjectname = new File( mainStorage_SubDirBasedOnName, storage_Object_Name__Create__Result.getMainObjectname() );

				File storage_Object_Name_File_GzipObjectname = new File( mainStorage_SubDirBasedOnName, storage_Object_Name__Create__Result.getGzipObjectname() );

				if ( storage_Object_Name_File_MainObjectname.exists() || storage_Object_Name_File_GzipObjectname.exists() ) {
					
					storage_Object_Name_File_AlreadyExists = true;
				}
			}
			
			if ( storage_Object_Name_File_AlreadyExists ) {
				//  Already exists so delete the uploaded file

				if ( ! saveUploadedFileToLocalDiskFile__Response.savedFile.delete() ) {

					String msg = "Fail to delete temp uploaded data file.  Temp uploaded data File: " 
							+ saveUploadedFileToLocalDiskFile__Response.savedFile.getAbsolutePath();
					log.error( msg );
					throw new SpectralFileFileUploadFileSystemException(msg);
				}
			} else {

				//  NOT already exists so move uploaded file to perm keep subdir and perm keep filename 
				
				if ( gzipCompressContents ) {
					
					//  GZIP compressed incoming contents

					//  First create GZIP "Meta Data" file and then move it
					
					{
						MetaFileContents metaFileContents = new MetaFileContents();
						
						metaFileContents.setOriginalFileSize(saveUploadedFileToLocalDiskFile__Response.originalFileSize);
						metaFileContents.setFileIsGZIP(gzipCompressContents);
						
						String metaFilename = Storage_Object_Name__Create.create_GZIP_MetaData_Storage_Object_Name(saveUploadedFileToLocalDiskFile__Response.API_Key);
						
						File uploadFileTempDir_metaFile = new File( mainStorage_SubDirBasedOnName, metaFilename );
						
						try ( FileOutputStream fos = new FileOutputStream(uploadFileTempDir_metaFile) ) {
							
							MetaFileContents_WriteToOutputStream.writeToOutputStream(metaFileContents, fos);
						}
						
						//  Move file to output dir
						
						File mainStorage_SubDirBasedOnName__MetaFile = new File( mainStorage_SubDirBasedOnName, metaFilename );

						if ( ! uploadFileTempDir_metaFile.renameTo(mainStorage_SubDirBasedOnName__MetaFile) ) {

							String msg = "Failed to rename meta data file to final storage name and path.  From File: " 
									+ uploadFileTempDir_metaFile.getAbsolutePath()
									+ ", To file: "
									+ mainStorage_SubDirBasedOnName__MetaFile.getAbsolutePath();
							log.error( msg );
							throw new SpectralFileFileUploadFileSystemException(msg);
						}
					}

					String storage_Object_Name_ToUse = storage_Object_Name__Create__Result.getGzipObjectname();

					File storage_Object_Name_File = new File( mainStorage_SubDirBasedOnName, storage_Object_Name_ToUse );

					if ( ! saveUploadedFileToLocalDiskFile__Response.savedFile.renameTo(storage_Object_Name_File) ) {

						String msg = "Failed to rename data file to final storage name and path.  From File: " 
								+ saveUploadedFileToLocalDiskFile__Response.savedFile.getAbsolutePath()
								+ ", To file: "
								+ storage_Object_Name_File.getAbsolutePath();
						log.error( msg );
						throw new SpectralFileFileUploadFileSystemException(msg);
					}
				
				} else {
				
					//  NOT GZIP Compressed contents

					String storage_Object_Name_ToUse = storage_Object_Name__Create__Result.getMainObjectname();

					File storage_Object_Name_File = new File( mainStorage_SubDirBasedOnName, storage_Object_Name_ToUse );

					if ( ! saveUploadedFileToLocalDiskFile__Response.savedFile.renameTo(storage_Object_Name_File) ) {

						String msg = "Failed to rename data file to final storage name and path.  From File: " 
								+ saveUploadedFileToLocalDiskFile__Response.savedFile.getAbsolutePath()
								+ ", To file: "
								+ storage_Object_Name_File.getAbsolutePath();
						log.error( msg );
						throw new SpectralFileFileUploadFileSystemException(msg);
					}
				}
			}
			
			{
				//  Delete the temp dir the uploaded data file was written to
				
				File fromFile_ContainingFolder = saveUploadedFileToLocalDiskFile__Response.savedFile.getParentFile();
				
				if ( ! fromFile_ContainingFolder.delete() ) {
					log.warn( "Failed to delete temp uploaded file temp containing dir: " + fromFile_ContainingFolder.getAbsolutePath() );
				}
			}
			
			
			
			UploadFile_UploadFile_Response uploadResponse = new UploadFile_UploadFile_Response();
			
			uploadResponse.setStatusSuccess(true);

			uploadResponse.setApiKey_Assigned(saveUploadedFileToLocalDiskFile__Response.API_Key);


			WriteResponseObjectToOutputStream.getSingletonInstance()
			.writeResponseObjectToOutputStream( uploadResponse, servetResponseFormat, response );
			
			log.info( "Completed processing Upload");
			
		} catch ( FailResponseSentException e ) {
			
			
		} catch (Throwable ex){

			log.error( "Exception: " + ex.toString(), ex );

			response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR /* 500  */ );

			UploadFile_UploadFile_Response uploadResponse = new UploadFile_UploadFile_Response();
			uploadResponse.setStatusSuccess(false);
			
			try {
				WriteResponseObjectToOutputStream.getSingletonInstance()
				.writeResponseObjectToOutputStream( uploadResponse, servetResponseFormat, response );
			} catch ( Exception e ) {
				throw new ServletException( e );
			} finally {

			}
			
			//  response.sendError  sends a HTML page so don't use here since return JSON instead
			
//			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR /* 500  */ );
			
//			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR /* 500  */, responseJSONString );
			
//			throw new ServletException( ex );
		}

	}
	

//  AWS S3 Support commented out.  See file ZZ__AWS_S3_Support_CommentedOut.txt in GIT repo root.

//	/**
//	 * @param request
//	 * @param response
//	 * @param scanFilenameToProcess
//	 * @param uploadScanFileTempKey_Dir
//	 * @throws Exception 
//	 */
//	private void saveUploadedScanFileToS3Object(
//			HttpServletRequest request, 
//			HttpServletResponse response,
//			String scanFilenameToProcess, 
//			File uploadScanFileTempKey_Dir ) throws Exception {
//		
//
//		//  Compute the API key on the fly as the scan file data comes in: 
//		Compute_Hashes compute_Hashes = Compute_Hashes.getNewInstance();
//		
//		final String bucketName = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket();
//		
//		String uploadScanFileTempKey_Dir_Name = uploadScanFileTempKey_Dir.getName();
//		
//		String s3_Object_Key = 
//				Create_S3_Object_Paths.getInstance()
//				.get_ScanFile_Uploaded_S3ObjectPath( uploadScanFileTempKey_Dir_Name, scanFilenameToProcess );
//
//		//  Write a file to uploadScanFileTempKey_Dir with info on file to be written to S3
//		{
//			UploadScanfileS3Location uploadScanfileS3Location = new UploadScanfileS3Location();
//			uploadScanfileS3Location.setScanFilenameToProcess( scanFilenameToProcess );
//			uploadScanfileS3Location.setS3_bucketName( bucketName );
//			uploadScanfileS3Location.setS3_objectName( s3_Object_Key );
//			
//			JAXBContext jaxbContext = JAXBContext.newInstance( UploadScanfileS3Location.class );
//			Marshaller marshaller = jaxbContext.createMarshaller();
//		
//			File scanfileS3InfoFile = new File( uploadScanFileTempKey_Dir, UploadProcessing_InputScanfileS3InfoConstants.SCANFILE_S3_LOCATION_FILENAME );
//			
//			try ( OutputStream os = new FileOutputStream( scanfileS3InfoFile ) ) {
//				marshaller.marshal( uploadScanfileS3Location, os );
//			} catch (Exception e ) {
//				String msg = "Failed to write uploadScanfileS3Location to scanfileS3InfoFile: " + scanfileS3InfoFile.getAbsolutePath();
//				log.error( msg, e );
//				throw new SpectralFileWebappInternalException( msg, e );
//			}
//		}		
//		
//		//  Transfer the file from the stream to an S3 object
//		final AmazonS3 amazonS3 = CommonReader_File_And_S3_Holder.getSingletonInstance().getCommonReader_File_And_S3().getS3_Client();
//
//		byte[] uploadPartByteBuffer = new byte[ S3_MULIPART_UPLOAD_PART_SIZE ];
//
//		int partNumber = 0; // Must start at 1, incremented at top of loop, max of 10,000
//		int bytesRead = 0;
//		
//		try ( InputStream scanDataFileOnDiskIS = request.getInputStream() ) {
//
//	        // Create a list of UploadPartResponse objects. You get one of these
//	        // for each part upload.
//	        List<PartETag> partETags = new ArrayList<>( 10001 ); // Init to max possible size
//
//	    	InitiateMultipartUploadResult initResponse = null;
//	    	
//    		int uploadInitToS3_RetryCounter = 0;
//    		
//    		while ( true ) {
//    			try {
//    				// Step 1: Initialize.
//    				InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest( bucketName, s3_Object_Key );
//    				initResponse = amazonS3.initiateMultipartUpload(initRequest);
//
//    				break; //  Exit while( true ) since S3 call succeeded
//
//    			} catch ( Exception e ) {
//    				uploadInitToS3_RetryCounter++;
//    				if ( uploadInitToS3_RetryCounter > UPLOAD_SCAN_FILE_INIT_UPLOAD_TO_S3_RETRY_COUNT_MAX ) {
//    					throw e;
//    				}
//    				Thread.sleep( UPLOAD_SCAN_FILE_INIT_UPLOAD_TO_S3_RETRY_DELAY );
//    			}
//    		}
//    		
//        	String uploadId = initResponse.getUploadId();
//        
//	        try {
//	        	//  Step 2:  Upload parts.
//	        	while ( ( bytesRead = populateBufferFromScanDataFile( scanDataFileOnDiskIS, uploadPartByteBuffer ) ) > 0 ) {
//
//					compute_Hashes.updateHashesComputing( uploadPartByteBuffer, bytesRead );
//					
//	        		partNumber++;
//	        		boolean lastPart = false;
//	        		if ( bytesRead < uploadPartByteBuffer.length ) { // uploadPartByteBuffer not full so is at end of file
//	        			lastPart = true;
//	        		}
//	        		
//	        		int uploadPartToS3_RetryCounter = 0;
//	        		
//	        		while ( true ) {
//	        			try {
//	        				ByteArrayInputStream scanFilePartIS = new ByteArrayInputStream( uploadPartByteBuffer, 0 /* offset */, bytesRead /* length */ );
//	        				UploadPartRequest uploadRequest = 
//	        						new UploadPartRequest().withUploadId( uploadId )
//	        						.withBucketName( bucketName )
//	        						.withKey( s3_Object_Key )
//	        						.withInputStream( scanFilePartIS )
//	        						.withPartNumber( partNumber )
//	        						.withPartSize( bytesRead )
//	        						.withLastPart( lastPart );
//
//	        				//   Consider computing MD5 on scanFilePartIS and add to uploadRequest
//	        				//       S3 uses that for an integrity check
//
//	        				UploadPartResult result =  amazonS3.uploadPart( uploadRequest );
//	        				PartETag partETag = result.getPartETag();
//	        				partETags.add( partETag );
//	        				
//	        				break; //  Exit while( true ) since S3 call succeeded
//
//	        			} catch ( Exception e ) {
//	        				uploadPartToS3_RetryCounter++;
//	        				if ( uploadPartToS3_RetryCounter > UPLOAD_SCAN_FILE_PART_TO_S3_RETRY_COUNT_MAX ) {
//	        					throw e;
//	        				}
//	        				Thread.sleep( UPLOAD_SCAN_FILE_PART_TO_S3_RETRY_DELAY );
//	        			}
//	        		}
//	        		
//	        		if ( bytesRead < uploadPartByteBuffer.length ) { // uploadPartByteBuffer not full so is at end of file
//	        			break; // exit loop since at last part
//	        		}
//	        	}
//
//        		int uploadPartToS3_RetryCounter = 0;
//        		
//        		while ( true ) {
//        			try {
//        				// Step 3: Complete.
//        				CompleteMultipartUploadRequest compRequest = new 
//        						CompleteMultipartUploadRequest(
//        								bucketName, 
//        								s3_Object_Key, 
//        								uploadId,
//        								partETags);
//
//        				amazonS3.completeMultipartUpload( compRequest );
//
//        				break; //  Exit while( true ) since S3 call succeeded
//
//        			} catch ( Exception e ) {
//        				uploadPartToS3_RetryCounter++;
//        				if ( uploadPartToS3_RetryCounter > UPLOAD_SCAN_FILE_COMPLETE_UPLOAD_TO_S3_RETRY_COUNT_MAX ) {
//        					throw e;
//        				}
//        				Thread.sleep( UPLOAD_SCAN_FILE_COMPLETE_UPLOAD_TO_S3_RETRY_DELAY );
//        			}
//        		}
//	        } catch (Exception e) {
//	        	log.error( "Exception transfering uploaded Scan file from request.inputstream to S3. amazonS3.abortMultipartUpload(...) will be called next " );
//	        	amazonS3.abortMultipartUpload( new AbortMultipartUploadRequest( bucketName, s3_Object_Key, uploadId ) );
//	        	throw e;
//	        }
//		}
//		
//
//		writeAPIKeyToFile( compute_Hashes, uploadScanFileTempKey_Dir );
//	}
//	
//	/**
//	 * @param scanDataFileIS
//	 * @param uploadPartByteBuffer
//	 * @return number of bytes read into uploadPartByteBuffer.  If < uploadPartByteBuffer.length, at last buffer for file
//	 * @throws IOException 
//	 */
//	private int populateBufferFromScanDataFile( InputStream scanDataFileIS, byte[] uploadPartByteBuffer ) throws IOException {
//		
//		int byteBufferLength = uploadPartByteBuffer.length;
//		
//		int bytesRead = 0;
//		int byteBufferIndex = 0;
//		
//		while ( ( bytesRead = 
//				scanDataFileIS.read( uploadPartByteBuffer, byteBufferIndex, byteBufferLength - byteBufferIndex) ) != -1 ) {
//			byteBufferIndex += bytesRead;
//			if ( byteBufferIndex >= byteBufferLength ) {
//				break;
//			}
//		}
//		
//		return byteBufferIndex;
//	}
	
	/**
	 * @param httpRequest
	 * @param httpResponse
	 * @param scanFilenameToProcess
	 * @param uploadScanFileTempKey_Dir
	 * @throws Exception 
	 */
	private INTERNAL__saveUploadedFileToLocalDiskFile__Response saveUploadedFileToLocalDiskFile(
			
			HttpServletRequest httpRequest, 
			HttpServletResponse httpResponse,
			File uploadScanFileTempKey_Dir,
			boolean gzipCompressContents
			) throws Exception {
		
		//  Compute the API key on the fly as the scan file data comes in: 
		Hashes_Compute hashes_Compute = Hashes_Compute.getNewInstance();
		
		//  File to write the incoming data to:
		File uploadedFileOnDisk = new File( uploadScanFileTempKey_Dir, STANDARD_UPLOAD_FILE_CONTENTS_TEMP_FILENAME );
				
		//  Transfer the file from the stream to a disk file
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		OutputStream outStream = null;
		
		long bytesReadTotal = 0;
		
		try {
			inputStream = httpRequest.getInputStream();
			fileOutputStream = new FileOutputStream( uploadedFileOnDisk ); 
			outStream = fileOutputStream;

			if ( gzipCompressContents ) {
				
				outStream = new GZIPOutputStream( fileOutputStream );
			}

			byte[] byteBuffer = new byte[ COPY_FILE_ARRAY_SIZE ];
			int bytesRead;

			while ( ( bytesRead = inputStream.read( byteBuffer ) ) > 0 ){
				
				hashes_Compute.updateHashesComputing( byteBuffer, bytesRead );
				
				bytesReadTotal += bytesRead;

			       // file upload size limit
				if ( bytesReadTotal > FileUploadConstants.MAX_FILE_UPLOAD_SIZE ) {

					log.warn( "Upload File size Exceeded.  Bytes Read count so far: " + bytesReadTotal
							+ ", max upload file size: " + FileUploadConstants.MAX_FILE_UPLOAD_SIZE 
							+ ", max upload file size (formatted): " + FileUploadConstants.MAX_FILE_UPLOAD_SIZE_FORMATTED
							+ ", writing to file: " + uploadedFileOnDisk.getAbsolutePath() );
					
					httpResponse.setStatus( HttpServletResponse.SC_BAD_REQUEST /* 400  */ );
					
					UploadFile_UploadFile_Response uploadResponse = new UploadFile_UploadFile_Response();
					uploadResponse.setStatusSuccess(false);
					uploadResponse.setFileSizeLimitExceeded(true);
					uploadResponse.setMaxSize( FileUploadConstants.MAX_FILE_UPLOAD_SIZE );
					uploadResponse.setMaxSizeFormatted( FileUploadConstants.MAX_FILE_UPLOAD_SIZE_FORMATTED );

					WriteResponseObjectToOutputStream.getSingletonInstance()
					.writeResponseObjectToOutputStream( uploadResponse, servetResponseFormat, httpResponse );
					
					throw new FailResponseSentException();
				}

				outStream.write( byteBuffer, 0, bytesRead );
				
			}
		} catch ( Exception e ) {
			
			String msg = "Failed writing request to file: " + uploadedFileOnDisk.getAbsolutePath();
			log.error(msg, e);
			httpResponse.setStatus( 500 );

			throw new FailResponseSentException();
		} finally {

			boolean closeOutputStreamFail = false;
			try {
				if ( outStream != null ) {
					outStream.close();
				} 

				if ( fileOutputStream != null ) {
					fileOutputStream.close();  //  Close since maybe not close outStream
				}
			} catch(Exception e){
				closeOutputStreamFail = true;

				String msg = "Failed closing file: " + uploadedFileOnDisk.getAbsolutePath();
				log.error(msg, e);
				httpResponse.setStatus( 500 );

				throw new FailResponseSentException();
			} finally {
				try {
					if ( inputStream != null ) {
						inputStream.close();
					}
				} catch(Exception e){ 
					if ( ! closeOutputStreamFail ) {
					}
					String msg = "Failed closing input stream for file: " + uploadedFileOnDisk.getAbsolutePath();
					log.error(msg, e);
					httpResponse.setStatus( 500 );

					throw new FailResponseSentException();
				}
			}
		}
		
		INTERNAL__saveUploadedFileToLocalDiskFile__Response method_Response = new INTERNAL__saveUploadedFileToLocalDiskFile__Response();
		
		method_Response.API_Key = API_Key__Create_From_File_Hashes_And_Length.getNewInstance().getAPI_Key__Create_From_File_Hashes_And_Length(hashes_Compute, bytesReadTotal);
		
		method_Response.savedFile = uploadedFileOnDisk;
		
		method_Response.originalFileSize = bytesReadTotal;
		
		return method_Response;
	}

	/**
	 * 
	 *
	 */
	private static class INTERNAL__saveUploadedFileToLocalDiskFile__Response {

		private File savedFile;
		
		private long originalFileSize;
		private String API_Key;
	}
	
	/**
	 * 
	 *
	 */
	private static class FailResponseSentException extends Exception {

		private static final long serialVersionUID = 1L;
	}
}
