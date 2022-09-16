package org.argeo.app.swt.docbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.DName;
import org.argeo.api.acr.spi.ProvidedContent;
import org.argeo.api.cms.ux.Cms2DSize;
import org.argeo.api.cms.ux.CmsImageManager;
import org.argeo.app.api.EntityNames;
import org.argeo.app.api.EntityType;
import org.argeo.app.docbook.DbkAttr;
import org.argeo.app.docbook.DbkType;
import org.argeo.cms.acr.SvgAttrs;
import org.argeo.cms.swt.acr.AcrSwtImageManager;
import org.eclipse.swt.graphics.ImageData;

/** Add DocBook images support to {@link CmsImageManager}. */
public class DbkImageManager extends AcrSwtImageManager {
	private Content baseFolder = null;

	public DbkImageManager(Content baseFolder) {
		this.baseFolder = baseFolder;
	}

	Content getImageDataNode(Content mediaObjectNode) {
		return mediaObjectNode.child(DbkType.imageobject).child(DbkType.imagedata);
	}

//	@Override
//	public Binary getImageBinary(Node node) {
//		Node fileNode = null;
//		if (DbkUtils.isDbk(node, DbkType.mediaobject)) {
//			Node imageDataNode = getImageDataNode(node);
//			fileNode = getFileNode(imageDataNode);
//		}
//		try {
//			if (node.isNodeType(NT_FILE)) {
//				fileNode = node;
//			}
//			if (fileNode != null) {
//				return node.getNode(JCR_CONTENT).getProperty(JCR_DATA).getBinary();
//			} else {
//				return null;
//			}
//		} catch (RepositoryException e) {
//			throw new JcrException(e);
//		}
//	}

	public Cms2DSize getImageSize(Content mediaObjectNode) {
		Content imageDataNode = getImageDataNode(mediaObjectNode);
		Content fileNode = getFileNode(imageDataNode);
		if (fileNode == null)
			return new Cms2DSize(0, 0);
		Cms2DSize intrinsicSize;
		if (fileNode.containsKey(SvgAttrs.width) && fileNode.containsKey(SvgAttrs.height)) {
			int width = fileNode.get(SvgAttrs.width, Integer.class).orElseThrow();
			int height = fileNode.get(SvgAttrs.height, Integer.class).orElseThrow();
			intrinsicSize = new Cms2DSize(width, height);
		} else {
			try (InputStream in = fileNode.open(InputStream.class)) {
				ImageData id = new ImageData(in);
				intrinsicSize = updateSize(fileNode, id);
			} catch (IOException e) {
				throw new RuntimeException("Cannot load file " + fileNode, e);
			}
		}
		// TODO interpret image data infos
		return intrinsicSize;
	}

	protected Cms2DSize updateSize(Content fileNode, ImageData id) {
		fileNode.addContentClasses(EntityType.box.qName());
		fileNode.put(SvgAttrs.width, id.width);
		fileNode.put(SvgAttrs.height, id.height);
		return new Cms2DSize(id.width, id.height);
	}

//	@Override
//	protected void processNewImageFile(Content mediaObjectNode, Content fileNode, ImageData id) throws IOException {
//		Node imageDataNode = getImageDataNode(mediaObjectNode);
//		updateSize(fileNode, id);
//		String filePath = fileNode.getPath();
//		String relPath = filePath.substring(baseFolder.getPath().length() + 1);
//		imageDataNode.setProperty(DbkAttr.fileref.name(), relPath);
//	}

	@Override
	public String getImageUrl(Content mediaObjectNode) {
		Content imageDataNode = getImageDataNode(mediaObjectNode);
		// TODO factorise
		String fileref = imageDataNode.get(DbkAttr.fileref, String.class).orElse(null);
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
		Content fileNode = getFileNode(imageDataNode);
		String url = getDataPathForUrl(fileNode);
		return url;
	}

	protected Content getFileNode(Content imageDataNode) {
		// FIXME make URL use case more robust
		String fileref = imageDataNode.get(DbkAttr.fileref, String.class).orElse(null);
		if (fileref == null)
			return null;
		return ((ProvidedContent) baseFolder).getContent(fileref);
	}

	protected Content getMediaFolder() {
		// TODO check edition status
		Content mediaFolder = baseFolder.anyOrAddChild(EntityNames.MEDIA, DName.collection.qName());
		return mediaFolder;
	}
}
