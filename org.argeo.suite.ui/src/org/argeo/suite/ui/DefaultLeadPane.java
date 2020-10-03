package org.argeo.suite.ui;

import java.util.Dictionary;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class DefaultLeadPane implements CmsUiProvider, ManagedService {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());

		Label dashboard = new Label(parent, SWT.NONE);
		CmsUiUtils.style(dashboard, WorkStyles.leadPane);
		dashboard.setText(WorkMsg.dashboard.lead());
		return dashboard;
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		// TODO Auto-generated method stub

	}

}
