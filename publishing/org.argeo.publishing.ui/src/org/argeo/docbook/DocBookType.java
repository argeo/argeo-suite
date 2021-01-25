package org.argeo.docbook;

import org.argeo.entity.JcrName;

/** Types related to DocBook. */
public enum DocBookType implements JcrName {
	book, article, section,
	//
	info, title, para,
	//
	mediaobject, imageobject, imagedata,
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
