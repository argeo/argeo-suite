package org.argeo.suite.ui;

import java.util.Dictionary;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.LocaleUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.util.LangUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class DefaultHeader implements CmsUiProvider, ManagedService {
	public final static String TITLE_PROPERTY = "argeo.work.header.title";
	private Map<String, String> properties;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		Label lbl = new Label(parent, SWT.NONE);
		String title = properties.get(TITLE_PROPERTY);
		lbl.setText(LocaleUtils.isLocaleKey(title) ? LocaleUtils.local(title, getClass().getClassLoader()).toString()
				: title);
		CmsUiUtils.style(lbl, WorkStyles.header);
		lbl.setLayoutData(CmsUiUtils.fillWidth());
		return lbl;
	}

	public void init(Map<String, String> properties) {
		this.properties = new TreeMap<>(properties);
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties != null)
			this.properties.putAll(LangUtils.dictToStringMap(properties));
	}

}
