package org.yeastrc.file_object_storage.web_app.config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileWebappConfigException;

/**
 * Update ConfigDataInWebApp with contents in config file
 *
 */
public class ConfigDataInWebApp_Reader {

	private static final Logger log = LoggerFactory.getLogger(ConfigDataInWebApp_Reader.class);
	

	private static String CONFIG_DEFAULTS_FILENAME = "file_object_storage_config_defaults.properties";
	private static String CONFIG_OVERRIDES_FILENAME = "file_object_storage_config.properties";

	private static String PROPERTY_NAME__WEBAPP_WORK_DIRECTORY = "webapp.work.directory";
	

	private static enum AllowNoPropertiesFile { YES, NO }
	
	//  private constructor
	private ConfigDataInWebApp_Reader() { }
	
	/**
	 * @return newly created instance
	 */
	public static ConfigDataInWebApp_Reader getInstance() { 
		return new ConfigDataInWebApp_Reader(); 
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void readConfigDataInWebApp() throws Exception {
		
		ConfigDataInWebApp configDataInWebApp = new ConfigDataInWebApp();

		String webappWorkDirectoryDefaultString =
				processPropertiesFilename( CONFIG_DEFAULTS_FILENAME, AllowNoPropertiesFile.NO, configDataInWebApp );
		
		String webappWorkDirectoryString =
				processPropertiesFilename( CONFIG_OVERRIDES_FILENAME, AllowNoPropertiesFile.YES, configDataInWebApp );
		
		if ( StringUtils.isEmpty( webappWorkDirectoryString ) 
				&& ( ! StringUtils.isEmpty( webappWorkDirectoryDefaultString ) ) ) {
			
			webappWorkDirectoryString = webappWorkDirectoryDefaultString;
		}

		if ( StringUtils.isEmpty( webappWorkDirectoryString ) ) {
			String msg = "Property '" + PROPERTY_NAME__WEBAPP_WORK_DIRECTORY 
					+ "' in config is empty or missing";
			log.error( msg );
			throw new SpectralFileWebappConfigException( msg );
		}
		
		File workDirectory = new File( webappWorkDirectoryString );
		if ( ! ( workDirectory.exists() && workDirectory.isDirectory() && workDirectory.canRead() ) ) {
			String msg = "Property '" + PROPERTY_NAME__WEBAPP_WORK_DIRECTORY 
					+ "' in config does not exist, is not  a directory, or is not readable. Value: "
					+ webappWorkDirectoryString;
			log.error( msg );
			throw new SpectralFileWebappConfigException( msg );
		}
		
		configDataInWebApp.setWebappWorkDirectory( workDirectory );

		log.warn( "INFO: '" + PROPERTY_NAME__WEBAPP_WORK_DIRECTORY 
				+ "' has value: " 
				+ configDataInWebApp.getWebappWorkDirectory().getCanonicalPath() );
		
		ConfigDataInWebApp.setInstance( configDataInWebApp );
	}

	/**
	 * @param propertiesFilename
	 * @param configDataInWebApp
	 * @throws IOException
	 * @throws SpectralFileWebappConfigException 
	 */
	private String processPropertiesFilename( 
			String propertiesFilename, 
			AllowNoPropertiesFile allowNoPropertiesFile,
			ConfigDataInWebApp configDataInWebApp ) throws Exception {

		InputStream propertiesFileAsStream = null;
		try {
			//  Get config file from class path
			ClassLoader thisClassLoader = this.getClass().getClassLoader();
			URL configPropFile = thisClassLoader.getResource( propertiesFilename );
			if ( configPropFile == null ) {
				//  No properties file
				//  No properties file
				if ( allowNoPropertiesFile == AllowNoPropertiesFile.YES ) {
					return null;  //  EARLY EXIT
				}
				String msg = "Properties file '" + propertiesFilename + "' not found in class path.";
				log.error( msg );
				throw new SpectralFileWebappConfigException( msg );
			} else {
				log.info( "Properties file '" 
						+ propertiesFilename 
						+ "' found, load path = " 
						+ configPropFile.getFile() );
			}
			propertiesFileAsStream = thisClassLoader.getResourceAsStream( propertiesFilename );
			if ( propertiesFileAsStream == null ) {
				//  No properties file
				if ( allowNoPropertiesFile == AllowNoPropertiesFile.YES ) {
					return null;  //  EARLY EXIT
				}
				String msg = "Properties file '" 
				+ propertiesFilename 
				+ "' not found in class path.";
				log.error( msg );
				throw new SpectralFileWebappConfigException( msg );
			}
			Properties configProps = new Properties();
			configProps.load(propertiesFileAsStream);
			String propertyValue = null;
			propertyValue = configProps.getProperty( PROPERTY_NAME__WEBAPP_WORK_DIRECTORY );
			if ( StringUtils.isNotEmpty( propertyValue ) ) {
				return propertyValue;
			}
			
			return null;

		} catch ( RuntimeException e ) {
			log.error( "Error processing Properties file '" 
					+ propertiesFilename 
					+ "', exception: " 
					+ e.toString(), e );
			throw e;
		} finally {
			if ( propertiesFileAsStream != null ) {
				propertiesFileAsStream.close();
			}
		}
	}
	
}
