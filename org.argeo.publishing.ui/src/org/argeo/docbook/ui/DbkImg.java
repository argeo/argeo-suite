package org.argeo.docbook.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.widgets.Img;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadListener;
import org.eclipse.rap.fileupload.FileUploadReceiver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** DocBook specific image area. */
public class DbkImg extends Img {
	private static final long serialVersionUID = -6150996708899219074L;

	public DbkImg(Composite parent, int swtStyle, Node imgNode, DbkImageManager imageManager)
			throws RepositoryException {
		super(parent, swtStyle, imgNode, imageManager);
	}

	@Override
	protected Node getUploadFolder() {
		Node mediaFolder = ((DbkImageManager) getImageManager()).getMediaFolder();
		return mediaFolder;
	}

	@Override
	protected String getUploadName() {
		return null;
	}

	@Override
	protected void setContainerLayoutData(Composite composite) {
		composite.setLayoutData(CmsSwtUtils.grabWidth(SWT.CENTER, SWT.DEFAULT));
	}

	@Override
	protected void setControlLayoutData(Control control) {
		control.setLayoutData(CmsSwtUtils.grabWidth(SWT.CENTER, SWT.DEFAULT));
	}

	@Override
	protected FileUploadHandler prepareUpload(FileUploadReceiver receiver) {
		FileUploadHandler fileUploadHandler = super.prepareUpload(receiver);
		fileUploadHandler.addUploadListener(new FileUploadListener() {

			@Override
			public void uploadProgress(FileUploadEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void uploadFinished(FileUploadEvent event) {
			}

			@Override
			public void uploadFailed(FileUploadEvent event) {
				// TODO Auto-generated method stub

			}
		});
		return fileUploadHandler;
	}

}
