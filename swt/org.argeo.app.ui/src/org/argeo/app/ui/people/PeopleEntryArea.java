package org.argeo.app.ui.people;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentRepository;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.ldap.LdapAttrs;
import org.argeo.api.acr.ldap.LdapObjs;
import org.argeo.api.cms.directory.CmsDirectory;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.api.cms.directory.UserDirectory;
import org.argeo.api.cms.ux.CmsIcon;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.app.ui.SuiteMsg;
import org.argeo.app.ui.SuiteUxEvent;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.auth.CmsRole;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.auth.UserAdminUtils;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.SwtGuidedFormDialog;
import org.argeo.cms.swt.widgets.SwtTableView;
import org.argeo.cms.swt.widgets.SwtTreeView;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ux.widgets.AbstractHierarchicalPart;
import org.argeo.cms.ux.widgets.Column;
import org.argeo.cms.ux.widgets.DefaultTabularPart;
import org.argeo.cms.ux.widgets.GuidedForm;
import org.argeo.cms.ux.widgets.HierarchicalPart;
import org.eclipse.jface.window.Window;
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
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

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
		HierarchicalPart<HierarchyUnit> hierarchyPart = new AbstractHierarchicalPart<>() {

			@Override
			public List<HierarchyUnit> getChildren(HierarchyUnit parent) {
				List<HierarchyUnit> visible = new ArrayList<>();
				if (parent != null) {
					if (parent instanceof CmsDirectory) // do no show children of the directories
						return visible;
					for (HierarchyUnit hu : parent.getDirectHierarchyUnits(true)) {
						visible.add(hu);
					}
				} else {
					for (UserDirectory directory : cmsUserManager.getUserDirectories()) {
						if (CurrentUser.implies(CmsRole.userAdmin, directory.getBase())) {
							visible.add(directory);
						}
						for (HierarchyUnit hu : directory.getDirectHierarchyUnits(true)) {
							if (CurrentUser.implies(CmsRole.userAdmin, hu.getBase())) {
								visible.add(hu);
							}
						}

					}
				}
				return visible;
			}

			@Override
			public String getText(HierarchyUnit model) {
				return model.getHierarchyUnitLabel(CurrentUser.locale());
			}

			@Override
			public CmsIcon getIcon(HierarchyUnit model) {
				Content content = ContentUtils.hierarchyUnitToContent(contentSession, model);
				if (content.hasContentClass(LdapObjs.organization))
					return SuiteIcon.organisation;
				else if (content.hasContentClass(LdapObjs.posixGroup))
					return SuiteIcon.users;
				else
					return SuiteIcon.addressBook;
			}

		};
		SwtTreeView<HierarchyUnit> directoriesView = new SwtTreeView<>(sashForm, SWT.BORDER, hierarchyPart);

		DefaultTabularPart<HierarchyUnit, Content> usersPart = new DefaultTabularPart<>() {

			@Override
			protected List<Content> asList(HierarchyUnit hu) {
				List<Content> roles = new ArrayList<>();
				UserDirectory ud = (UserDirectory) hu.getDirectory();
				if (ud.getRealm().isPresent()) {
					for (Role r : ud.getHierarchyUnitRoles(ud, null, true)) {
						Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
						if (content.hasContentClass(LdapObjs.inetOrgPerson, LdapObjs.organization))
							roles.add(content);
					}

				} else {
					for (HierarchyUnit directChild : hu.getDirectHierarchyUnits(false)) {
						if (!(directChild.isType(HierarchyUnit.Type.FUNCTIONAL)
								|| directChild.isType(HierarchyUnit.Type.ROLES))) {
							for (Role r : ud.getHierarchyUnitRoles(directChild, null, false)) {
								Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
								if (content.hasContentClass(LdapObjs.inetOrgPerson, LdapObjs.organization,
										LdapObjs.groupOfNames))
									roles.add(content);
							}
						}
					}
				}
				return roles;
			}
		};
		usersPart.addColumn(new Column<Content>() {

			@Override
			public String getText(Content role) {
				if (role.hasContentClass(LdapObjs.inetOrgPerson))
					return UserAdminUtils.getUserDisplayName(role.adapt(User.class));
				else if (role.hasContentClass(LdapObjs.organization))
					return role.attr(LdapAttrs.o);
				else if (role.hasContentClass(LdapObjs.groupOfNames))
					return role.attr(LdapAttrs.cn);
				else
					return null;
			}

			@Override
			public CmsIcon getIcon(Content role) {
				if (role.hasContentClass(LdapObjs.posixAccount))
					return SuiteIcon.user;
				else if (role.hasContentClass(LdapObjs.inetOrgPerson))
					return SuiteIcon.person;
				else if (role.hasContentClass(LdapObjs.organization))
					return SuiteIcon.organisationContact;
				else if (role.hasContentClass(LdapObjs.groupOfNames))
					return SuiteIcon.group;
				else
					return null;
			}

			@Override
			public int getWidth() {
				return 300;
			}

		});
//		usersPart.addColumn((Column<Content>) (role) -> role.attr(LdapAttrs.mail));
		usersPart.addColumn(new Column<Content>() {

			@Override
			public String getText(Content role) {
				return role.attr(LdapAttrs.mail);
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
//		CmsUiUtils.style(deleteItem, SuiteStyle.recentItems);
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
			if (dialog.open() == Window.OK) {
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
			if (dialog.open() == Window.OK) {
				// TODO create
			}
		});

		ToolItem addItem = new ToolItem(bottomToolBar, SWT.PUSH);
		addItem.setEnabled(false);
		addItem.setImage(theme.getSmallIcon(SuiteIcon.add));

		sashForm.setWeights(new int[] { 30, 70 });

		SwtTableView<?, ?> usersTable = new SwtTableView<>(bottom, SWT.BORDER, usersPart);
		usersTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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
