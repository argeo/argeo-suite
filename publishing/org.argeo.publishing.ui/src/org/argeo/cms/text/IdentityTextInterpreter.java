package org.argeo.cms.text;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;

/** Based on HTML with a few Wiki-like shortcuts. */
public class IdentityTextInterpreter implements TextInterpreter, CmsNames {

	@Override
	public void write(Item item, String content) {
		try {
			if (item instanceof Node) {
				Node node = (Node) item;
				if (node.isNodeType(CmsTypes.CMS_STYLED)) {
					String raw = convertToStorage(node, content);
					validateBeforeStoring(raw);
					node.setProperty(CMS_CONTENT, raw);
				} else {
					throw new CmsException("Don't know how to interpret " + node);
				}
			} else {// property
				Property property = (Property) item;
				property.setValue(content);
			}
			// item.getSession().save();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot set content on " + item, e);
		}
	}

	@Override
	public String readSimpleHtml(Item item) {
		return raw(item);
	}

	@Override
	public String read(Item item) {
		try {
			String raw = raw(item);
			return convertFromStorage(item, raw);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot get " + item + " for edit", e);
		}
	}

	@Override
	public String raw(Item item) {
		try {
			item.getSession().refresh(true);
			if (item instanceof Node) {
				Node node = (Node) item;
				if (node.isNodeType(CmsTypes.CMS_STYLED)) {
					// WORKAROUND FOR BROKEN PARARAPHS
					if (!node.hasProperty(CMS_CONTENT)) {
						node.setProperty(CMS_CONTENT, "");
						node.getSession().save();
					}

					return node.getProperty(CMS_CONTENT).getString();
				} else {
					throw new CmsException("Don't know how to interpret " + node);
				}
			} else {// property
				Property property = (Property) item;
				return property.getString();
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot get " + item + " content", e);
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
