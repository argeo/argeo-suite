package org.argeo.suite.workbench;

import org.argeo.connect.people.workbench.rap.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.PeopleWorkbenchServiceImpl;
import org.argeo.suite.workbench.parts.DefaultDashboardEditor;

/** Centralize workbench services from the various base apps */
public class AsWorkbenchServiceImpl extends PeopleWorkbenchServiceImpl implements PeopleWorkbenchService {

	@Override
	public String getDefaultEditorId() {
		return DefaultDashboardEditor.ID;
	}
}

// extends PeopleWorkbenchServiceImpl {
//
// public String getDefaultEditorId() {
// return DefaultDashboardEditor.ID;
// }
//
// //
// // @Override
// // public Image getIconForType(Node entity) {
// // try {
// // if (entity.isNodeType(AoTypes.OFFICE_ACCOUNT))
// // return AoImages.ICON_ACCOUNT;
// // else if (entity.isNodeType(TrackerTypes.TRACKER_ISSUE))
// // return AoImages.ICON_ISSUE;
// // else if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT))
// // return AoImages.ICON_PROJECT;
// // else if (entity.isNodeType(AoTypes.OFFICE_PROSPECT))
// // return AoImages.ICON_PROSPECT;
// // else
// // return super.getIconForType(entity);
// // } catch (RepositoryException re) {
// // throw new PeopleException("Unable to get image for node", re);
// // }
// // }
// }
