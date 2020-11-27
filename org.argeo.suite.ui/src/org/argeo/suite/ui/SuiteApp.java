package org.argeo.suite.ui;

import static org.argeo.cms.ui.CmsView.CMS_VIEW_UID_PROPERTY;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.ui.AbstractCmsApp;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.dialogs.CmsFeedback;
import org.argeo.cms.ui.util.CmsEvent;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.entity.EntityConstants;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.argeo.suite.RankedObject;
import org.argeo.util.LangUtils;
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
	public final static String DASHBOARD_PID = PID_PREFIX + "dashboard";
	public final static String RECENT_ITEMS_PID = PID_PREFIX + "recentItems";

	private final static String DEFAULT_UI_NAME = "app";
	private final static String DEFAULT_THEME_ID = "org.argeo.suite.theme.default";

	private Map<String, RankedObject<CmsUiProvider>> uiProvidersByPid = Collections.synchronizedMap(new HashMap<>());
	private Map<String, RankedObject<CmsUiProvider>> uiProvidersByType = Collections.synchronizedMap(new HashMap<>());
	private Map<String, RankedObject<SuiteLayer>> layersByPid = Collections.synchronizedSortedMap(new TreeMap<>());
	private Map<String, RankedObject<SuiteLayer>> layersByType = Collections.synchronizedSortedMap(new TreeMap<>());

	private CmsUserManager cmsUserManager;

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
				ui.logout();
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

				for (String key : layersByPid.keySet()) {
					SuiteLayer layer = layersByPid.get(key).get();
					ui.addLayer(key, layer);
				}
				refreshPart(findUiProvider(LEAD_PANE_PID), ui.getLeadPane(), context);
			}
			ui.layout(true, true);
			setState(parent, state);
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

	private <T> T findByType(Map<String, RankedObject<T>> byType, Node context) {
		try {
			// mixins
			Set<String> types = new TreeSet<>();
			for (NodeType nodeType : context.getMixinNodeTypes()) {
				String typeName = nodeType.getName();
				if (byType.containsKey(typeName)) {
					types.add(typeName);
				}
			}
			// primary node type
			{
				NodeType nodeType = context.getPrimaryNodeType();
				String typeName = nodeType.getName();
				if (byType.containsKey(typeName)) {
					types.add(typeName);
				}
				for (NodeType mixin : nodeType.getDeclaredSupertypes()) {
					if (byType.containsKey(mixin.getName())) {
						types.add(mixin.getName());
					}
				}
			}
			// entity type
			if (context.isNodeType(EntityType.entity.get())) {
				if (context.hasProperty(EntityNames.ENTITY_TYPE)) {
					String typeName = context.getProperty(EntityNames.ENTITY_TYPE).getString();
					if (byType.containsKey(typeName)) {
						types.add(typeName);
					}
				}
			}

//			if (context.getPath().equals("/")) {// root node
//				types.add("nt:folder");
//			}
			if (NodeUtils.isUserHome(context) && byType.containsKey("nt:folder")) {// home node
				types.add("nt:folder");
			}

			if (types.size() == 0)
				throw new IllegalArgumentException("No component found for " + context);
			String type = types.iterator().next();
			if (!byType.containsKey(type))
				throw new IllegalArgumentException("No component found for " + context + " with type " + type);
			return byType.get(type).get();
		} catch (RepositoryException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void setState(Composite parent, String state) {
		CmsView cmsView = CmsView.getCmsView(parent);
		if(cmsView.isAnonymous())
			return;
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
						log.error("Cannot interpret " + state);
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
				session = cmsView.doAs(() -> Jcr.login(getRepository(), workspace));

				Node node = session.getNode(path);

				cmsView.sendEvent(SuiteEvent.switchLayer.topic(), SuiteEvent.eventProperties(node));
				cmsView.sendEvent(SuiteEvent.refreshPart.topic(), SuiteEvent.eventProperties(node));
			}
		} catch (RepositoryException e) {
			log.error("Cannot load state " + state, e);
			cmsView.navigateTo("~");
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

	/*
	 * Events management
	 */

	@Override
	public void handleEvent(Event event) {

		// Specific UI related events
		SuiteUi ui = getRelatedUi(event);
		try {
			String currentLayerId = ui.getCurrentLayerId();
			SuiteLayer currentLayer = currentLayerId != null ? layersByPid.get(currentLayerId).get() : null;
			if (isTopic(event, SuiteEvent.refreshPart)) {
				String nodePath = get(event, SuiteEvent.NODE_PATH);
				String workspace = get(event, SuiteEvent.WORKSPACE);
				Node node = Jcr.getNode(ui.getSession(workspace), nodePath);
				CmsUiProvider uiProvider = findByType(uiProvidersByType, node);
				currentLayer.view(uiProvider, ui.getCurrentWorkArea(), node);
				ui.getCmsView().stateChanged(nodeToState(node), Jcr.getTitle(node));
			} else if (isTopic(event, SuiteEvent.openNewPart)) {
				String nodePath = get(event, SuiteEvent.NODE_PATH);
				String workspace = get(event, SuiteEvent.WORKSPACE);
				Node node = Jcr.getNode(ui.getSession(workspace), nodePath);
				CmsUiProvider uiProvider = findByType(uiProvidersByType, node);
				currentLayer.open(uiProvider, ui.getCurrentWorkArea(), node);
				ui.getCmsView().stateChanged(nodeToState(node), Jcr.getTitle(node));
			} else if (isTopic(event, SuiteEvent.switchLayer)) {
				String layerId = get(event, SuiteEvent.LAYER);
				if (layerId != null) {
					ui.switchToLayer(layerId, Jcr.getRootNode(ui.getSession(null)));
				} else {
					String nodePath = get(event, SuiteEvent.NODE_PATH);
					String workspace = get(event, SuiteEvent.WORKSPACE);
					if (nodePath != null) {
						Node node = Jcr.getNode(ui.getSession(workspace), nodePath);
						SuiteLayer layer = findByType(layersByType, node);
						ui.switchToLayer(layer, node);
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot handle event " + event, e);
//			CmsView.getCmsView(ui).exception(e);
		}

	}

	private String nodeToState(Node node) {
		return '/' + Jcr.getWorkspaceName(node) + Jcr.getPath(node);
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
			return null;
//			throw new IllegalArgumentException("Property " + key + " must be set");
		return value.toString();

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
			List<String> types = LangUtils.toStringList(properties.get(EntityConstants.TYPE));
			for (String type : types)
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
		if (properties.containsKey(EntityConstants.TYPE)) {
			List<String> types = LangUtils.toStringList(properties.get(EntityConstants.TYPE));
			for (String type : types) {
				if (uiProvidersByType.containsKey(type)) {
					if (uiProvidersByType.get(type).equals(new RankedObject<CmsUiProvider>(uiProvider, properties))) {
						uiProvidersByType.remove(type);
					}
				}
			}
		}
	}

	public void addLayer(SuiteLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			RankedObject.putIfHigherRank(layersByPid, pid, layer, properties);
		}
		if (properties.containsKey(EntityConstants.TYPE)) {
			List<String> types = LangUtils.toStringList(properties.get(EntityConstants.TYPE));
			for (String type : types)
				RankedObject.putIfHigherRank(layersByType, type, layer, properties);
		}
	}

	public void removeLayer(SuiteLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			if (layersByPid.containsKey(pid)) {
				if (layersByPid.get(pid).equals(new RankedObject<SuiteLayer>(layer, properties))) {
					layersByPid.remove(pid);
				}
			}
		}
		if (properties.containsKey(EntityConstants.TYPE)) {
			List<String> types = LangUtils.toStringList(properties.get(EntityConstants.TYPE));
			for (String type : types) {
				if (layersByType.containsKey(type)) {
					if (layersByType.get(type).equals(new RankedObject<CmsUiProvider>(layer, properties))) {
						layersByType.remove(type);
					}
				}
			}
		}
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

}
