package org.argeo.suite.web;

import java.nio.file.spi.FileSystemProvider;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.cms.CmsMsg;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.LifeCycleUiProvider;
import org.argeo.cms.util.CmsUtils;
import org.argeo.cms.widgets.auth.CmsLogin;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.web.pages.PeopleDefaultPage;
import org.argeo.connect.resources.ResourceService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.suite.web.fs.MyFilesBrowserPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Default entry point for the Argeo Suite CMS. Forwards the request to the
 * relevant CmsUiProvider
 */
public class DefaultMainPage implements LifeCycleUiProvider {

	private CmsUiProvider dashboardPage;
	private CmsUiProvider peoplePage;
	private CmsUiProvider fsBrowserPage;

	/* DEPENDENCY INJECTION */
	private ResourceService resourceService;
	private PeopleService peopleService;
	private FileSystemProvider nodeFileSystemProvider;
	private Map<String, String> peopleIconPaths;

	public DefaultMainPage() {
	}

	@Override
	public void init(Session adminSession) throws RepositoryException {
		dashboardPage = new DefaultDashboard(resourceService, peopleService, peopleIconPaths);
		peoplePage = new PeopleDefaultPage(resourceService, peopleService, peopleIconPaths);
		fsBrowserPage = new MyFilesBrowserPage(nodeFileSystemProvider);
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		if (CurrentUser.isAnonymous())
			return createAnonymousUi(parent, context);

		if (context.isNodeType(PeopleTypes.PEOPLE_ENTITY))
			return peoplePage.createUi(parent, context);
		else if (peopleService.getBasePath(null).equals(context.getPath()))
			return peoplePage.createUi(parent, context);
		else if (context.isNodeType(NodeType.NT_FOLDER) || context.isNodeType(NodeType.NT_FILE))
			return fsBrowserPage.createUi(parent, context);
		else
			return dashboardPage.createUi(parent, context);
	}

	public Control createAnonymousUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		Composite body = new Composite(parent, SWT.NO_FOCUS);
		body.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		body.setLayout(new GridLayout());

		Composite loginCmp = new Composite(body, SWT.NO_FOCUS);
		loginCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		loginCmp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		CmsLogin login = new MyCmsLogin(CmsUtils.getCmsView());
		// Composite credBlockCmp =
		login.createCredentialsBlock(loginCmp);
		// Use a custom style that has no border, among other
		// CmsUtils.style(loginCmp, ArgeoStyles.LOGIN_INLINE_CREDBLOCK);

		Label anonymousLbl = new Label(body, SWT.WRAP);
		anonymousLbl.setText("You should login or register to access your private dashboard");

		return body;

	}

	private class MyCmsLogin extends CmsLogin {

		public MyCmsLogin(CmsView cmsView) {
			super(cmsView);
		}

		@Override
		protected boolean login() {
			boolean result = super.login();
			return result;
		}

		@Override
		protected void extendsCredentialsBlock(Composite credentialsBlock, Locale selectedLocale,
				SelectionListener loginSelectionListener) {
			Button loginBtn = new Button(credentialsBlock, SWT.PUSH);
			loginBtn.setText(CmsMsg.login.lead(selectedLocale));
			loginBtn.setLayoutData(CmsUtils.fillWidth());
			loginBtn.addSelectionListener(loginSelectionListener);
			// CmsUtils.style(loginBtn, ArgeoStyles.LOGIN_SIGNIN_BTN);
		}
	}

	@Override
	public void destroy() {
	}

	/* DEPENDENCY INJECTION */
	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setNodeFileSystemProvider(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}

	public void setPeopleIconPaths(Map<String, String> peopleIconPaths) {
		this.peopleIconPaths = peopleIconPaths;
	}
}
