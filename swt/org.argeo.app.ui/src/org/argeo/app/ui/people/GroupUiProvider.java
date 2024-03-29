package org.argeo.app.ui.people;

import java.util.ArrayList;
import java.util.List;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.ldap.LdapAcrUtils;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.acr.spi.ProvidedContent;
import org.argeo.api.cms.directory.CmsGroup;
import org.argeo.api.cms.directory.CmsRole;
import org.argeo.api.cms.directory.CmsUser;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.app.swt.ux.SuiteSwtUtils;
import org.argeo.app.ux.SuiteIcon;
import org.argeo.app.ux.SuiteMsg;
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

public class GroupUiProvider implements SwtUiProvider {
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);

		Content hierarchyUnitContent = context.getParent().getParent();
		HierarchyUnit hierarchyUnit = hierarchyUnitContent.adapt(HierarchyUnit.class);

		ContentSession contentSession = ((ProvidedContent) context).getSession();

		TabularPart<Content, Content> membersPart = new AbstractTabularPart<Content, Content>() {
			List<CmsRole> roles;

			@Override
			public int getItemCount() {
				roles = new ArrayList<CmsRole>(context.adapt(CmsGroup.class).getDirectMembers());
				return roles.size();
			}

			@Override
			public Content getData(int row) {
				CmsRole role = roles.get(row);
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
		String title = (context.hasContentClass(LdapObj.organization) ? SuiteMsg.org.lead() : SuiteMsg.group.lead())
				+ " " + LdapAcrUtils.getLocalized(context, LdapAttr.cn.qName(), CurrentUser.locale()) + " ("
				+ hierarchyUnit.getHierarchyUnitLabel(CurrentUser.locale()) + ")";
		SuiteSwtUtils.addFormLabel(area, title);

		// toolbar
		ToolBar toolBar = new ToolBar(area, SWT.NONE);
		toolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

		ToolItem deleteItem = new ToolItem(toolBar, SWT.FLAT);
		deleteItem.setEnabled(false);
		deleteItem.setImage(theme.getSmallIcon(SuiteIcon.delete));

		ToolItem addItem = new ToolItem(toolBar, SWT.FLAT);
		addItem.setImage(theme.getSmallIcon(SuiteIcon.add));
		addItem.setEnabled(CurrentUser.implies(org.argeo.cms.auth.CmsSystemRole.groupAdmin, hierarchyUnit.getBase()));

		// members view
		SwtTableView<Content, Content> membersView = new SwtTableView<>(area, SWT.BORDER, membersPart);
		membersView.setLayoutData(CmsSwtUtils.fillAll());
		membersView.refresh();

		// CONTROLLER
		membersPart.onSelected((model) -> {
			deleteItem.setEnabled(CurrentUser.implies(org.argeo.cms.auth.CmsSystemRole.groupAdmin, hierarchyUnit.getBase()));
			deleteItem.setData(model);
		});

		addItem.addSelectionListener((Selected) (e) -> {
			ChooseUserDialog chooseUserDialog = new ChooseUserDialog(parent.getDisplay().getActiveShell(),
					SuiteMsg.chooseAMember.lead(), contentSession, cmsUserManager, hierarchyUnit);
			if (chooseUserDialog.open() == CmsDialog.OK) {
				Content chosen = chooseUserDialog.getSelected();
				cmsUserManager.addMember(context.adapt(CmsGroup.class), chosen.adapt(CmsUser.class));
				membersPart.refresh();
			}
		});

		deleteItem.addSelectionListener((Selected) (e) -> {
			if (deleteItem.getData() != null) {
				Content chosen = (Content) deleteItem.getData();
				cmsUserManager.removeMember(context.adapt(CmsGroup.class), chosen.adapt(CmsUser.class));
				membersPart.refresh();
			}
		});

		return membersView;

	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

}
