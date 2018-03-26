package org.argeo.suite.e4.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.SystemAppService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

/** Generic dashboard for Argeo Suite applications */
public abstract class AbstractSuiteDashboard {

	// DEPENDENCY INJECTION
	@Inject
	private Repository repository;
	@Inject
	private ResourcesService resourcesService;
	@Inject
	private SystemAppService systemAppService;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;

	private Session session;

	// UI Objects
	private FormToolkit toolkit;

	public void init()  {
		session = ConnectJcrUtils.login(repository);
//		updateTooltip(input);
	}

//	private void updateTooltip(IEditorInput input) {
//		if (input instanceof EntityEditorInput) {
//			EntityEditorInput sei = (EntityEditorInput) input;
//			sei.setTooltipText("My Dashboard");
//		}
//	}

	/**
	 * Implementing classes must call super in order to create the correct form
	 * toolkit
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(Display.getCurrent());
		init();
	}

	// UTILS
	protected Composite createGadgetCmp(Composite parent, int widthHint, int heightHint) {
		Composite gadgetCmp = toolkit.createComposite(parent, SWT.BORDER);
		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gd.widthHint = widthHint;
		gd.heightHint = heightHint;
		gadgetCmp.setLayoutData(gd);
		CmsUtils.style(gadgetCmp, ConnectUiStyles.GADGET_BOX);
		return gadgetCmp;
	}

	protected Composite createGadgetTitleCmp(Composite parent, String title) {
		Composite titleCmp = toolkit.createComposite(parent, SWT.BACKGROUND | SWT.INHERIT_NONE);
		CmsUtils.style(titleCmp, ConnectUiStyles.GADGET_HEADER);
		titleCmp.setBackground(null);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		titleCmp.setLayoutData(gd);
		titleCmp.setLayout(new GridLayout());

		Label titleLbl = toolkit.createLabel(titleCmp, title + " ", SWT.BOLD);
		CmsUtils.style(titleLbl, ConnectUiStyles.GADGET_HEADER);
		titleLbl.setBackground(null);
		return titleCmp;
	}

	protected Composite createGadgetBodyCmp(Composite parent) {
		Composite bodyCmp = toolkit.createComposite(parent, SWT.BACKGROUND | SWT.INHERIT_NONE);
		bodyCmp.setLayoutData(EclipseUiUtils.fillAll());
		bodyCmp.setLayout(new GridLayout());
		return bodyCmp;
	}

	protected Link createOpenEntityEditorLink(final AppWorkbenchService peopleUiService, Composite parent,
			final String label, final Node entity) {
		Link link = new Link(parent, SWT.NONE);
		link.setText("<a>" + label + "</a>");
		link.setLayoutData(EclipseUiUtils.fillWidth());
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				// Map<String, String> params = new HashMap<String, String>();
				// params.put(ConnectEditor.PARAM_JCR_ID,
				// ConnectJcrUtils.getIdentifier(entity));
				// CommandUtils.callCommand(peopleUiService.getOpenEntityEditorCmdId(), params);
				peopleUiService.openEntityEditor(entity);
			}
		});
		return link;
	}

	// Life cycle
	@PreDestroy
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}


	// Expose to implementing classes
	protected Session getSession() {
		return session;
	}

	public ResourcesService getResourcesService() {
		return resourcesService;
	}

	protected SystemAppService getSystemAppService() {
		return systemAppService;
	}

	protected SystemWorkbenchService getSystemWorkbenchService() {
		return systemWorkbenchService;
	}

	protected FormToolkit getFormToolkit() {
		return toolkit;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setSystemAppService(SystemAppService systemAppService) {
		this.systemAppService = systemAppService;
	}

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}
}