package org.argeo.app.ui.people;

import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.cms.CmsTheme;
import org.argeo.app.api.SuiteRole;
import org.argeo.app.ui.SuiteEvent;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.app.ui.dialogs.NewUserWizard;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.dialogs.CmsWizardDialog;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.util.naming.LdapAttrs;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.useradmin.User;

/** Entry to the admin area. */
public class SuiteUsersEntryArea implements CmsUiProvider {

	private CmsUserManager cmsUserManager;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsTheme theme = CmsSwtUtils.getCmsTheme(parent);
		parent.setLayout(new GridLayout());
		TableViewer usersViewer = new TableViewer(parent);
		usersViewer.setContentProvider(new UsersContentProvider());

		TableViewerColumn idCol = new TableViewerColumn(usersViewer, SWT.NONE);
		idCol.getColumn().setWidth(70);
		idCol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				return getUserProperty(element, LdapAttrs.uid.name());
			}
		});

		TableViewerColumn givenNameCol = new TableViewerColumn(usersViewer, SWT.NONE);
		givenNameCol.getColumn().setWidth(150);
		givenNameCol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				return getUserProperty(element, LdapAttrs.givenName.name());
			}
		});

		TableViewerColumn snCol = new TableViewerColumn(usersViewer, SWT.NONE);
		snCol.getColumn().setWidth(150);
		snCol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				return getUserProperty(element, LdapAttrs.sn.name());
			}
		});

		TableViewerColumn mailCol = new TableViewerColumn(usersViewer, SWT.NONE);
		mailCol.getColumn().setWidth(400);
		mailCol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				return getUserProperty(element, LdapAttrs.mail.name());
			}
		});

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
		usersViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				User user = (User) usersViewer.getStructuredSelection().getFirstElement();
				if (user != null) {
//					Node userNode = getOrCreateUserNode(user, context);
					CmsSwtUtils.getCmsView(parent).sendEvent(SuiteEvent.openNewPart.topic(),
							SuiteEvent.eventProperties(user));
				}

			}
		});
		usersViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				User user = (User) usersViewer.getStructuredSelection().getFirstElement();
				if (user != null) {
//					Node userNode = getOrCreateUserNode(user, context);
					CmsSwtUtils.getCmsView(parent).sendEvent(SuiteEvent.refreshPart.topic(),
							SuiteEvent.eventProperties(user));
					deleteItem.setEnabled(true);
				} else {
					deleteItem.setEnabled(false);
				}
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

		usersViewer.getTable().setLayoutData(CmsSwtUtils.fillAll());
		usersViewer.setInput(cmsUserManager);

		return usersViewer.getTable();
	}

//	private Node getOrCreateUserNode(User user, Node context) {
//		return JcrUtils.mkdirs(Jcr.getSession(context),
//				"/" + EntityType.user.name() + "/" + getUserProperty(user, LdapAttrs.uid.name()),
//				EntityType.user.get());
//	}

	private String getUserProperty(Object element, String key) {
		Object value = ((User) element).getProperties().get(key);
		return value != null ? value.toString() : null;
	}

	class UsersContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			CmsUserManager cum = (CmsUserManager) inputElement;
			Set<User> users = cum.listUsersInGroup(SuiteRole.coworker.dn(), null);
			return users.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

}
