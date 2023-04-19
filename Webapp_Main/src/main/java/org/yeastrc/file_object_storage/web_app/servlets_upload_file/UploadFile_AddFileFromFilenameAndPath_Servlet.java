package org.yeastrc.file_object_storage.web_app.servlets_upload_file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
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
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageBadRequestToServletException;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageDeserializeRequestException;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageFileUploadFileSystemException;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageWebappInternalException;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.TempDir_OnLocalFileSystem_CreateToUploadFileTo;
import org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem.StorageDir_OnLocalFileSystem_CreateToStoreFileIn.StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM;
import org.yeastrc.file_object_storage.web_app.hashes_compute.Hashes_Compute;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents;
import org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents.MetaFileContents_WriteToOutputStream;
import org.yeastrc.file_object_storage.web_app.servlets_common.GetRequestObjectFromInputStream;
import org.yeastrc.file_object_storage.web_app.servlets_common.Get_ServletResultDataFormat_FromServletInitParam;
import org.yeastrc.file_object_storage.web_app.servlets_common.WriteResponseObjectToOutputStream;
import org.yeastrc.file_object_storage.web_app.servlets_common.WriteResponseStringToOutputStream;
import org.yeastrc.file_object_storage.web_app.servlets_upload_file__utils.UploadFile_Util__OutputStream__SendTo_AWS_S3;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_AddFileFromFilenameAndPath_Request;
import org.yeastrc.file_object_storage.web_app.shared_server_client.webservice_request_response.main.UploadFile_AddFileFromFilenameAndPath_Response;
import org.yeastrc.file_object_storage.web_app.utils.API_Key__Create_From_File_Hashes_And_Length;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create;
import org.yeastrc.file_object_storage.web_app.utils.Storage_Object_Name__Create.Storage_Object_Name__Create__Result__MainName;

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
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


/**
 * Accept a filename with it's path
 * 
 *  This Servlet should be considered a webservice as it returns JSON or XML
 */
public class UploadFile_AddFileFromFilenameAndPath_Servlet extends HttpServlet {

	private static final Logger log = LoggerFactory.getLogger( UploadFile_AddFileFromFilenameAndPath_Servlet.class );

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
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		log.info( "INFO:  doPost called");
		

		UploadFile_AddFileFromFilenameAndPath_Request uploadFile_AddFileFromFilenameAndPath_Request = null;

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
				uploadFile_AddFileFromFilenameAndPath_Request = (UploadFile_AddFileFromFilenameAndPath_Request) requestObj;
			} catch (Exception e) {
				String msg = "Failed to cast requestObj to UploadFile_AddFileFromFilenameAndPath_Request";
				log.error( msg, e );
				throw new FileObjectStorageBadRequestToServletException( e );
			}
		} catch (FileObjectStorageBadRequestToServletException e) {

			response.setStatus( HttpServletResponse.SC_BAD_REQUEST /* 400  */ );

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
		
//		log.warn("INFO:: uploadFile_AddFileFromFilenameAndPath_Request: " + uploadFile_AddFileFromFilenameAndPath_Request );
		
		try {
		
			UploadFile_AddFileFromFilenameAndPath_Response webserviceResponse_Object = 
					processRequest( uploadFile_AddFileFromFilenameAndPath_Request, request, response );
			
			if ( webserviceResponse_Object != null ) {

				WriteResponseObjectToOutputStream.getSingletonInstance()
				.writeResponseObjectToOutputStream( webserviceResponse_Object, servetResponseFormat, response );
			}
			
		} catch (Throwable e) {

			String msg = "Failed to process request";
			log.error( msg, e );
			response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR /* 500  */ );
			
		} finally {
			
			
		}
	}
	
	/**
	 * @param uploadFile_AddFileFromFilenameAndPath_Request
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	private UploadFile_AddFileFromFilenameAndPath_Response processRequest( 
			
			UploadFile_AddFileFromFilenameAndPath_Request uploadFile_AddFileFromFilenameAndPath_Request,
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		try {
			UploadFile_AddFileFromFilenameAndPath_Response webserviceResponse = new UploadFile_AddFileFromFilenameAndPath_Response();

			ConfigData_Directories_ProcessUploadInfo_InWorkDirectory configData_Directories_ProcessUploadInfo_InWorkDirectory = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance();
			
			if ( configData_Directories_ProcessUploadInfo_InWorkDirectory.getSubmittedFilePathRestrictions() == null
					|| configData_Directories_ProcessUploadInfo_InWorkDirectory.getSubmittedFilePathRestrictions().isEmpty() ) {
				//  No Configuration for allowed Paths so reject request
				String msg = "No Configuration for allowed Paths so reject request. configData_Directories_ProcessUploadInfo_InWorkDirectory.getSubmittedFilePathRestrictions() is null or empty.  "; 
				log.warn( msg );
				
				webserviceResponse.setUploadFileWithPath_FilePathsAllowedNotConfigured(true);
				// Set in case caller not checking the first thing set true
				webserviceResponse.setUploadFileWithPath_FilePathNotAllowed(true);

				return webserviceResponse;  // EARLY EXIT
			}
			
			//  Values from the webservice request
			String submitted_filenameWithPathString = uploadFile_AddFileFromFilenameAndPath_Request.getFilenameWithPath();
			
			boolean gzipCompressTheStoredFile = uploadFile_AddFileFromFilenameAndPath_Request.isGzipCompressTheStoredFile();
			
			BigInteger submitted_fileSize = uploadFile_AddFileFromFilenameAndPath_Request.getFileSize();
			long submitted_fileSize_AsLong = 0; 

			if ( StringUtils.isEmpty( submitted_filenameWithPathString ) ) {
				String msg = "request is missing filenameWithPath ";
				log.warn( msg );
				throw new FileObjectStorageBadRequestToServletException( msg );
			}

			if ( submitted_fileSize == null ) {
				String msg = "request is missing fileSize ";
				log.warn( msg );
				throw new FileObjectStorageBadRequestToServletException( msg );
			}
			
			try {
				submitted_fileSize_AsLong = submitted_fileSize.longValueExact();
			} catch ( Exception e ) {
				String msg = "fileSize in request is outside the value of a Java numeric type 'long': " + submitted_fileSize.toString();
				log.warn( msg, e );
				String msgReturned = "fileSize in request is outside the range of allowed values";
				throw new FileObjectStorageBadRequestToServletException( msgReturned );
			}

			//  Process Submitted Filename With Path
			
			File submittedFileWithPath_FileObject = new File( submitted_filenameWithPathString );
			if ( ! submittedFileWithPath_FileObject.exists() ) {
				//  File with path not found
				
				log.warn("Provided filenameWithPath does not exist: " + submitted_filenameWithPathString );

				UploadFile_AddFileFromFilenameAndPath_Response uploadResponse = new UploadFile_AddFileFromFilenameAndPath_Response();
				uploadResponse.setStatusSuccess(false);
				uploadResponse.setUploadFileWithPath_FileNotFound(true);

				return webserviceResponse;  // EARLY EXIT
			}
			
			{ // Validate that File Path Submitted is allowed
				String fileWithPathCanonical = submittedFileWithPath_FileObject.getCanonicalPath();
				
				boolean matchedAPathRestrictionToStartOfFilenameWithPath = false;
				
				for ( String pathStartRestrictionEntry : configData_Directories_ProcessUploadInfo_InWorkDirectory.getSubmittedFilePathRestrictions() ) {
					if ( fileWithPathCanonical.startsWith( pathStartRestrictionEntry ) ) {
						matchedAPathRestrictionToStartOfFilenameWithPath = true;
						break;
					}
				}
				
				if ( ! matchedAPathRestrictionToStartOfFilenameWithPath ) {
					//  Start of File with path not found not found in PathsRestrictions
					
					log.warn("Submitted filenameWithPath does not start with one of the configured allowed paths.  Submitted filenameWithPath (enclosed with '|'): |" 
							+ submitted_filenameWithPathString
							+ "|, submitted_filenameWithPath as Java .getCanonicalPath() string: (enclosed with '|'): |"
							+ fileWithPathCanonical
							+ "|, configured allowed paths (comma separated): " 
							+ StringUtils.join( configData_Directories_ProcessUploadInfo_InWorkDirectory.getSubmittedFilePathRestrictions(), "," ) );
	
					UploadFile_AddFileFromFilenameAndPath_Response uploadResponse = new UploadFile_AddFileFromFilenameAndPath_Response();
					uploadResponse.setStatusSuccess(false);
					uploadResponse.setUploadFileWithPath_FilePathNotAllowed( true );

					return webserviceResponse;  // EARLY EXIT
				}
			}

			
			long submittedFileWithPath__Length = submittedFileWithPath_FileObject.length();
			
			if ( submitted_fileSize_AsLong != submittedFileWithPath__Length ) {
				//  Filesize of actual file does not match file size in the request
				
				String msg = "fileSize in request does not match size of file submitted. File Submitted: "
						+ submitted_filenameWithPathString
						+ ", File Size Submitted: "
						+ submitted_fileSize_AsLong
						+ ", Size of file submitted: "
						+ submittedFileWithPath__Length
						+ ", submitted filenameWithPath: "
						+ submitted_filenameWithPathString ;
				log.warn( msg );

				UploadFile_AddFileFromFilenameAndPath_Response uploadResponse = new UploadFile_AddFileFromFilenameAndPath_Response();
				uploadResponse.setStatusSuccess(false);
				uploadResponse.setUploadFileWithPath_NotMatch_SubmittedFileSize(true);

				return webserviceResponse;  // EARLY EXIT
			}
			
		       // file upload size limit
			if ( submittedFileWithPath__Length > FileUploadConstants.MAX_FILE_UPLOAD_SIZE ) {

				log.warn( " File Exceeds allowed size.  File size: " + submittedFileWithPath__Length
						+ ", max upload file size: " + FileUploadConstants.MAX_FILE_UPLOAD_SIZE 
						+ ", max upload file size (formatted): " + FileUploadConstants.MAX_FILE_UPLOAD_SIZE_FORMATTED
						+ ", submitted filenameWithPath: " 
						+ submitted_filenameWithPathString );
				
				UploadFile_AddFileFromFilenameAndPath_Response uploadResponse = new UploadFile_AddFileFromFilenameAndPath_Response();
				uploadResponse.setStatusSuccess(false);
				uploadResponse.setFileSizeLimitExceeded(true);
				uploadResponse.setMaxSize( FileUploadConstants.MAX_FILE_UPLOAD_SIZE );
				uploadResponse.setMaxSizeFormatted( FileUploadConstants.MAX_FILE_UPLOAD_SIZE_FORMATTED );

				return webserviceResponse;  // EARLY EXIT
			}
			
			////////////////////
			
			String api_Key_ForSubmittedFile = null;
			
			{
	
				//   First check if submitted file already exists
				
				//  Compute the API key on the submitted file: 
				Hashes_Compute hashes_Compute = Hashes_Compute.getNewInstance();
						
				//  Read the file to compute the hash
				InputStream inputStream = null;
				
				long bytesReadTotal = 0;
				
				try {
					inputStream = new FileInputStream(submittedFileWithPath_FileObject);
	
					byte[] byteBuffer = new byte[ COPY_FILE_ARRAY_SIZE ];
					int bytesRead;
	
					while ( ( bytesRead = inputStream.read( byteBuffer ) ) > 0 ){
						
						hashes_Compute.updateHashesComputing( byteBuffer, bytesRead );
						
						bytesReadTotal += bytesRead;
					}
				} catch ( Exception e ) {
					
					String msg = "Failed reading submitted file: " + submittedFileWithPath_FileObject.getAbsolutePath();
					log.error(msg, e);
	
					throw new FileObjectStorageWebappInternalException(msg);
				} finally {

					try {
						if ( inputStream != null ) {
							inputStream.close();
						}
					} catch(Exception e){ 
						String msg = "Failed closing input stream for file: " + submittedFileWithPath_FileObject.getAbsolutePath();
						log.error(msg, e);
						throw new FileObjectStorageWebappInternalException(msg);
					}

				}
				
	
				api_Key_ForSubmittedFile = 
						API_Key__Create_From_File_Hashes_And_Length.getNewInstance().
						getAPI_Key__Create_From_File_Hashes_And_Length(hashes_Compute, bytesReadTotal);
			}
			


			if ( StringUtils.isNotEmpty( ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage() ) ) {
				
				this._saveTo_AWS_S3(

						submitted_fileSize_AsLong,
						gzipCompressTheStoredFile,
						
						api_Key_ForSubmittedFile,
						
						submittedFileWithPath_FileObject
						);;
				
				
			} else {
				//  Save to Local File System
				
				this._saveTo_LocalFile(

						submitted_fileSize_AsLong,
						gzipCompressTheStoredFile,
						
						api_Key_ForSubmittedFile,
						
						submittedFileWithPath_FileObject
						);
			}

			webserviceResponse.setStatusSuccess(true);
			
			webserviceResponse.setApiKey_Assigned(api_Key_ForSubmittedFile);


			return webserviceResponse;
			
		} catch (FileObjectStorageBadRequestToServletException e) {

			response.setStatus( HttpServletResponse.SC_BAD_REQUEST /* 400  */ );

			if ( StringUtils.isNotEmpty( e.getMessage() ) ) {
				WriteResponseStringToOutputStream.getInstance()
				.writeResponseStringToOutputStream( e.getMessage(), response);
			}
			
			return null;
		}
	}
	
	/**
	 * Save to Local File System
	 * 
	 * @param submitted_fileSize_AsLong
	 * @param gzipCompressTheStoredFile
	 * @param api_Key_ForSubmittedFile
	 * @param submittedFileWithPath_FileObject
	 * @throws Exception
	 */
	private void _saveTo_LocalFile(
			

			long submitted_fileSize_AsLong,
			boolean gzipCompressTheStoredFile,
			
			String api_Key_ForSubmittedFile,
			
			File submittedFileWithPath_FileObject
			) throws Exception {

		
		Storage_Object_Name__Create__Result__MainName storage_Object_Name__Create__Result =
				Storage_Object_Name__Create.create_Main_Storage_Object_Name( api_Key_ForSubmittedFile );
		
		String[] storage_Object_Name_AllPossible = { storage_Object_Name__Create__Result.getMainObjectname(), storage_Object_Name__Create__Result.getGzipObjectname() };

		File mainStorage_SubDirBasedOnName =
				StorageDir_OnLocalFileSystem_CreateToStoreFileIn.getInstance().createToStoreFileIn(
						storage_Object_Name_AllPossible,
						StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM.YES );

		{
			//  Check if any of existing versions of object filename exists

			File storage_Object_Name_File_MainObjectname = new File( mainStorage_SubDirBasedOnName, storage_Object_Name__Create__Result.getMainObjectname() );

			File storage_Object_Name_File_GzipObjectname = new File( mainStorage_SubDirBasedOnName, storage_Object_Name__Create__Result.getGzipObjectname() );

			if ( storage_Object_Name_File_MainObjectname.exists() || storage_Object_Name_File_GzipObjectname.exists() ) {



				//  !!!!!!!!    Already exists so skip processing submitted file    !!!!!!

				return;  // EARLY EXIT   !!!!!
			}
		}
		
		//  Copy file to local uploadFileTempDir

		File uploadFileTempDir = TempDir_OnLocalFileSystem_CreateToUploadFileTo.getInstance().createTempDirToUploadFileTo();
		
		//  File to write the copy of the file data to:
		File localCopyOfFileOnDisk_TempLocation = new File( uploadFileTempDir, STANDARD_UPLOAD_FILE_CONTENTS_TEMP_FILENAME );
				
		//  Copy the file to the local copy
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		OutputStream outStream = null;
		
		try {
			inputStream = new FileInputStream(submittedFileWithPath_FileObject);
			fileOutputStream = new FileOutputStream( localCopyOfFileOnDisk_TempLocation ); 
			
			outStream = fileOutputStream;
			
			if ( gzipCompressTheStoredFile ) {
				outStream = new GZIPOutputStream( fileOutputStream );
			}

			byte[] byteBuffer = new byte[ COPY_FILE_ARRAY_SIZE ];
			int bytesRead;

			while ( ( bytesRead = inputStream.read( byteBuffer ) ) > 0 ){
				
				outStream.write( byteBuffer, 0, bytesRead );
			}
		} catch ( Exception e ) {
			
			String msg = "Failed writing request to file: " + localCopyOfFileOnDisk_TempLocation.getAbsolutePath();
			log.error(msg, e);

			throw new FileObjectStorageWebappInternalException(msg);
		} finally {

			boolean closeOutputStreamFail = false;
			try {
				if ( outStream != null ) {
					outStream.close();
				} else if ( fileOutputStream != null ) {
					fileOutputStream.close();  //  Close since not close outStream
				}
			} catch(Exception e){
				closeOutputStreamFail = true;

				String msg = "Failed closing file: " + localCopyOfFileOnDisk_TempLocation.getAbsolutePath();
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
					String msg = "Failed closing input stream for file: " + localCopyOfFileOnDisk_TempLocation.getAbsolutePath();
					log.error(msg, e);
					throw new FileObjectStorageWebappInternalException(msg);
				}
			}
		}
		
		//  Then move local copy of file to final perm storage
		

		File rootBaseDir = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getFileObjectStorage_BaseDirectory();
		
		File mainStorage_BaseDir = new File( rootBaseDir, FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR );
		if ( ! mainStorage_BaseDir.exists() ) {
//			boolean mkdirResult = 
			mainStorage_BaseDir.mkdir();
		}
		if ( ! mainStorage_BaseDir.exists() ) {
			String msg = "mainStorage_BaseDir does not exist after testing for it and attempting to create it.  mainStorage_BaseDir: " 
					+ mainStorage_BaseDir.getAbsolutePath();
			log.error( msg );
			throw new FileObjectStorageFileUploadFileSystemException(msg);
		}
		
		boolean storage_Object_Name_File_AlreadyExists = false;
		
		{
			//  Check if any of existing versions of object filename exists

			{
				//  Check if any of existing versions of object filename exists
				
				File storage_Object_Name_File_MainObjectname = new File( mainStorage_SubDirBasedOnName, storage_Object_Name__Create__Result.getMainObjectname() );

				File storage_Object_Name_File_GzipObjectname = new File( mainStorage_SubDirBasedOnName, storage_Object_Name__Create__Result.getGzipObjectname() );

				if ( storage_Object_Name_File_MainObjectname.exists() || storage_Object_Name_File_GzipObjectname.exists() ) {

					//  Already exists so skip processing submitted file
	
					//   Return the computed api_Key

					return;  // EARLY EXIT
				}
			}
		}
		
		if ( storage_Object_Name_File_AlreadyExists ) {
			//  Already exists so delete the uploaded file

			if ( ! localCopyOfFileOnDisk_TempLocation.delete() ) {

				String msg = "Fail to delete temp uploaded data file.  Temp uploaded data File: " 
						+ localCopyOfFileOnDisk_TempLocation.getAbsolutePath();
				log.error( msg );
				throw new FileObjectStorageFileUploadFileSystemException(msg);
			}
		} else {

			//  NOT already exists so move uploaded file to perm keep subdir and perm keep filename 

			if ( gzipCompressTheStoredFile ) {
				
				//  GZIP compressed incoming contents

				//  First create GZIP "Meta Data" file and then move it
				
				//  First create GZIP "Meta Data" file and then move it
				
				{
					MetaFileContents metaFileContents = new MetaFileContents();
					
					metaFileContents.setOriginalFileSize(submitted_fileSize_AsLong);
					metaFileContents.setFileIsGZIP(gzipCompressTheStoredFile);
					
					String metaFilename = Storage_Object_Name__Create.create_GZIP_MetaData_Storage_Object_Name(api_Key_ForSubmittedFile);
					
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

				if ( ! localCopyOfFileOnDisk_TempLocation.renameTo(storage_Object_Name_File) ) {

					String msg = "Failed to rename data file to final storage name and path.  From File: " 
							+ localCopyOfFileOnDisk_TempLocation.getAbsolutePath()
							+ ", To file: "
							+ storage_Object_Name_File.getAbsolutePath();
					log.error( msg );
					throw new FileObjectStorageFileUploadFileSystemException(msg);
				}
				
			} else {
				
				//  NOT GZIP 

				String storage_Object_Name_ToUse = storage_Object_Name__Create__Result.getMainObjectname();
				
				File storage_Object_Name_File = new File( mainStorage_SubDirBasedOnName, storage_Object_Name_ToUse );

				if ( ! localCopyOfFileOnDisk_TempLocation.renameTo(storage_Object_Name_File) ) {

					String msg = "Failed to rename data file to final storage name and path.  From File: " 
							+ localCopyOfFileOnDisk_TempLocation.getAbsolutePath()
							+ ", To file: "
							+ storage_Object_Name_File.getAbsolutePath();
					log.error( msg );
					throw new FileObjectStorageFileUploadFileSystemException(msg);
				}
			}
		}
		
		{
			File fileRename__fromFile_ContainingFolder = localCopyOfFileOnDisk_TempLocation.getParentFile();
			
//			log.warn( "INFO::  Deleting  localCopyOfFileOnDisk.getParentFile():  " + localCopyOfFileOnDisk_TempLocation.getParentFile().getAbsolutePath() );

//			log.warn( "INFO:: SKIPPING Deleting  localCopyOfFileOnDisk.getParentFile():  " + localCopyOfFileOnDisk_TempLocation.getParentFile().getAbsolutePath() );
			
			if ( ! fileRename__fromFile_ContainingFolder.delete() ) {
				log.warn( "Failed to delete temp uploaded file temp containing dir: " + fileRename__fromFile_ContainingFolder.getAbsolutePath() );
			}
		}
	}

	/**
	 * Save to AWS_S3
	 * 
	 * @param submitted_fileSize_AsLong
	 * @param gzipCompressTheStoredFile
	 * @param api_Key_ForSubmittedFile
	 * @param submittedFileWithPath_FileObject
	 * @throws Exception
	 */
	private void _saveTo_AWS_S3(

			long submitted_fileSize_AsLong,
			boolean gzipCompressTheStoredFile,
			
			String api_Key_ForSubmittedFile,
			
			File submittedFileWithPath_FileObject
			) throws Exception {

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
						.bucket(ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage())
						.key( storage_Object_Name_File_MainObjectname )
						.build();

//				HeadObjectResponse headObjectResponse = 
				amazonS3_Client_ForOutput.headObject(headObjectRequest);   //  Throws 'NoSuchKeyException' if key not found
				
				//  !!!!!!!!    Already exists so skip processing submitted file    !!!!!!

				return;  // EARLY EXIT   !!!!!
				
			} catch ( NoSuchKeyException e ) {
				//  Eat Exception
			} catch ( Throwable t ) {
				//  Eat Exception
				log.error( "INFO ONLY: Processing Continues:: Failed to check AWS S3 bucket for existing object.  Bucket: " 
						+ ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage()
						+ ", Object Key: "
						+ storage_Object_Name_File_MainObjectname
						,
						t );
			}
			
			try {
				HeadObjectRequest headObjectRequest = 
						HeadObjectRequest
						.builder()
						.bucket(ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage())
						.key( storage_Object_Name_File_GzipObjectname )
						.build();

//				HeadObjectResponse headObjectResponse = 
				amazonS3_Client_ForOutput.headObject(headObjectRequest);  //  Throws 'NoSuchKeyException' if key not found

				//  !!!!!!!!    Already exists so skip processing submitted file    !!!!!!

				return;  // EARLY EXIT   !!!!!
				
			} catch ( NoSuchKeyException e ) {
				//  Eat Exception
			} catch ( Throwable t ) {
				//  Eat Exception
				log.error( "INFO ONLY: Processing Continues:: Failed to check AWS S3 bucket for existing object.  Bucket: " 
						+ ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage()
						+ ", Object Key: "
						+ storage_Object_Name_File_GzipObjectname
						,
						t );
			}
			
		}
		
		final String amazonS3_bucketName = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket_MainStorage();
		
		String s3_Object_Key = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + storage_Object_Name__Create__Result.getMainObjectname();
		
		if ( gzipCompressTheStoredFile ) {
		
			s3_Object_Key = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR + "/" + storage_Object_Name__Create__Result.getGzipObjectname();
		}
		

		if ( ( ! gzipCompressTheStoredFile ) && submittedFileWithPath_FileObject.length() < YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MAXIMUM_PUT_OBJECT_OR_UPLOAD_PART_SIZE ) {
		
			//  Uploaded File Size (ContentLength) is UNDER 5GB limit so can 'put' to S3 with single 'putObject' call, passing in the input stream from the HTTP Connection

			try ( InputStream inputStreamFromPOSTLocal = new FileInputStream(submittedFileWithPath_FileObject) ) {

	            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
	                .bucket(amazonS3_bucketName)
	                .key(s3_Object_Key)
	                .contentLength( submittedFileWithPath_FileObject.length() )
	                .build();
	            
	            RequestBody amazonS3_RequestBody = RequestBody.fromInputStream(inputStreamFromPOSTLocal, submittedFileWithPath_FileObject.length());

				amazonS3_Client_ForOutput.putObject(putObjectRequest, amazonS3_RequestBody );
			}

		} else {

			//  Uploaded File Size (ContentLength) is OVER 5GB limit so need to 'put' to S3 using Multipart

			String uploadId_S3Client_Client = null;

			try {
				try {
					// Step 1: Initialize.
					
					CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
			                .bucket(amazonS3_bucketName)
			                .key(s3_Object_Key)
			                .build();

			        CreateMultipartUploadResponse response = amazonS3_Client_ForOutput.createMultipartUpload(createMultipartUploadRequest);
			        uploadId_S3Client_Client = response.uploadId();

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
					final long submittedFileWithPath_FileObject__Length = submittedFileWithPath_FileObject.length();
					
					//  Size of each part to send to S3
					int partSize_To_S3_Client = 
							( (int) (Math.ceil( submittedFileWithPath_FileObject__Length / YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MAXIMUM_PART_NUMBER ) ) )
									+ 1000;  //  Add a bit to ensure not go over max number of parts
					
					if ( partSize_To_S3_Client < YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MINIMUM_PUT_OBJECT_SIZE_EXCEPT_LAST_PUT ) {
						//  Set to MIN size 
						partSize_To_S3_Client = YRC_FileObjectStorage__AWS_S3_Constants.AWS_S3_MULTIPART_UPLOAD_UPLOAD_PART__MINIMUM_PUT_OBJECT_SIZE_EXCEPT_LAST_PUT;
					}

					try ( InputStream inputStream = new FileInputStream(submittedFileWithPath_FileObject) ) {

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
											uploadId_S3Client_Client, 
											completedPart_List);

							outputStream = uploadFile_Util__OutputStream__SendTo_AWS_S3;

							if ( gzipCompressTheStoredFile ) {

								//	Create GZIP Output Stream

								outputStream = new GZIPOutputStream(outputStream);
							}

							byte[] byteBuffer = new byte[ COPY_FILE_ARRAY_SIZE ];
							int bytesRead;

							while ( ( bytesRead = inputStream.read( byteBuffer ) ) > 0 ){
								
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
				}

				if ( gzipCompressTheStoredFile ) {

					//  First create GZIP "Meta Data" S3 object
					
					{
						MetaFileContents metaFileContents = new MetaFileContents();
						
						metaFileContents.setOriginalFileSize(submitted_fileSize_AsLong);
						metaFileContents.setFileIsGZIP(gzipCompressTheStoredFile);
						
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
			        		.uploadId(uploadId_S3Client_Client)
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
			  
		}
			
	}
	
}
