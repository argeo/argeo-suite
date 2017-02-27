package org.argeo.suite.workbench;

import org.argeo.activities.workbench.parts.MyTodoListView;
import org.argeo.documents.workbench.parts.MyFilesView;
import org.argeo.people.workbench.rap.views.QuickSearchView;
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
		left.addView(MyFilesView.ID);
		left.addView(QuickSearchView.ID);
		left.addView(MyTodoListView.ID);
	}
}
