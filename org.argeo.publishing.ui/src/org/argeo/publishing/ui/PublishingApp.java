package org.argeo.publishing.ui;

import static org.argeo.suite.ui.SuiteApp.DEFAULT_THEME_ID_PROPERTY;
import static org.argeo.suite.ui.SuiteApp.DEFAULT_UI_NAME_PROPERTY;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.cms.CmsApp;
import org.argeo.api.cms.CmsUi;
import org.argeo.cms.AbstractCmsApp;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.jcr.Jcr;
import org.argeo.suite.ui.SuiteApp;
import org.argeo.util.LangUtils;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * A {@link CmsApp} dedicated to publishing, typically a public or internal web
 * site.
 */
public class PublishingApp extends AbstractCmsApp implements EventHandler {
	private final static Log log = LogFactory.getLog(PublishingApp.class);

	private String pid;
	private String defaultThemeId;
	private String defaultUiName = "";

	private String publicBasePath = null;

	private CmsUiProvider landingPage;
	private CmsUiProvider defaultProvider = new DocumentUiProvider();

	private Repository repository;

	public void init(Map<String, String> properties) {
		if (properties.containsKey(DEFAULT_UI_NAME_PROPERTY))
			defaultUiName = LangUtils.get(properties, DEFAULT_UI_NAME_PROPERTY);
		if (properties.containsKey(DEFAULT_THEME_ID_PROPERTY))
			defaultThemeId = LangUtils.get(properties, DEFAULT_THEME_ID_PROPERTY);
		publicBasePath = LangUtils.get(properties, SuiteApp.PUBLIC_BASE_PATH_PROPERTY);
		pid = properties.get(Constants.SERVICE_PID);

		if (log.isDebugEnabled())
			log.info("Publishing App " + pid + " started");
	}

	public void destroy(Map<String, String> properties) {
		if (log.isDebugEnabled())
			log.info("Publishing App " + pid + " stopped");

	}

	@Override
	public Set<String> getUiNames() {
		Set<String> uiNames = new HashSet<>();
		uiNames.add(defaultUiName);
		return uiNames;
	}

	@Override
	public CmsUi initUi(Object uiParent) {
		Composite parent = (Composite) uiParent;
//		Session adminSession = NodeUtils.openDataAdminSession(getRepository(), null);
		Session session = Jcr.login(getRepository(), null);
		parent.setLayout(new GridLayout());
		Node indexNode = Jcr.getNode(session, publicBasePath + "/index");
//		try {
//			indexNode = JcrUtils.getOrAdd(Jcr.getRootNode(adminSession), DocumentPage.WWW, DbkType.article.get());
//			adminSession.save();
//		} catch (RepositoryException e) {
//			throw new IllegalStateException(e);
//		}

		Control page;
		if (landingPage != null) {
			page = landingPage.createUiPart(parent, indexNode);
		} else {
			page = defaultProvider.createUiPart(parent, indexNode);
		}
		return (CmsUi) page;
	}

	@Override
	public void refreshUi(CmsUi cmsUi, String state) {
		Composite parent = (Composite) cmsUi;
		parent.setLayout(new GridLayout());
		if (landingPage != null)
			landingPage.createUiPart(parent, null);
		else
			defaultProvider.createUiPart(parent, null);
	}

	@Override
	public void setState(CmsUi cmsUi, String state) {

	}

	@Override
	protected String getThemeId(String uiName) {
		return defaultThemeId;
	}

	public void setLandingPage(CmsUiProvider landingPage) {
		this.landingPage = landingPage;
	}

	@Override
	public void handleEvent(Event event) {
		// TODO listen to some events

	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
