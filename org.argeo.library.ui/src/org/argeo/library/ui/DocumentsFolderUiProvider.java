package org.argeo.library.ui;

import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.cms.CmsView;
import org.argeo.cms.fs.CmsFsUtils;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.jcr.Jcr;
import org.argeo.suite.ui.SuiteEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** UI provider of a document folder. */
public class DocumentsFolderUiProvider implements CmsUiProvider {
	private FileSystemProvider nodeFileSystemProvider;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		DocumentsFolderComposite dfc = new DocumentsFolderComposite(parent, SWT.NONE, context) {

			@Override
			protected void externalNavigateTo(Path path) {
				Node folderNode = cmsView.doAs(() -> CmsFsUtils.getNode(Jcr.getSession(context).getRepository(), path));
				parent.addDisposeListener((e1) -> Jcr.logout(folderNode));
				cmsView.sendEvent(SuiteEvent.openNewPart.topic(), SuiteEvent.eventProperties(folderNode));
			}
		};
		dfc.setLayoutData(CmsSwtUtils.fillAll());
		dfc.populate(cmsView.doAs(() -> CmsFsUtils.getPath(nodeFileSystemProvider, context)));
		return dfc;
	}

	public void setNodeFileSystemProvider(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}

}
