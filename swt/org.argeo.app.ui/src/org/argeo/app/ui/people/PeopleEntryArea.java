package org.argeo.app.ui.people;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentRepository;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.app.ui.SuiteMsg;
import org.argeo.app.ui.SuiteUxEvent;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.SwtGuidedFormDialog;
import org.argeo.cms.swt.widgets.SwtTableView;
import org.argeo.cms.swt.widgets.SwtTreeView;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ux.widgets.CmsDialog;
import org.argeo.cms.ux.widgets.Column;
import org.argeo.cms.ux.widgets.GuidedForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/** Entry to the admin area. */
public class PeopleEntryArea implements SwtUiProvider, CmsUiProvider {

	private CmsUserManager cmsUserManager;

	private ContentRepository contentRepository;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		parent.setLayout(new GridLayout());

		ContentSession contentSession = contentRepository.get();
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		CmsSwtUtils.fill(sashForm);

		// VIEW
		HierarchyUnitPart hierarchyPart = new HierarchyUnitPart(contentSession, cmsUserManager);
		SwtTreeView<HierarchyUnit> directoriesView = new SwtTreeView<>(sashForm, SWT.BORDER, hierarchyPart);

		UsersPart usersPart = new UsersPart(contentSession, cmsUserManager);
		usersPart.addColumn(new Column<Content>() {

			@Override
			public String getText(Content role) {
				return role.attr(LdapAttr.mail);
			}

			@Override
			public int getWidth() {
				return 300;
			}
		});
		// toolbar
		Composite bottom = new Composite(sashForm, SWT.NONE);
		bottom.setLayoutData(CmsSwtUtils.fillWidth());
		bottom.setLayout(CmsSwtUtils.noSpaceGridLayout());
		ToolBar bottomToolBar = new ToolBar(bottom, SWT.NONE);
		bottomToolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

		ToolItem deleteItem = new ToolItem(bottomToolBar, SWT.FLAT);
		deleteItem.setEnabled(false);
		deleteItem.setImage(theme.getSmallIcon(SuiteIcon.delete));

		Menu menu = new Menu(Display.getCurrent().getActiveShell(), SWT.POP_UP);
		// TODO display add user only if hierarchy unit is a POSIX group
		// hierarchyUnit.hasContentClass(LdapObjs.posixGroup.qName())
		MenuItem addUserItem = new MenuItem(menu, SWT.PUSH);
		addUserItem.setImage(theme.getSmallIcon(SuiteIcon.user));
		addUserItem.setText(SuiteMsg.user.lead());
		addUserItem.addSelectionListener((Selected) (e) -> {
			HierarchyUnit hierarchyUnit = usersPart.getInput();
			Content huContent = ContentUtils.hierarchyUnitToContent(contentSession, hierarchyUnit);
			GuidedForm wizard = new NewUserForm(cmsUserManager, huContent);
			SwtGuidedFormDialog dialog = new SwtGuidedFormDialog(parent.getShell(), wizard);
			if (dialog.open() == CmsDialog.OK) {
				// TODO create
			}
		});

		MenuItem addOrgItem = new MenuItem(menu, SWT.PUSH);
		addOrgItem.setImage(theme.getSmallIcon(SuiteIcon.organisation));
		addOrgItem.setText(SuiteMsg.org.lead());
		addOrgItem.addSelectionListener((Selected) (e) -> {
			HierarchyUnit hierarchyUnit = usersPart.getInput();
			Content huContent = ContentUtils.hierarchyUnitToContent(contentSession, hierarchyUnit);
			GuidedForm wizard = new NewOrgForm(cmsUserManager, huContent);
			SwtGuidedFormDialog dialog = new SwtGuidedFormDialog(parent.getShell(), wizard);
			if (dialog.open() == CmsDialog.OK) {
				// TODO create
			}
		});

		ToolItem addItem = new ToolItem(bottomToolBar, SWT.PUSH);
		addItem.setEnabled(false);
		addItem.setImage(theme.getSmallIcon(SuiteIcon.add));

		sashForm.setWeights(new int[] { 30, 70 });

		SwtTableView<?, ?> usersView = new SwtTableView<>(bottom, SWT.BORDER, usersPart);
		usersView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// CONTROLLER
		hierarchyPart.onSelected((o) -> {
			if (o instanceof HierarchyUnit) {
				HierarchyUnit hierarchyUnit = (HierarchyUnit) o;
				usersPart.setInput(hierarchyUnit);
				addItem.setEnabled(true);
//				cmsView.sendEvent(SuiteUxEvent.refreshPart.topic(), SuiteUxEvent
//						.eventProperties(ContentUtils.hierarchyUnitToContent(contentSession, hierarchyUnit)));
			}
		});

		usersPart.onSelected((o) -> {
			Content user = (Content) o;
			if (user != null) {
				cmsView.sendEvent(SuiteUxEvent.refreshPart.topic(), SuiteUxEvent.eventProperties(user));
				deleteItem.setEnabled(true);
			} else {
				deleteItem.setEnabled(false);
			}
		});

		usersPart.onAction((o) -> {
			Content user = (Content) o;
			if (user != null) {
				cmsView.sendEvent(SuiteUxEvent.openNewPart.topic(), SuiteUxEvent.eventProperties(user));
			}
		});

		addItem.addSelectionListener((Selected) (e) -> {
//			if (e.detail == SWT.ARROW) {
			Rectangle rect = addItem.getBounds();
			Point pt = new Point(rect.x, rect.y + rect.height);
			pt = bottomToolBar.toDisplay(pt);
			menu.setLocation(pt.x, pt.y);
			menu.setVisible(true);
//			}
		});

		directoriesView.refresh();
//		usersView.refresh();

		return sashForm;
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		return createUiPart(parent, JcrContent.nodeToContent(context));
	}

	public void setContentRepository(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

}
