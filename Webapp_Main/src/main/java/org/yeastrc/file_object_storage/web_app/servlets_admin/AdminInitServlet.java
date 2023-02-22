package org.yeastrc.file_object_storage.web_app.servlets_admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.constants_enums.AdminPageConstants;

/**
 * Init and forward to admin page
 *
 */
public class AdminInitServlet extends HttpServlet {

	private static final Logger log = LoggerFactory.getLogger(AdminInitServlet.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		try {
			HttpSession session = request.getSession();

			session.setAttribute( AdminPageConstants.ADMIN_KEY_SESSION_KEY_PARAMETER_NAME, getRandomKey() );

			request.getRequestDispatcher("/WEB-INF/jsp-pages/adminPage.jsp").forward( request, response );
		} catch ( RuntimeException e ) {
			log.error( "Exception: ", e );
			throw e;
		} catch ( IOException e ) {
			log.error( "Exception: ", e );
			throw e;
		}
	}
	
	/**
	 * @return
	 */
	private String getRandomKey() {

		long currTime = System.currentTimeMillis();
		
		double randomVal = Math.random();
		if ( randomVal < 0.5 ) {
			//  Make randomVal always >= 0.5
			randomVal += 0.5;
		}
		
		long uploadKeyAddition = ( (long) ( currTime * randomVal ) );
		
		String uploadKeyString = StringUtils.reverse( Long.toString( currTime ).substring( 4 ) );
		int uploadKeyStringLength = uploadKeyString.length();
		
		String uploadKeyAdditionString = Long.toString( uploadKeyAddition );
		int uploadKeyAdditionStringLength = uploadKeyAdditionString.length();
		
		int uploadKeyAdditionStringOutputLength = uploadKeyStringLength;
		
		if ( uploadKeyAdditionStringOutputLength > uploadKeyAdditionStringLength ) {
			uploadKeyAdditionStringOutputLength = uploadKeyAdditionStringLength;
		}
		

		String uploadKeyAdditionFinalString = uploadKeyAdditionString.substring( uploadKeyStringLength - uploadKeyAdditionStringOutputLength );

		return uploadKeyAdditionFinalString;
	}
}
