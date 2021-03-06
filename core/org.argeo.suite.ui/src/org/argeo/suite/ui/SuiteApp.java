package org.argeo.suite.ui;

import static org.argeo.cms.ui.CmsView.CMS_VIEW_UID_PROPERTY;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.NodeUtils;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.LocaleUtils;
import org.argeo.cms.Localized;
import org.argeo.cms.auth.CmsSession;
import org.argeo.cms.ui.AbstractCmsApp;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.dialogs.CmsFeedback;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.eclipse.ui.specific.UiContext;
import org.argeo.entity.EntityConstants;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
import org.argeo.suite.RankedObject;
import org.argeo.suite.SuiteUtils;
import org.argeo.util.LangUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.useradmin.User;

/** The Argeo Suite App. */
public class SuiteApp extends AbstractCmsApp implements EventHandler {
	private final static Log log = LogFactory.getLog(SuiteApp.class);

	public final static String PUBLIC_BASE_PATH_PROPERTY = "publicBasePath";
	public final static String DEFAULT_UI_NAME_PROPERTY = "defaultUiName";
	public final static String DEFAULT_THEME_ID_PROPERTY = "defaultThemeId";
	private final static String LOGIN = "login";

	private String publicBasePath = null;

	private String pidPrefix;
	private String headerPid;
	private String footerPid;
	private String leadPanePid;
	private String loginScreenPid;

	private String defaultLayerPid = "argeo.suite.ui.dashboardLayer";

	private String defaultUiName = "app";
	private String defaultThemeId = "org.argeo.suite.theme.default";

	private Map<String, RankedObject<CmsUiProvider>> uiProvidersByPid = Collections.synchronizedMap(new HashMap<>());
	private Map<String, RankedObject<CmsUiProvider>> uiProvidersByType = Collections.synchronizedMap(new HashMap<>());
	private Map<String, RankedObject<SuiteLayer>> layersByPid = Collections.synchronizedSortedMap(new TreeMap<>());
	private Map<String, RankedObject<SuiteLayer>> layersByType = Collections.synchronizedSortedMap(new TreeMap<>());

	private CmsUserManager cmsUserManager;

	// TODO make more optimal or via CmsSession/CmsView
	private Map<String, SuiteUi> managedUis = new HashMap<>();

	public void init(Map<String, Object> properties) {
		if (log.isDebugEnabled())
			log.info("Argeo Suite App started");

		if (properties.containsKey(DEFAULT_UI_NAME_PROPERTY))
			defaultUiName = LangUtils.get(properties, DEFAULT_UI_NAME_PROPERTY);
		if (properties.containsKey(DEFAULT_THEME_ID_PROPERTY))
			defaultThemeId = LangUtils.get(properties, DEFAULT_THEME_ID_PROPERTY);
		publicBasePath = LangUtils.get(properties, PUBLIC_BASE_PATH_PROPERTY);

		if (properties.containsKey(Constants.SERVICE_PID)) {
			String servicePid = properties.get(Constants.SERVICE_PID).toString();
			if (servicePid.endsWith(".app")) {
				pidPrefix = servicePid.substring(0, servicePid.length() - "app".length());
			}
		}

		if (pidPrefix == null)
			throw new IllegalArgumentException("PID prefix must be set.");

		headerPid = pidPrefix + "header";
		footerPid = pidPrefix + "footer";
		leadPanePid = pidPrefix + "leadPane";
		loginScreenPid = pidPrefix + "loginScreen";
	}

	public void destroy(Map<String, Object> properties) {
		for (SuiteUi ui : managedUis.values())
			if (!ui.isDisposed())
				ui.dispose();
		if (log.isDebugEnabled())
			log.info("Argeo Suite App stopped");

	}

	@Override
	public Set<String> getUiNames() {
		HashSet<String> uiNames = new HashSet<>();
		uiNames.add(defaultUiName);
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
		SuiteUi argeoSuiteUi = new SuiteUi(parent, SWT.INHERIT_DEFAULT);
		String uid = cmsView.getUid();
		managedUis.put(uid, argeoSuiteUi);
		argeoSuiteUi.addDisposeListener((e) -> {
			managedUis.remove(uid);
			if (log.isDebugEnabled())
				log.debug("Suite UI " + uid + " has been disposed.");
		});
		return argeoSuiteUi;
	}

	@Override
	public String getThemeId(String uiName) {
		return defaultThemeId;
	}

	@Override
	public void refreshUi(Composite parent, String state) {
		try {
			Node context = null;
			SuiteUi ui = (SuiteUi) parent;
			CmsView cmsView = CmsView.getCmsView(parent);
			CmsUiProvider headerUiProvider = findUiProvider(headerPid);
			CmsUiProvider footerUiProvider = findUiProvider(footerPid);
			Localized appTitle = null;
			if (headerUiProvider instanceof DefaultHeader) {
				appTitle = ((DefaultHeader) headerUiProvider).getTitle();
			}
			ui.setTitle(appTitle);

			if (cmsView.isAnonymous() && publicBasePath == null) {// internal app, must login
				ui.logout();
				if (headerUiProvider != null)
					refreshPart(headerUiProvider, ui.getHeader(), context);
				ui.refreshBelowHeader(false);
				refreshPart(findUiProvider(loginScreenPid), ui.getBelowHeader(), context);
				if (footerUiProvider != null)
					refreshPart(footerUiProvider, ui.getFooter(), context);
				ui.layout(true, true);
				setState(ui, LOGIN);
			} else {
				if (LOGIN.equals(state))
					state = null;
				CmsSession cmsSession = cmsView.getCmsSession();
				if (ui.getUserDir() == null) {
					// FIXME NPE on CMSSession when logging in from anonymous
					if (cmsSession==null || cmsView.isAnonymous()) {
						assert publicBasePath != null;
						ui.initSessions(getRepository(), publicBasePath);
					} else {
						Session adminSession = null;
						try {
							adminSession = NodeUtils.openDataAdminSession(getRepository(), null);
							Node userDir = SuiteUtils.getOrCreateCmsSessionNode(adminSession, cmsSession);
							ui.initSessions(getRepository(), userDir.getPath());
						} finally {
							Jcr.logout(adminSession);
						}
					}
				}
				initLocale(cmsSession);
				context = stateToNode(ui, state);
				if (context == null)
					context = ui.getUserDir();

				if (headerUiProvider != null)
					refreshPart(headerUiProvider, ui.getHeader(), context);
				ui.refreshBelowHeader(true);
				for (String key : layersByPid.keySet()) {
					SuiteLayer layer = layersByPid.get(key).get();
					ui.addLayer(key, layer);
				}
				refreshPart(findUiProvider(leadPanePid), ui.getLeadPane(), context);
				if (footerUiProvider != null)
					refreshPart(footerUiProvider, ui.getFooter(), context);
				ui.layout(true, true);
				setState(parent, state != null ? state : defaultLayerPid);
			}
		} catch (Exception e) {
			CmsFeedback.show("Unexpected exception", e);
		}
	}

	private void initLocale(CmsSession cmsSession) {
		if (cmsSession == null)
			return;
		Locale locale = cmsSession.getLocale();
		UiContext.setLocale(locale);
		LocaleUtils.setThreadLocale(locale);

	}

	private void refreshPart(CmsUiProvider uiProvider, Composite part, Node context) {
		CmsUiUtils.clear(part);
		uiProvider.createUiPart(part, context);
	}

	private CmsUiProvider findUiProvider(String pid) {
		if (!uiProvidersByPid.containsKey(pid))
			return null;
		return uiProvidersByPid.get(pid).get();
	}

	private SuiteLayer findLayer(String pid) {
		if (!layersByPid.containsKey(pid))
			return null;
		return layersByPid.get(pid).get();
	}

	private <T> T findByType(Map<String, RankedObject<T>> byType, Node context) {
		if (context == null)
			throw new IllegalArgumentException("A node should be provided");
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
				throw new IllegalArgumentException("No type found for " + context);
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
		if (state == null)
			return;
		if (!state.startsWith("/")) {
			if (parent instanceof SuiteUi) {
				SuiteUi ui = (SuiteUi) parent;
				if (LOGIN.equals(state) || state.equals("~")) {
					String appTitle = "";
					if (ui.getTitle() != null)
						appTitle = ui.getTitle().lead();
					ui.getCmsView().stateChanged(state, appTitle);
					return;
				}
				String currentLayerId = ui.getCurrentLayerId();
				if (state.equals(currentLayerId))
					return; // does nothing
				else {
					Map<String, Object> properties = new HashMap<>();
					properties.put(SuiteEvent.LAYER, state);
					ui.getCmsView().sendEvent(SuiteEvent.switchLayer.topic(), properties);
				}
			}
			return;
		}
		SuiteUi suiteUi = (SuiteUi) parent;
		Node node = stateToNode(suiteUi, state);
		if (node == null) {
			suiteUi.getCmsView().navigateTo("~");
		} else {
			suiteUi.getCmsView().sendEvent(SuiteEvent.switchLayer.topic(), SuiteEvent.eventProperties(node));
			suiteUi.getCmsView().sendEvent(SuiteEvent.refreshPart.topic(), SuiteEvent.eventProperties(node));
		}
	}

	private String nodeToState(Node node) {
		return '/' + Jcr.getWorkspaceName(node) + Jcr.getPath(node);
	}

	private Node stateToNode(SuiteUi suiteUi, String state) {
		if (suiteUi == null)
			return null;
		if (state == null || !state.startsWith("/"))
			return null;

		String path = state.substring(1);
		String workspace;
		if (path.equals("")) {
			workspace = null;
			path = "/";
		} else {
			int index = path.indexOf('/');
			if (index == 0) {
				log.error("Cannot interpret " + state);
//				cmsView.navigateTo("~");
				return null;
			} else if (index > 0) {
				workspace = path.substring(0, index);
				path = path.substring(index);
			} else {// index<0, assuming root node
				workspace = path;
				path = "/";
			}
		}
		Session session = suiteUi.getSession(workspace);
		if (session == null)
			return null;
		Node node = Jcr.getNode(session, path);
		return node;
	}

	/*
	 * Events management
	 */

	@Override
	public void handleEvent(Event event) {

		// Specific UI related events
		SuiteUi ui = getRelatedUi(event);
		if (ui == null)
			return;
		try {
			String appTitle = "";
			if (ui.getTitle() != null)
				appTitle = ui.getTitle().lead() + " - ";

//			String currentLayerId = ui.getCurrentLayerId();
//			SuiteLayer currentLayer = currentLayerId != null ? layersByPid.get(currentLayerId).get() : null;
			if (SuiteUiUtils.isTopic(event, SuiteEvent.refreshPart)) {
				Node node = getNode(ui, event);
				if (node == null)
					return;
				CmsUiProvider uiProvider = findByType(uiProvidersByType, node);
				SuiteLayer layer = findByType(layersByType, node);
				ui.switchToLayer(layer, node);
				ui.getCmsView().runAs(() -> layer.view(uiProvider, ui.getCurrentWorkArea(), node));
				ui.getCmsView().stateChanged(nodeToState(node), appTitle + Jcr.getTitle(node));
			} else if (SuiteUiUtils.isTopic(event, SuiteEvent.openNewPart)) {
				Node node = getNode(ui, event);
				if (node == null)
					return;
				CmsUiProvider uiProvider = findByType(uiProvidersByType, node);
				SuiteLayer layer = findByType(layersByType, node);
				ui.switchToLayer(layer, node);
				ui.getCmsView().runAs(() -> layer.open(uiProvider, ui.getCurrentWorkArea(), node));
				ui.getCmsView().stateChanged(nodeToState(node), appTitle + Jcr.getTitle(node));
			} else if (SuiteUiUtils.isTopic(event, SuiteEvent.switchLayer)) {
				String layerId = get(event, SuiteEvent.LAYER);
				if (layerId != null) {
//					ui.switchToLayer(layerId, ui.getUserDir());
					SuiteLayer suiteLayer = findLayer(layerId);
					Localized layerTitle = suiteLayer.getTitle();
					ui.getCmsView().runAs(() -> ui.switchToLayer(layerId, ui.getUserDir()));
					String title = null;
					if (layerTitle != null)
						title = layerTitle.lead();
					ui.getCmsView().stateChanged(layerId, appTitle + title);
				} else {
					Node node = getNode(ui, event);
					if (node != null) {
						SuiteLayer layer = findByType(layersByType, node);
						ui.getCmsView().runAs(() -> ui.switchToLayer(layer, node));
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot handle event " + event, e);
//			CmsView.getCmsView(ui).exception(e);
		}

	}

	private Node getNode(SuiteUi ui, Event event) {
		String nodePath = get(event, SuiteEvent.NODE_PATH);
		String workspaceName = get(event, SuiteEvent.WORKSPACE);
		Session session = ui.getSession(workspaceName);
		Node node;
		if (nodePath == null) {
			// look for a user
			String username = get(event, SuiteEvent.USERNAME);
			if (username == null)
				return null;
			User user = cmsUserManager.getUser(username);
			if (user == null)
				return null;
			LdapName userDn;
			try {
				userDn = new LdapName(user.getName());
			} catch (InvalidNameException e) {
				throw new IllegalArgumentException("Badly formatted username", e);
			}
			String userNodePath = SuiteUtils.getUserNodePath(userDn);
			if (Jcr.itemExists(session, userNodePath))
				node = Jcr.getNode(session, userNodePath);
			else {
				Session adminSession = null;
				try {
					adminSession = NodeUtils.openDataAdminSession(getRepository(), workspaceName);
					SuiteUtils.getOrCreateUserNode(adminSession, userDn);
				} finally {
					Jcr.logout(adminSession);
				}
				node = Jcr.getNode(session, userNodePath);
			}
		} else {
			node = Jcr.getNode(session, nodePath);
		}
		return node;
	}

	private SuiteUi getRelatedUi(Event event) {
		return managedUis.get(get(event, CMS_VIEW_UID_PROPERTY));
	}

	public static String get(Event event, String key) {
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
