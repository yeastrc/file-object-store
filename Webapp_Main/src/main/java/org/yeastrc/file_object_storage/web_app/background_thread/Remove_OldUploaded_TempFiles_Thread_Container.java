package org.yeastrc.file_object_storage.web_app.background_thread;

import org.slf4j.LoggerFactory;  import org.slf4j.Logger;

/**
 * Container that holds current ComputeAPIKeyForScanFileThread  Instance or null
 *
 */
public class Remove_OldUploaded_TempFiles_Thread_Container

extends A_BackgroundThreadContainers_Common_AbstractBaseClass 

{

	private static final Logger log = LoggerFactory.getLogger(Remove_OldUploaded_TempFiles_Thread_Container.class);

	/**
	 * Package Private
	 * @return
	 */
	static Remove_OldUploaded_TempFiles_Thread_Container getInstance(){
		
		return new Remove_OldUploaded_TempFiles_Thread_Container();
	}
	
	
	private volatile Remove_OldUploaded_TempFiles_Thread threadInstance = null;

	private volatile int threadCreateCount = 0;

	private volatile boolean shutdownRequested = false;

	/**
	 * From Context
	 */
	@Override
	synchronized void initial_CreateStart_Thread() {

		if ( log.isInfoEnabled() ) {
			log.info("initial_CreateStart_Thread() called:  " );
		}

		createThread();
	}

	/**
	 * shutdown was received from the operating system.  This is called on a different thread.
	 */
	@Override
	synchronized void shutdown() {
		
		log.warn( "INFO: shutdown() called" );
		
		shutdownRequested = true;

		if ( threadInstance != null ) {
			
			threadInstance.shutdown();
		}
	}

	/**
	 * @return
	 */
	public boolean isThreadAlive() {
		
		if ( threadInstance != null ) {
			
			return threadInstance.isAlive();
		}
		
		return false;
	}
	
	/**
	 * awaken thread to process request
	 */
	public synchronized void awakenToProcessAScanFile() {

		if ( log.isInfoEnabled() ) {
			log.info("awakenToProcessAScanFile() called:  " );
		}

		if ( shutdownRequested ) {
			
			log.warn( "!!!!!!!!!!!!" );
			log.warn( "!!!!!!  awakenToProcessAScanFile() called and shutdownRequested is true!!!!  Skipping processing in this method. !!!!" );
			log.warn( "!!!!!!!!!!!!" );
			
			return; //  EARLY RETURN
		}
		
		if ( threadInstance != null ) {
			
			threadInstance.awaken();
		}
	}

	/**
	 * 
	 */
	private void createThread() {
		
		threadCreateCount++;
		
		threadInstance = new Remove_OldUploaded_TempFiles_Thread();
		threadInstance.setName( "ComputeAPIKeyForScanFileThread-Thread-" + threadCreateCount );
		
		threadInstance.start();
		
		if ( log.isWarnEnabled() ) {
			String msg = "INFO: Exit: createThread(startThread) Creating new ComputeAPIKeyForScanFileThread (extends Thread) object. threadCreateCount: " + threadCreateCount
					+ ", instance: " + threadInstance;
			log.warn( msg );
		}

	}
	
}
