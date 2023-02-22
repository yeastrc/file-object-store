package org.yeastrc.file_object_storage.web_app.servlets_common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.constants_enums.ServetResponseFormatEnum;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileSerializeRequestException;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileWebappConfigException;

/**
 * Write the Response Object to the output stream
 * 
 * 
 * JSON requests currently throw new IllegalArgumentException since no Jackson jars in web app
 *
 */
public class WriteResponseObjectToOutputStream {

	private static final Logger log = LoggerFactory.getLogger( WriteResponseObjectToOutputStream.class );

	private static WriteResponseObjectToOutputStream instance = null;

	//  private constructor
	private WriteResponseObjectToOutputStream() { }
	
	/**
	 * @return Singleton instance
	 */
	public synchronized static WriteResponseObjectToOutputStream getSingletonInstance() throws Exception {
		if ( instance == null ) {
			instance = new WriteResponseObjectToOutputStream();
		}
		return instance; 
	}
	
	/**
	 * @param webserviceResponseAsObject
	 * @param servetResponseFormat
	 * @param response
	 * @throws Exception
	 */
	public void writeResponseObjectToOutputStream( 
			Object webserviceResponseAsObject,
			ServetResponseFormatEnum servetResponseFormat,
			HttpServletResponse response ) throws Exception {
		try {

			ByteArrayOutputStream outputStreamBufferOfServerResponse = 
					new ByteArrayOutputStream( 1000000 );
			
			if ( servetResponseFormat == ServetResponseFormatEnum.XML ) {

				JAXBContext jaxbContext = Z_JAXBContext_ForRequestResponse.getSingletonInstance().getJAXBContext();

				// Marshal Java object into XML
				try {
					Marshaller marshaller = jaxbContext.createMarshaller();

					marshaller.setProperty( Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.toString() );
					
					marshaller.marshal( webserviceResponseAsObject, outputStreamBufferOfServerResponse );
					
				} catch ( JAXBException e ) {
					String msg = "Failed to serialize response object";
					log.error( msg, e );
					throw new SpectralFileSerializeRequestException( msg, e );
				}
			} else if ( servetResponseFormat == ServetResponseFormatEnum.JSON ) {

				String msg = "JSON not currently supported.  Add in Jackson jars for support";
				log.error( msg );
				throw new IllegalArgumentException( msg );
				
//				// send the JSON response 
//				ObjectMapper mapper = new ObjectMapper();  //  Jackson JSON library object
//				mapper.writeValue( outputStreamBufferOfServerResponse, webserviceResponseAsObject ); // where first param can be File, OutputStream or Writer
				
			} else {
				String msg = "Unknown value for servetResponseFormat: " + servetResponseFormat;
				log.error( msg );
				throw new SpectralFileWebappConfigException( msg );
			}
			
			response.setContentLength( outputStreamBufferOfServerResponse.size() );
			
			try ( OutputStream outputStream = response.getOutputStream() ) {
				outputStreamBufferOfServerResponse.writeTo( outputStream );
			} catch ( IOException e ) {
				String msg = "Failed to write response object to output stream";
				log.error( msg, e );
				throw e;
			} finally {
			}
			
		} finally {

		}
	}
}
