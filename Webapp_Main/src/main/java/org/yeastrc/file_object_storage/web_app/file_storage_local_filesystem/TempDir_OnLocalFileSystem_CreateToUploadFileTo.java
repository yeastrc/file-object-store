package org.yeastrc.file_object_storage.web_app.file_storage_local_filesystem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.yeastrc.file_object_storage.web_app.config.ConfigData_Directories_ProcessUploadInfo_InWorkDirectory;
import org.yeastrc.file_object_storage.web_app.constants_enums.FileUploadConstants;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileFileUploadFileSystemException;
import org.slf4j.Logger;

/**
 * 
 *
 */
public class TempDir_OnLocalFileSystem_CreateToUploadFileTo {

	private static final Logger log = LoggerFactory.getLogger(TempDir_OnLocalFileSystem_CreateToUploadFileTo.class);

	//  private constructor
	private TempDir_OnLocalFileSystem_CreateToUploadFileTo() { }
	
	/**
	 * @return newly created instance
	 */
	public static TempDir_OnLocalFileSystem_CreateToUploadFileTo getInstance() { 
		return new TempDir_OnLocalFileSystem_CreateToUploadFileTo(); 
	}
	
	/**
	 * @return
	 * @throws SpectralFileFileUploadFileSystemException 
	 * @throws IOException 
	 */
	public File createTempDirToUploadFileTo() throws SpectralFileFileUploadFileSystemException, IOException {
		
		File rootBaseDir = ConfigData_Directories_ProcessUploadInfo_InWorkDirectory.getSingletonInstance().getFileObjectStorage_BaseDirectory();
		
		//  Get the File object for the Base Subdir used to first store the files in this request 
		String uploadFileTempDirString = FileUploadConstants.UPLOAD_FILE_TEMP_BASE_DIR;
		File uploadFileTempDir = new File( rootBaseDir, uploadFileTempDirString );
		if ( ! uploadFileTempDir.exists() ) {
//			boolean mkdirResult = 
			uploadFileTempDir.mkdir();
		}
		if ( ! uploadFileTempDir.exists() ) {
			String msg = "uploadFileTempDir does not exist after testing for it and attempting to create it.  uploadFileTempDir: " 
					+ uploadFileTempDir.getAbsolutePath();
			log.error( msg );
			throw new SpectralFileFileUploadFileSystemException(msg);
		}
		
		//  Create subdir for this specific file upload
		
		//  First part is YYYYMMDD 
		
		String currentDate_yyyymmdd = new SimpleDateFormat("yyyy_MM_dd").format( new Date() );
		
		long uploadKey = System.currentTimeMillis();
		File createdSubDir = null;
		int retryCreateSubdirCount = 0;
		while ( createdSubDir == null ) {
			retryCreateSubdirCount++;
			if ( retryCreateSubdirCount > 4 ) {
				String msg = "Failed to create subdir after 4 attempts.";
				log.error( msg );
				throw new SpectralFileFileUploadFileSystemException(msg);
			}
			int uploadKeyIncrement = ( (int) ( Math.random() * 10 ) ) + 5;
			uploadKey += uploadKeyIncrement;
			createdSubDir =
					createSubDirForUploadFileTempDir( currentDate_yyyymmdd, uploadKey, uploadFileTempDir );
		}
		
		return createdSubDir;
	}
	

	/**
	 * @param currentDate_yyyymmdd
	 * @param uploadKey
	 * @param uploadTempBase
	 * @return null if subdir already exists
	 * @throws SpectralFileFileUploadFileSystemException 
	 * @throws IOException 
	 */
	private File createSubDirForUploadFileTempDir( String currentDate_yyyymmdd, long uploadKey, File uploadTempBase ) throws SpectralFileFileUploadFileSystemException, IOException {
		
		File subdir = getSubDirForUploadFileTempDir( currentDate_yyyymmdd, uploadKey, uploadTempBase );
		if ( subdir.exists() ) {
			//  Subdir already exists so need new uploadKey to create unique subdir
			return null;
		}
		if ( ! subdir.mkdir() ) {
			String msg = "Failed to make temp upload subdir: " + subdir.getCanonicalPath();
			log.error( msg );
			throw new SpectralFileFileUploadFileSystemException( msg );
		}
		return subdir;
	}

	/**
	 * @param uploadKey
	 * @param uploadTempBase
	 * @return
	 * @throws ProxlWebappFileUploadFileSystemException 
	 * @throws IOException 
	 */
	private File getSubDirForUploadFileTempDir( String currentDate_yyyymmdd, long uploadKey, File uploadTempBase ) {
		String subdirName = FileUploadConstants.UPLOAD_FILE_TEMP_SUB_DIR_PREFIX 
				+ currentDate_yyyymmdd
				+ "_"
				+ uploadKey;
		File subdir = new File( uploadTempBase, subdirName );
		return subdir;
	}
	
}
