package org.argeo.app.ui.people;

import static org.argeo.cms.auth.UserAdminUtils.getProperty;

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
			List<Role> roles = new ArrayList<>();

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
							// if (r instanceof Person || r instanceof Organization)
							roles.add(r);
						}
					}
				}
				// roles = hu.getHierarchyUnitRoles(null, false);
				return roles.size();
			}

		};
		usersView.addColumn(new Column<Role>() {

			@Override
			public String getText(Role role) {
				String txt = getProperty(role, LdapAttrs.o);
				if (txt != null && !txt.equals(""))
					return txt;
				txt = getProperty(role, LdapAttrs.cn);
				if (txt != null && !txt.equals(""))
					return txt;
				txt = UserAdminUtils.getUserDisplayName(role);
				return txt;
			}

			@Override
			public CmsIcon getIcon(Role role) {
				String txt = getProperty(role, LdapAttrs.o);
				if (txt != null && !txt.equals(""))
					return SuiteIcon.organisation;
				txt = getProperty(role, LdapAttrs.uid);
				if (txt != null && !txt.equals(""))
					return SuiteIcon.person;
				return SuiteIcon.group;
			}

			@Override
			public int getWidth() {
				return 300;
			}

		});
		usersView.addColumn((Column<Role>) (role) -> getProperty(role, LdapAttrs.mail));

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
			User user = (User) o;
			if (user != null) {
				cmsView.sendEvent(SuiteEvent.refreshPart.topic(), SuiteEvent.eventProperties(user));
				deleteItem.setEnabled(true);
			} else {
				deleteItem.setEnabled(false);
			}
		});

		usersView.onAction((o) -> {
			User user = (User) o;
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
