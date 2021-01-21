package org.argeo.docbook.ui;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
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
				if (node.isNodeType(DocBookTypes.PARA) || node.isNodeType(DocBookTypes.TITLE)) {
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
				if (node.isNodeType(DocBookTypes.PARA) || node.isNodeType(DocBookTypes.TITLE)) {
					Node jcrText = node.getNode(Jcr.JCR_XMLTEXT);
					String txt = jcrText.getProperty(Jcr.JCR_XMLCHARACTERS).getString();
					// TODO make it more robust
					// txt = txt.replace("\n", "").replace("\t", "");
					txt = txt.replace("\t", "  ");
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

	final static int BR_LENGTH = "<br/>".length();

	public String readSimpleHtml(Item item) {
		String raw = raw(item);
//		raw = "<span style='text-align:justify'>" + raw + "</span>";
		if (raw.length() == 0)
			return raw;
		try (StringReader reader = new StringReader(raw)) {
			List<String> lines = IOUtils.readLines(reader);
			if (lines.size() == 1)
				return lines.get(0);
			StringBuilder sb = new StringBuilder(raw.length() + lines.size() * BR_LENGTH);
			for (int i = 0; i < lines.size(); i++) {
				if (i != 0)
					sb.append("<br/>");
				sb.append(lines.get(i));
			}
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		String[] lines = raw.split("[\r\n]+");
//		if (lines.length == 1)
//			return lines[0];
//		StringBuilder sb = new StringBuilder(raw.length() + lines.length * BR_LENGTH);
//		for (int i = 0; i < lines.length; i++) {
//			if (i != 0)
//				sb.append("<br/>");
//			sb.append(lines[i]);
//		}
//		return sb.toString();
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
