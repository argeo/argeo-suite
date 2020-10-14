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
import org.argeo.cms.ui.AbstractCmsApp;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.dialogs.CmsFeedback;
import org.argeo.cms.ui.util.CmsEvent;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityTypes;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.argeo.suite.RankingKey;
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

	private final static String DEFAULT_UI_NAME = "app";
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
		if (cmsView == null)
			throw new IllegalStateException("No CMS view is registered.");
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
			ArgeoSuiteUi ui = (ArgeoSuiteUi) parent;
			refreshPart(findUiProvider(HEADER_PID, context), ui.getHeader(), context);
			CmsView cmsView = CmsView.getCmsView(parent);
			if (cmsView.isAnonymous()) {
				ui.refreshBelowHeader(false);
				refreshPart(findUiProvider(LOGIN_SCREEN_PID, context), ui.getBelowHeader(), context);
			} else {
				try {
					if (ui.getSession() == null)
						ui.setSession(getRepository().login());
					context = ui.getSession().getRootNode();

				} catch (RepositoryException e) {
					e.printStackTrace();
				}
				ui.refreshBelowHeader(true);

				ui.addLayer(ArgeoSuiteUi.DASHBOARD_LAYER);
				ui.addLayer("documents");
				ui.addLayer("locations");
				ui.addLayer("people");
				ui.switchToLayer(ArgeoSuiteUi.DASHBOARD_LAYER);

				refreshPart(findUiProvider(DASHBOARD_PID, context), ui.getTabbedArea().getCurrent(), context);
				refreshPart(findUiProvider(LEAD_PANE_PID, context), ui.getLeadPane(), context);
				refreshPart(findUiProvider(RECENT_ITEMS_PID, context), ui.getEntryArea(), context);
			}
			ui.layout(true, true);
		} catch (Exception e) {
			CmsFeedback.show("Unexpected exception", e);
		}
	}

	private void refreshPart(CmsUiProvider uiProvider, Composite part, Node context) {
		CmsUiUtils.clear(part);
		uiProvider.createUiPart(part, context);
	}

	private CmsUiProvider findUiProvider(String pid, Node context) {
		CmsUiProvider found = null;
		if (pid != null) {
			SortedMap<RankingKey, CmsUiProvider> subMap = uiProviders.subMap(RankingKey.minPid(pid),
					RankingKey.maxPid(pid));
			providers: for (RankingKey key : subMap.keySet()) {
				if (key.getPid() == null || !key.getPid().equals(pid))
					break providers;
				found = subMap.get(key);
			}
			if (found != null)
				return found;
		}

		if (found == null && context != null) {
			SortedMap<RankingKey, CmsUiProvider> subMap = null;
			String dataType = null;
			if (Jcr.isNodeType(context, EntityTypes.ENTITY_ENTITY)) {
				dataType = Jcr.get(context, EntityNames.ENTITY_TYPE);
				subMap = uiProviders.subMap(RankingKey.minDataType(dataType), RankingKey.maxDataType(dataType));
			}
			providers: for (RankingKey key : subMap.keySet()) {
				if (key.getDataType() == null || !key.getDataType().equals(dataType))
					break providers;
				found = subMap.get(key);
			}
			if (found == null)
				found = uiProviders.get(new RankingKey(null, null, null, dataType, null));
			if (found != null)
				return found;
		}

		// nothing
		if (log.isWarnEnabled())
			log.warn("No UI provider found for" + (pid != null ? " pid " + pid : "")
					+ (context != null ? " " + context : ""));
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

				refreshEntityUi(null, node);
			}
		} catch (RepositoryException e) {
			log.error("Cannot load state " + state, e);
			cmsView.navigateTo("~");
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

	private void refreshEntityUi(Composite parent, Node context) {
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
		if (partKey.getPid() != null || partKey.getDataType() != null) {
			uiProviders.put(partKey, uiProvider);
			if (log.isDebugEnabled())
				log.debug("Added UI provider " + partKey + " (" + uiProvider.getClass().getName() + ") to CMS app.");
		}
//		}

	}

	public void removeUiProvider(CmsUiProvider uiProvider, Map<String, Object> properties) {
		RankingKey partKey = new RankingKey(properties);
//		String servicePid = properties.get(Constants.SERVICE_PID);
		uiProviders.remove(partKey);

	}

	@Override
	public void handleEvent(Event event) {

		// Specific UI related events
		ArgeoSuiteUi ui = getRelatedUi(event);
		if (isTopic(event, SuiteEvent.refreshPart)) {
			Node node = Jcr.getNodeById(ui.getSession(), get(event, SuiteEvent.NODE_ID));
			ui.getTabbedArea().view(findUiProvider(DASHBOARD_PID, node), node);
//			ui.layout(true, true);
		} else if (isTopic(event, SuiteEvent.openNewPart)) {
			Node node = Jcr.getNodeById(ui.getSession(), get(event, SuiteEvent.NODE_ID));
			ui.getTabbedArea().open(findUiProvider(DASHBOARD_PID, node), node);
//			ui.layout(true, true);
		} else if (isTopic(event, SuiteEvent.switchLayer)) {
			String layer = get(event, SuiteEvent.LAYER);
			ui.switchToLayer(layer);
		}

	}

	private ArgeoSuiteUi getRelatedUi(Event event) {
		return managedUis.get(get(event, CMS_VIEW_UID_PROPERTY));
	}

	private static boolean isTopic(Event event, CmsEvent cmsEvent) {
		return event.getTopic().equals(cmsEvent.topic());
	}

	private static String get(Event event, String key) {
		Object value = event.getProperty(key);
		if (value == null)
			throw new IllegalArgumentException("Property " + key + " must be set");
		return value.toString();

	}
}
