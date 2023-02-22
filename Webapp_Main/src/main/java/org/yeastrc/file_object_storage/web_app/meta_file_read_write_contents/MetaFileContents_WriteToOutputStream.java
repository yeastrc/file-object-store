package org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class MetaFileContents_WriteToOutputStream {

	/**
	 * @param metaFileContents
	 * @param outputStream
	 * @throws Exception
	 */
	public static void writeToOutputStream( MetaFileContents metaFileContents, OutputStream outputStream ) throws Exception {
		
		JAXBContext jaxbContext = JAXBContext.newInstance( MetaFileContents.class );
		
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		marshaller.setProperty( Marshaller.JAXB_ENCODING, MetaFileContents_Constants.XML_ENCODING_CHARACTER_SET );
		try {
			marshaller.marshal( metaFileContents, outputStream );
		} catch ( Exception e ) {
			throw e;
		} finally {
		}
		
	}
}
