package org.argeo.documents.ui;

import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.fs.CmsFsUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** UI provider of a document folder. */
public class DocumentsFolderUiProvider implements CmsUiProvider {
	private FileSystemProvider nodeFileSystemProvider;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsView cmsView = CmsView.getCmsView(parent);
		DocumentsFolderComposite dfc = new DocumentsFolderComposite(parent, SWT.NONE, context);
		dfc.setLayoutData(CmsUiUtils.fillAll());
		dfc.populate(cmsView.doAs(() -> CmsFsUtils.getPath(nodeFileSystemProvider, context)));
		return dfc;
	}

	public void setNodeFileSystemProvider(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}

}