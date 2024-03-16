package org.argeo.app.ui.people;

import java.util.ArrayList;
import java.util.List;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.cms.directory.CmsRole;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.api.cms.directory.UserDirectory;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.ux.widgets.DefaultTabularPart;

public class UsersPart extends DefaultTabularPart<HierarchyUnit, Content> {
	private ContentSession contentSession;
	private CmsUserManager cmsUserManager;

	public UsersPart(ContentSession contentSession, CmsUserManager cmsUserManager) {
		this.contentSession = contentSession;
		this.cmsUserManager = cmsUserManager;
		addColumn(new UserColumn() {

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
			for (CmsRole r : ud.getHierarchyUnitRoles(ud, null, true)) {
				Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
				if (content.hasContentClass(LdapObj.inetOrgPerson, LdapObj.organization))
					roles.add(content);
			}

		} else {
			for (HierarchyUnit directChild : hu.getDirectHierarchyUnits(false)) {
				if (!(directChild.isType(HierarchyUnit.Type.FUNCTIONAL)
						|| directChild.isType(HierarchyUnit.Type.ROLES))) {
					for (CmsRole r : ud.getHierarchyUnitRoles(directChild, null, false)) {
						Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
						if (content.hasContentClass(LdapObj.inetOrgPerson, LdapObj.organization, LdapObj.groupOfNames))
							roles.add(content);
					}
				}
			}
		}
		return roles;
	}

}
