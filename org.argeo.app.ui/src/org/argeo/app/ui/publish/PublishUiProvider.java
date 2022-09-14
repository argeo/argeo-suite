package org.argeo.app.ui.publish;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.ux.CmsEditable;
import org.argeo.app.swt.docbook.DocBookViewer;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.ScrolledPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PublishUiProvider implements SwtUiProvider {

	@Override
	public Control createUiPart(Composite parent, Content context) {
		ScrolledPage page = new ScrolledPage(parent, SWT.NONE);
		page.setLayoutData(CmsSwtUtils.fillAll());
		page.setLayout(CmsSwtUtils.noSpaceGridLayout());
		DocBookViewer docBookViewer = new DocBookViewer(page, 0, context, CmsEditable.NON_EDITABLE);
//		docBookViewer.setLayoutData(CmsSwtUtils.fillAll());
		docBookViewer.refresh();
		return docBookViewer.getControl();
	}

}
