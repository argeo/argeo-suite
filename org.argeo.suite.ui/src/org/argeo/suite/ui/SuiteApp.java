package org.argeo.suite.ui;

import static org.argeo.cms.ui.CmsView.CMS_VIEW_UID_PROPERTY;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.NodeUtils;
import org.argeo.cms.ui.AbstractCmsApp;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.dialogs.CmsFeedback;
import org.argeo.cms.ui.util.CmsEvent;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.entity.EntityConstants;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityTypes;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.argeo.suite.RankedObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/** The Argeo Suite App. */
public class SuiteApp extends AbstractCmsApp implements EventHandler {
	private final static Log log = LogFactory.getLog(SuiteApp.class);

	public final static String PID_PREFIX = "argeo.suite.ui.";
	public final static String HEADER_PID = PID_PREFIX + "header";
	public final static String LEAD_PANE_PID = PID_PREFIX + "leadPane";
	public final static String LOGIN_SCREEN_PID = PID_PREFIX + "loginScreen";
	// public final static String DASHBOARD_LAYER_PID = PID_PREFIX +
	// "dashboardLayer";
	public final static String DASHBOARD_PID = PID_PREFIX + "dashboard";
	public final static String RECENT_ITEMS_PID = PID_PREFIX + "recentItems";

	private final static String DEFAULT_UI_NAME = "app";
	private final static String DEFAULT_THEME_ID = "org.argeo.suite.theme.default";

	private Map<String, RankedObject<CmsUiProvider>> uiProvidersByPid = Collections.synchronizedMap(new HashMap<>());
	private Map<String, RankedObject<CmsUiProvider>> uiProvidersByType = Collections.synchronizedMap(new HashMap<>());
	private Map<String, RankedObject<SuiteLayer>> layers = Collections.synchronizedSortedMap(new TreeMap<>());

	// TODO make more optimal or via CmsSession/CmsView
	private Map<String, SuiteUi> managedUis = new HashMap<>();

//	private CmsUiProvider headerPart = null;

	public void init(Map<String, String> properties) {
		if (log.isDebugEnabled())
			log.info("Argeo Suite App started");
	}

	public void destroy(Map<String, String> properties) {
		for (SuiteUi ui : managedUis.values())
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
		SuiteUi argeoSuiteUi = new SuiteUi(parent, SWT.NONE);
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
			SuiteUi ui = (SuiteUi) parent;
			refreshPart(findUiProvider(HEADER_PID), ui.getHeader(), context);
			CmsView cmsView = CmsView.getCmsView(parent);
			if (cmsView.isAnonymous()) {
				ui.refreshBelowHeader(false);
				refreshPart(findUiProvider(LOGIN_SCREEN_PID), ui.getBelowHeader(), context);
			} else {
				try {
					if (ui.getUserHome() == null)
						ui.initSessions(getRepository());
					context = ui.getUserHome();

				} catch (RepositoryException e) {
					e.printStackTrace();
				}
				ui.refreshBelowHeader(true);

				for (String key : layers.keySet()) {
					SuiteLayer layer = layers.get(key).get();
					ui.addLayer(key, layer);
				}

//				ui.addLayer(ArgeoSuiteUi.DASHBOARD_LAYER);
//				ui.addLayer("documents");
//				ui.addLayer("locations");
//				ui.addLayer("people");
				// ui.switchToLayer(DASHBOARD_LAYER_PID, context);

//				refreshPart(findUiProvider(DASHBOARD_PID), ui.getTabbedArea().getCurrent(), context);
				refreshPart(findUiProvider(LEAD_PANE_PID), ui.getLeadPane(), context);
//				refreshPart(findUiProvider(RECENT_ITEMS_PID), ui.getEntryArea(), context);
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

	private CmsUiProvider findUiProvider(String pid) {
		if (!uiProvidersByPid.containsKey(pid))
			throw new IllegalArgumentException("No UI provider registered as " + pid);
		return uiProvidersByPid.get(pid).get();
	}

	private CmsUiProvider findUiProvider(Node context) {
		try {
			// mixins
			Set<String> types = new TreeSet<>();
			for (NodeType nodeType : context.getMixinNodeTypes()) {
				String typeName = nodeType.getName();
				if (uiProvidersByType.containsKey(typeName)) {
					types.add(typeName);
				}
			}
			// primary node type
			{
				NodeType nodeType = context.getPrimaryNodeType();
				String typeName = nodeType.getName();
				if (uiProvidersByType.containsKey(typeName)) {
					types.add(typeName);
				}
			}
			// entity type
			if (context.isNodeType(EntityTypes.ENTITY_ENTITY)) {
				String typeName =context.getProperty(EntityNames.ENTITY_TYPE).getString();
				if (uiProvidersByType.containsKey(typeName)) {
					types.add(typeName);
				}
			}

//			if (context.getPath().equals("/")) {// root node
//				types.add("nt:folder");
//			}
			if (NodeUtils.isUserHome(context) && uiProvidersByType.containsKey("nt:folder")) {// home node
				types.add("nt:folder");
			}

			if (types.size() == 0)
				throw new IllegalArgumentException("No UI provider found for " + context);
			return uiProvidersByType.get(types.iterator().next()).get();
		} catch (RepositoryException e) {
			throw new IllegalStateException(e);
		}
	}
//	private CmsUiProvider findUiProvider(String pid, Node context) {
//		CmsUiProvider found = null;
//		if (pid != null) {
//			SortedMap<RankingKey, CmsUiProvider> subMap = uiProvidersByPid.subMap(RankingKey.minPid(pid),
//					RankingKey.maxPid(pid));
//			providers: for (RankingKey key : subMap.keySet()) {
//				if (key.getPid() == null || !key.getPid().equals(pid))
//					break providers;
//				found = subMap.get(key);
//			}
//			if (found != null)
//				return found;
//		}
//
//		if (found == null && context != null) {
//			SortedMap<RankingKey, CmsUiProvider> subMap = null;
//			String dataType = null;
//			if (Jcr.isNodeType(context, EntityTypes.ENTITY_ENTITY)) {
//				dataType = Jcr.get(context, EntityNames.ENTITY_TYPE);
//				subMap = uiProvidersByPid.subMap(RankingKey.minDataType(dataType), RankingKey.maxDataType(dataType));
//			}
//			providers: for (RankingKey key : subMap.keySet()) {
//				if (key.getDataType() == null || !key.getDataType().equals(dataType))
//					break providers;
//				found = subMap.get(key);
//			}
//			if (found == null)
//				found = uiProvidersByPid.get(new RankingKey(null, null, null, dataType, null));
//			if (found != null)
//				return found;
//		}
//
//		// nothing
//		if (log.isWarnEnabled())
//			log.warn("No UI provider found for" + (pid != null ? " pid " + pid : "")
//					+ (context != null ? " " + context : ""));
//		return new CmsUiProvider() {
//
//			@Override
//			public Control createUi(Composite parent, Node context) throws RepositoryException {
//				return parent;
//			}
//		};
//	}

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
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			RankedObject.putIfHigherRank(uiProvidersByPid, pid, uiProvider, properties);
		}
		if (properties.containsKey(EntityConstants.TYPE)) {
			// TODO manage String arrays as well
			String type = (String) properties.get(EntityConstants.TYPE);
			RankedObject.putIfHigherRank(uiProvidersByType, type, uiProvider, properties);
		}
	}

	public void removeUiProvider(CmsUiProvider uiProvider, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			if (uiProvidersByPid.containsKey(pid)) {
				if (uiProvidersByPid.get(pid).equals(new RankedObject<CmsUiProvider>(uiProvider, properties))) {
					uiProvidersByPid.remove(pid);
				}
			}
		}

	}

	public void addLayer(SuiteLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			RankedObject.putIfHigherRank(layers, pid, layer, properties);
		}
	}

	public void removeLayer(SuiteLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			if (layers.containsKey(pid)) {
				if (layers.get(pid).equals(new RankedObject<SuiteLayer>(layer, properties))) {
					layers.remove(pid);
				}
			}
		}
	}

	@Override
	public void handleEvent(Event event) {

		// Specific UI related events
		SuiteUi ui = getRelatedUi(event);
		try {
			String currentLayerId = ui.getCurrentLayerId();
			SuiteLayer layer = currentLayerId != null ? layers.get(currentLayerId).get() : null;
			if (isTopic(event, SuiteEvent.refreshPart)) {
				String nodeId = get(event, SuiteEvent.NODE_ID);
				String workspace = get(event, SuiteEvent.WORKSPACE);
				Node node = Jcr.getNodeById(ui.getSession(workspace), nodeId);
				CmsUiProvider uiProvider = findUiProvider(node);
				layer.view(uiProvider, ui.getCurrentWorkArea(), node);
			} else if (isTopic(event, SuiteEvent.openNewPart)) {
				String nodeId = get(event, SuiteEvent.NODE_ID);
				String workspace = get(event, SuiteEvent.WORKSPACE);
				Node node = Jcr.getNodeById(ui.getSession(workspace), nodeId);
				CmsUiProvider uiProvider = findUiProvider(node);
				layer.open(uiProvider, ui.getCurrentWorkArea(), node);
			} else if (isTopic(event, SuiteEvent.switchLayer)) {
				String layerId = get(event, SuiteEvent.LAYER);
				ui.switchToLayer(layerId, Jcr.getRootNode(ui.getSession(null)));
			}
		} catch (Exception e) {
			log.error("Cannot handle event " + event, e);
//			CmsView.getCmsView(ui).exception(e);
		}

	}

	private SuiteUi getRelatedUi(Event event) {
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