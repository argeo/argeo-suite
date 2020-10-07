package org.argeo.suite.ui;

import static org.argeo.suite.ui.SuiteIcon.dashboard;

import javax.jcr.Session;

import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/** The {@link CmsView} for the work ergonomics of Argeo Suite. */
public class ArgeoSuiteUi extends Composite {
	private static final long serialVersionUID = 6207018859086689108L;

	private Composite header;
	private Composite belowHeader;
	private Composite leadPane;

	private SashForm dynamicArea;
	private Composite entryArea;
	private Composite editorArea;
	private CTabFolder editorTabFolder;

	private Composite defaultBody;

	private CmsTheme theme;
	
	private Session session;

	public ArgeoSuiteUi(Composite parent, int style) {
		super(parent, style);
		theme = CmsTheme.getCmsTheme(parent);
		this.setLayout(CmsUiUtils.noSpaceGridLayout());

		header = new Composite(this, SWT.NONE);
		CmsUiUtils.style(header, SuiteStyle.header);
		header.setLayoutData(CmsUiUtils.fillWidth());

		belowHeader = new Composite(this, SWT.NONE);
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
		CmsUiUtils.style(leadPane, SuiteStyle.leadPane);
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

		editorTabFolder = new CTabFolder(editorArea, SWT.NONE);
		editorTabFolder.setLayoutData(CmsUiUtils.fillAll());

		// TODO make it dynamic
		Composite buttons = new Composite(editorTabFolder, SWT.NONE);
		buttons.setLayout(CmsUiUtils.noSpaceGridLayout());
		ToolBar toolBar = new ToolBar(buttons, SWT.NONE);
		toolBar.setLayoutData(new GridData(SWT.END, SWT.TOP, false, false));
		ToolItem deleteItem = new ToolItem(toolBar, SWT.PUSH);
		deleteItem.setImage(SuiteIcon.delete.getSmallIcon(theme));
		deleteItem.setEnabled(false);
		editorTabFolder.setTopRight(buttons);

		CTabItem defaultTab = new CTabItem(editorTabFolder, SWT.NONE);
		// defaultTab.setText("Home");
		defaultTab.setImage(dashboard.getSmallIcon(theme));
		defaultBody = new Composite(editorTabFolder, SWT.NONE);
		defaultTab.setControl(defaultBody);
		editorTabFolder.setSelection(defaultTab);

		// editorArea.setSingle(true);
	}

	/*
	 * GETTERS / SETTERS
	 */

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

	Composite getBelowHeader() {
		return belowHeader;
	}

	Session getSession() {
		return session;
	}

	void setSession(Session session) {
		this.session = session;
	}

	
}
