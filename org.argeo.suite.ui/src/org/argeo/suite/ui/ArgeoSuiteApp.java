package org.argeo.suite.ui;

import static org.argeo.cms.ui.CmsView.CMS_VIEW_UID_PROPERTY;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.RankingKey;
import org.argeo.cms.ui.AbstractCmsApp;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.dialogs.CmsFeedback;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/** The Argeo Suite App. */
public class ArgeoSuiteApp extends AbstractCmsApp implements EventHandler {
	private final static Log log = LogFactory.getLog(ArgeoSuiteApp.class);

	public final static String PID_PREFIX = "argeo.suite.ui.";
	public final static String HEADER_PID = PID_PREFIX + "header";
	public final static String LEAD_PANE_PID = PID_PREFIX + "leadPane";
	public final static String LOGIN_SCREEN_PID = PID_PREFIX + "loginScreen";
	public final static String DASHBOARD_PID = PID_PREFIX + "dashboard";
	public final static String RECENT_ITEMS_PID = PID_PREFIX + "recentItems";

	private final static String DEFAULT_UI_NAME = "work";
	private final static String DEFAULT_THEME_ID = "org.argeo.suite.theme.default";

	private SortedMap<RankingKey, CmsUiProvider> uiProviders = Collections.synchronizedSortedMap(new TreeMap<>());

	// TODO make more optimal or via CmsSession/CmsView
	private Map<String, ArgeoSuiteUi> managedUis = new HashMap<>();

//	private CmsUiProvider headerPart = null;

	public void init(Map<String, String> properties) {
		if (log.isDebugEnabled())
			log.info("Argeo Suite App started");
	}

	public void destroy(Map<String, String> properties) {
		for (ArgeoSuiteUi ui : managedUis.values())
			if (!ui.isDisposed())
				ui.dispose();
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
		CmsView cmsView = CmsView.getCmsView(parent);
		CmsTheme theme = getTheme(uiName);
		if (theme != null)
			CmsTheme.registerCmsTheme(parent.getShell(), theme);
		ArgeoSuiteUi argeoSuiteUi = new ArgeoSuiteUi(parent, SWT.NONE);
		String uid = cmsView.getUid();
		managedUis.put(uid, argeoSuiteUi);
		argeoSuiteUi.addDisposeListener((e) -> {
			managedUis.remove(uid);
			if (log.isDebugEnabled())
				log.debug("Suite UI " + uid + " has been disposed.");
		});
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
		try {
			Node context = null;
			ArgeoSuiteUi argeoSuiteUi = (ArgeoSuiteUi) parent;
			refreshPart(findUiProvider(HEADER_PID, context), argeoSuiteUi.getHeader(), context);
			CmsView cmsView = CmsView.getCmsView(parent);
			if (cmsView.isAnonymous()) {
				refreshPart(findUiProvider(LOGIN_SCREEN_PID, context), argeoSuiteUi.getDefaultBody(), context);
			} else {
				try {
					if (argeoSuiteUi.getSession() == null)
						argeoSuiteUi.setSession(getRepository().login());
					context = argeoSuiteUi.getSession().getRootNode();

				} catch (RepositoryException e) {
					e.printStackTrace();
				}
				refreshPart(findUiProvider(DASHBOARD_PID, context), argeoSuiteUi.getDefaultBody(), context);
			}
			refreshPart(findUiProvider(LEAD_PANE_PID, context), argeoSuiteUi.getLeadPane(), context);
			refreshPart(findUiProvider(RECENT_ITEMS_PID, context), argeoSuiteUi.getEntryArea(), context);
			argeoSuiteUi.layout(true, true);
		} catch (Exception e) {
			CmsFeedback.show("Unexpected exception", e);
		}
	}

	private void refreshPart(CmsUiProvider uiProvider, Composite part, Node context) {
		for (Control child : part.getChildren())
			child.dispose();
		uiProvider.createUiPart(part, context);
	}

	private CmsUiProvider findUiProvider(String pid, Node context) {
		if (pid != null) {
			SortedMap<RankingKey, CmsUiProvider> subMap = uiProviders.subMap(RankingKey.minPid(pid),
					RankingKey.maxPid(pid));
			CmsUiProvider found = null;
			providers: for (RankingKey key : subMap.keySet()) {
				if (key.getPid() == null || !key.getPid().equals(pid))
					break providers;
				found = subMap.get(key);
				log.debug(key);
			}
//			if (uiProviders.containsKey(pid))
//				return uiProviders.get(pid);
			if (found != null)
				return found;
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

	public void addUiProvider(CmsUiProvider uiProvider, Map<String, Object> properties) {
		RankingKey partKey = new RankingKey(properties);
//		String servicePid = properties.get(Constants.SERVICE_PID);
//		if (servicePid == null) {
//			log.error("No service pid found for " + uiProvider.getClass() + ", " + properties);
//		} else {
		uiProviders.put(partKey, uiProvider);
		if (log.isDebugEnabled())
			log.debug("Added UI provider " + partKey + " to CMS app.");
//		}

	}

	public void removeUiProvider(CmsUiProvider uiProvider, Map<String, Object> properties) {
		RankingKey partKey = new RankingKey(properties);
//		String servicePid = properties.get(Constants.SERVICE_PID);
		uiProviders.remove(partKey);

	}

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(SuiteEvent.switchLayer.topic())) {
			String layer = get(event, SuiteEvent.LAYER_PARAM);
			managedUis.get(get(event, CMS_VIEW_UID_PROPERTY)).switchToLayer(layer);
		}

	}

	private static String get(Event event, String key) {
		Object value = event.getProperty(key);
		if (value == null)
			throw new IllegalArgumentException("Property " + key + " must be set");
		return value.toString();

	}

//	public void setHeaderPart(CmsUiProvider headerPart) {
//		this.headerPart = headerPart;
//		if (log.isDebugEnabled())
//			log.debug("Header set.");
//	}

}
