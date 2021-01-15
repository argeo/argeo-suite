package org.argeo.docbook.ui;

import javax.jcr.Node;

import org.argeo.jcr.JcrxApi;

/** Utilities around DocBook. */
public class DbkUtils {
	public static String getTitle(Node node) {
		return JcrxApi.getXmlValue(node, DocBookTypes.TITLE);
	}

	/** Singleton. */
	private DbkUtils() {
	}
}
