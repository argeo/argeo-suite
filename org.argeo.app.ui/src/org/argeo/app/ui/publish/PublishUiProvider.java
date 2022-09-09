package org.argeo.app.ui.publish;

import org.argeo.api.acr.Content;
import org.argeo.app.ux.docbook.DocBookViewer;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PublishUiProvider implements SwtUiProvider {

	@Override
	public Control createUiPart(Composite parent, Content context) {
		DocBookViewer docBookViewer = new DocBookViewer(parent, 0, context);
		return docBookViewer;
	}

}
