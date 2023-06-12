package org.argeo.app.ui.people;

import java.util.ArrayList;
import java.util.List;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.cms.directory.CmsDirectory;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.api.cms.directory.UserDirectory;
import org.argeo.api.cms.ux.CmsIcon;
import org.argeo.app.ux.SuiteIcon;
import org.argeo.cms.CurrentUser;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.auth.CmsRole;
import org.argeo.cms.ux.widgets.AbstractHierarchicalPart;
import org.argeo.cms.ux.widgets.Column;

public class HierarchyUnitPart extends AbstractHierarchicalPart<HierarchyUnit> {
	private ContentSession contentSession;
	private CmsUserManager cmsUserManager;

	public HierarchyUnitPart(ContentSession contentSession, CmsUserManager cmsUserManager) {
		this.contentSession = contentSession;
		this.cmsUserManager = cmsUserManager;

		addColumn(new Column<HierarchyUnit>() {

			@Override
			public String getText(HierarchyUnit model) {
				return model.getHierarchyUnitLabel(CurrentUser.locale());
			}

			@Override
			public CmsIcon getIcon(HierarchyUnit model) {
				Content content = ContentUtils.hierarchyUnitToContent(contentSession, model);
				if (content.hasContentClass(LdapObj.organization))
					return SuiteIcon.organisation;
				else if (content.hasContentClass(LdapObj.posixGroup))
					return SuiteIcon.users;
				else
					return SuiteIcon.addressBook;
			}
		});
	}

	@Override
	public List<HierarchyUnit> getChildren(HierarchyUnit parent) {
		List<HierarchyUnit> visible = new ArrayList<>();
		if (parent != null) {
			if (parent instanceof CmsDirectory) // do no show children of the directories
				return visible;
			for (HierarchyUnit hu : parent.getDirectHierarchyUnits(true)) {
				visible.add(hu);
			}
		} else {
			for (UserDirectory directory : cmsUserManager.getUserDirectories()) {
				if (CurrentUser.implies(CmsRole.userAdmin, directory.getBase())) {
					visible.add(directory);
				}
				for (HierarchyUnit hu : directory.getDirectHierarchyUnits(true)) {
					if (CurrentUser.implies(CmsRole.userAdmin, hu.getBase())) {
						visible.add(hu);
					}
				}

			}
		}
		return visible;
	}

}
