package org.argeo.app.ui.people;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class HierarchyUnitUiProvider implements SwtUiProvider {
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		new Label(parent,0).setText("Hierarchy unit "+context);
		return null;
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

}
