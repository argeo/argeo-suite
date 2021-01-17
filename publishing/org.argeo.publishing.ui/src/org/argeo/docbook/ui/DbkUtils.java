package org.argeo.docbook.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.JcrxApi;

/** Utilities around DocBook. */
public class DbkUtils {
	public static String getTitle(Node node) {
		return JcrxApi.getXmlValue(node, DocBookTypes.TITLE);
	}

	public static void setTitle(Node node, String txt) {
		try {
			Node titleNode = JcrUtils.getOrAdd(node, DocBookTypes.TITLE, DocBookTypes.TITLE);
			JcrxApi.setXmlValue(node, titleNode, txt);
		} catch (RepositoryException e) {
			throw new JcrException("Cannot add empty paragraph to " + node, e);
		}
	}

	public static Node addParagraph(Node node, String txt) {
		try {
			Node para = node.addNode(DocBookTypes.PARA, DocBookTypes.PARA);
			JcrxApi.setXmlValue(node, para, txt);
			return para;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot add empty paragraph to " + node, e);
		}
	}

	/** Singleton. */
	private DbkUtils() {
	}
}
