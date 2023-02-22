package org.yeastrc.file_object_storage.web_app.servlet_context;

import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.background_thread.A_BackgroundThreads_Containers_Manager;
import org.yeastrc.file_object_storage.web_app.config.A_Load_Config;
import org.yeastrc.file_object_storage.web_app.log_error_after_webapp_undeploy_started.Log_Info_Error_AfterWebAppUndeploy_Started;

/**
 * This class is loaded and the method "contextInitialized" is called when the web application is first loaded by the container
 *
 */
public class ServletContextAppListener extends HttpServlet implements ServletContextListener {
	
	private static final Logger log = LoggerFactory.getLogger( ServletContextAppListener.class );
	private static final long serialVersionUID = 1L;
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		
		log.warn( "INFO:  !!!!!!!!!!!!!!!" );
		log.warn( "INFO:  !!!!!!!!!!!!!!!   Start up of web app  'File Object Storage' beginning  !!!!!!!!!!!!!!!!!!!! " );
		
//		boolean isDevEnv = false;
//		java.util.Properties prop = System.getProperties();
//		String devEnv = prop.getProperty("devEnv");
//		if ( "Y".equals(devEnv ) ) {
//			isDevEnv = true;
//		}
		
		try {
			A_Load_Config.getInstance().load_Config();
		} catch (Exception e) {
			String msg = "Failed to load config";
			log.error( msg, e );
			throw new RuntimeException( e );
		} 

		ServletContext context = event.getServletContext();
		String contextPath = context.getContextPath();
		CurrentContext.setCurrentWebAppContext( contextPath );
		
		startBackgroundThreads(); 
		

		
		log.warn( "INFO:  !!!!!!!!!!!!!!!   Start up of web app  'File Object Storage' complete  !!!!!!!!!!!!!!!!!!!! " );
		log.warn( "INFO: contextPath: " + contextPath );
		log.warn( "INFO:  !!!!!!!!!!!!!!!" );

	}

	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		
		//  !!! Log4J2 stops logging before ServletContext::contextDestroyed(...) is called  !!!
		
		Webapp_Undeploy_Started_Completed.setWebapp_Undeploy_Started(true);
		
//		ServletContext context = event.getServletContext();
		
		//  Nothing output since Log4J2 has stopped logging
//		log.warn("INFO:  !!!!!!!!  Web app Undeploying STARTING  !!!!!!!!");

		Log_Info_Error_AfterWebAppUndeploy_Started.log_INFO_AfterWebAppUndeploy_Started("  !!!!!!!!" );
		Log_Info_Error_AfterWebAppUndeploy_Started.log_INFO_AfterWebAppUndeploy_Started("  !!!!!!!!  Web app Undeploying STARTING  !!!!!!!!" );

		
		stopBackgroundThreads();
		
		
		//  Nothing output since Log4J2 has stopped logging
//		log.warn("INFO:  !!!!!!!!  Web app Undeploying FINISHED  !!!!!!!!");

		Log_Info_Error_AfterWebAppUndeploy_Started.log_INFO_AfterWebAppUndeploy_Started(
				"  !!!!!!!!  Web app Undeploying: Initial run of contextDestroyed(...) is complete.  Webapp will not undeploy until all background threads exit run()." );
		
		Log_Info_Error_AfterWebAppUndeploy_Started.log_INFO_AfterWebAppUndeploy_Started("  !!!!!!!!" );
		
		Webapp_Undeploy_Started_Completed.setWebapp_Undeploy_Completed(true);
	}


	
	//  Start / Stop Background Threads

	/**
	 * Start Background Threads
	 */
	private void startBackgroundThreads() {
		
		try {
			A_BackgroundThreads_Containers_Manager.getSingletonInstance().initial_CreateStart_Thread();
		} catch (Exception e) {
			log.error( "Failed: A_BackgroundThreads_Containers_Manager.getSingletonInstance().initial_CreateStart_Thread();", e );
			throw new RuntimeException( e );
		} 
	}
	

	/**
	 * Stop Background Threads
	 */
	private void stopBackgroundThreads() {
		
		A_BackgroundThreads_Containers_Manager.getSingletonInstance().shutdownBackgroundThreads();
	}
}
