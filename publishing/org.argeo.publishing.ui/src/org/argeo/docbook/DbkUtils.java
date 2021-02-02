package org.argeo.docbook;

import static org.argeo.docbook.DbkType.para;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.JcrxApi;

/** Utilities around DocBook. */
public class DbkUtils {
	private final static Log log = LogFactory.getLog(DbkUtils.class);

	/** Get or add a DocBook element. */
	public static Node getOrAddDbk(Node parent, DbkType child) {
		try {
			if (!parent.hasNode(child.get())) {
				return addDbk(parent, child);
			} else {
				return parent.getNode(child.get());
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot get or add element " + child.get() + " to " + parent, e);
		}
	}

	/** Add a DocBook element to this node. */
	public static Node addDbk(Node parent, DbkType child) {
		try {
			Node node = parent.addNode(child.get(), child.get());
			return node;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot add element " + child.get() + " to " + parent, e);
		}
	}

	/** Whether this DocBook element is of this type. */
	public static boolean isDbk(Node node, DbkType type) {
		return Jcr.getName(node).equals(type.get());
	}

	public static String getTitle(Node node) {
		return JcrxApi.getXmlValue(node, DbkType.title.get());
	}

	public static void setTitle(Node node, String txt) {
		Node titleNode = getOrAddDbk(node, DbkType.title);
		JcrxApi.setXmlValue(node, titleNode, txt);
	}

	public static Node getMetadata(Node infoContainer) {
		try {
			if (!infoContainer.hasNode(DbkType.info.get()))
				return null;
			Node info = infoContainer.getNode(DbkType.info.get());
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
				String r = Jcr.get(n, DbkAttr.role.name());
				if (r != null && r.equals(role))
					return n;
			}
			return null;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot get child from " + parent + " with role " + role, e);
		}
	}

	public static Node addParagraph(Node node, String txt) {
		Node p = addDbk(node, para);
		JcrxApi.setXmlValue(node, p, txt);
		return p;
	}

	public static Node insertImageAfter(Node sibling) {
		try {

			Node parent = sibling.getParent();
			Node mediaNode = addDbk(parent, DbkType.mediaobject);
			// TODO optimise?
			parent.orderBefore(mediaNode.getName() + "[" + mediaNode.getIndex() + "]",
					sibling.getName() + "[" + sibling.getIndex() + "]");
			parent.orderBefore(sibling.getName() + "[" + sibling.getIndex() + "]",
					mediaNode.getName() + "[" + mediaNode.getIndex() + "]");

			Node imageNode = addDbk(mediaNode, DbkType.imageobject);
			Node imageDataNode = addDbk(imageNode, DbkType.imagedata);
//			Node infoNode = imageNode.addNode(DocBookTypes.INFO, DocBookTypes.INFO);
//			Node fileNode = JcrUtils.copyBytesAsFile(mediaFolder, EntityType.box.get(), new byte[0]);
//			fileNode.addMixin(EntityType.box.get());
//			fileNode.setProperty(EntityNames.SVG_WIDTH, 0);
//			fileNode.setProperty(EntityNames.SVG_LENGTH, 0);
//			fileNode.addMixin(NodeType.MIX_MIMETYPE);
//
//			// we assume this is a folder next to the main DocBook document
//			// TODO make it more robust and generic
//			String fileRef = mediaNode.getName();
//			imageDataNode.setProperty(DocBookNames.DBK_FILEREF, fileRef);
			return mediaNode;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot insert empty image after " + sibling, e);
		}
	}

	public static void exportXml(Node node, OutputStream out) throws IOException {
		try {
			node.getSession().exportDocumentView(node.getPath(), out, false, false);
		} catch (RepositoryException e) {
			throw new JcrException("Cannot export " + node + " to XML", e);
		}
	}

	public static void exportToFs(Node baseNode, DbkType type, Path directory) {
		String fileName = Jcr.getName(baseNode) + ".dbk.xml";
		Path filePath = directory.resolve(fileName);
		Node docBookNode = Jcr.getNode(baseNode, type.get());
		if (docBookNode == null)
			throw new IllegalArgumentException("No " + type.get() + " under " + baseNode);
		try {
			Files.createDirectories(directory);
			try (OutputStream out = Files.newOutputStream(filePath)) {
				exportXml(docBookNode, out);
			}
			JcrUtils.copyFilesToFs(baseNode, directory, true);
			if (log.isDebugEnabled())
				log.debug("DocBook " + baseNode + " exported to " + filePath.toAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** Singleton. */
	private DbkUtils() {
	}

}
