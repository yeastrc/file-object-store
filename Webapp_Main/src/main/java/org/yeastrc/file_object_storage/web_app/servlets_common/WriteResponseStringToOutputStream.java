package org.yeastrc.file_object_storage.web_app.servlets_common;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;  import org.slf4j.Logger;

/**
 * Write the Response String to the output stream
 *
 */
public class WriteResponseStringToOutputStream {

	private static final Logger log = LoggerFactory.getLogger( WriteResponseStringToOutputStream.class );

	//  private constructor
	private WriteResponseStringToOutputStream() { }
	
	/**
	 * @return Singleton instance
	 */
	public synchronized static WriteResponseStringToOutputStream getInstance() {
		return new WriteResponseStringToOutputStream();
	}
	
	/**
	 * @param responseString
	 * @param response
	 * @throws IOException 
	 * @throws Exception
	 */
	public void writeResponseStringToOutputStream( 
			String responseString,
			HttpServletResponse response ) throws IOException {
		try {
			byte[] responseBytes = responseString.getBytes( StandardCharsets.UTF_8 );
			
			try ( OutputStream outputStream = response.getOutputStream() ) {
				outputStream.write( responseBytes );
			} catch ( IOException e ) {
				String msg = "Failed to write response string to output stream";
				log.error( msg, e );
				throw e;
			} finally {
			}
			
		} finally {

		}
	}
}
