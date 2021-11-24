package org.argeo.docbook;

import static org.argeo.docbook.DbkType.para;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

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

	/** Whether this node is a DocBook type. */
	public static boolean isDbk(Node node) {
		String name = Jcr.getName(node);
		for (DbkType type : DbkType.values()) {
			if (name.equals(type.get()))
				return true;
		}
		return false;
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

	/**
	 * Removes a paragraph if it empty. The sesison is not saved.
	 * 
	 * @return true if the paragraph was empty and it was removed
	 */
	public static boolean removeIfEmptyParagraph(Node node) {
		try {
			if (isDbk(node, DbkType.para)) {
				NodeIterator nit = node.getNodes();
				if (!nit.hasNext()) {
					node.remove();
					return true;
				}
				Node first = nit.nextNode();
				if (nit.hasNext())
					return false;
				if (first.getName().equals(Jcr.JCR_XMLTEXT)) {
					String str = Jcr.get(first, Jcr.JCR_XMLCHARACTERS);
					if (str != null && str.trim().equals("")) {
						node.remove();
						return true;
					}
				} else {
					return false;
				}
			}
			return false;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot remove possibly empty paragraph", e);
		}
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

	public static Node insertVideoAfter(Node sibling) {
		try {

			Node parent = sibling.getParent();
			Node mediaNode = addDbk(parent, DbkType.mediaobject);
			// TODO optimise?
			parent.orderBefore(mediaNode.getName() + "[" + mediaNode.getIndex() + "]",
					sibling.getName() + "[" + sibling.getIndex() + "]");
			parent.orderBefore(sibling.getName() + "[" + sibling.getIndex() + "]",
					mediaNode.getName() + "[" + mediaNode.getIndex() + "]");

			Node videoNode = addDbk(mediaNode, DbkType.videoobject);
			Node videoDataNode = addDbk(videoNode, DbkType.videodata);
			return mediaNode;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot insert empty image after " + sibling, e);
		}
	}

	public static String getMediaFileref(Node node) {
		try {
			Node mediadata;
			if (node.hasNode(DbkType.imageobject.get())) {
				mediadata = node.getNode(DbkType.imageobject.get()).getNode(DbkType.imagedata.get());
			} else if (node.hasNode(DbkType.videoobject.get())) {
				mediadata = node.getNode(DbkType.videoobject.get()).getNode(DbkType.videodata.get());
			} else {
				throw new IllegalArgumentException("Fileref not found in " + node);
			}

			if (mediadata.hasProperty(DbkAttr.fileref.name())) {
				return mediadata.getProperty(DbkAttr.fileref.name()).getString();
			} else {
				return null;
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot retrieve file ref from " + node, e);
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

	public static void importXml(Node baseNode, InputStream in) throws IOException {
		try {
			baseNode.getSession().importXML(baseNode.getPath(), in,
					ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
		} catch (RepositoryException e) {
			throw new JcrException("Cannot import XML to " + baseNode, e);
		}

	}

	/** Singleton. */
	private DbkUtils() {
	}

}
