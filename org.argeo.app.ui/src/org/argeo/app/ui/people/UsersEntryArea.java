package org.argeo.app.ui.people;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsTheme;
import org.argeo.app.ui.SuiteEvent;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.app.ui.dialogs.NewUserWizard;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.dialogs.CmsWizardDialog;
import org.argeo.cms.ui.CmsUiProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.User;

/** Entry to the admin area. */
public class UsersEntryArea implements SwtUiProvider, CmsUiProvider {

	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsTheme theme = CmsSwtUtils.getCmsTheme(parent);
		parent.setLayout(new GridLayout());

		List<User> orgs = cmsUserManager.listGroups(null, true, false);

		final Tree tree = new Tree(parent, SWT.VIRTUAL | SWT.BORDER);
		tree.addListener(SWT.SetData, event -> {
			TreeItem item = (TreeItem) event.item;
			TreeItem parentItem = item.getParentItem();
			String text = null;
			if (parentItem == null) {
				int index = tree.indexOf(item);
				User org = (User) orgs.get(index);
				item.setData(org);
				text = org.getName();
			} else {
				text = parentItem.getText() + " - " + parentItem.indexOf(item);
			}
			item.setText(text);
			item.setItemCount(10);
		});
		tree.setItemCount(orgs.size());
		tree.setLayoutData(CmsSwtUtils.fillAll());

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

//		tree.addSelectionListener((Selected) (e) -> {
//			User user = (User) e.item.getData();
//			if (user != null) {
//				CmsSwtUtils.getCmsView(parent).sendEvent(SuiteEvent.refreshPart.topic(),
//						SuiteEvent.eventProperties(user));
//				deleteItem.setEnabled(true);
//			} else {
//				deleteItem.setEnabled(false);
//			}
//		});
//		tree.addListener(SWT.MouseDoubleClick, (e) -> {
//			User user = (User) e.item.getData();
//			if (user != null) {
//				CmsSwtUtils.getCmsView(parent).sendEvent(SuiteEvent.openNewPart.topic(),
//						SuiteEvent.eventProperties(user));
//			}
//
//		});
		
		tree.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				User user = (User) e.item.getData();
				if (user != null) {
					CmsSwtUtils.getCmsView(parent).sendEvent(SuiteEvent.refreshPart.topic(),
							SuiteEvent.eventProperties(user));
					deleteItem.setEnabled(true);
				} else {
					deleteItem.setEnabled(false);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				User user = (User) e.item.getData();
				if (user != null) {
					CmsSwtUtils.getCmsView(parent).sendEvent(SuiteEvent.openNewPart.topic(),
							SuiteEvent.eventProperties(user));
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

		return tree;
	}

	private String getUserProperty(Object element, String key) {
		Object value = ((User) element).getProperties().get(key);
		return value != null ? value.toString() : null;
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		return createUiPart(parent, JcrContent.nodeToContent(context));
	}

}
