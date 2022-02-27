package org.argeo.app.docbook;

import org.argeo.app.api.JcrName;

/** Supported DocBook elements */
public enum DbkType implements JcrName {
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

}
