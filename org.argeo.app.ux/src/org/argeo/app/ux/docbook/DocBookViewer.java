package org.argeo.app.ux.docbook;

import org.argeo.api.acr.Content;
import org.argeo.cms.swt.acr.ContentComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DocBookViewer extends ContentComposite {

	public DocBookViewer(Composite parent, int style, Content item) {
		super(parent, style, item);
		new Label(parent, 0).setText(item.toString());
	}

}
