package org.argeo.suite.workbench;

import org.argeo.connect.people.workbench.rap.views.MyTasksView;
import org.argeo.connect.people.workbench.rap.views.PeopleDefaultView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** Default office perspective */
public class DashboardPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea);
		left.addView(MyTasksView.ID);
		// Only show contacts to coworkers
		// if (CurrentUser.isInRole(AoRole.coworker.dn()))
		left.addView(PeopleDefaultView.ID);
	}
}
