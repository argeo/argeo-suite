package org.argeo.app.ui.people;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.cms.directory.CmsGroup;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.app.ui.SuiteMsg;
import org.argeo.app.ui.SuiteUiUtils;
import org.argeo.cms.CurrentUser;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.useradmin.Role;

public class GroupUiProvider implements SwtUiProvider {
	private CmsUserManager cmsUserManager;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsGroup group = context.adapt(CmsGroup.class);
		Content hierarchyUnitContent = context.getParent().getParent();
		HierarchyUnit hierarchyUnit = hierarchyUnitContent.adapt(HierarchyUnit.class);

		// TODO localise at content level
		String title;
		if (context.hasContentClass(LdapObj.organization))
			title = SuiteMsg.org.lead() + " " + context.attr(LdapAttr.cn) + " ("
					+ hierarchyUnit.getHierarchyUnitLabel(CurrentUser.locale()) + ")";
		else
			title = SuiteMsg.group.lead() + " " + context.attr(LdapAttr.cn) + " ("
					+ hierarchyUnit.getHierarchyUnitLabel(CurrentUser.locale()) + ")";
		SuiteUiUtils.addFormLabel(parent, title);

		for (Role member : group.getMembers()) {
			new Label(parent, 0).setText(member.getName());
		}
		return null;

	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

}
