package org.argeo.entity;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/** JCR utilities around the entity concepts. */
public class EntityJcrUtils {
	/**
	 * The name of a node which will be serialized as XML text, as per section 7.3.1
	 * of the JCR 2.0 specifications.
	 */
	public final static String JCR_XMLTEXT = "jcr:xmltext";

	/**
	 * The name of a property which will be serialized as XML text, as per section
	 * 7.3.1 of the JCR 2.0 specifications.
	 */
	public final static String JCR_XMLCHARACTERS = "jcr:xmlcharacters";

	/*
	 * XML
	 */
	/**
	 * Set both as a property and as a subnode which will be exported as an XML
	 * element.
	 */

//	public static void setAttrAndElem(Node node, String name, String value) {
//		Jcr.set(node, name, value);
//		setElem(node, name, value);
//	}
	/**
	 * Set as a subnode which will be exported as an XML element.
	 */
	public static String getXmlValue(Node node, String name) {
		try {
			if (!node.hasNode(name))
				throw new IllegalArgumentException("No XML text named " + name);
			return node.getNode(name).getNode(JCR_XMLTEXT).getProperty(JCR_XMLCHARACTERS).getString();
		} catch (RepositoryException e) {
			throw new IllegalStateException("Cannot get " + name + " as XML text", e);
		}
	}

	/**
	 * Set as a subnode which will be exported as an XML element.
	 */
	public static void setXmlValue(Node node, String name, String value) {
		try {
			if (node.hasNode(name))
				node.getNode(name).getNode(JCR_XMLTEXT).setProperty(JCR_XMLCHARACTERS, value);
			else
				node.addNode(name, EntityType.xmlvalue.qualified()).addNode(JCR_XMLTEXT, EntityType.xmltext.qualified())
						.setProperty(JCR_XMLCHARACTERS, value);
		} catch (RepositoryException e) {
			throw new IllegalStateException("Cannot set " + name + " as XML text", e);
		}
	}

}
