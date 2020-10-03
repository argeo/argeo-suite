package org.argeo.suite.ui;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ArgeoSuiteUi {
	private Composite parent;

	private Composite header;
	private Composite leadPane;

	private SashForm dynamicArea;
	private Composite entryArea;
	private CTabFolder editorArea;

	private Composite defaultBody;

	public ArgeoSuiteUi(Composite parent, int style) {
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
			editorArea = new CTabFolder(dynamicArea, SWT.NONE);
			entryArea = new Composite(dynamicArea, SWT.NONE);
		} else {
			entryArea = new Composite(dynamicArea, SWT.NONE);
			editorArea = new CTabFolder(dynamicArea, SWT.NONE);
		}
		int[] weights = new int[] { 2000, 8000 };
		dynamicArea.setWeights(weights);

		Composite buttons = new Composite(editorArea, SWT.NONE);
		buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
		Button delete = new Button(buttons, SWT.PUSH);
		delete.setText("Delete");
		editorArea.setTopRight(buttons);

		CTabItem defaultTab = new CTabItem(editorArea, SWT.NONE);
		defaultTab.setText("Home");
		defaultBody = new Composite(editorArea, SWT.NONE);
		defaultTab.setControl(defaultBody);

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

	CTabFolder getEditorArea() {
		return editorArea;
	}

	Composite getDefaultBody() {
		return defaultBody;
	}

}
