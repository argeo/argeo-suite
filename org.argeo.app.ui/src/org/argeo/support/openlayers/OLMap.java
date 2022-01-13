package org.argeo.support.openlayers;

import org.argeo.cms.swt.CmsSwtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class OLMap extends Composite {
	private Label div;

	public OLMap(Composite parent, int style) {
		super(parent, style);
		setLayout(CmsSwtUtils.noSpaceGridLayout());
		div = new Label(this, SWT.NONE);
		CmsSwtUtils.markup(div);
		CmsSwtUtils.disableMarkupValidation(div);
		div.setText("<div id='map'></div>");
		div.setLayoutData(CmsSwtUtils.fillAll());
	}

}
