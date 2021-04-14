package org.argeo.suite.ui;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.BundleContext;

/** Footer of a standard Argeo Suite application. */
public class DefaultFooter implements CmsUiProvider {
	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(CmsUiUtils.noSpaceGridLayout());
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayoutData(new GridData(0, 0));
		Control contentControl = createContent(content, context);

		// TODO support and guarantee

		return contentControl;
	}

	protected Control createContent(Composite parent, Node context) throws RepositoryException {
		return parent;
	}

	public void init(BundleContext bundleContext, Map<String, String> properties) {
	}

	public void destroy(BundleContext bundleContext, Map<String, String> properties) {

	}
}
