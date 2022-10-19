package org.argeo.app.ui;

import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.BundleContext;

/** Footer of a standard Argeo Suite application. */
public class DefaultFooter implements CmsUiProvider {
	@Override
	public Control createUiPart(Composite parent, Content context) {
		parent.setLayout(CmsSwtUtils.noSpaceGridLayout());
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayoutData(new GridData(0, 0));
		Control contentControl = createContent(content, context);

		// TODO support and guarantee

		return contentControl;
	}

	protected Control createContent(Composite parent, Content context) {
		return parent;
	}

	public void init(BundleContext bundleContext, Map<String, String> properties) {
	}

	public void destroy(BundleContext bundleContext, Map<String, String> properties) {

	}
}
