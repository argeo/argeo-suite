package org.argeo.suite.ui;

import java.util.Dictionary;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.Localized;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsIcon;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/** Side pane listing various perspectives. */
public class DefaultLeadPane implements CmsUiProvider, ManagedService {
	@Override
	public Control createUi(Composite parent, Node node) throws RepositoryException {
		CmsView cmsView = CmsView.getCmsView(parent);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		parent.setLayout(layout);

		Button dashboardB = createButton(parent, SuiteMsg.dashboard, SuiteIcon.dashboard);
		if (!cmsView.isAnonymous()) {
			createButton(parent, SuiteMsg.documents, SuiteIcon.documents);
			createButton(parent, SuiteMsg.people, SuiteIcon.people);
			createButton(parent, SuiteMsg.locations, SuiteIcon.location);
		}
		return dashboardB;
	}

	protected Button createButton(Composite parent, Localized msg, CmsIcon icon) {
		CmsTheme theme = CmsTheme.getCmsTheme(parent);
		Button button = new Button(parent, SWT.FLAT);
		CmsUiUtils.style(button, SuiteStyle.leadPane);
		button.setToolTipText(msg.lead());
		button.setImage(icon.getBigIcon(theme));
		return button;
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	public void init(Map<String, String> properties) {

	}
}
