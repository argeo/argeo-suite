package org.argeo.suite.fs;

import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.fs.CmsFsBrowser;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Default file browser page for the CMS */
public class MyFilesBrowserPage implements CmsUiProvider {

	private FileSystemProvider nodeFileSystemProvider;

	public MyFilesBrowserPage(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		if (CurrentUser.isAnonymous())
			// TODO implement public file display
			return null;

		CmsFsBrowser browser = new CmsFsBrowser(parent, SWT.NO_FOCUS, context, nodeFileSystemProvider);
		browser.setLayoutData(EclipseUiUtils.fillAll());

		// TODO set input on the default home folder parent for one user's
		// files
		return browser;
	}
}
