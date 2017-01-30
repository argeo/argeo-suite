package org.argeo.suite.workbench;

import org.argeo.connect.people.workbench.rap.views.MyTasksView;
import org.argeo.connect.people.workbench.rap.views.QuickSearchView;
import org.argeo.suite.workbench.parts.MyFilesView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** Default Argeo Suite Dashboard perspective */
public class DashboardPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea);
		// Only show contacts if current user is a coworker
		// if (CurrentUser.isInRole(Role.coworker.dn()))
		left.addView(MyTasksView.ID);
		left.addView(QuickSearchView.ID);
		left.addView(MyFilesView.ID);
	}
}
