package org.yeastrc.file_object_storage.web_app.meta_file_read_write_contents;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contents of Meta File
 *
 */
@XmlRootElement(name="metaFileContents")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetaFileContents {

	// Properties as XML attributes

	@XmlAttribute // attribute name is property name
	private Boolean fileIsGZIP;

	@XmlAttribute // attribute name is property name
	private Long originalFileSize;

	public Boolean getFileIsGZIP() {
		return fileIsGZIP;
	}

	public void setFileIsGZIP(Boolean fileIsGZIP) {
		this.fileIsGZIP = fileIsGZIP;
	}

	public Long getOriginalFileSize() {
		return originalFileSize;
	}

	public void setOriginalFileSize(Long originalFileSize) {
		this.originalFileSize = originalFileSize;
	}

}
