package org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem;

import java.io.File;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.yeastrc.file_object_storage.web_app.config.ConfigData_Directories_ProcessUploadInfo_InWorkDirectory;
import org.yeastrc.file_object_storage.web_app.constants_enums.FileUploadConstants;
import org.yeastrc.file_object_storage.web_app.exceptions.FileObjectStorageFileUploadFileSystemException;
import org.slf4j.Logger;

/**
 * Return the Storage dir for specific Storage filename
 *
 */
public class StorageDir_OnLocalFileSystem_CreateToStoreFileIn {

	private static final Logger log = LoggerFactory.getLogger(StorageDir_OnLocalFileSystem_CreateToStoreFileIn.class);
	
	public enum StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM { YES, NO }

	//  private constructor
	private StorageDir_OnLocalFileSystem_CreateToStoreFileIn() { }
	
	/**
	 * @return newly created instance
	 */
	public static StorageDir_OnLocalFileSystem_CreateToStoreFileIn getInstance() { 
		return new StorageDir_OnLocalFileSystem_CreateToStoreFileIn(); 
	}
	
	/**
	 * @param filename
	 * @param createSubdirIfNotExists_ENUM - Flag of create subdir if not exist
	 * @return - null if flag is not create if not exist and not exist
	 * @throws IOException
	 * @throws FileObjectStorageFileUploadFileSystemException 
	 */
	public File createToStoreFileIn( 
			
			String[] filenames_All,
			StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM createSubdirIfNotExists_ENUM  
			
			) throws IOException, FileObjectStorageFileUploadFileSystemException {
		
		String filenameFirstStart = null;
		
		{
			final int FILENAME_MIN_LENGTH = 7;
			
			for ( String filenames_All_Entry : filenames_All) {
		
				if ( filenames_All_Entry.length() < FILENAME_MIN_LENGTH ) {
					throw new IllegalArgumentException( "filename length is < " + FILENAME_MIN_LENGTH );
				}
				
				//  Ensure all 'Start' is same
				
				if ( filenameFirstStart == null ) {
					filenameFirstStart = filenames_All_Entry.substring(0, FILENAME_MIN_LENGTH);
				} else {
					String filenameStart_Current = filenames_All_Entry.substring(0, FILENAME_MIN_LENGTH);
					if ( ! filenameFirstStart.equals( filenameStart_Current ) ) {
						throw new IllegalArgumentException( "filenames not have same starting string.  filenameFirstStart: "
								+ filenameFirstStart + ", filenameStart_Current: " + filenameStart_Current  );
					}
				}
			}
		}
		
		File rootBaseDir = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getFileObjectStorage_BaseDirectory();
		
		//  Get the File object for the Base Subdir used to store the files permanently 
		String fileStorageDirString = FileUploadConstants.FILE_OBJECT_STORAGE__MAIN_STORAGE_BASE_DIR;
		File fileStorageDir = new File( rootBaseDir, fileStorageDirString );
		
		if ( ! fileStorageDir.exists() ) {
			
			if ( createSubdirIfNotExists_ENUM == StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM.NO ) {
				//  Subdir NOT found and Flag is NO so return null
				return null; // EARLY RETURN
			}
			
//			boolean mkdirResult = 
			fileStorageDir.mkdir();
		}
		if ( ! fileStorageDir.exists() ) {
			String msg = "fileStorageDir does not exist after testing for it and attempting to create it.  fileStorageDir: " 
					+ fileStorageDir.getAbsolutePath();
			log.error( msg );
			throw new FileObjectStorageFileUploadFileSystemException(msg);
		}
		
		//  First Subdir with first 2 characters of filename
		
		File firstSubDir = null;
		
		{
			String firstSubDir_String = filenameFirstStart.substring(0, 2);
			firstSubDir = new File( fileStorageDir, firstSubDir_String );

			if ( ! firstSubDir.exists() ) {
				
				if ( createSubdirIfNotExists_ENUM == StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM.NO ) {
					//  Subdir NOT found and Flag is NO so return null
					return null; // EARLY RETURN
				}
				
//				boolean mkdirResult = 
				firstSubDir.mkdir();
			}
			if ( ! firstSubDir.exists() ) {
				String msg = "firstSubDir does not exist after testing for it and attempting to create it.  firstSubDir: " 
						+ firstSubDir.getAbsolutePath();
				log.error( msg );
				throw new FileObjectStorageFileUploadFileSystemException(msg);
			}
		}

		//  Second Subdir with second 2 characters of filename
		
		File secondSubDir = null;
		
		{
			String secondSubDir_String = filenameFirstStart.substring(2, 4);
			secondSubDir = new File( firstSubDir, secondSubDir_String );

			if ( ! secondSubDir.exists() ) {
				
				if ( createSubdirIfNotExists_ENUM == StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM.NO ) {
					//  Subdir NOT found and Flag is NO so return null
					return null; // EARLY RETURN
				}
				
//				boolean mkdirResult = 
				secondSubDir.mkdir();
			}
			if ( ! secondSubDir.exists() ) {
				String msg = "secondSubDir does not exist after testing for it and attempting to create it.  secondSubDir: " 
						+ secondSubDir.getAbsolutePath();
				log.error( msg );
				throw new FileObjectStorageFileUploadFileSystemException(msg);
			}
		}

		//  Third Subdir with third 2 characters of filename
		
		File thirdSubDir = null;
		
		{
			String thirdSubDir_String = filenameFirstStart.substring(4, 6);
			thirdSubDir = new File( secondSubDir, thirdSubDir_String );

			if ( ! thirdSubDir.exists() ) {
				
				if ( createSubdirIfNotExists_ENUM == StorageDir_OnLocalFileSystem_CreateToStoreFileIn__CreateSubdirIfNotExists_ENUM.NO ) {
					//  Subdir NOT found and Flag is NO so return null
					return null; // EARLY RETURN
				}
				
//				boolean mkdirResult = 
				thirdSubDir.mkdir();
			}
			if ( ! thirdSubDir.exists() ) {
				String msg = "thirdSubDir does not exist after testing for it and attempting to create it.  thirdSubDir: " 
						+ thirdSubDir.getAbsolutePath();
				log.error( msg );
				throw new FileObjectStorageFileUploadFileSystemException(msg);
			}
		}
		
		//  Return Third Subdir
		
		return thirdSubDir;
	}
	
}
