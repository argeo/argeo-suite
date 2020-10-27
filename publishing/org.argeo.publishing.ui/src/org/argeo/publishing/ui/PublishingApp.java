package org.argeo.publishing.ui;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.NodeUtils;
import org.argeo.cms.ui.AbstractCmsApp;
import org.argeo.cms.ui.CmsApp;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.docbook.ui.DocBookTypes;
import org.argeo.docbook.ui.DocumentPage;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Constants;

/**
 * A {@link CmsApp} dedicated to publishing, typically a public or internal web
 * site.
 */
public class PublishingApp extends AbstractCmsApp {
	private final static Log log = LogFactory.getLog(PublishingApp.class);

	private String pid;
	private String defaultThemeId;

	private CmsUiProvider landingPage;

	public void init(Map<String, String> properties) {
		defaultThemeId = properties.get("defaultThemeId");
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
		uiNames.add("");
		return uiNames;
	}

	@Override
	public Composite initUi(Composite parent) {
		Session adminSession = NodeUtils.openDataAdminSession(getRepository(), null);
		parent.setLayout(new GridLayout());
		Node indexNode;
		try {
			indexNode = JcrUtils.getOrAdd(Jcr.getRootNode(adminSession), DocumentPage.WWW, DocBookTypes.ARTICLE);
			adminSession.save();
		} catch (RepositoryException e) {
			throw new IllegalStateException(e);
		}

		Control page;
		if (landingPage != null) {
			page = landingPage.createUiPart(parent, indexNode);
		} else {
			page = new DocumentPage().createUiPart(parent, indexNode);
		}
		return (Composite) page;
	}

	@Override
	public void refreshUi(Composite parent, String state) {
		parent.setLayout(new GridLayout());
		new DocumentPage().createUiPart(parent, null);
	}

	@Override
	public void setState(Composite parent, String state) {

	}

	@Override
	protected String getThemeId(String uiName) {
		return defaultThemeId;
	}

	public void setLandingPage(CmsUiProvider landingPage) {
		this.landingPage = landingPage;
	}

}
