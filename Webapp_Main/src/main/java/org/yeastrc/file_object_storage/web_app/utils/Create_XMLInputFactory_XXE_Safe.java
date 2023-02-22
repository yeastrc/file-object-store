package org.yeastrc.file_object_storage.web_app.utils;

import javax.xml.stream.XMLInputFactory;

/**
 * Create XMLInputFactory that has the settings that make it safe from XXE
 *
 */
public class Create_XMLInputFactory_XXE_Safe {

	/**
	 * Create XMLInputFactory that has the settings that make it safe from XXE
	 * 
	 * @return
	 */
	public static XMLInputFactory create_XMLInputFactory_XXE_Safe() {

	    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	    //  XXE  Mitigation
	    //  prevents using external resources when parsing xml
	    xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

	    //  prevents using external document type definition when parsing xml
	    xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
	  
		return xmlInputFactory;
	}
}
