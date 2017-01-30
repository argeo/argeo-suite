package org.argeo.suite.workbench.parts;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.workbench.rap.PeopleStyles;
import org.argeo.connect.people.workbench.rap.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.editors.util.EntityEditorInput;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;

/** Generic dashboard for Argeo Suite applications */
public abstract class AbstractSuiteDashboard extends EditorPart {

	// DEPENDENCY INJECTION
	private Repository repository;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;

	private Session session;

	// UI Objects
	private Image logoImg;
	private FormToolkit toolkit;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);

		session = ConnectJcrUtils.login(repository);
		// initialiseImg();
		updateTooltip(input);
	}

	private void updateTooltip(IEditorInput input) {
		if (input instanceof EntityEditorInput) {
			EntityEditorInput sei = (EntityEditorInput) input;
			sei.setTooltipText("My Dashboard");
		}
	}

	// private void initialiseImg() {
	// InputStream is = null;
	// try {
	// String imgPath = peopleService.getInstanceConfPath() + "/"
	// + AoNames.AO_DEFAULT_LOGO;
	// if (session.nodeExists(imgPath)) {
	// Node imageNode = session.getNode(imgPath).getNode(
	// Node.JCR_CONTENT);
	// is = imageNode.getProperty(Property.JCR_DATA).getBinary()
	// .getStream();
	// logoImg = new Image(this.getSite().getShell().getDisplay(), is);
	// }
	// } catch (RepositoryException re) {
	// throw new AoException(
	// "Unable to initialise specific logo for demo app", re);
	// } finally {
	// IOUtils.closeQuietly(is);
	// }
	// }

	/**
	 * Implementing classes must call super in order to create the correct form
	 * toolkit
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(getSite().getShell().getDisplay());
	}

	// UTILS
	protected Composite createGadgetCmp(Composite parent, int widthHint, int heightHint) {
		Composite gadgetCmp = toolkit.createComposite(parent, SWT.BORDER);
		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gd.widthHint = widthHint;
		gd.heightHint = heightHint;
		gadgetCmp.setLayoutData(gd);
		CmsUtils.style(gadgetCmp, PeopleStyles.PEOPLE_CLASS_GADGET);
		return gadgetCmp;
	}

	protected Composite createGadgetTitleCmp(Composite parent, String title) {
		Composite titleCmp = toolkit.createComposite(parent, SWT.BACKGROUND | SWT.INHERIT_NONE);
		CmsUtils.style(titleCmp, PeopleStyles.GADGET_HEADER);
		titleCmp.setBackground(null);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		titleCmp.setLayoutData(gd);
		titleCmp.setLayout(new GridLayout());

		Label titleLbl = toolkit.createLabel(titleCmp, title + " ", SWT.BOLD);
		CmsUtils.style(titleLbl, PeopleStyles.GADGET_HEADER);
		titleLbl.setBackground(null);
		return titleCmp;
	}

	protected Composite createGadgetBodyCmp(Composite parent) {
		Composite bodyCmp = toolkit.createComposite(parent, SWT.BACKGROUND | SWT.INHERIT_NONE);
		bodyCmp.setLayoutData(EclipseUiUtils.fillAll());
		bodyCmp.setLayout(new GridLayout());
		return bodyCmp;
	}

	// LIFE CYCLE
	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		if (logoImg != null)
			logoImg.dispose();
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		// Do nothing
	}

	// Expose to implementing classes
	protected PeopleService getPeopleService() {
		return peopleService;
	}

	protected PeopleWorkbenchService getPeopleWorkbenchService() {
		return peopleWorkbenchService;
	}

	protected Session getSession() {
		return session;
	}

	protected Image getLogoImg() {
		return logoImg;
	}

	protected FormToolkit getFormToolkit() {
		return toolkit;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleWorkbenchService(PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
