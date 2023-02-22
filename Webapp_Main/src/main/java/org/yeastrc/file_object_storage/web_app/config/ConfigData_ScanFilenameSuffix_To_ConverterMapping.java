package org.yeastrc.file_object_storage.web_app.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileWebappConfigException;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileWebappInternalException;

/**
 * 
 *
 */
public class ConfigData_ScanFilenameSuffix_To_ConverterMapping {

	private static final Logger log = LoggerFactory.getLogger( ConfigData_ScanFilenameSuffix_To_ConverterMapping.class );
	
	private volatile static ConfigData_ScanFilenameSuffix_To_ConverterMapping instance;
 
	// package private constructor
	ConfigData_ScanFilenameSuffix_To_ConverterMapping() { }
	
	/**
	 * @return Singleton instance
	 * @throws SpectralFileWebappInternalException 
	 */
	public static ConfigData_ScanFilenameSuffix_To_ConverterMapping getSingletonInstance() throws SpectralFileWebappInternalException { 
		if ( instance == null ) {
			throw new SpectralFileWebappInternalException( "Singleton Instance NOT yet created" );
		}
		return instance; 
	}
	
	/**
	 * package private
	 * @param instance
	 * @throws SpectralFileWebappInternalException 
	 */
	static void setInstance(ConfigData_ScanFilenameSuffix_To_ConverterMapping instance) throws SpectralFileWebappInternalException {
		
		if ( ! instance.configurationComplete ) {
			throw new SpectralFileWebappInternalException("Error call setInstance(...): ( ! instance.configurationComplete )");
		}
		
		ConfigData_ScanFilenameSuffix_To_ConverterMapping.instance = instance;
	}
	
	////
	
	private boolean configurationComplete;

	/**
	 * Package Private
	 * 
	 * @throws SpectralFileWebappInternalException 
	 * 
	 */
	void setConfigurationComplete() throws SpectralFileWebappInternalException {
		
		if ( configurationComplete ) {
			throw new SpectralFileWebappInternalException("Error call setConfigurationComplete(...) since already called");
		}
		
		scanfilename_suffix_to_converter_base_url_mapping__UNMODIFIABLE = Collections.unmodifiableList(scanfilename_suffix_to_converter_base_url_mapping);
		
		this.configurationComplete = true;
	}
	
	private List<ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry> scanfilename_suffix_to_converter_base_url_mapping = new ArrayList<>();
	
	private List<ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry> scanfilename_suffix_to_converter_base_url_mapping__UNMODIFIABLE = null;
	
	private Map<String, ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry> scanfilename_suffix_to_converter_base_url_mapping_Map_Key__scanfilename_suffix = new HashMap<>();

	/**
	 * Package Private
	 * @param entry
	 * @throws SpectralFileWebappConfigException 
	 * @throws SpectralFileWebappInternalException 
	 */
	void addSingleEntry( ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry entry, String configFilename ) throws SpectralFileWebappConfigException, SpectralFileWebappInternalException {
		
		if ( StringUtils.isEmpty( entry.converter_base_url ) || StringUtils.isEmpty( entry.scan_filename_suffix ) ) {
			throw new IllegalArgumentException( "( StringUtils.isEmpty( entry.converter_base_url ) || StringUtils.isEmpty( entry.scan_filename_suffix ) )" );
		}
		
		if ( configurationComplete ) {
			throw new SpectralFileWebappInternalException("Error call addSingleEntry(...) since already called setConfigurationComplete(...)");
		}
		
		{
			ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry entry_Existing = scanfilename_suffix_to_converter_base_url_mapping_Map_Key__scanfilename_suffix.get( entry.scan_filename_suffix );

			if ( entry_Existing != null ) {
				
				//  already have entry for entry.scan_filename_suffix
				
				if ( entry_Existing.converter_base_url.equals( entry.converter_base_url ) ) {
					
					//  Existing enry has same converter_base_url so just skip
					
					return; // EARLY RETURN
					
				} else {
				
					String msg = "!!! Config File ERROR: The same file suffix '" + entry.scan_filename_suffix + "' is under 2 different entries with different converter_base_url values in config file '" + configFilename + "' !!!";
					log.error(msg);
					throw new SpectralFileWebappConfigException(msg);
				}
			}
		}
		
		scanfilename_suffix_to_converter_base_url_mapping_Map_Key__scanfilename_suffix.put( entry.scan_filename_suffix, entry );
		
		scanfilename_suffix_to_converter_base_url_mapping.add(entry);
	}
	
	/**
	 * @return
	 */
	public List<ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry> getEntries() {
	
		return scanfilename_suffix_to_converter_base_url_mapping__UNMODIFIABLE;
	}
	
	/**
	 * @param suffix
	 * @return
	 */
	public ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry getEntry_For_ScanFilenameSuffix(String suffix) {
		
		return scanfilename_suffix_to_converter_base_url_mapping_Map_Key__scanfilename_suffix.get(suffix);
	}

	////////
	
	//  class for Single Entry
	
	/**
	 * 
	 *
	 */
	public static class ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry {
		
		private String scan_filename_suffix;
		private String converter_base_url;

		@Override
		public String toString() {
			return "ConfigData_ScanFilenameSuffix_To_ConverterMapping_SingleEntry [scan_filename_suffix="
					+ scan_filename_suffix + ", converter_base_url=" + converter_base_url + "]";
		}
		
		public String getScan_filename_suffix() {
			return scan_filename_suffix;
		}
		public String getConverter_base_url() {
			return converter_base_url;
		}
		
		//  Package Private Setters
		
		void setScan_filename_suffix(String scan_filename_suffix) {
			this.scan_filename_suffix = scan_filename_suffix;
		}
		void setConverter_base_url(String converter_base_url) {
			this.converter_base_url = converter_base_url;
		}
	}


}
