package org.argeo.cms.text;

import java.io.IOException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.util.DefaultImageManager;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/** Manages only public images so far. */
public class CmsTextImageManager extends DefaultImageManager {
	@Override
	public Point getImageSize(Node node) throws RepositoryException {
		return new Point(
				node.hasProperty(CmsNames.CMS_IMAGE_WIDTH) ? (int) node.getProperty(CmsNames.CMS_IMAGE_WIDTH).getLong()
						: 0,
				node.hasProperty(CmsNames.CMS_IMAGE_HEIGHT)
						? (int) node.getProperty(CmsNames.CMS_IMAGE_HEIGHT).getLong()
						: 0);
	}

	@Override
	public Binary getImageBinary(Node node) throws RepositoryException {
		Binary res = super.getImageBinary(node);
		if (res == null && node.isNodeType(CmsTypes.CMS_STYLED) && node.hasProperty(CmsNames.CMS_DATA)) {
			return node.getProperty(CmsNames.CMS_DATA).getBinary();
		} else {
			return null;
		}
	}

	@Override
	protected void processNewImageFile(Node context,
			Node fileNode, ImageData id)
			throws RepositoryException, IOException {
		fileNode.addMixin(CmsTypes.CMS_IMAGE);
		fileNode.setProperty(CmsNames.CMS_IMAGE_WIDTH, id.width);
		fileNode.setProperty(CmsNames.CMS_IMAGE_HEIGHT, id.height);

	}
}
