package org.yeastrc.file_object_storage.web_app.log_error_after_webapp_undeploy_started;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yeastrc.file_object_storage.web_app.servlet_context.CurrentContext;
import org.yeastrc.file_object_storage.web_app.servlet_context.Webapp_Undeploy_Started_Completed;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralStorageProcessingException;

/**
 * Log4J2 has shut down before ServletContext::contextDestroyed(...) is called
 * 
 * So this provides a way to write to sysout/syserr with a prefix of this webapp and the webapp context.
 * 
 * In Tomcat, this will be written to file catalina.out
 *
 */
public class Log_Info_Error_AfterWebAppUndeploy_Started {
	
	private static final Logger log = LoggerFactory.getLogger( Log_Info_Error_AfterWebAppUndeploy_Started.class );

	/**
	 * @param msg
	 * @throws SpectralStorageProcessingException 
	 */
	public static void log_INFO_AfterWebAppUndeploy_Started( String msg ) throws SpectralStorageProcessingException {
		
		if ( ! Webapp_Undeploy_Started_Completed.isWebapp_Undeploy_Started() ) {
			
			String errorMsg = "Invalid to call when NOT Undeploy started.  true: if ( ! Webapp_Undeploy_Started_Completed.isWebapp_Undeploy_Started() ).  msg: " + msg;
			log.error(errorMsg);
			throw new SpectralStorageProcessingException( errorMsg );
		}
		
		String logMsg = getLogMsgPrefix() + " INFO:  " + msg;
		
		System.out.println( logMsg );
	}

	/**
	 * @param msg
	 * @throws SpectralStorageProcessingException 
	 */
	public static void log_ERROR_AfterWebAppUndeploy_Started( String msg ) throws SpectralStorageProcessingException {
		
		log_ERROR_AfterWebAppUndeploy_Started( msg, null /* throwable */ );
	}
	
	/**
	 * @param msg
	 * @param throwable
	 * @throws SpectralStorageProcessingException 
	 */
	public static void log_ERROR_AfterWebAppUndeploy_Started( String msg, Throwable throwable ) throws SpectralStorageProcessingException {
		
		if ( ! Webapp_Undeploy_Started_Completed.isWebapp_Undeploy_Started() ) {
			
			String errorMsg = "Invalid to call when NOT Undeploy started.  true: if ( ! Webapp_Undeploy_Started_Completed.isWebapp_Undeploy_Started() ).  msg: " + msg;
			log.error(errorMsg);
			throw new SpectralStorageProcessingException( errorMsg );
		}
		
		String logMsg = getLogMsgPrefix() + " ERROR:  " + msg;
		
		System.err.println( logMsg );
		
		if ( throwable != null )  {
			throwable.printStackTrace();
		}
	}
	
	/**
	 * @return
	 */
	private static String getLogMsgPrefix() {
		
		String now = new Date().toString();
		
		String prefix = now + ":  Spectral Storage Accept Import Webapp: CurrentContext: " + CurrentContext.getCurrentWebAppContext() 
		+ ". ";
		
		return prefix;
	}
}
