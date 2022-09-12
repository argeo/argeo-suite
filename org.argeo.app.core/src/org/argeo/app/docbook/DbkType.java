package org.argeo.app.docbook;

import org.argeo.app.api.JcrName;
import org.argeo.util.naming.QNamed;

/** Supported DocBook elements */
public enum DbkType implements JcrName, QNamed {
	book, article, section,
	//
	info, title, para,
	//
	mediaobject, imageobject, imagedata, videoobject, videodata, caption,
	//
	link,
	//
	;

	@Override
	public String getPrefix() {
		return prefix();
	}

	@Deprecated
	public static String prefix() {
		return "dbk";
	}

	@Override
	public String getNamespace() {
		return namespace();
	}

	public static String namespace() {
		return "http://docbook.org/ns/docbook";
	}

	@Override
	public String getDefaultPrefix() {
		return "dbk";
	}

}
