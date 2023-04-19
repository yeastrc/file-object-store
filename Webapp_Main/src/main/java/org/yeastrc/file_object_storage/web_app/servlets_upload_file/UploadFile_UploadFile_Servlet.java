package org.yeastrc.file_object_storage.web_app.servlets_upload_file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.yeastrc.file_object_storage.web_app.constants_enums.YRC_FileObjectStorage__AWS_S3_Constants;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageFileUploadFileSystemException;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.TempDir_OnLocalFileSystem_CreateToUploadFileTo;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn.StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM;
import org.yeastrc.file_object_storage.web_app.hashes_compute.Hashes_Compute;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents_WriteToOutputStream;
import org.yeastrc.file_object_storage.web_app.servlets_common.Get_ServletResultDataFormat_FromServletInitParam;
import org.yeastrc.file_object_storage.web_app.servlets_common.WriteResponseObjectToOutputStream;
import org.yeastrc.file_object_storage.web_app.servlets_upload_file__utils.UploadFile_Util__OutputStream__SendTo_AWS_S3;
import org.yeastrc.file_object_storage.web_app.shared_server_client.constants_enums.WebserviceFileObjectStorage_QueryParamsConstants;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_UploadFile_Response;
import org.yeastrc.file_object_storage.web_app.utils.API_Key__Create_From_File_Hashes_And_Length;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create.Storage_Object_Name__Create__Result__MainName;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws ServletException, IOException {

		log.info( "INFO:  doPost called");

		try {
			boolean gzipCompressContents = false;

			{
				String gzipCompressContents_String = httpRequest.getParameter( WebserviceFileObjectStorage_QueryParamsConstants.UPLOAD_FILE_GZIP_COMPRESS_CONTENTS_QUERY_PARAM );

				if ( WebserviceFileObjectStorage_QueryParamsConstants.UPLOAD_FILE_GZIP_COMPRESS_CONTENTS_QUERY_PARAM__VALUE_TRUE.equals(gzipCompressContents_String) ) {
					gzipCompressContents = true;
				}
			}

			//			String requestURL = request.getRequestURL().toString();

			long postContentLength = httpRequest.getContentLengthLong();


			// file upload size limit
			if ( postContentLength > FileUploadConstants.MAX_FILE_UPLOAD_SIZE ) {

				log.warn( "Upload File size Exceeded.  File size uploaded: " + postContentLength
						+ ", max upload file size: " + FileUploadConstants.MAX_FILE_UPLOAD_SIZE 
						+ ", max upload file size (formatted): " + FileUploadConstants.MAX_FILE_UPLOAD_SIZE_FORMATTED);

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

			UploadFile_UploadFile_Response uploadResponse = new UploadFile_UploadFile_Response();

			if ( StringUtils.isNotEmpty( ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage() ) ) {

				//  Save uploaded scan file to S3 Object. Returns a response to client if fail
				
				INTERNAL__saveUploadedFileToLocalDiskFile__Response saveUploadedFileToLocalDiskFile__Response = 
						saveUploadedScanFileToS3Object( httpRequest, httpResponse, gzipCompressContents );

				uploadResponse.setApiKey_Assigned(saveUploadedFileToLocalDiskFile__Response.API_Key);
				
			} else {

				//  Save uploaded scan file to local disk file. Returns a response to client if fail

				INTERNAL__saveUploadedFileToLocalDiskFile__Response saveUploadedFileToLocalDiskFile__Response = 
						saveUploadedFileToLocalDiskFile__Start(httpRequest, httpResponse, gzipCompressContents);

				uploadResponse.setApiKey_Assigned(saveUploadedFileToLocalDiskFile__Response.API_Key);
			}

			uploadResponse.setStatusSuccess(true);


			WriteResponseObjectToOutputStream.getSingletonInstance()
			.writeResponseObjectToOutputStream( uploadResponse, servetResponseFormat, httpResponse );

			log.info( "Completed processing Upload");

		} catch ( FailResponseSentException e ) {


		} catch (Throwable ex){

			log.error( "Exception: " + ex.toString(), ex );

			httpResponse.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR /* 500  */ );

			UploadFile_UploadFile_Response uploadResponse = new UploadFile_UploadFile_Response();
			uploadResponse.setStatusSuccess(false);

			try {
				WriteResponseObjectToOutputStream.getSingletonInstance()
				.writeResponseObjectToOutputStream( uploadResponse, servetResponseFormat, httpResponse );
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

	/**
	 * @param httpRequest
	 * @param httpResponse
	 * @param scanFilenameToProcess
	 * @param uploadScanFileTempKey_Dir
	 * @throws Exception 
	 */
	private INTERNAL__saveUploadedFileToLocalDiskFile__Response saveUploadedFileToLocalDiskFile__Start(

			HttpServletRequest httpRequest, 
			HttpServletResponse httpResponse,
			boolean gzipCompressContents
			) throws Exception {

		File uploadFileTempDir = TempDir_OnLocalFileSystem_CreateToUploadFileTo.getInstance().createTempDirToUploadFileTo();

		INTERNAL__saveUploadedFileToLocalDiskFile__Response saveUploadedFileToLocalDiskFile__Response =
				saveUploadedFileToLocalDiskFile( httpRequest, httpResponse, uploadFileTempDir, gzipCompressContents );

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
			throw new FileObjectStorageFileUploadFileSystemException(msg);
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
				throw new FileObjectStorageFileUploadFileSystemException(msg);
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
						throw new FileObjectStorageFileUploadFileSystemException(msg);
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
					throw new FileObjectStorageFileUploadFileSystemException(msg);
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
					throw new FileObjectStorageFileUploadFileSystemException(msg);
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

		return saveUploadedFileToLocalDiskFile__Response;
	}

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
	 * @param httpRequest
	 * @param httpResponse
	 * @param scanFilenameToProcess
	 * @param uploadScanFileTempKey_Dir
	 * @throws Exception 
	 */
	private INTERNAL__saveUploadedFileToLocalDiskFile__Response saveUploadedScanFileToS3Object(

			HttpServletRequest httpRequest, 
			HttpServletResponse httpResponse,
			boolean gzipCompressContents
			) throws Exception {


		final long httpRequest_ContentLengthLong = httpRequest.getContentLengthLong();

		final String tempUpload_S3_ObjectName = get_TempUpload_S3_ObjectName();

		S3Client amazonS3_Client_ForOutput = null;

		String s3Bucket_MainStorage = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage();

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


		String s3Bucket_For_TempInputFileStorage = null;

		String api_Key_ForSubmittedFile = null;

		{  // Use Region from Config, otherwise SDK use from Environment Variable

			if ( StringUtils.isEmpty( ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_TemporaryInputFileStorage() ) ) {

				//  NO bucket for 'Input File' Storage so use the main storage bucket

				s3Bucket_For_TempInputFileStorage = s3Bucket_MainStorage;

			} else {

				s3Bucket_For_TempInputFileStorage = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_TemporaryInputFileStorage();
			}
		}

		S3Client amazonS3_Client_ForTempStorage = amazonS3_Client_ForOutput; //  Now both same Region so use same client

		try {

			//  First:  Copy incoming file contents to temp storage and compute Hash so have the S3 Object Key for final storage of the file contents


			///   !!!!!    Comment out use 'putObject' since would have to create an InputStream to compute the Hash key while reading the InputStream from the HTTP request

			//			if ( httpRequest_ContentLengthLong < YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MAXIMUM_PUT_OBJECT_OR_UPLOAD_PART_SIZE ) {
			//
			//				//  Uploaded File Size (ContentLength) is UNDER 5GB limit so can 'put' to S3 with single 'putObject' call, passing in the input stream from the HTTP Connection
			//
			//				try ( InputStream inputStreamFromPOSTLocal = httpRequest.getInputStream() ) {
			//
			//					PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			//							.bucket(s3Bucket_For_TempInputFileStorage)
			//							.key(tempUpload_S3_ObjectName)
			//							.contentLength( httpRequest_ContentLengthLong )
			//							.build();
			//
			//					RequestBody amazonS3_RequestBody = RequestBody.fromInputStream(inputStreamFromPOSTLocal, httpRequest_ContentLengthLong);
			//
			//					amazonS3_Client_ForOutput.putObject(putObjectRequest, amazonS3_RequestBody );
			//				}
			//
			//			} else {

			//  Uploaded File Size (ContentLength) is OVER 5GB limit so need to 'put' to S3 using Multipart

			String uploadId_S3Client_Client = null;

			try {
				try {
					// Step 1: Initialize.

					CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
							.bucket(s3Bucket_For_TempInputFileStorage)
							.key(tempUpload_S3_ObjectName)
							.build();

					CreateMultipartUploadResponse response = amazonS3_Client_ForOutput.createMultipartUpload(createMultipartUploadRequest);
					uploadId_S3Client_Client = response.uploadId();

				} catch ( Exception e ) {
					String msg = "Failed to initialize upload of Data File to S3.  "
							+ "s3Bucket_For_TempInputFileStorage: " + s3Bucket_For_TempInputFileStorage
							+ ", tempUpload_S3_ObjectName: " + tempUpload_S3_ObjectName;
					log.error( msg, e );
					throw e;
				}

				// Create a list of UploadPartResponse objects. You get one of these
				// for each part upload.
				List<CompletedPart> completedPart_List = new ArrayList<>( 10001 ); // Init to max possible size

				{
					//  Size of each part to send to S3
					int partSize_To_S3_Client = 
							( (int) (Math.ceil( httpRequest_ContentLengthLong / YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MAXIMUM_PART_NUMBER ) ) )
							+ 1000;  //  Add a bit to ensure not go over max number of parts

					if ( partSize_To_S3_Client < YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MINIMUM_PUT_OBJECT_SIZE_EXCEPT_LAST_PUT ) {
						//  Set to MIN size 
						partSize_To_S3_Client = YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MINIMUM_PUT_OBJECT_SIZE_EXCEPT_LAST_PUT;
					}

					try ( InputStream inputStream = httpRequest.getInputStream() ) {

						//  Compute the API key on the submitted file: 
						Hashes_Compute hashes_Compute = Hashes_Compute.getNewInstance();

						long bytesReadTotal = 0;

						UploadFile_Util__OutputStream__SendTo_AWS_S3 uploadFile_Util__OutputStream__SendTo_AWS_S3 = null;
						OutputStream outputStream = null;

						try {
							//  Create OutputStream object of class declared at bottom of this file.  This object will automatically send Upload Parts to S3
							uploadFile_Util__OutputStream__SendTo_AWS_S3 = 
									new UploadFile_Util__OutputStream__SendTo_AWS_S3(
											partSize_To_S3_Client, 
											amazonS3_Client_ForOutput,
											s3Bucket_For_TempInputFileStorage,
											tempUpload_S3_ObjectName, 
											uploadId_S3Client_Client, 
											completedPart_List);

							outputStream = uploadFile_Util__OutputStream__SendTo_AWS_S3;

							byte[] byteBuffer = new byte[ COPY_FILE_ARRAY_SIZE ];
							int bytesRead;

							while ( ( bytesRead = inputStream.read( byteBuffer ) ) > 0 ) {

								hashes_Compute.updateHashesComputing( byteBuffer, bytesRead );
								bytesReadTotal += bytesRead;

								outputStream.write( byteBuffer, 0, bytesRead );
							}

						} catch ( Throwable t ) {

							throw t;

						} finally {

							try {
								if ( outputStream != null ) {
									outputStream.close();
								}
							} catch ( Throwable t ) {

							}
							try {
								if ( uploadFile_Util__OutputStream__SendTo_AWS_S3 != null ) {
									uploadFile_Util__OutputStream__SendTo_AWS_S3.close();
								}
							} catch ( Throwable t ) {

							}
						}

						api_Key_ForSubmittedFile = 
								API_Key__Create_From_File_Hashes_And_Length.getNewInstance().
								getAPI_Key__Create_From_File_Hashes_And_Length(hashes_Compute, bytesReadTotal);
					}
				}
				

				try {
					// Step 3: Complete.

					// Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
					// parts and finish the multipart operation.
					CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
							.parts(completedPart_List)
							.build();

					CompleteMultipartUploadRequest completeMultipartUploadRequest =
							CompleteMultipartUploadRequest.builder()
							.bucket(s3Bucket_For_TempInputFileStorage)
							.key(tempUpload_S3_ObjectName)
							.uploadId(uploadId_S3Client_Client)
							.multipartUpload(completedMultipartUpload)
							.build();

					amazonS3_Client_ForOutput.completeMultipartUpload(completeMultipartUploadRequest);

				} catch ( Exception e ) {
					String msg = "Failed to upload part of TEMP Data File to S3.  "
							+ "bucketName: " + s3Bucket_For_TempInputFileStorage
							+ ", s3_Object_Key: " + tempUpload_S3_ObjectName;
					log.error( msg, e );
					throw e;
				}


			} catch ( Throwable t) {

				log.error( "Error while performing S3 MultipartUpload.  Will Now abort the multiplart upload by call amazonS3_Client.abortMultipartUpload(...)", t );

				try {
					AbortMultipartUploadRequest abortMultipartUploadRequest = 
							AbortMultipartUploadRequest
							.builder()
							.bucket(s3Bucket_For_TempInputFileStorage)
							.key(tempUpload_S3_ObjectName)
							.uploadId(uploadId_S3Client_Client)
							.build();

					amazonS3_Client_ForOutput.abortMultipartUpload( abortMultipartUploadRequest );

				} catch ( Throwable t2_Abort) {
					// Eat Exception
					log.error( "Error while performing S3 MultipartUpload.  Now Exception from abort the multiplart upload by call amazonS3_Client.abortMultipartUpload(...)", t2_Abort );
				}
				throw t;

			} finally {

			}

			//			}  //  if ( httpRequest_ContentLengthLong < YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MAXIMUM_PUT_OBJECT_OR_UPLOAD_PART_SIZE ) {

			//  Second:   Check if S3 Object is already exist

			Storage_Object_Name__Create__Result__MainName storage_Object_Name__Create__Result =
					Storage_Object_Name__Create.create_Main_Storage_Object_Name( api_Key_ForSubmittedFile );

			{
				//  Check if any of existing versions of object filename exists

				String storage_Object_Name_File_MainObjectname = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + storage_Object_Name__Create__Result.getMainObjectname();

				String storage_Object_Name_File_GzipObjectname = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + storage_Object_Name__Create__Result.getGzipObjectname();


				try {
					HeadObjectRequest headObjectRequest = 
							HeadObjectRequest
							.builder()
							.bucket(s3Bucket_MainStorage)
							.key( storage_Object_Name_File_MainObjectname )
							.build();

					//					HeadObjectResponse headObjectResponse = 
					amazonS3_Client_ForOutput.headObject(headObjectRequest);   //  Throws 'NoSuchKeyException' if key not found

					//  !!!!!!!!    Already exists so skip processing submitted file    !!!!!!

					INTERNAL__saveUploadedFileToLocalDiskFile__Response method_Response = new INTERNAL__saveUploadedFileToLocalDiskFile__Response();

					method_Response.API_Key = api_Key_ForSubmittedFile;

					//  method_Response.savedFile = uploadedFileOnDisk;

					method_Response.originalFileSize = httpRequest_ContentLengthLong;

					return method_Response;  // EARLY EXIT   !!!!!

				} catch ( NoSuchKeyException e ) {
					//  Eat Exception
				} catch ( Throwable t ) {
					//  Eat Exception
					log.error( "INFO ONLY: Processing Continues:: Failed to check AWS S3 bucket for existing object.  Bucket: " 
							+ s3Bucket_MainStorage
							+ ", Object Key: "
							+ storage_Object_Name_File_MainObjectname
							,
							t );
				}

				try {
					HeadObjectRequest headObjectRequest = 
							HeadObjectRequest
							.builder()
							.bucket(s3Bucket_MainStorage)
							.key( storage_Object_Name_File_GzipObjectname )
							.build();

					//					HeadObjectResponse headObjectResponse = 
					amazonS3_Client_ForOutput.headObject(headObjectRequest);  //  Throws 'NoSuchKeyException' if key not found

					//  !!!!!!!!    Already exists so skip processing submitted file    !!!!!!

					INTERNAL__saveUploadedFileToLocalDiskFile__Response method_Response = new INTERNAL__saveUploadedFileToLocalDiskFile__Response();

					method_Response.API_Key = api_Key_ForSubmittedFile;

					//  method_Response.savedFile = uploadedFileOnDisk;

					method_Response.originalFileSize = httpRequest_ContentLengthLong;

					return method_Response;  // EARLY EXIT   !!!!!

				} catch ( NoSuchKeyException e ) {
					//  Eat Exception
				} catch ( Throwable t ) {
					//  Eat Exception
					log.error( "INFO ONLY: Processing Continues:: Failed to check AWS S3 bucket for existing object.  Bucket: " 
							+ s3Bucket_MainStorage
							+ ", Object Key: "
							+ storage_Object_Name_File_GzipObjectname
							,
							t );
				}

			}

			//   Third:  Not already exist so copy object from Temp S3 bucket to Main S3 bucket

			//    'copyObject' method on client is restricted to the same 5GB limit as the 'putObject' method

			//        Also doing compression here so cannot use SDK Copy

			GetObjectRequest getObjectRequest = 
					GetObjectRequest
					.builder()
					.bucket(s3Bucket_For_TempInputFileStorage)
					.key(tempUpload_S3_ObjectName)
					.build();

			try ( 
					ResponseInputStream<GetObjectResponse> getObjectResponseMainObject_UsableAs_InputStream =
					amazonS3_Client_ForTempStorage.getObject(getObjectRequest) ) {

				//  GetObjectResponse getObjectResponse = getObjectResponseMainObject_UsableAs_InputStream.response();

				//				getObjectResponse.

				final String amazonS3_bucketName = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage();

				String s3_Object_Key = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + storage_Object_Name__Create__Result.getMainObjectname();

				if ( gzipCompressContents ) {

					s3_Object_Key = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + storage_Object_Name__Create__Result.getGzipObjectname();
				}


				if ( ( ! gzipCompressContents ) && httpRequest_ContentLengthLong < YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MAXIMUM_PUT_OBJECT_OR_UPLOAD_PART_SIZE ) {

					//  Uploaded File Size (ContentLength) is UNDER 5GB limit so can 'put' to S3 with single 'putObject' call, passing in the input stream from the HTTP Connection

					PutObjectRequest putObjectRequest = PutObjectRequest.builder()
							.bucket(amazonS3_bucketName)
							.key(s3_Object_Key)
							.contentLength( httpRequest_ContentLengthLong )
							.build();

					RequestBody amazonS3_RequestBody = RequestBody.fromInputStream(getObjectResponseMainObject_UsableAs_InputStream, httpRequest_ContentLengthLong);

					amazonS3_Client_ForOutput.putObject(putObjectRequest, amazonS3_RequestBody );

				} else {

					//  Uploaded File Size (ContentLength) is OVER 5GB limit so need to 'put' to S3 using Multipart

					String uploadId_S3Client_Client__Copy_Temp_Main = null;

					try {
						try {
							// Step 1: Initialize.

							CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
									.bucket(amazonS3_bucketName)
									.key(s3_Object_Key)
									.build();

							CreateMultipartUploadResponse response = amazonS3_Client_ForOutput.createMultipartUpload(createMultipartUploadRequest);
							uploadId_S3Client_Client__Copy_Temp_Main = response.uploadId();

						} catch ( Exception e ) {
							String msg = "Failed to initialize upload of Data File to S3.  "
									+ "bucketName: " + amazonS3_bucketName
									+ ", s3_Object_Key: " + s3_Object_Key;
							log.error( msg, e );
							throw e;
						}

						// Create a list of UploadPartResponse objects. You get one of these
						// for each part upload.
						List<CompletedPart> completedPart_List = new ArrayList<>( 10001 ); // Init to max possible size

						{
							//  Size of each part to send to S3
							int partSize_To_S3_Client = 
									( (int) (Math.ceil( httpRequest_ContentLengthLong / YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MAXIMUM_PART_NUMBER ) ) )
									+ 1000;  //  Add a bit to ensure not go over max number of parts

							if ( partSize_To_S3_Client < YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MINIMUM_PUT_OBJECT_SIZE_EXCEPT_LAST_PUT ) {
								//  Set to MIN size 
								partSize_To_S3_Client = YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MINIMUM_PUT_OBJECT_SIZE_EXCEPT_LAST_PUT;
							}

							UploadFile_Util__OutputStream__SendTo_AWS_S3 uploadFile_Util__OutputStream__SendTo_AWS_S3 = null;
							OutputStream outputStream = null;

							try {
								//  Create OutputStream object of class declared at bottom of this file.  This object will automatically send Upload Parts to S3
								uploadFile_Util__OutputStream__SendTo_AWS_S3 = 
										new UploadFile_Util__OutputStream__SendTo_AWS_S3(
												partSize_To_S3_Client, 
												amazonS3_Client_ForOutput,
												amazonS3_bucketName,
												s3_Object_Key, 
												uploadId_S3Client_Client__Copy_Temp_Main, 
												completedPart_List);

								outputStream = uploadFile_Util__OutputStream__SendTo_AWS_S3;

								if ( gzipCompressContents ) {

									//	Create GZIP Output Stream

									outputStream = new GZIPOutputStream(outputStream);
								}

								byte[] byteBuffer = new byte[ COPY_FILE_ARRAY_SIZE ];
								int bytesRead;

								while ( ( bytesRead = getObjectResponseMainObject_UsableAs_InputStream.read( byteBuffer ) ) > 0 ){

									outputStream.write( byteBuffer, 0, bytesRead );
								}

							} catch ( Throwable t ) {

								throw t;

							} finally {

								try {
									if ( outputStream != null ) {
										outputStream.close();
									}
								} catch ( Throwable t ) {

								}
								try {
									if ( uploadFile_Util__OutputStream__SendTo_AWS_S3 != null ) {
										uploadFile_Util__OutputStream__SendTo_AWS_S3.close();
									}
								} catch ( Throwable t ) {

								}
							}
						}

						if ( gzipCompressContents ) {

							//  First create GZIP "Meta Data" S3 object

							{
								MetaFileContents metaFileContents = new MetaFileContents();

								metaFileContents.setOriginalFileSize(httpRequest_ContentLengthLong);
								metaFileContents.setFileIsGZIP(gzipCompressContents);

								String metaFilename = Storage_Object_Name__Create.create_GZIP_MetaData_Storage_Object_Name(api_Key_ForSubmittedFile);

								String s3_Object_Key__metaFilename = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + metaFilename;

								ByteArrayOutputStream metaFile_Contents_ByteArrayOutputStream = new ByteArrayOutputStream( 100000 );

								MetaFileContents_WriteToOutputStream.writeToOutputStream(metaFileContents, metaFile_Contents_ByteArrayOutputStream);

								PutObjectRequest putObjectRequest = PutObjectRequest.builder()
										.bucket(amazonS3_bucketName)
										.key(s3_Object_Key__metaFilename)
										.contentLength( (long) metaFile_Contents_ByteArrayOutputStream.size() )
										.build();

								RequestBody amazonS3_RequestBody = RequestBody.fromBytes( metaFile_Contents_ByteArrayOutputStream.toByteArray() );

								amazonS3_Client_ForOutput.putObject(putObjectRequest, amazonS3_RequestBody );
							}
						}

						try {
							// Step 3: Complete.

							// Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
							// parts and finish the multipart operation.
							CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
									.parts(completedPart_List)
									.build();

							CompleteMultipartUploadRequest completeMultipartUploadRequest =
									CompleteMultipartUploadRequest.builder()
									.bucket(amazonS3_bucketName)
									.key(s3_Object_Key)
									.uploadId(uploadId_S3Client_Client__Copy_Temp_Main)
									.multipartUpload(completedMultipartUpload)
									.build();

							amazonS3_Client_ForOutput.completeMultipartUpload(completeMultipartUploadRequest);

						} catch ( Exception e ) {
							String msg = "Failed to upload part of Data File to S3.  "
									+ "bucketName: " + amazonS3_bucketName
									+ ", s3_Object_Key: " + s3_Object_Key;
							log.error( msg, e );
							throw e;
						}

					} catch ( Throwable t) {

						log.error( "Error while performing S3 MultipartUpload.  Will Now abort the multiplart upload by call amazonS3_Client.abortMultipartUpload(...)", t );

						try {
							AbortMultipartUploadRequest abortMultipartUploadRequest = 
									AbortMultipartUploadRequest
									.builder()
									.bucket(amazonS3_bucketName)
									.key(s3_Object_Key)
									.uploadId(uploadId_S3Client_Client__Copy_Temp_Main)
									.build();

							amazonS3_Client_ForOutput.abortMultipartUpload( abortMultipartUploadRequest );

						} catch ( Throwable t2_Abort) {
							// Eat Exception
							log.error( "Error while performing S3 MultipartUpload.  Now Exception from abort the multiplart upload by call amazonS3_Client.abortMultipartUpload(...)", t2_Abort );
						}
						
						throw t;

					} finally {

					}

				}


			} catch ( NoSuchKeyException e ) {

				throw e;
			}

		} finally {

			try {

				//  Delete the Temp S3 Object

				DeleteObjectRequest deleteObjectRequest = 
						DeleteObjectRequest
						.builder()
						.bucket(s3Bucket_For_TempInputFileStorage)
						.key( tempUpload_S3_ObjectName )
						.build();

				amazonS3_Client_ForTempStorage.deleteObject(deleteObjectRequest);

			} catch (Throwable T) {

			}
		}

		INTERNAL__saveUploadedFileToLocalDiskFile__Response method_Response = new INTERNAL__saveUploadedFileToLocalDiskFile__Response();

		method_Response.API_Key = api_Key_ForSubmittedFile;

		//  method_Response.savedFile = uploadedFileOnDisk;

		method_Response.originalFileSize = httpRequest_ContentLengthLong;

		return method_Response;  // EARLY EXIT   !!!!!
	}

	/**
	 * @return
	 */
	private String get_TempUpload_S3_ObjectName() {

		String currentDate_yyyymmdd = new SimpleDateFormat("yyyy_MM_dd").format( new Date() );

		String tempUpload_S3_ObjectName = 
				FileUploadConstants.UPLOAD_FILE_TEMP_BASE_DIR
				+ "/"
				+ FileUploadConstants.UPLOAD_FILE_TEMP_SUB_DIR_PREFIX 
				+ currentDate_yyyymmdd
				+ "_"
				+ System.currentTimeMillis()
				+ "_"
				+ System.nanoTime();

		return tempUpload_S3_ObjectName;
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
