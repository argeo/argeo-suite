package org.argeo.library.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.argeo.api.cms.CmsView;
import org.argeo.api.cms.CmsConstants;
import org.argeo.cms.fs.CmsFsUtils;
import org.argeo.cms.jcr.CmsJcrUtils;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.eclipse.ui.fs.FsTreeViewer;
import org.argeo.jcr.Jcr;
import org.argeo.suite.ui.SuiteEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Tree view of a user root folders. */
public class DocumentsTreeUiProvider implements CmsUiProvider {
	private FileSystemProvider nodeFileSystemProvider;
	private Repository repository;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		FsTreeViewer fsTreeViewer = new FsTreeViewer(parent, SWT.NONE);
		fsTreeViewer.configureDefaultSingleColumnTable(500);
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		Node homeNode = CmsJcrUtils.getUserHome(cmsView.doAs(() -> Jcr.login(repository, CmsConstants.HOME_WORKSPACE)));
		parent.addDisposeListener((e1) -> Jcr.logout(homeNode));
		Path homePath = CmsFsUtils.getPath(nodeFileSystemProvider, homeNode);
		fsTreeViewer.addSelectionChangedListener((e) -> {
			IStructuredSelection selection = (IStructuredSelection) fsTreeViewer.getSelection();
			if (selection.isEmpty())
				return;
			else {
				Path newSelected = (Path) selection.getFirstElement();
				if (Files.isDirectory(newSelected)) {
					Node folderNode = cmsView.doAs(() -> CmsFsUtils.getNode(repository, newSelected));
					parent.addDisposeListener((e1) -> Jcr.logout(folderNode));
					cmsView.sendEvent(SuiteEvent.refreshPart.topic(), SuiteEvent.eventProperties(folderNode));
				}
			}
		});
		fsTreeViewer.addDoubleClickListener((e) -> {
			IStructuredSelection selection = (IStructuredSelection) fsTreeViewer.getSelection();
			if (selection.isEmpty())
				return;
			else {
				Path newSelected = (Path) selection.getFirstElement();
				if (Files.isDirectory(newSelected)) {
					Node folderNode = cmsView.doAs(() -> CmsFsUtils.getNode(repository, newSelected));
					parent.addDisposeListener((e1) -> Jcr.logout(folderNode));
					cmsView.sendEvent(SuiteEvent.openNewPart.topic(), SuiteEvent.eventProperties(folderNode));
				}
			}
		});
		fsTreeViewer.setPathsInput(homePath);
		fsTreeViewer.getControl().setLayoutData(CmsSwtUtils.fillAll());
		fsTreeViewer.getControl().getParent().layout(true, true);
		return fsTreeViewer.getControl();
	}

	public void setNodeFileSystemProvider(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
