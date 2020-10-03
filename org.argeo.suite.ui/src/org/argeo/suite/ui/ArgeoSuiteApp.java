package org.argeo.suite.ui;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.CmsApp;
import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Constants;

public class ArgeoSuiteApp implements CmsApp {
	private final static Log log = LogFactory.getLog(ArgeoSuiteApp.class);
	public final static String PID_PREFIX = "argeo.work.";
	public final static String HEADER_PID = PID_PREFIX + "header";
	public final static String LEAD_PANE_PID = PID_PREFIX + "leadPane";

	private final static String DEFAULT_UI_NAME = "work";

	private ArgeoSuiteUi argeoSuiteUi;

	private Map<String, CmsUiProvider> uiProviders = new TreeMap<>();

	@Override
	public Set<String> getUiNames() {
		HashSet<String> uiNames = new HashSet<>();
		uiNames.add(DEFAULT_UI_NAME);
		return uiNames;
	}

	@Override
	public void initUi(String uiName, Composite parent) {
		if (DEFAULT_UI_NAME.equals(uiName)) {
			argeoSuiteUi = new ArgeoSuiteUi(parent, SWT.NONE);
			refresh(uiName);
		}

	}

	public void refresh(String uiName) {
		if (DEFAULT_UI_NAME.equals(uiName)) {
			Node context = null;
			uiProviders.get(HEADER_PID).createUiPart(argeoSuiteUi.getHeader(), context);
			uiProviders.get(LEAD_PANE_PID).createUiPart(argeoSuiteUi.getLeadPane(), context);
		}
	}

	public void addUiProvider(CmsUiProvider uiProvider, Map<String, String> properties) {
		String servicePid = properties.get(Constants.SERVICE_PID);
		if (servicePid == null) {
			log.error("No service pid found for " + uiProvider.getClass() + ", " + properties);
		} else {
			uiProviders.put(servicePid, uiProvider);
		}

	}

	public void removeUiProvider(CmsUiProvider uiProvider, Map<String, String> properties) {
		String servicePid = properties.get(Constants.SERVICE_PID);
		uiProviders.remove(servicePid);

	}

//	static class UiProviderKey {
//		private Map<String, String> properties;
//
//		public UiProviderKey(Map<String, String> properties) {
//			super();
//			this.properties = properties;
//		}
//
//	}
}
