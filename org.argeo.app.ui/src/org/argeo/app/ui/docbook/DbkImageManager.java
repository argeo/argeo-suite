package org.argeo.app.ui.docbook;

import static javax.jcr.Node.JCR_CONTENT;
import static javax.jcr.Property.JCR_DATA;
import static javax.jcr.nodetype.NodeType.NT_FILE;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.api.cms.Cms2DSize;
import org.argeo.api.cms.CmsImageManager;
import org.argeo.app.api.EntityNames;
import org.argeo.app.api.EntityType;
import org.argeo.app.docbook.DbkAttr;
import org.argeo.app.docbook.DbkType;
import org.argeo.app.docbook.DbkUtils;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.util.DefaultImageManager;
import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.graphics.ImageData;

/** Add DocBook images support to {@link CmsImageManager}. */
public class DbkImageManager extends DefaultImageManager {
	private Node baseFolder = null;

	public DbkImageManager(Node baseFolder) {
		this.baseFolder = baseFolder;
	}

	Node getImageDataNode(Node mediaObjectNode) {
		try {
			if (mediaObjectNode.hasNode(DbkType.imageobject.get())) {
				Node imageDataNode = mediaObjectNode.getNode(DbkType.imageobject.get())
						.getNode(DbkType.imagedata.get());
				return imageDataNode;
			} else {
				throw new IllegalStateException("No image data found for " + mediaObjectNode);
			}
		} catch (RepositoryException e) {
			throw new JcrException(e);
		}
	}

	@Override
	public Binary getImageBinary(Node node) {
		Node fileNode = null;
		if (DbkUtils.isDbk(node, DbkType.mediaobject)) {
			Node imageDataNode = getImageDataNode(node);
			fileNode = getFileNode(imageDataNode);
		}
		try {
			if (node.isNodeType(NT_FILE)) {
				fileNode = node;
			}
			if (fileNode != null) {
				return node.getNode(JCR_CONTENT).getProperty(JCR_DATA).getBinary();
			} else {
				return null;
			}
		} catch (RepositoryException e) {
			throw new JcrException(e);
		}
	}

	public Cms2DSize getImageSize(Node mediaObjectNode) {
		Node imageDataNode = getImageDataNode(mediaObjectNode);
		Node fileNode = getFileNode(imageDataNode);
		if (fileNode == null)
			return new Cms2DSize(0, 0);
		try {
			Cms2DSize intrinsicSize;
			if (fileNode.hasProperty(EntityNames.SVG_WIDTH) && fileNode.hasProperty(EntityNames.SVG_HEIGHT)) {
				int width = (int) fileNode.getProperty(EntityNames.SVG_WIDTH).getLong();
				int height = (int) fileNode.getProperty(EntityNames.SVG_HEIGHT).getLong();
				intrinsicSize = new Cms2DSize(width, height);
			} else {
				try (InputStream in = JcrUtils.getFileAsStream(fileNode)) {
					ImageData id = new ImageData(in);
					intrinsicSize = updateSize(fileNode, id);
				} catch (IOException e) {
					throw new RuntimeException("Cannot load file " + fileNode, e);
				}
			}
			// TODO interpret image data infos
			return intrinsicSize;
		} catch (RepositoryException e) {
			throw new JcrException(e);
		}
	}

	protected Cms2DSize updateSize(Node fileNode, ImageData id) throws RepositoryException {
		fileNode.addMixin(EntityType.box.get());
		fileNode.setProperty(EntityNames.SVG_WIDTH, id.width);
		fileNode.setProperty(EntityNames.SVG_HEIGHT, id.height);
		return new Cms2DSize(id.width, id.height);
	}

	@Override
	protected void processNewImageFile(Node mediaObjectNode, Node fileNode, ImageData id)
			throws RepositoryException, IOException {
		Node imageDataNode = getImageDataNode(mediaObjectNode);
		updateSize(fileNode, id);
		String filePath = fileNode.getPath();
		String relPath = filePath.substring(baseFolder.getPath().length() + 1);
		imageDataNode.setProperty(DbkAttr.fileref.name(), relPath);
	}

	@Override
	public String getImageUrl(Node mediaObjectNode) {
		Node imageDataNode = getImageDataNode(mediaObjectNode);
		// TODO factorise
		String fileref = null;
		try {
			if (imageDataNode.hasProperty(DbkAttr.fileref.name()))
				fileref = imageDataNode.getProperty(DbkAttr.fileref.name()).getString();
		} catch (RepositoryException e) {
			throw new JcrException(e);
		}
		if (fileref == null)
			return null;
		URI fileUri;
		try {
			// FIXME it messes up with the '/'
			fileUri = new URI(URLEncoder.encode(fileref, StandardCharsets.UTF_8.toString()));
		} catch (URISyntaxException | UnsupportedEncodingException e) {
			throw new IllegalArgumentException("File ref in " + imageDataNode + " is badly formatted", e);
		}
		if (fileUri.getScheme() != null)
			return fileUri.toString();
		// local
		Node fileNode = getFileNode(imageDataNode);
		String url = CmsUiUtils.getDataPathForUrl(fileNode);
		return url;
	}

	protected Node getFileNode(Node imageDataNode) {
		// FIXME make URL use case more robust
		try {
			String fileref = null;
			if (imageDataNode.hasProperty(DbkAttr.fileref.name()))
				fileref = imageDataNode.getProperty(DbkAttr.fileref.name()).getString();
			if (fileref == null)
				return null;
			Node fileNode;
			if (fileref.startsWith("/"))
				fileNode = baseFolder.getSession().getNode(fileref);
			else
				fileNode = baseFolder.getNode(fileref);
			return fileNode;
		} catch (RepositoryException e) {
			throw new JcrException(e);
		}
	}

	protected Node getMediaFolder() {
		try {
			// TODO check edition status
			Node mediaFolder = JcrUtils.getOrAdd(baseFolder, EntityNames.MEDIA, NodeType.NT_FOLDER);
			return mediaFolder;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot get media folder", e);
		}
	}
}
