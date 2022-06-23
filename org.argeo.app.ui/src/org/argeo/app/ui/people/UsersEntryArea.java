package org.argeo.app.ui.people;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentRepository;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.cms.ux.CmsIcon;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.ui.SuiteEvent;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.app.ui.dialogs.NewUserWizard;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.auth.CmsRole;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.auth.UserAdminUtils;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.dialogs.CmsWizardDialog;
import org.argeo.cms.swt.widgets.SwtHierarchicalPart;
import org.argeo.cms.swt.widgets.SwtTabularPart;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ux.widgets.Column;
import org.argeo.cms.ux.widgets.HierarchicalPart;
import org.argeo.cms.ux.widgets.TabularPart;
import org.argeo.osgi.useradmin.UserDirectory;
import org.argeo.util.LangUtils;
import org.argeo.util.directory.HierarchyUnit;
import org.argeo.util.naming.LdapAttrs;
import org.argeo.util.naming.LdapObjs;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/** Entry to the admin area. */
public class UsersEntryArea implements SwtUiProvider, CmsUiProvider {

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

		// MODEL
		List<UserDirectory> directories = new ArrayList<>(cmsUserManager.getUserDirectories());
		// List<User> orgs = cmsUserManager.listGroups(null, true, false);

		// VIEW
		HierarchicalPart directoriesView = new SwtHierarchicalPart(sashForm, SWT.NONE) {

			@Override
			protected void refreshRootItem(TreeItem item) {
				int index = getTree().indexOf(item);
				UserDirectory directory = (UserDirectory) directories.get(index);
				List<HierarchyUnit> visible = new ArrayList<>();
//				item.setData(directory);
				item.setText(directory.getName());
//				if (CmsRole.userAdmin.implied(CurrentUser.getCmsSession().getSubject(), directory.getGlobalId())) {
//					visible.addAll(directory.getRootHierarchyUnits(true));
//					
//				} else {
				for (HierarchyUnit hu : directory.getDirectHierarchyUnits(true)) {
					if (CurrentUser.implies(CmsRole.userAdmin, hu.getContext())) {
						visible.add(hu);
					}
				}
//				}
				item.setData(visible);
				item.setItemCount(visible.size());
			}

			@Override
			protected void refreshItem(TreeItem parentItem, TreeItem item) {
				int index = getTree().indexOf(item);
				Iterable<HierarchyUnit> children;
				if (parentItem.getData() instanceof Iterable)
					children = (Iterable<HierarchyUnit>) parentItem.getData();
				else
					children = ((HierarchyUnit) parentItem.getData()).getDirectHierachyUnits(true);
				HierarchyUnit child = LangUtils.getAt(children, index);
				item.setData(child);
				item.setText(child.getHierarchyUnitName());
				item.setItemCount(LangUtils.size(child.getDirectHierachyUnits(true)));
			}

			@Override
			protected int getRootItemCount() {
				return directories.size();
			}

		};

		TabularPart usersView = new SwtTabularPart(sashForm, SWT.NONE) {
			List<Content> roles = new ArrayList<>();

			@Override
			protected Object getData(int row) {
				return roles.get(row);
			}
//			@Override
//			protected void refreshItem(TableItem item) {
//				int index = getTable().indexOf(item);
//				User role = (User) roles.get(index);
//				item.setData(role);
//				item.setText(role.getName());
//				Image icon;
//				if (role instanceof Organization) {
//					icon = theme.getSmallIcon(SuiteIcon.organisation);
//				} else if (role instanceof FunctionalGroup) {
//					icon = theme.getSmallIcon(SuiteIcon.group);
//				} else if (role instanceof Person) {
//					icon = theme.getSmallIcon(SuiteIcon.person);
//				} else {
//					icon = null;
//				}
//				item.setImage(icon);
//			}

			@Override
			protected int getItemCount() {
				roles.clear();
				HierarchyUnit hu = (HierarchyUnit) getInput();
				if (hu == null)
					return 0;
				UserDirectory ud = (UserDirectory) hu.getDirectory();
				for (HierarchyUnit directChild : hu.getDirectHierachyUnits(false)) {
					if (!directChild.isFunctional()) {
						for (Role r : ud.getHierarchyUnitRoles(directChild, null, false)) {
							Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
							// if (r instanceof Person || r instanceof Organization)
							if (content.hasContentClass(LdapObjs.inetOrgPerson.qName(), LdapObjs.organization.qName()))
								roles.add(content);
						}
					}
				}
				// roles = hu.getHierarchyUnitRoles(null, false);
				return roles.size();
			}

		};
		usersView.addColumn(new Column<Content>() {

			@Override
			public String getText(Content role) {
				if (role.isContentClass(LdapObjs.inetOrgPerson.qName()))
					return UserAdminUtils.getUserDisplayName(role.adapt(User.class));
				else if (role.isContentClass(LdapObjs.organization.qName()))
					return role.attr(LdapAttrs.o.qName());
				else if (role.isContentClass(LdapObjs.groupOfNames.qName()))
					return role.attr(LdapAttrs.cn.qName());
				else
					return null;
			}

			@Override
			public CmsIcon getIcon(Content role) {
				if (role.isContentClass(LdapObjs.inetOrgPerson.qName()))
					return SuiteIcon.person;
				else if (role.isContentClass(LdapObjs.organization.qName()))
					return SuiteIcon.organisation;
				else if (role.isContentClass(LdapObjs.groupOfNames.qName()))
					return SuiteIcon.group;
				else
					return null;
			}

			@Override
			public int getWidth() {
				return 300;
			}

		});
		usersView.addColumn((Column<Content>) (role) -> role.attr(LdapAttrs.mail.qName()));

		// toolbar
		Composite bottom = new Composite(parent, SWT.NONE);
		bottom.setLayoutData(CmsSwtUtils.fillWidth());
		bottom.setLayout(CmsSwtUtils.noSpaceGridLayout());
		ToolBar bottomToolBar = new ToolBar(bottom, SWT.NONE);
		bottomToolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
		ToolItem deleteItem = new ToolItem(bottomToolBar, SWT.FLAT);
		deleteItem.setEnabled(false);
//		CmsUiUtils.style(deleteItem, SuiteStyle.recentItems);
		deleteItem.setImage(theme.getSmallIcon(SuiteIcon.delete));
		ToolItem addItem = new ToolItem(bottomToolBar, SWT.FLAT);
		addItem.setImage(theme.getSmallIcon(SuiteIcon.add));

		sashForm.setWeights(new int[] { 30, 70 });

		// CONTROLLER
		directoriesView.onSelected((o) -> {
			if (o instanceof HierarchyUnit) {
				usersView.setInput((HierarchyUnit) o);
			}
		});

		usersView.onSelected((o) -> {
			Content user = (Content) o;
			if (user != null) {
				cmsView.sendEvent(SuiteEvent.refreshPart.topic(), SuiteEvent.eventProperties(user));
				deleteItem.setEnabled(true);
			} else {
				deleteItem.setEnabled(false);
			}
		});

		usersView.onAction((o) -> {
			Content user = (Content) o;
			if (user != null) {
				cmsView.sendEvent(SuiteEvent.openNewPart.topic(), SuiteEvent.eventProperties(user));
			}
		});

		addItem.addSelectionListener((Selected) (e) -> {
			// SuiteUtils.getOrCreateUserNode(adminSession, userDn);
			Wizard wizard = new NewUserWizard(null);
			CmsWizardDialog dialog = new CmsWizardDialog(parent.getShell(), wizard);
			// WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() == Window.OK) {
				// TODO create
			}
		});

		directoriesView.refresh();
		usersView.refresh();

		return sashForm;
	}

//	static String getProperty(Role role, LdapAttrs attr) {
//		Object value = role.getProperties().get(attr.name());
//		return value != null ? value.toString() : null;
//	}

//	private boolean isOrganisation(Role role) {
//		String[] objectClasses = role.getProperties().get(LdapAttrs.objectClasses.name()).toString().split("\\n");
//		for (String objectClass : objectClasses) {
//			if (LdapObjs.organization.name().equalsIgnoreCase(objectClass))
//				return true;
//		}
//		return false;
//	}

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
