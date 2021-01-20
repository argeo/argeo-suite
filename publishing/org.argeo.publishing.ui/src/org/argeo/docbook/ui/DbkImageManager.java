package org.argeo.docbook.ui;

import java.io.IOException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsImageManager;
import org.argeo.cms.ui.util.DefaultImageManager;
import org.argeo.entity.EntityNames;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/** Add DocBook images support to {@link CmsImageManager}. */
public class DbkImageManager extends DefaultImageManager {
	@Override
	public Binary getImageBinary(Node node) throws RepositoryException {
		Binary binary = super.getImageBinary(node);
		return binary;
	}

	public Point getImageSize(Node node) throws RepositoryException {
		int width = node.hasProperty(EntityNames.SVG_WIDTH) ? (int) node.getProperty(EntityNames.SVG_WIDTH).getLong()
				: 0;
		int height = node.hasProperty(EntityNames.SVG_HEIGHT) ? (int) node.getProperty(EntityNames.SVG_HEIGHT).getLong()
				: 0;
		return new Point(width, height);
	}

	@Override
	protected void processNewImageFile(Node fileNode, ImageData id) throws RepositoryException, IOException {
		fileNode.setProperty(EntityNames.SVG_WIDTH, id.width);
		fileNode.setProperty(EntityNames.SVG_HEIGHT, id.height);
	}

}
