package org.argeo.app.ui.people;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ldap.LdapAcrUtils;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.cms.directory.CmsUser;
import org.argeo.api.cms.ux.CmsIcon;
import org.argeo.app.ux.SuiteIcon;
import org.argeo.cms.CurrentUser;
import org.argeo.cms.ux.widgets.Column;

public class UserColumn implements Column<Content> {
	@Override
	public String getText(Content role) {
		if (role.hasContentClass(LdapObj.inetOrgPerson))
			return role.adapt(CmsUser.class).getDisplayName();
		else if (role.hasContentClass(LdapObj.organization))
			return role.attr(LdapAttr.o);
		else if (role.hasContentClass(LdapObj.groupOfNames)) {
			// TODO make it more generic at ACR level
			Object label = LdapAcrUtils.getLocalized(role, LdapAttr.cn.qName(), CurrentUser.locale());
			return label.toString();
		} else
			return null;
	}

	@Override
	public CmsIcon getIcon(Content role) {
		if (role.hasContentClass(LdapObj.posixAccount))
			return SuiteIcon.user;
		else if (role.hasContentClass(LdapObj.inetOrgPerson))
			return SuiteIcon.person;
		else if (role.hasContentClass(LdapObj.organization))
			return SuiteIcon.organisationContact;
		else if (role.hasContentClass(LdapObj.groupOfNames))
			return SuiteIcon.group;
		else
			return null;
	}

}
