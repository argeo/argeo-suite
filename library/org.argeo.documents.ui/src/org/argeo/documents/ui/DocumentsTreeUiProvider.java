package org.argeo.documents.ui;

import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.NodeUtils;
import org.argeo.cms.fs.CmsFsUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.eclipse.ui.fs.FsTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Tree view of a user root folders. */
public class DocumentsTreeUiProvider implements CmsUiProvider {
	private FileSystemProvider nodeFileSystemProvider;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		FsTreeViewer fsTreeViewer = new FsTreeViewer(parent, SWT.NONE);
		fsTreeViewer.configureDefaultSingleColumnTable(500);
		Node homeNode = NodeUtils.getUserHome(context.getSession());
		Path homePath = CmsFsUtils.getPath(nodeFileSystemProvider, homeNode);
		fsTreeViewer.setPathsInput(homePath);
		fsTreeViewer.getControl().setLayoutData(CmsUiUtils.fillAll());
		fsTreeViewer.getControl().getParent().layout(true, true);
		return fsTreeViewer.getControl();
	}

	public void setNodeFileSystemProvider(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}

}
