package org.argeo.suite.ui;

import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUserManager;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.argeo.naming.LdapAttrs;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.useradmin.User;

/** Entry to the admin area. */
public class AdminEntryArea implements CmsUiProvider {

	private CmsUserManager cmsUserManager;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsTheme theme = CmsTheme.getCmsTheme(parent);
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
		givenNameCol.getColumn().setWidth(70);
		givenNameCol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				return getUserProperty(element, LdapAttrs.givenName.name());
			}
		});

		TableViewerColumn snCol = new TableViewerColumn(usersViewer, SWT.NONE);
		snCol.getColumn().setWidth(70);
		snCol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				return getUserProperty(element, LdapAttrs.sn.name());
			}
		});

		TableViewerColumn mailCol = new TableViewerColumn(usersViewer, SWT.NONE);
		mailCol.getColumn().setWidth(200);
		mailCol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				return getUserProperty(element, LdapAttrs.mail.name());
			}
		});

		Composite bottom = new Composite(parent, SWT.NONE);
		bottom.setLayoutData(CmsUiUtils.fillWidth());
		bottom.setLayout(CmsUiUtils.noSpaceGridLayout());
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
					Node userNode = getOrCreateUserNode(user, context);
					CmsView.getCmsView(parent).sendEvent(SuiteEvent.openNewPart.topic(),
							SuiteEvent.eventProperties(userNode));
				}

			}
		});
		usersViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				User user = (User) usersViewer.getStructuredSelection().getFirstElement();
				if (user != null) {
					Node userNode = getOrCreateUserNode(user, context);
					CmsView.getCmsView(parent).sendEvent(SuiteEvent.refreshPart.topic(),
							SuiteEvent.eventProperties(userNode));
					deleteItem.setEnabled(true);
				} else {
					deleteItem.setEnabled(false);
				}
			}
		});

		usersViewer.getTable().setLayoutData(CmsUiUtils.fillAll());
		usersViewer.setInput(cmsUserManager);

		return usersViewer.getTable();
	}

	private Node getOrCreateUserNode(User user, Node context) {
		return JcrUtils.mkdirs(Jcr.getSession(context),
				"/" + EntityType.user.name() + "/" + getUserProperty(user, LdapAttrs.uid.name()),
				EntityType.user.get());
	}

	private String getUserProperty(Object element, String key) {
		Object value = ((User) element).getProperties().get(key);
		return value != null ? value.toString() : null;
	}

	class UsersContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			CmsUserManager cum = (CmsUserManager) inputElement;
			String baseGroup = "cn=apaf-coworker,cn=groups,cn=accounts,dc=id,dc=argeo,dc=pro";
			Set<User> users = cum.listUsersInGroup(baseGroup, null);
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
