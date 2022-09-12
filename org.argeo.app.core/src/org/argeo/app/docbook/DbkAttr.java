package org.argeo.app.docbook;

import javax.xml.XMLConstants;

import org.argeo.util.naming.QNamed;

/** Supported DocBook attributes. */
public enum DbkAttr implements QNamed {
	role,
	//
	fileref, contentwidth, contentdepth
	//
	;

	public final static String XLINK_HREF = "xlink:href";

	@Override
	public String getNamespace() {
		return XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getDefaultPrefix() {
		return XMLConstants.DEFAULT_NS_PREFIX;
	}

}
