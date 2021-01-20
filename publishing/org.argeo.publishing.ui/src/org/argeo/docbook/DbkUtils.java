package org.argeo.docbook;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.docbook.ui.DocBookNames;
import org.argeo.docbook.ui.DocBookTypes;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
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

	public static Node getMetadata(Node infoContainer) {
		try {
			if (!infoContainer.hasNode(DocBookTypes.INFO))
				return null;
			Node info = infoContainer.getNode(DocBookTypes.INFO);
			if (!info.hasNode(EntityType.local.get()))
				return null;
			return info.getNode(EntityType.local.get());
		} catch (RepositoryException e) {
			throw new JcrException("Cannot retrieve metadata from " + infoContainer, e);
		}
	}

	public static Node getChildByRole(Node parent, String role) {
		try {
			NodeIterator baseSections = parent.getNodes();
			while (baseSections.hasNext()) {
				Node n = baseSections.nextNode();
				String r = Jcr.get(n, DocBookNames.DBK_ROLE);
				if (r != null && r.equals(role))
					return n;
			}
			return null;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot get child from " + parent + " with role " + role, e);
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

	public static Node insertImageAfter(Node sibling) {
		try {
			Node parent = sibling.getParent();
			Node mediaNode = parent.addNode(DocBookTypes.MEDIAOBJECT, DocBookTypes.MEDIAOBJECT);
			// TODO optimise?
			parent.orderBefore(mediaNode.getName() + "[" + mediaNode.getIndex() + "]",
					sibling.getName() + "[" + sibling.getIndex() + "]");
			parent.orderBefore(sibling.getName() + "[" + sibling.getIndex() + "]",
					mediaNode.getName() + "[" + mediaNode.getIndex() + "]");

			Node imageNode = mediaNode.addNode(DocBookTypes.IMAGEOBJECT, DocBookTypes.IMAGEOBJECT);
			Node infoNode = imageNode.addNode(DocBookTypes.INFO, DocBookTypes.INFO);
			Node imageDataNode = JcrUtils.copyBytesAsFile(infoNode, EntityType.box.get(), new byte[0]);
			imageDataNode.addMixin(EntityType.box.get());
			imageDataNode.setProperty(EntityNames.SVG_WIDTH, 0);
			imageDataNode.setProperty(EntityNames.SVG_LENGTH, 0);
			imageDataNode.addMixin(NodeType.MIX_MIMETYPE);
			return imageDataNode;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot insert empty image after " + sibling, e);
		}
	}

	/** Singleton. */
	private DbkUtils() {
	}
}
