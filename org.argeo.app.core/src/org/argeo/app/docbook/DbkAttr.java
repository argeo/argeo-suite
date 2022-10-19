package org.argeo.app.docbook;

import org.argeo.api.acr.QNamed;

/** Supported DocBook attributes. */
public enum DbkAttr implements QNamed.Unqualified {
	role,
	//
	fileref, contentwidth, contentdepth
	//
	;

	public final static String XLINK_HREF = "xlink:href";

}
