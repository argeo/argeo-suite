package org.argeo.support.openlayers;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class OLMap extends Composite {
	private Label div;

	public OLMap(Composite parent, int style) {
		super(parent, style);
		setLayout(CmsUiUtils.noSpaceGridLayout());
		div = new Label(this, SWT.NONE);
		CmsUiUtils.markup(div);
		CmsUiUtils.disableMarkupValidation(div);
		div.setText("<div id='map'></div>");
		div.setLayoutData(CmsUiUtils.fillAll());
	}

}
