package org.yeastrc.file_object_storage.web_app.servlets_common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.LoggerFactory;  import org.slf4j.Logger;
import org.yeastrc.file_object_storage.web_app.exceptions.SpectralFileDeserializeRequestException;
import org.yeastrc.file_object_storage.web_app.utils.Create_XMLInputFactory_XXE_Safe;

/**
 * Read the Request Object off the input stream and de-serialize it and return it
 *
 */
public class GetRequestObjectFromInputStream {

	private static final Logger log = LoggerFactory.getLogger( GetRequestObjectFromInputStream.class );

	private static GetRequestObjectFromInputStream instance = null;

	//  private constructor
	private GetRequestObjectFromInputStream() { }
	
	/**
	 * @return Singleton instance
	 */
	public synchronized static GetRequestObjectFromInputStream getSingletonInstance() throws Exception {
		if ( instance == null ) {
			instance = new GetRequestObjectFromInputStream();
		}
		return instance; 
	}
	
	/**
	 * @param webserviceRequest
	 * @param webserviceURL
	 * @return
	 * @throws Exception
	 */
	public Object getRequestObjectFromStream( 
			HttpServletRequest request ) throws Exception {
		Object webserviceRequestAsObject = null;
		try {

			//  Get request XML from client
			ByteArrayOutputStream outputStreamBufferOfClientRequest = new ByteArrayOutputStream( 1000000 );
			InputStream inputStream = null;
			try {
				inputStream = request.getInputStream();
				int nRead;
				byte[] data = new byte[ 16384 ];
				while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
					outputStreamBufferOfClientRequest.write(data, 0, nRead);
				}
			} catch ( IOException e ) {
				String msg = "Failed to read request object from input stream";
				log.error( msg, e );
				throw e;
			} finally {
				if ( inputStream != null ) {
					try {
						inputStream.close();
					} catch ( IOException e ) {
						throw e;
					}
				}
			}
			
			JAXBContext jaxbContext = Z_JAXBContext_ForRequestResponse.getSingletonInstance().getJAXBContext();
			
			byte[] outputStreamBufferOfClientRequest_ByteArray = outputStreamBufferOfClientRequest.toByteArray();
			ByteArrayInputStream inputStreamBufferOfClientRequest = 
					new ByteArrayInputStream( outputStreamBufferOfClientRequest_ByteArray );
			// Unmarshal received XML into Java objects
			try {
				XMLInputFactory xmlInputFactory = Create_XMLInputFactory_XXE_Safe.create_XMLInputFactory_XXE_Safe();
				XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader( new StreamSource( inputStreamBufferOfClientRequest ) );
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				webserviceRequestAsObject = unmarshaller.unmarshal( xmlStreamReader );
			} catch ( Exception e ) {
				String msg = "Failed to deserialize request object";
				log.error( msg, e );
				throw new SpectralFileDeserializeRequestException( msg, e );
			}
			return webserviceRequestAsObject; 
		} finally {

		}
	}
	
}
