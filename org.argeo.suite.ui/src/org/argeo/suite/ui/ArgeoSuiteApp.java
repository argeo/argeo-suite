package org.argeo.suite.ui;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.AbstractCmsApp;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Constants;

/** The Argeo Suite App. */
public class ArgeoSuiteApp extends AbstractCmsApp {
	private final static Log log = LogFactory.getLog(ArgeoSuiteApp.class);

	public final static String PID_PREFIX = "argeo.suite.ui.";
	public final static String HEADER_PID = PID_PREFIX + "header";
	public final static String LEAD_PANE_PID = PID_PREFIX + "leadPane";
	public final static String LOGIN_SCREEN_PID = PID_PREFIX + "loginScreen";
	public final static String DASHBOARD_PID = PID_PREFIX + "dashboard";
	public final static String RECENT_ITEMS_PID = PID_PREFIX + "recentItems";

	private final static String DEFAULT_UI_NAME = "work";
	private final static String DEFAULT_THEME_ID = "org.argeo.suite.theme.default";

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
	public Composite initUi(Composite parent) {
		String uiName = parent.getData(UI_NAME_PROPERTY) != null ? parent.getData(UI_NAME_PROPERTY).toString() : null;
		CmsTheme theme = getTheme(uiName);
		if (theme != null)
			CmsTheme.registerCmsTheme(parent.getShell(), theme);
		ArgeoSuiteUi argeoSuiteUi = new ArgeoSuiteUi(parent, SWT.NONE);
		refreshUi(argeoSuiteUi, null);
		return argeoSuiteUi;
	}

	@Override
	public String getThemeId(String uiName) {
		// TODO make it configurable
		return DEFAULT_THEME_ID;
	}

	@Override
	public void refreshUi(Composite parent, String state) {
		Node context = null;
		ArgeoSuiteUi argeoSuiteUi = (ArgeoSuiteUi) parent;
		refreshPart(findUiProvider(HEADER_PID, context), argeoSuiteUi.getHeader(), context);
		CmsView cmsView = CmsView.getCmsView(parent);
		if (cmsView.isAnonymous()) {
			refreshPart(findUiProvider(LOGIN_SCREEN_PID, context), argeoSuiteUi.getDefaultBody(), context);
		} else {
			refreshPart(findUiProvider(DASHBOARD_PID, context), argeoSuiteUi.getDefaultBody(), context);
		}
		refreshPart(findUiProvider(LEAD_PANE_PID, context), argeoSuiteUi.getLeadPane(), context);
		refreshPart(findUiProvider(RECENT_ITEMS_PID, context), argeoSuiteUi.getEntryArea(), context);
		argeoSuiteUi.layout(true, true);
	}

	private void refreshPart(CmsUiProvider uiProvider, Composite part, Node context) {
		for (Control child : part.getChildren())
			child.dispose();
		uiProvider.createUiPart(part, context);
	}

	private CmsUiProvider findUiProvider(String pid, Node context) {
		if (pid != null) {
			if (uiProviders.containsKey(pid))
				return uiProviders.get(pid);
		}

		// nothing
		return new CmsUiProvider() {

			@Override
			public Control createUi(Composite parent, Node context) throws RepositoryException {
				return parent;
			}
		};
	}

	@Override
	public void setState(Composite parent, String state) {
		CmsView cmsView = CmsView.getCmsView(parent);
		// for the time being we systematically open a session, in order to make sure
		// that home is initialised
		Session session = null;
		try {
			if (state != null && state.startsWith("/")) {
				String path = state.substring(1);
				String workspace;
				if (path.equals("")) {
					workspace = null;
					path = "/";
				} else {
					int index = path.indexOf('/');
					if (index == 0) {
						log.error("Cannot interpret // " + state);
						cmsView.navigateTo("~");
						return;
					} else if (index > 0) {
						workspace = path.substring(0, index);
						path = path.substring(index);
					} else {// index<0, assuming root node
						workspace = path;
						path = "/";
					}
				}
				session = getRepository().login(workspace);

				Node node = session.getNode(path);
				refreshEntityUi(node);
			}
		} catch (RepositoryException e) {
			log.error("Cannot load state " + state, e);
			cmsView.navigateTo("~");
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

	private void refreshEntityUi(Node node) {

	}

	/*
	 * Dependency injection.
	 */

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
}
