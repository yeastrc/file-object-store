package org.yeastrc.file_object_storage.web_app.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Config data for config file in web app Work Directory
 * 
 * The Secondary configuration for the web app
 * 
 * Config for which remote computers can access
 * 
 * 
 * Singleton Instance
 *
 */
public class ConfigData_Allowed_Remotes_InWorkDirectory {
	
	private volatile static ConfigData_Allowed_Remotes_InWorkDirectory instance;
 
	// package private constructor
	ConfigData_Allowed_Remotes_InWorkDirectory() { }
	
	/**
	 * @return Singleton instance
	 */
	public static ConfigData_Allowed_Remotes_InWorkDirectory getSingletonInstance() { 
		return instance; 
	}
	/**
	 * package private
	 * @param instance
	 */
	static void setInstance(ConfigData_Allowed_Remotes_InWorkDirectory instance) {
		ConfigData_Allowed_Remotes_InWorkDirectory.instance = instance;
	}

	/**
	 * When True, then connection for Update and Importer is allowed from any IP address
	 */
	private boolean accessAllowed_allRemoteIps_update_importer;

	/**
	 * Overall.  Applied to all URLs
	 */
	private Set<String> allowedRemoteIPs_Overall = new HashSet<>();
	
	/**
	 * URLs that start with "/admin/" also have applied filter using this allowed IP list
	 */
	private Set<String> allowedRemoteIPs_Admin = new HashSet<>();

	/**
	 * URLs that start with "/update/" also have applied filter using this allowed IP list
	 */
	private Set<String> allowedRemoteIPs_Update = new HashSet<>();
	
	/**
	 * @param allowedRemoteIP
	 */
	public void addAllowedRemoteIP_Overall( String allowedRemoteIP) {
		this.allowedRemoteIPs_Overall.add( allowedRemoteIP );
	}
//	public void clearAllowedRemoteIP_Overall_Collection() {
//		this.allowedRemoteIPs_Overall.clear();
//	}

	/**
	 * Adds to allowedRemoteIPs_Overall as well
	 * @param allowedRemoteIP
	 */
	public void addAllowedRemoteIP_Admin( String allowedRemoteIP) {
		this.allowedRemoteIPs_Admin.add( allowedRemoteIP );
		this.allowedRemoteIPs_Overall.add( allowedRemoteIP );
	}
//	public void clearAllowedRemoteIP_Admin_Collection() {
//		this.allowedRemoteIPs_Admin.clear();
//	}

	/**
	 * Adds to allowedRemoteIPs_Overall as well
	 * @param allowedRemoteIP
	 */
	public void addAllowedRemoteIP_Update( String allowedRemoteIP) {
		this.allowedRemoteIPs_Update.add( allowedRemoteIP );
		this.allowedRemoteIPs_Overall.add( allowedRemoteIP );
	}
//	public void clearAllowedRemoteIP_Update_Collection() {
//		this.allowedRemoteIPs_Update.clear();
//	}


	public Set<String> getAllowedRemoteIPs_Overall() {
		return allowedRemoteIPs_Overall;
	}

	public void setAllowedRemoteIPs_Overall(Set<String> allowedRemoteIPs_Overall) {
		this.allowedRemoteIPs_Overall = allowedRemoteIPs_Overall;
	}

	public Set<String> getAllowedRemoteIPs_Admin() {
		return allowedRemoteIPs_Admin;
	}

	public void setAllowedRemoteIPs_Admin(Set<String> allowedRemoteIPs_Admin) {
		this.allowedRemoteIPs_Admin = allowedRemoteIPs_Admin;
	}

	public Set<String> getAllowedRemoteIPs_Update() {
		return allowedRemoteIPs_Update;
	}

	public void setAllowedRemoteIPs_Update(Set<String> allowedRemoteIPs_Update) {
		this.allowedRemoteIPs_Update = allowedRemoteIPs_Update;
	}

	public boolean isAccessAllowed_allRemoteIps_update_importer() {
		return accessAllowed_allRemoteIps_update_importer;
	}

	public void setAccessAllowed_allRemoteIps_update_importer(boolean accessAllowed_allRemoteIps_update_importer) {
		this.accessAllowed_allRemoteIps_update_importer = accessAllowed_allRemoteIps_update_importer;
	}
}
