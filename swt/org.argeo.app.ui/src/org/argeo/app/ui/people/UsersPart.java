package org.argeo.app.ui.people;

import java.util.ArrayList;
import java.util.List;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.api.cms.directory.UserDirectory;
import org.argeo.api.cms.ux.CmsIcon;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.auth.UserAdminUtils;
import org.argeo.cms.ux.widgets.Column;
import org.argeo.cms.ux.widgets.DefaultTabularPart;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

public class UsersPart extends DefaultTabularPart<HierarchyUnit, Content> {
	private ContentSession contentSession;
	private CmsUserManager cmsUserManager;

	public UsersPart(ContentSession contentSession, CmsUserManager cmsUserManager) {
		this.contentSession = contentSession;
		this.cmsUserManager = cmsUserManager;
		addColumn(new Column<Content>() {

			@Override
			public String getText(Content role) {
				if (role.hasContentClass(LdapObj.inetOrgPerson))
					return UserAdminUtils.getUserDisplayName(role.adapt(User.class));
				else if (role.hasContentClass(LdapObj.organization))
					return role.attr(LdapAttr.o);
				else if (role.hasContentClass(LdapObj.groupOfNames))
					return role.attr(LdapAttr.cn);
				else
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

			@Override
			public int getWidth() {
				return 300;
			}

		});
	}

	@Override
	protected List<Content> asList(HierarchyUnit hu) {
		List<Content> roles = new ArrayList<>();
		UserDirectory ud = (UserDirectory) hu.getDirectory();
		if (ud.getRealm().isPresent()) {
			for (Role r : ud.getHierarchyUnitRoles(ud, null, true)) {
				Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
				if (content.hasContentClass(LdapObj.inetOrgPerson, LdapObj.organization))
					roles.add(content);
			}

		} else {
			for (HierarchyUnit directChild : hu.getDirectHierarchyUnits(false)) {
				if (!(directChild.isType(HierarchyUnit.Type.FUNCTIONAL)
						|| directChild.isType(HierarchyUnit.Type.ROLES))) {
					for (Role r : ud.getHierarchyUnitRoles(directChild, null, false)) {
						Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
						if (content.hasContentClass(LdapObj.inetOrgPerson, LdapObj.organization,
								LdapObj.groupOfNames))
							roles.add(content);
					}
				}
			}
		}
		return roles;
	}

}
