package org.argeo.app.ui.publish;

import static org.argeo.app.swt.ux.SwtArgeoApp.DEFAULT_THEME_ID_PROPERTY;
import static org.argeo.app.swt.ux.SwtArgeoApp.DEFAULT_UI_NAME_PROPERTY;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.api.cms.CmsApp;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.ux.CmsUi;
import org.argeo.app.swt.ux.SwtArgeoApp;
import org.argeo.app.ux.AbstractArgeoApp;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.util.LangUtils;
import org.argeo.jcr.Jcr;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Constants;

/**
 * A {@link CmsApp} dedicated to publishing, typically a public or internal web
 * site.
 */
public class PublishingApp extends AbstractArgeoApp {
	private final static CmsLog log = CmsLog.getLog(PublishingApp.class);

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
		publicBasePath = LangUtils.get(properties, SwtArgeoApp.PUBLIC_BASE_PATH_PROPERTY);
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
			landingPage.createUiPart(parent, (Node) null);
		else
			defaultProvider.createUiPart(parent, (Node) null);
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

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
