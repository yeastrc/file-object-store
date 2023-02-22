package org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class MetaFileContents_ReadFromInputStream {

	/**
	 * @param metaFileContents
	 * @param outputStream
	 * @throws Exception
	 */
	public static MetaFileContents readFromInputStream( InputStream inputStream ) throws Exception {
		
		JAXBContext jaxbContext = JAXBContext.newInstance( MetaFileContents.class );
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Object contentsAsObject = unmarshaller.unmarshal( inputStream );

		MetaFileContents metaFileContents = (MetaFileContents) contentsAsObject;
		
		return metaFileContents;
	}
}
