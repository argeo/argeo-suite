package org.argeo.app.ui.people;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsTheme;
import org.argeo.api.cms.CmsView;
import org.argeo.app.ui.SuiteEvent;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.app.ui.dialogs.NewUserWizard;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.dialogs.CmsWizardDialog;
import org.argeo.cms.swt.widgets.SwtHierarchicalPart;
import org.argeo.cms.swt.widgets.SwtTabularPart;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ux.widgets.HierarchicalPart;
import org.argeo.cms.ux.widgets.TabularPart;
import org.argeo.osgi.useradmin.HierarchyUnit;
import org.argeo.osgi.useradmin.UserDirectory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/** Entry to the admin area. */
public class UsersEntryArea implements SwtUiProvider, CmsUiProvider {

	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsTheme theme = CmsSwtUtils.getCmsTheme(parent);
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		parent.setLayout(new GridLayout());

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
				item.setData(directory);
				item.setText(directory.getBasePath());

				item.setItemCount(directory.getHierarchyChildCount());
			}

			@Override
			protected void refreshItem(TreeItem parentItem, TreeItem item) {
				int index = getTree().indexOf(item);
				HierarchyUnit parent = (HierarchyUnit) parentItem.getData();
				HierarchyUnit child = parent.getHierarchyChild(index);
				item.setData(child);
				item.setText(child.getHierarchyUnitName());
				item.setItemCount(child.getHierarchyChildCount());
			}

			@Override
			protected int getRootItemCount() {
				return directories.size();
			}

		};

		TabularPart usersView = new SwtTabularPart(sashForm, SWT.NONE) {
			List<? extends Role> roles;

			@Override
			protected void refreshItem(TableItem item) {
				int index = getTable().indexOf(item);
				User role = (User) roles.get(index);
				item.setData(role);
				item.setText(role.getName());
			}

			@Override
			protected int getItemCount() {
				HierarchyUnit hu = (HierarchyUnit) getInput();
				if (hu == null)
					return 0;
				roles = hu.getRoles(null, false);
				return roles.size();
			}

		};

		Composite bottom = new Composite(parent, SWT.NONE);
		bottom.setLayoutData(CmsSwtUtils.fillWidth());
		bottom.setLayout(CmsSwtUtils.noSpaceGridLayout());
		ToolBar bottomToolBar = new ToolBar(bottom, SWT.NONE);
		bottomToolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
		ToolItem deleteItem = new ToolItem(bottomToolBar, SWT.FLAT);
		deleteItem.setEnabled(false);
//		CmsUiUtils.style(deleteItem, SuiteStyle.recentItems);
		deleteItem.setImage(SuiteIcon.delete.getSmallIcon(theme));
		ToolItem addItem = new ToolItem(bottomToolBar, SWT.FLAT);
		addItem.setImage(SuiteIcon.add.getSmallIcon(theme));

		// CONTROLLER
		directoriesView.onSelected((o) -> {
			HierarchyUnit hu = (HierarchyUnit) o;
			usersView.setInput(hu);
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

//	private String getUserProperty(Object element, String key) {
//		Object value = ((User) element).getProperties().get(key);
//		return value != null ? value.toString() : null;
//	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		return createUiPart(parent, JcrContent.nodeToContent(context));
	}

}
