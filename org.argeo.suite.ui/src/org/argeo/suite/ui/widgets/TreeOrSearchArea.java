package org.argeo.suite.ui.widgets;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Displays a tree by default, which becomes a list if the search text field is
 * used.
 */
public class TreeOrSearchArea extends Composite {
	private static final long serialVersionUID = -1302546480076719532L;

	private Text searchT;
	private StackLayout bodyLayout;

	private TreeViewer treeViewer;
	private TreeViewer searchResultsViewer;

	public TreeOrSearchArea(Composite parent, int style) {
		super(parent, style);
		createUi(this);
	}

	protected void createUi(Composite parent) {
		parent.setLayout(new GridLayout());
		Composite searchC = new Composite(parent, SWT.NONE);
		searchC.setLayout(new GridLayout());
		searchC.setLayoutData(CmsUiUtils.fillWidth());
		createSearchUi(searchC);

		Composite bodyC = new Composite(parent, SWT.NONE);
		bodyC.setLayoutData(CmsUiUtils.fillAll());
		bodyLayout = new StackLayout();
		bodyC.setLayout(bodyLayout);
		Composite treeC = new Composite(bodyC, SWT.NONE);
		createTreeUi(treeC);
		Composite searchResultsC = new Composite(bodyC, SWT.NONE);
		createSearchResultsUi(searchResultsC);

		bodyLayout.topControl = treeC;
	}

	protected void createSearchUi(Composite parent) {
		parent.setLayout(CmsUiUtils.noSpaceGridLayout());
		searchT = new Text(parent, SWT.MULTI | SWT.BORDER);
		searchT.setLayoutData(CmsUiUtils.fillWidth());
	}

	protected void createTreeUi(Composite parent) {
		parent.setLayout(CmsUiUtils.noSpaceGridLayout());
		treeViewer = new TreeViewer(parent);
		treeViewer.getTree().setLayoutData(CmsUiUtils.fillAll());
	}

	protected void createSearchResultsUi(Composite parent) {
		parent.setLayout(CmsUiUtils.noSpaceGridLayout());
		searchResultsViewer = new TreeViewer(parent);
		searchResultsViewer.getTree().setLayoutData(CmsUiUtils.fillAll());
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public TreeViewer getSearchResultsViewer() {
		return searchResultsViewer;
	}

}
