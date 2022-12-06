package org.argeo.app.ui;

import static org.argeo.api.cms.ux.CmsView.CMS_VIEW_UID_PROPERTY;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentRepository;
import org.argeo.api.acr.spi.ProvidedSession;
import org.argeo.api.cms.CmsConstants;
import org.argeo.api.cms.CmsEventSubscriber;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.CmsSession;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.ux.CmsTheme;
import org.argeo.api.cms.ux.CmsUi;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.api.EntityConstants;
import org.argeo.app.api.EntityNames;
import org.argeo.app.api.EntityType;
import org.argeo.app.api.RankedObject;
import org.argeo.app.core.SuiteUtils;
import org.argeo.cms.AbstractCmsApp;
import org.argeo.cms.LocaleUtils;
import org.argeo.cms.Localized;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.jcr.CmsJcrUtils;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.cms.jcr.acr.JcrContentProvider;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.dialogs.CmsFeedback;
import org.argeo.cms.util.LangUtils;
import org.argeo.cms.ux.CmsUxUtils;
import org.argeo.eclipse.ui.specific.UiContext;
import org.argeo.jcr.JcrException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Constants;
import org.osgi.service.useradmin.User;

/** The Argeo Suite App. */
public class SuiteApp extends AbstractCmsApp implements CmsEventSubscriber {
	private final static CmsLog log = CmsLog.getLog(SuiteApp.class);

	public final static String PUBLIC_BASE_PATH_PROPERTY = "publicBasePath";
	public final static String DEFAULT_UI_NAME_PROPERTY = "defaultUiName";
	public final static String DEFAULT_THEME_ID_PROPERTY = "defaultThemeId";
	public final static String DEFAULT_LAYER_PROPERTY = "defaultLayer";
	private final static String LOGIN = "login";
	private final static String HOME_STATE = "~";

	private String publicBasePath = null;

	private String pidPrefix;
	private String headerPid;
	private String footerPid;
	private String leadPanePid;
	private String adminLeadPanePid;
	private String loginScreenPid;

	private String defaultUiName = "app";
	private String adminUiName = "admin";

	// FIXME such default names make refactoring more dangerous
	@Deprecated
	private String defaultLayerPid = "argeo.suite.ui.dashboardLayer";
	@Deprecated
	private String defaultThemeId = "org.argeo.app.theme.default";

	// TODO use QName as key for byType
	private Map<String, RankedObject<SwtUiProvider>> uiProvidersByPid = Collections.synchronizedMap(new HashMap<>());
	private Map<String, RankedObject<SwtUiProvider>> uiProvidersByType = Collections.synchronizedMap(new HashMap<>());
	private Map<String, RankedObject<SuiteLayer>> layersByPid = Collections.synchronizedSortedMap(new TreeMap<>());
	private Map<String, RankedObject<SuiteLayer>> layersByType = Collections.synchronizedSortedMap(new TreeMap<>());

	private CmsUserManager cmsUserManager;

	// TODO make more optimal or via CmsSession/CmsView
	private Map<String, SuiteUi> managedUis = new HashMap<>();

	// ACR
	private ContentRepository contentRepository;
	private JcrContentProvider jcrContentProvider;

	// JCR
//	private Repository repository;

	public void init(Map<String, Object> properties) {
		for (SuiteUxEvent event : SuiteUxEvent.values()) {
			getCmsContext().getCmsEventBus().addEventSubscriber(event.topic(), this);
		}

		if (log.isDebugEnabled())
			log.info("Argeo Suite App started");

		if (properties.containsKey(DEFAULT_UI_NAME_PROPERTY))
			defaultUiName = LangUtils.get(properties, DEFAULT_UI_NAME_PROPERTY);
		if (properties.containsKey(DEFAULT_THEME_ID_PROPERTY))
			defaultThemeId = LangUtils.get(properties, DEFAULT_THEME_ID_PROPERTY);
		if (properties.containsKey(DEFAULT_LAYER_PROPERTY))
			defaultLayerPid = LangUtils.get(properties, DEFAULT_LAYER_PROPERTY);
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
		adminLeadPanePid = pidPrefix + "adminLeadPane";
		loginScreenPid = pidPrefix + "loginScreen";
	}

	public void destroy(Map<String, Object> properties) {
		for (SuiteUi ui : managedUis.values())
			if (!ui.isDisposed()) {
				ui.getDisplay().syncExec(() -> ui.dispose());
			}
		if (log.isDebugEnabled())
			log.info("Argeo Suite App stopped");

	}

	@Override
	public Set<String> getUiNames() {
		HashSet<String> uiNames = new HashSet<>();
		uiNames.add(defaultUiName);
		uiNames.add(adminUiName);
		return uiNames;
	}

	@Override
	public CmsUi initUi(Object parent) {
		Composite uiParent = (Composite) parent;
		String uiName = uiParent.getData(UI_NAME_PROPERTY) != null ? uiParent.getData(UI_NAME_PROPERTY).toString()
				: null;
		CmsView cmsView = CmsSwtUtils.getCmsView(uiParent);
		if (cmsView == null)
			throw new IllegalStateException("No CMS view is registered.");
		CmsTheme theme = getTheme(uiName);
		if (theme != null)
			CmsSwtUtils.registerCmsTheme(uiParent.getShell(), theme);
		SuiteUi argeoSuiteUi = new SuiteUi(uiParent, SWT.INHERIT_DEFAULT);
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
		String themeId = System.getProperty("org.argeo.app.theme.default");
		if (themeId != null)
			return themeId;
		return defaultThemeId;
	}

	@Override
	public void refreshUi(CmsUi cmsUi, String state) {
		try {
			Content context = null;
			SuiteUi ui = (SuiteUi) cmsUi;

			String uiName = Objects.toString(ui.getParent().getData(UI_NAME_PROPERTY), null);
			if (uiName == null)
				throw new IllegalStateException("UI name should not be null");
			CmsView cmsView = CmsSwtUtils.getCmsView(ui);

			ProvidedSession contentSession = (ProvidedSession) CmsUxUtils.getContentSession(contentRepository, cmsView);

			SwtUiProvider headerUiProvider = findUiProvider(headerPid);
			SwtUiProvider footerUiProvider = findUiProvider(footerPid);
			SwtUiProvider leadPaneUiProvider;
			if (adminUiName.equals(uiName)) {
				leadPaneUiProvider = findUiProvider(adminLeadPanePid);
			} else {
				leadPaneUiProvider = findUiProvider(leadPanePid);
			}

			Localized appTitle = null;
			if (headerUiProvider instanceof DefaultHeader) {
				appTitle = ((DefaultHeader) headerUiProvider).getTitle();
			}
			ui.setTitle(appTitle);

			if (cmsView.isAnonymous() && publicBasePath == null) {// internal app, must login
				ui.logout();
				ui.setLoginScreen(true);
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
				if (ui.isLoginScreen()) {
					ui.setLoginScreen(false);
				}
				CmsSession cmsSession = cmsView.getCmsSession();
				if (ui.getUserDir() == null) {
					// FIXME NPE on CMSSession when logging in from anonymous
					if (cmsSession == null || cmsView.isAnonymous()) {
						assert publicBasePath != null;
						Content userDir = contentSession
								.get(ContentUtils.SLASH + CmsConstants.SYS_WORKSPACE + publicBasePath);
						ui.setUserDir(userDir);
					} else {
						Node userDirNode = jcrContentProvider.doInAdminSession((adminSession) -> {
							Node node = SuiteUtils.getOrCreateCmsSessionNode(adminSession, cmsSession);
							return node;
						});
						Content userDir = contentSession
								.get(ContentUtils.SLASH + CmsConstants.SYS_WORKSPACE + userDirNode.getPath());
						ui.setUserDir(userDir);
//						Content userDir = contentSession.getSessionRunDir();
//						ui.setUserDir(userDir);
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

				if (leadPaneUiProvider != null)
					refreshPart(leadPaneUiProvider, ui.getLeadPane(), context);
				if (footerUiProvider != null)
					refreshPart(footerUiProvider, ui.getFooter(), context);
				ui.layout(true, true);
				setState(ui, state != null ? state : defaultLayerPid);
			}
		} catch (Exception e) {
			CmsFeedback.error("Unexpected exception", e);
		}
	}

	private void initLocale(CmsSession cmsSession) {
		if (cmsSession == null)
			return;
		Locale locale = cmsSession.getLocale();
		UiContext.setLocale(locale);
		LocaleUtils.setThreadLocale(locale);

	}

	private void refreshPart(SwtUiProvider uiProvider, Composite part, Content context) {
		CmsSwtUtils.clear(part);
		uiProvider.createUiPart(part, context);
	}

	private SwtUiProvider findUiProvider(String pid) {
		if (!uiProvidersByPid.containsKey(pid))
			return null;
		return uiProvidersByPid.get(pid).get();
	}

	private SuiteLayer findLayer(String pid) {
		if (!layersByPid.containsKey(pid))
			return null;
		return layersByPid.get(pid).get();
	}

	private <T> T findByType(Map<String, RankedObject<T>> byType, Content content) {
		if (content == null)
			throw new IllegalArgumentException("A node should be provided");

		if (content instanceof JcrContent) {
			Node context = ((JcrContent) content).getJcrNode();
			try {
				// mixins
				Set<String> types = new TreeSet<>();
				for (NodeType mixinType : context.getMixinNodeTypes()) {
					String mixinTypeName = mixinType.getName();
					if (byType.containsKey(mixinTypeName)) {
						types.add(mixinTypeName);
					}
					for (NodeType superType : mixinType.getDeclaredSupertypes()) {
						if (byType.containsKey(superType.getName())) {
							types.add(superType.getName());
						}
					}
				}
				// primary node type
				NodeType primaryType = context.getPrimaryNodeType();
				String primaryTypeName = primaryType.getName();
				if (byType.containsKey(primaryTypeName)) {
					types.add(primaryTypeName);
				}
				for (NodeType superType : primaryType.getDeclaredSupertypes()) {
					if (byType.containsKey(superType.getName())) {
						types.add(superType.getName());
					}
				}
				// entity type
				if (context.isNodeType(EntityType.entity.get())) {
					if (context.hasProperty(EntityNames.ENTITY_TYPE)) {
						String entityTypeName = context.getProperty(EntityNames.ENTITY_TYPE).getString();
						if (byType.containsKey(entityTypeName)) {
							types.add(entityTypeName);
						}
					}
				}

				if (CmsJcrUtils.isUserHome(context) && byType.containsKey("nt:folder")) {// home node
					types.add("nt:folder");
				}

				if (types.size() == 0)
					throw new IllegalArgumentException(
							"No type found for " + context + " (" + listTypes(context) + ")");
				String type = types.iterator().next();
				if (!byType.containsKey(type))
					throw new IllegalArgumentException("No component found for " + context + " with type " + type);
				return byType.get(type).get();
			} catch (RepositoryException e) {
				throw new IllegalStateException(e);
			}

		} else {
			List<QName> objectClasses = content.getContentClasses();
			Set<String> types = new TreeSet<>();
			for (QName cc : objectClasses) {
				String type = cc.getPrefix() + ":" + cc.getLocalPart();
				if (byType.containsKey(type))
					types.add(type);
			}
			if (types.size() == 0) {
				throw new IllegalArgumentException("No type found for " + content + " (" + objectClasses + ")");
			}
			String type = types.iterator().next();
			if (!byType.containsKey(type))
				throw new IllegalArgumentException("No component found for " + content + " with type " + type);
			return byType.get(type).get();
		}
	}

	private static String listTypes(Node context) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(context.getPrimaryNodeType().getName());
			for (NodeType superType : context.getPrimaryNodeType().getDeclaredSupertypes()) {
				sb.append(' ');
				sb.append(superType.getName());
			}

			for (NodeType nodeType : context.getMixinNodeTypes()) {
				sb.append(' ');
				sb.append(nodeType.getName());
				if (nodeType.getName().equals(EntityType.local.get()))
					sb.append('/').append(context.getProperty(EntityNames.ENTITY_TYPE).getString());
				for (NodeType superType : nodeType.getDeclaredSupertypes()) {
					sb.append(' ');
					sb.append(superType.getName());
				}
			}
			return sb.toString();
		} catch (RepositoryException e) {
			throw new JcrException(e);
		}
	}

	@Override
	public void setState(CmsUi cmsUi, String state) {
		if (state == null)
			return;
		if (!state.startsWith("/")) {
			if (cmsUi instanceof SuiteUi) {
				SuiteUi ui = (SuiteUi) cmsUi;
				if (LOGIN.equals(state)) {
					String appTitle = "";
					if (ui.getTitle() != null)
						appTitle = ui.getTitle().lead();
					ui.getCmsView().stateChanged(state, appTitle);
					return;
				}
				Map<String, Object> properties = new HashMap<>();
				String layerId = HOME_STATE.equals(state) ? defaultLayerPid : state;
				properties.put(SuiteUxEvent.LAYER, layerId);
				properties.put(SuiteUxEvent.CONTENT_PATH, HOME_STATE);
				ui.getCmsView().sendEvent(SuiteUxEvent.switchLayer.topic(), properties);
			}
			return;
		}
		SuiteUi suiteUi = (SuiteUi) cmsUi;
		if (suiteUi.isLoginScreen()) {
			return;
		}

		Content node = stateToNode(suiteUi, state);
		if (node == null) {
			suiteUi.getCmsView().navigateTo(HOME_STATE);
		} else {
			suiteUi.getCmsView().sendEvent(SuiteUxEvent.switchLayer.topic(), SuiteUxEvent.eventProperties(node));
			suiteUi.getCmsView().sendEvent(SuiteUxEvent.refreshPart.topic(), SuiteUxEvent.eventProperties(node));
		}
	}

	// TODO move it to an internal package?
	static String nodeToState(Content node) {
		return node.getPath();
	}

	private Content stateToNode(SuiteUi suiteUi, String state) {
		if (suiteUi == null)
			return null;
		if (state == null || !state.startsWith("/"))
			return null;

		String path = state;

		ProvidedSession contentSession = (ProvidedSession) CmsUxUtils.getContentSession(contentRepository,
				suiteUi.getCmsView());
		return contentSession.get(path);
	}

	/*
	 * Events management
	 */

	@Override
	public void onEvent(String topic, Map<String, Object> event) {

		// Specific UI related events
		SuiteUi ui = getRelatedUi(event);
		if (ui == null)
			return;
		ui.getCmsView().runAs(() -> {
			try {
				String appTitle = "";
				if (ui.getTitle() != null)
					appTitle = ui.getTitle().lead() + " - ";

				if (SuiteUiUtils.isTopic(topic, SuiteUxEvent.refreshPart)) {
					Content node = getContentFromEvent(ui, event);
					if (node == null)
						return;
					SwtUiProvider uiProvider = findByType(uiProvidersByType, node);
					SuiteLayer layer = findByType(layersByType, node);
					ui.switchToLayer(layer, node);
					layer.view(uiProvider, ui.getCurrentWorkArea(), node);
					ui.getCmsView().stateChanged(nodeToState(node), appTitle + CmsUxUtils.getTitle(node));
				} else if (SuiteUiUtils.isTopic(topic, SuiteUxEvent.openNewPart)) {
					Content node = getContentFromEvent(ui, event);
					if (node == null)
						return;
					SwtUiProvider uiProvider = findByType(uiProvidersByType, node);
					SuiteLayer layer = findByType(layersByType, node);
					ui.switchToLayer(layer, node);
					layer.open(uiProvider, ui.getCurrentWorkArea(), node);
					ui.getCmsView().stateChanged(nodeToState(node), appTitle + CmsUxUtils.getTitle(node));
				} else if (SuiteUiUtils.isTopic(topic, SuiteUxEvent.switchLayer)) {
					String layerId = get(event, SuiteUxEvent.LAYER);
					if (layerId != null) {
						SuiteLayer suiteLayer = findLayer(layerId);
						if (suiteLayer == null)
							throw new IllegalArgumentException("No layer '" + layerId + "' available.");
						Localized layerTitle = suiteLayer.getTitle();
						// FIXME make sure we don't rebuild the work area twice
						Composite workArea = ui.switchToLayer(layerId, ui.getUserDir());
						String title = null;
						if (layerTitle != null)
							title = layerTitle.lead();
						Content nodeFromState = getContentFromEvent(ui, event);
						if (nodeFromState != null && nodeFromState.getPath().equals(ui.getUserDir().getPath())) {
							// default layer view is forced
							String state = defaultLayerPid.equals(layerId) ? "~" : layerId;
							ui.getCmsView().stateChanged(state, appTitle + title);
							suiteLayer.view(null, workArea, nodeFromState);
						} else {
							Content layerCurrentContext = suiteLayer.getCurrentContext(workArea);
							if (layerCurrentContext != null && !layerCurrentContext.equals(ui.getUserDir())) {
								// layer was already showing a context so we set the state to it
								ui.getCmsView().stateChanged(nodeToState(layerCurrentContext),
										appTitle + CmsUxUtils.getTitle(layerCurrentContext));
							} else {
								// no context was shown
								ui.getCmsView().stateChanged(layerId, appTitle + title);
							}
						}
					} else {
						Content node = getContentFromEvent(ui, event);
						if (node != null) {
							SuiteLayer layer = findByType(layersByType, node);
							ui.switchToLayer(layer, node);
						}
					}
				}
			} catch (Exception e) {
				CmsFeedback.error("Cannot handle event " + topic + " " + event, e);
//				log.error("Cannot handle event " + event, e);
			}
		});
	}

	protected Content getContentFromEvent(SuiteUi ui, Map<String, Object> event) {
		ProvidedSession contentSession = (ProvidedSession) CmsUxUtils.getContentSession(contentRepository,
				ui.getCmsView());

		String path = get(event, SuiteUxEvent.CONTENT_PATH);

		if (path != null && (path.equals(HOME_STATE) || path.equals("")))
			return ui.getUserDir();
		Content node;
		if (path == null) {
			// look for a user
			String username = get(event, SuiteUxEvent.USERNAME);
			if (username == null)
				return null;
			User user = cmsUserManager.getUser(username);
			if (user == null)
				return null;
			node = ContentUtils.roleToContent(cmsUserManager, contentSession, user);
		} else {
			node = contentSession.get(path);
		}
		return node;
	}

	private SuiteUi getRelatedUi(Map<String, Object> eventProperties) {
		return managedUis.get(get(eventProperties, CMS_VIEW_UID_PROPERTY));
	}

	public static String get(Map<String, Object> eventProperties, String key) {
		Object value = eventProperties.get(key);
		if (value == null)
			return null;
		return value.toString();

	}

	/*
	 * Dependency injection.
	 */

	public void addUiProvider(SwtUiProvider uiProvider, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			RankedObject.putIfHigherRank(uiProvidersByPid, pid, uiProvider, properties);
		}
		if (properties.containsKey(EntityConstants.TYPE)) {
			List<String> types = LangUtils.toStringList(properties.get(EntityConstants.TYPE));
			for (String type : types) {
				RankedObject.putIfHigherRank(uiProvidersByType, type, uiProvider, properties);
			}
		}
	}

	public void removeUiProvider(SwtUiProvider uiProvider, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			if (uiProvidersByPid.containsKey(pid)) {
				if (uiProvidersByPid.get(pid).equals(new RankedObject<SwtUiProvider>(uiProvider, properties))) {
					uiProvidersByPid.remove(pid);
				}
			}
		}
		if (properties.containsKey(EntityConstants.TYPE)) {
			List<String> types = LangUtils.toStringList(properties.get(EntityConstants.TYPE));
			for (String type : types) {
				if (uiProvidersByType.containsKey(type)) {
					if (uiProvidersByType.get(type).equals(new RankedObject<SwtUiProvider>(uiProvider, properties))) {
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
					if (layersByType.get(type).equals(new RankedObject<SuiteLayer>(layer, properties))) {
						layersByType.remove(type);
					}
				}
			}
		}
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

	protected ContentRepository getContentRepository() {
		return contentRepository;
	}

	public void setContentRepository(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

	public void setJcrContentProvider(JcrContentProvider jcrContentProvider) {
		this.jcrContentProvider = jcrContentProvider;
	}

}
