package org.argeo.app.ui.people;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.acr.spi.ProvidedContent;
import org.argeo.api.cms.directory.CmsGroup;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.app.ui.SuiteMsg;
import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.cms.CurrentUser;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.acr.SwtSection;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.SwtTableView;
import org.argeo.cms.ux.widgets.AbstractTabularPart;
import org.argeo.cms.ux.widgets.CmsDialog;
import org.argeo.cms.ux.widgets.TabularPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.useradmin.Role;

public class GroupUiProvider implements SwtUiProvider {
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);

		Content hierarchyUnitContent = context.getParent().getParent();
		HierarchyUnit hierarchyUnit = hierarchyUnitContent.adapt(HierarchyUnit.class);

		ContentSession contentSession = ((ProvidedContent) context).getSession();

		TabularPart<Content, Content> membersPart = new AbstractTabularPart<Content, Content>() {
			Role[] roles = context.adapt(CmsGroup.class).getMembers();

			@Override
			public int getItemCount() {
				return roles.length;
			}

			@Override
			public Content getData(int row) {
				Role role = roles[row];
				Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, role);
				return content;
			}

		};
		membersPart.addColumn(new UserColumn());

		// VIEW
		SwtSection area = new SwtSection(parent, 0, context);
		area.setLayoutData(CmsSwtUtils.fillAll());
		area.setLayout(new GridLayout());

		// title
		// TODO localise at content level
		String title;
		if (context.hasContentClass(LdapObj.organization))
			title = SuiteMsg.org.lead() + " " + context.attr(LdapAttr.cn) + " ("
					+ hierarchyUnit.getHierarchyUnitLabel(CurrentUser.locale()) + ")";
		else
			title = SuiteMsg.group.lead() + " " + context.attr(LdapAttr.cn) + " ("
					+ hierarchyUnit.getHierarchyUnitLabel(CurrentUser.locale()) + ")";
		SuiteUiUtils.addFormLabel(area, title);

		// toolbar
		ToolBar toolBar = new ToolBar(area, SWT.NONE);
		toolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

		ToolItem deleteItem = new ToolItem(toolBar, SWT.FLAT);
		deleteItem.setEnabled(false);
		deleteItem.setImage(theme.getSmallIcon(SuiteIcon.delete));

		ToolItem addItem = new ToolItem(toolBar, SWT.FLAT);
		addItem.setImage(theme.getSmallIcon(SuiteIcon.add));
		addItem.addSelectionListener((Selected) (e) -> {
			ChooseUserDialog chooseUserDialog = new ChooseUserDialog(parent.getDisplay().getActiveShell(),
					SuiteMsg.chooseAMember.lead(), contentSession, cmsUserManager, hierarchyUnit);
			if (chooseUserDialog.open() == CmsDialog.OK) {
				Content chosen = chooseUserDialog.getSelected();
				// TODO add
			}
		});

		// members view
		SwtTableView<Content, Content> membersView = new SwtTableView<>(area, SWT.BORDER, membersPart);
		membersView.setLayoutData(CmsSwtUtils.fillAll());
		membersView.refresh();
		return membersView;

	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

}
