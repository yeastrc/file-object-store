package org.yeastrc.file_object_storage.web_app.servlets_common;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.constants_enums.ServetResponseFormatEnum;
import org.yeastrc.file_object_storage.web_app.constants_enums.ServletResultDataFormatConstants;

/**
 * 
 *
 */
public class Get_ServletResultDataFormat_FromServletInitParam {

	private static final Logger log = LoggerFactory.getLogger( Get_ServletResultDataFormat_FromServletInitParam.class );

	//  private constructor
	private Get_ServletResultDataFormat_FromServletInitParam() { }
	
	/**
	 * @return instance
	 */
	public static Get_ServletResultDataFormat_FromServletInitParam getInstance() {
		return new Get_ServletResultDataFormat_FromServletInitParam();
	}
	
	/**
	 * @param config
	 * @return
	 * @throws ServletException
	 */
	public ServetResponseFormatEnum get_ServletResultDataFormat_FromServletInitParam( ServletConfig config ) throws ServletException {

		String servletResultDataFormat = config.getInitParameter( ServletResultDataFormatConstants.SERVLET_PARAM_NAME_RESULT_DATA_FORMAT );

		if ( ServletResultDataFormatConstants.XML.equals( servletResultDataFormat ) ) {
			return ServetResponseFormatEnum.XML;
		} else if ( ServletResultDataFormatConstants.JSON.equals( servletResultDataFormat ) ) {
				return ServetResponseFormatEnum.JSON;
		} else {
			
			String msg = "servlet name: " + config.getServletName() 
			+ ":  init parameter '" + ServletResultDataFormatConstants.SERVLET_PARAM_NAME_RESULT_DATA_FORMAT
			+ "' must be string '" + ServletResultDataFormatConstants.XML
			+ "' or string '" + ServletResultDataFormatConstants.JSON
			+ "'."
			+ "  init parameter '" + ServletResultDataFormatConstants.SERVLET_PARAM_NAME_RESULT_DATA_FORMAT
			+ "' has string: " + servletResultDataFormat;
			log.error( msg );
			throw new ServletException( msg );
		}
	}
}
