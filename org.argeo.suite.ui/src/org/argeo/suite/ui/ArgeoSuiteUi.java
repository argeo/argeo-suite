package org.argeo.suite.ui;

import static org.argeo.suite.ui.ArgeoSuiteIcon.dashboard;

import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ArgeoSuiteUi {
	private Composite parent;

	private Composite header;
	private Composite leadPane;

	private SashForm dynamicArea;
	private Composite entryArea;
	private Composite editorArea;
	private CTabFolder editorTabFolder;

	private Composite defaultBody;

	private CmsTheme theme;

	public ArgeoSuiteUi(Composite parent, int style) {
		theme = CmsTheme.getCmsTheme(parent);
		this.parent = parent;
		parent.setLayout(CmsUiUtils.noSpaceGridLayout());

		header = new Composite(parent, SWT.NONE);
		CmsUiUtils.style(header, WorkStyles.header);
		header.setLayoutData(CmsUiUtils.fillWidth());

		Composite belowHeader = new Composite(parent, SWT.NONE);
		belowHeader.setLayoutData(CmsUiUtils.fillAll());
		belowHeader.setLayout(CmsUiUtils.noSpaceGridLayout(2));

		if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
			dynamicArea = new SashForm(belowHeader, SWT.HORIZONTAL);
			leadPane = new Composite(belowHeader, SWT.NONE);
		} else {
			leadPane = new Composite(belowHeader, SWT.NONE);
			dynamicArea = new SashForm(belowHeader, SWT.HORIZONTAL);
		}
		leadPane.setLayoutData(CmsUiUtils.fillHeight());
		CmsUiUtils.style(leadPane, WorkStyles.leadPane);
		dynamicArea.setLayoutData(CmsUiUtils.fillAll());

		if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
			editorArea = new Composite(dynamicArea, SWT.BORDER);
			entryArea = new Composite(dynamicArea, SWT.BORDER);
		} else {
			entryArea = new Composite(dynamicArea, SWT.NONE);
			editorArea = new Composite(dynamicArea, SWT.NONE);
		}
		int[] weights = new int[] { 2000, 8000 };
		dynamicArea.setWeights(weights);
		editorArea.setLayout(new GridLayout());

		// TODO make it dynamic
		RecentItems recentItems = new RecentItems();
		recentItems.createUiPart(entryArea);

		editorTabFolder = new CTabFolder(editorArea, SWT.NONE);
		editorTabFolder.setLayoutData(CmsUiUtils.fillAll());
		Composite buttons = new Composite(editorTabFolder, SWT.NONE);
		RowLayout buttonsLayout = new RowLayout(SWT.HORIZONTAL);
		buttonsLayout.pack = false;
		buttons.setLayout(buttonsLayout);
		Button delete = new Button(buttons, SWT.FLAT);
		delete.setImage(ArgeoSuiteIcon.delete.getSmallIcon(theme));
		// int size = ArgeoSuiteIcon.delete.getSmallIconSize();
		// delete.setBounds(delete.getBounds().x,delete.getBounds().y,size,size);
		// delete.setSize(size, size);
		editorTabFolder.setTopRight(buttons);

		CTabItem defaultTab = new CTabItem(editorTabFolder, SWT.NONE);
		// defaultTab.setText("Home");
		defaultTab.setImage(dashboard.getSmallIcon(theme));
		defaultBody = new Composite(editorTabFolder, SWT.NONE);
		defaultTab.setControl(defaultBody);
		editorTabFolder.setSelection(defaultTab);

		// editorArea.setSingle(true);
	}

	Composite getParent() {
		return parent;
	}

	Composite getHeader() {
		return header;
	}

	Composite getLeadPane() {
		return leadPane;
	}

	SashForm getDynamicArea() {
		return dynamicArea;
	}

	Composite getEntryArea() {
		return entryArea;
	}

	CTabFolder getEditorTabFolder() {
		return editorTabFolder;
	}

	Composite getDefaultBody() {
		return defaultBody;
	}

}
