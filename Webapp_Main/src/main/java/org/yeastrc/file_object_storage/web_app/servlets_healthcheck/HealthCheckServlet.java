package org.yeastrc.file_object_storage.web_app.servlets_healthcheck;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;  import org.slf4j.Logger;

/**
 * Health Check
 *
 */
public class HealthCheckServlet extends HttpServlet {

	private static final Logger log = LoggerFactory.getLogger(HealthCheckServlet.class);

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
		
	}
	
}
