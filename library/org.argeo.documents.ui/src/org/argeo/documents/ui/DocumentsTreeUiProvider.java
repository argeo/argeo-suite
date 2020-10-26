package org.argeo.documents.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.argeo.api.NodeUtils;
import org.argeo.cms.fs.CmsFsUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
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
		Node homeNode = NodeUtils.getUserHome(context.getSession());
		Path homePath = CmsFsUtils.getPath(nodeFileSystemProvider, homeNode);
		CmsView cmsView = CmsView.getCmsView(parent);
		fsTreeViewer.addSelectionChangedListener((e) -> {
			IStructuredSelection selection = (IStructuredSelection) fsTreeViewer.getSelection();
			if (selection.isEmpty())
				return;
			else {
				Path newSelected = (Path) selection.getFirstElement();
				if (Files.isDirectory(newSelected)) {
					Node folderNode = cmsView.doAs(() -> CmsFsUtils.getNode(repository, newSelected));
					parent.addDisposeListener((e1) -> Jcr.logout(folderNode));
					Map<String, Object> properties = new HashMap<>();
					properties.put(SuiteEvent.NODE_ID, Jcr.getIdentifier(folderNode));
					properties.put(SuiteEvent.WORKSPACE, Jcr.getWorkspaceName(folderNode));
					cmsView.sendEvent(SuiteEvent.refreshPart.topic(), properties);
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
					Map<String, Object> properties = new HashMap<>();
					properties.put(SuiteEvent.NODE_ID, Jcr.getIdentifier(folderNode));
					properties.put(SuiteEvent.WORKSPACE, Jcr.getWorkspaceName(folderNode));
					cmsView.sendEvent(SuiteEvent.openNewPart.topic(), properties);
				}
			}
		});
		fsTreeViewer.setPathsInput(homePath);
		fsTreeViewer.getControl().setLayoutData(CmsUiUtils.fillAll());
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
