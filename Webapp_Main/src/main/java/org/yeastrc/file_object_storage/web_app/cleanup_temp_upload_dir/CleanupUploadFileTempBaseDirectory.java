package org.yeastrc.file_object_storage.web_app.cleanup_temp_upload_dir;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yeastrc.file_object_storage.web_app.config.ConfigData_Directories_ProcessUploadInfo_InWorkDirectory;
import org.yeastrc.file_object_storage.web_app.constants_enums.FileUploadConstants;

//  import com.amazonaws.AmazonServiceException;
//  import com.amazonaws.services.s3.AmazonS3;
//  import com.amazonaws.services.s3.model.DeleteObjectRequest;

/**
 * Clean up directory 'upload_file_temp_base_dir'
 * 
 * Remove subdirectories and their contents that are over X days old
 *
 */
public class CleanupUploadFileTempBaseDirectory {

	private static final Logger log = LoggerFactory.getLogger( CleanupUploadFileTempBaseDirectory.class );
	
	private static final long MILLISECONDS_IN_HOUR = 1000 * 60 * 60;
	
	
	private static final long DEFAULT_CUTOFF_IN_DAYS = 1;
	
	private static final long DEFAULT_CUTOFF_IN_HOURS = 24 * DEFAULT_CUTOFF_IN_DAYS;

	 private static final long DEFAULT_CUTOFF_IN_MILLISECONDS = DEFAULT_CUTOFF_IN_HOURS * MILLISECONDS_IN_HOUR;
	
	//  TESTING
//	private static final long DEFAULT_CUTOFF_IN_MILLISECONDS = 5 * 1000; // 5 seconds
	
	
	 //  For S3 cleanup
	
	private static final int RETRY_DELETE_FILE_MAX = 10;
	private static final int RETRY_DELETE_FILE_DELAY = 500;  // milliseconds

	
	
	
	private static final CleanupUploadFileTempBaseDirectory instance = new CleanupUploadFileTempBaseDirectory();

	private volatile boolean shutdownRequested = false;
	
	private long lastTimeProcessed = 0;

	//  private constructor
	private CleanupUploadFileTempBaseDirectory() { }
	
	/**
	 * @return newly created instance
	 */
	public static CleanupUploadFileTempBaseDirectory getSingletonInstance() { 
		return instance; 
	}

	/**
	 * shutdown was received from the operating system.  This is called on a different thread.
	 */
	public synchronized void shutdown() {
		
		log.warn( "INFO: shutdown() called" );
		
		shutdownRequested = true;
	}

	/**
	 * @throws Exception
	 */
	public void cleanupUploadFileTempBaseDirectory() throws Exception {
		
		synchronized ( this ) {
			long currentTime = System.currentTimeMillis();
			
			//  Run at most Once an Hour
			if ( ( lastTimeProcessed + MILLISECONDS_IN_HOUR ) > currentTime ) {
				// Not been an hour so exit
				return;  // EARYL EXIT
			}
			
			lastTimeProcessed = currentTime;
		}

		File tempUploadBaseDirectory = null;
		
		{
			File rootBaseDirectoryFile =
				ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getFileObjectStorage_BaseDirectory();

			tempUploadBaseDirectory = new File( rootBaseDirectoryFile, FileUploadConstants.UPLOAD_FILE_TEMP_BASE_DIR );
		}

		if ( ! tempUploadBaseDirectory.exists() ) {
			//  Files Temp Uploaded Base Dir does not exist.  Nothing uploaded yet
			return;  //  EARLY EXIT
		}
		
		final long cutoffForDelete_InMilliseconds = DEFAULT_CUTOFF_IN_MILLISECONDS;
		final long currentTime = System.currentTimeMillis();
		
		final long directoryLastModifiedCutoff_InMilliseconds = currentTime - cutoffForDelete_InMilliseconds;

		//  Process all directories 

		File[] tempUploadBaseDirectoryContents = tempUploadBaseDirectory.listFiles();


		for ( File tempUpload_fileDir : tempUploadBaseDirectoryContents ) {
			
			if ( shutdownRequested ) {
				return;
			}

			long directoryLastModified = tempUpload_fileDir.lastModified();
			
			if ( directoryLastModified < directoryLastModifiedCutoff_InMilliseconds ) {
				
				// Delete Subdir since last modified is less than (before) cutoff for delete time offset from current time
				
				deleteTempUploadDirectory( tempUpload_fileDir );
			}
		}
	}
	
	/**
	 * @param tempUpload_fileDir
	 * @throws Exception 
	 */
	private void deleteTempUploadDirectory( File tempUpload_fileDir ) throws Exception {

		// AWS S3 Support commented out.  See file ZZ__AWS_S3_Support_CommentedOut.txt in GIT repo root.
		
//		if ( StringUtils.isNotEmpty( ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getS3Bucket() ) ) {
//
//			//  First delete uploaded scan file in S3 bucket
//			deleteUploadedScanFileIn_S3_Object();
//		}
		
		//  Delete contents of directory and directory
		
		File[] dirContents = tempUpload_fileDir.listFiles();
		for ( File dirEntry : dirContents ) {
			
			if ( shutdownRequested ) {
				return;
			}
			
			if ( ! dirEntry.isFile() ) {
				String msg = "Entry of tempUpload_scanFileDir is not a file.  Entry: "
						+ dirEntry.getAbsolutePath()
						+ ", tempUpload_scanFileDir: "
						+ tempUpload_fileDir.getAbsolutePath();
				log.error( msg );
			}
			if ( ! dirEntry.delete() ) {
				String msg = "Entry of tempUpload_scanFileDir Failed to delete.  Entry: "
						+ dirEntry.getAbsolutePath()
						+ ", tempUpload_scanFileDir: "
						+ tempUpload_fileDir.getAbsolutePath();
				log.error( msg );
			}
			
			{
				if ( log.isInfoEnabled() ) {
					
					Exception fakeException = new Exception( "FAKE Exception for Stack Trace");
					
					String msg = "INFO: Deleted Entry of tempUpload_scanFileDir.  Entry: "
							+ dirEntry.getAbsolutePath()
							+ ", tempUpload_scanFileDir: "
							+ tempUpload_fileDir.getAbsolutePath();
					log.info( msg, fakeException );
				}
			}
		}

		if ( shutdownRequested ) {
			return;
		}
		
		if ( ! tempUpload_fileDir.delete() ) {
			String msg = "tempUpload_fileDir Failed to delete. tempUpload_scanFileDir: "
					+ tempUpload_fileDir.getAbsolutePath();
			log.error( msg );
		}

		if ( shutdownRequested ) {
			return;
		}
		
		{
			if ( log.isInfoEnabled() ) {
				
				Exception fakeException = new Exception( "FAKE Exception for Stack Trace");
				
				String msg = "INFO: Deleted tempUpload_fileDir. tempUpload_fileDir: "
						+ tempUpload_fileDir.getAbsolutePath();
				log.info( msg, fakeException );
			}
		}

	}

//  AWS S3 Support commented out.  See file ZZ__AWS_S3_Support_CommentedOut.txt in GIT repo root.

//	/**
//	 * @throws Exception
//	 */
//	private void deleteUploadedScanFileIn_S3_Object() throws Exception {
//
//		File scanFile_S3_LocationFile = new File( UploadProcessing_InputScanfileS3InfoConstants.SCANFILE_S3_LOCATION_FILENAME );
//		
//		if ( ! scanFile_S3_LocationFile.exists() ) {
//			//  No file with info on scan file location in S3, so must be local file
//			return;  //  EARLY EXIT
//		}
//		
//		UploadScanfileS3Location uploadScanfileS3Location = null;
//		
//		JAXBContext jaxbContext = JAXBContext.newInstance( UploadScanfileS3Location.class ); 
//		
//		try ( InputStream is = new FileInputStream( scanFile_S3_LocationFile ) ) {
//
//			XMLInputFactory xmlInputFactory = Create_XMLInputFactory_XXE_Safe.create_XMLInputFactory_XXE_Safe();
//			XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader( new StreamSource( is ) );
//			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//			Object uploadScanfileS3LocationAsObject = unmarshaller.unmarshal( xmlStreamReader );
//
//			if ( uploadScanfileS3LocationAsObject instanceof UploadScanfileS3Location ) {
//				uploadScanfileS3Location = ( UploadScanfileS3Location ) uploadScanfileS3LocationAsObject;
//			} else {
//				String msg = "Failed to deserialize data in " + scanFile_S3_LocationFile.getAbsolutePath();
//				log.error( msg );
//				throw new SpectralStorageProcessingException(msg);
//			}
//		}
//
//		if ( uploadScanfileS3Location.isS3_infoFrom_RemoteSystem() ) {
//			//  Scan File S3 object came from external system so that system is responsible for deleting it
//			return;  //  EARLY EXIT
//		}
//		
//		String s3_bucketName = uploadScanfileS3Location.getS3_bucketName();
//		String s3_objectName = uploadScanfileS3Location.getS3_objectName();
//		
//		final AmazonS3 amazonS3client = CommonReader_File_And_S3_Holder.getSingletonInstance().getCommonReader_File_And_S3().getS3_Client();
//		
//		int retryDeleteScanFileCount = 0;
//		
//		while ( true ) {
//			try {
//				amazonS3client.deleteObject( new DeleteObjectRequest(
//						uploadScanfileS3Location.getS3_bucketName(), uploadScanfileS3Location.getS3_objectName() ));
//
//				break;  // Exit loop on success
//
//			} catch (AmazonServiceException e) {
//				String msg = "Error deleting scan file on S3. s3_bucketName: " + s3_bucketName
//						+ " s3_objectName: " + s3_objectName;
//				log.error( msg );
//
//				retryDeleteScanFileCount++;
//				if ( retryDeleteScanFileCount > RETRY_DELETE_FILE_MAX ) {
//					throw new SpectralStorageProcessingException(msg, e);
//				}
//			} catch (Exception e) {
//				String msg = "Error deleting scan file on S3. s3_bucketName: " + s3_bucketName
//						+ " s3_objectName: " + s3_objectName;
//				log.error( msg );
//
//				retryDeleteScanFileCount++;
//				if ( retryDeleteScanFileCount > RETRY_DELETE_FILE_MAX ) {
//					throw new SpectralStorageProcessingException(msg, e);
//				}
//			}
//			
//			Thread.sleep( RETRY_DELETE_FILE_DELAY );
//		}
//
//		// the same object name as scan file but with ".submitted" on the end
//		String objectKey_SubmittedObject = uploadScanfileS3Location.getS3_objectName()
//				+ ScanFileToProcessConstants.SCAN_FILE_TO_PROCESS_FILENAME_SUBMITTED_FILE_SUFFIX;
//		
//
//		int retryDeleteScanFileSubmittedCount = 0;
//
//		while ( true ) {
//			try {
//				amazonS3client.deleteObject( new DeleteObjectRequest(
//						uploadScanfileS3Location.getS3_bucketName(), objectKey_SubmittedObject ) );
//
//				break;  // Exit loop on success
//
//			} catch (AmazonServiceException e) {
//				String msg = "Error deleting scan file on S3. s3_bucketName: " + s3_bucketName
//						+ " s3_objectName: " + s3_objectName;
//				log.error( msg );
//
//				retryDeleteScanFileSubmittedCount++;
//				if ( retryDeleteScanFileSubmittedCount > RETRY_DELETE_FILE_MAX ) {
//					throw new SpectralStorageProcessingException(msg, e);
//				}
//			} catch (Exception e) {
//				String msg = "Error deleting scan file on S3. s3_bucketName: " + s3_bucketName
//						+ " s3_objectName: " + s3_objectName;
//				log.error( msg );
//
//				retryDeleteScanFileSubmittedCount++;
//				if ( retryDeleteScanFileSubmittedCount > RETRY_DELETE_FILE_MAX ) {
//					throw new SpectralStorageProcessingException(msg, e);
//				}
//			}
//
//			Thread.sleep( RETRY_DELETE_FILE_DELAY );
//		}
//	}
	
	
}
