package org.argeo.docbook.ui;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.text.TextInterpreter;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrxType;

/** Based on HTML with a few Wiki-like shortcuts. */
public class DbkTextInterpreter implements TextInterpreter {

	@Override
	public void write(Item item, String content) {
		try {
			if (item instanceof Node) {
				Node node = (Node) item;
				if (node.isNodeType(DocBookTypes.PARA)||node.isNodeType(DocBookTypes.TITLE)) {
					String raw = convertToStorage(node, content);
					validateBeforeStoring(raw);
					Node jcrText;
					if (!node.hasNode(Jcr.JCR_XMLTEXT))
						jcrText = node.addNode(Jcr.JCR_XMLTEXT, JcrxType.JCRX_XMLTEXT);
					else
						jcrText = node.getNode(Jcr.JCR_XMLTEXT);
					jcrText.setProperty(Jcr.JCR_XMLCHARACTERS, raw);
				} else {
					throw new IllegalArgumentException("Don't know how to interpret " + node);
				}
			} else {// property
				Property property = (Property) item;
				property.setValue(content);
			}
			// item.getSession().save();
		} catch (RepositoryException e) {
			throw new JcrException("Cannot set content on " + item, e);
		}
	}

	@Override
	public String read(Item item) {
		try {
			String raw = raw(item);
			return convertFromStorage(item, raw);
		} catch (RepositoryException e) {
			throw new JcrException("Cannot get " + item + " for edit", e);
		}
	}

	@Override
	public String raw(Item item) {
		try {
			item.getSession().refresh(true);
			if (item instanceof Node) {
				Node node = (Node) item;
				if (node.isNodeType(DocBookTypes.PARA)||node.isNodeType(DocBookTypes.TITLE)) {
					Node jcrText = node.getNode(Jcr.JCR_XMLTEXT);
					String txt = jcrText.getProperty(Jcr.JCR_XMLCHARACTERS).getString();
					// TODO make it more robust
					txt = txt.replace("\n", "").replace("\t", "");
					return txt;
				} else {
					throw new IllegalArgumentException("Don't know how to interpret " + node);
				}
			} else {// property
				Property property = (Property) item;
				return property.getString();
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot get " + item + " content", e);
		}
	}

	// EXTENSIBILITY
	/**
	 * To be overridden, in order to make sure that only valid strings are being
	 * stored.
	 */
	protected void validateBeforeStoring(String raw) {
	}

	/** To be overridden, in order to support additional formatting. */
	protected String convertToStorage(Item item, String content) throws RepositoryException {
		return content;

	}

	/** To be overridden, in order to support additional formatting. */
	protected String convertFromStorage(Item item, String content) throws RepositoryException {
		return content;
	}
}
