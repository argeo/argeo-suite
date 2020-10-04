package org.argeo.suite.ui;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.AbstractCmsApp;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Constants;

/** The Argeo Suite App. */
public class ArgeoSuiteApp extends AbstractCmsApp {
	private final static Log log = LogFactory.getLog(ArgeoSuiteApp.class);

	public final static String PID_PREFIX = "argeo.work.";
	public final static String HEADER_PID = PID_PREFIX + "header";
	public final static String LEAD_PANE_PID = PID_PREFIX + "leadPane";

	private final static String DEFAULT_UI_NAME = "work";
	private final static String DEFAULT_THEME_ID = "org.argeo.suite.theme.default";

	private ArgeoSuiteUi argeoSuiteUi;

	private Map<String, CmsUiProvider> uiProviders = new TreeMap<>();

	public void init(Map<String, String> properties) {
		if (log.isDebugEnabled())
			log.info("Argeo Suite App started");
	}

	public void destroy(Map<String, String> properties) {
		if (log.isDebugEnabled())
			log.info("Argeo Suite App stopped");

	}

	@Override
	public Set<String> getUiNames() {
		HashSet<String> uiNames = new HashSet<>();
		uiNames.add(DEFAULT_UI_NAME);
		return uiNames;
	}

	@Override
	public void initUi(String uiName, Composite parent) {
		if (DEFAULT_UI_NAME.equals(uiName)) {
			CmsTheme theme = getTheme(uiName);
			if (theme != null)
				CmsTheme.registerCmsTheme(parent.getShell(), theme);
			argeoSuiteUi = new ArgeoSuiteUi(parent, SWT.NONE);
			refresh(uiName);
		}

	}

	@Override
	public String getThemeId(String uiName) {
		// TODO make it configurable
		return DEFAULT_THEME_ID;
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
			if (log.isDebugEnabled())
				log.debug("Added UI provider " + servicePid + " to CMS app.");
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
