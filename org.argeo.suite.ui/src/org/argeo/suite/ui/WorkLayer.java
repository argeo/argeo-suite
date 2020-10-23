package org.argeo.suite.ui;

import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.widgets.TabbedArea;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/** An app layer based on an entry area and an editor area. */
public class WorkLayer {
	private CmsTheme theme;
	private SashForm area;
	private Composite entryArea;
	private Composite editorArea;
	private TabbedArea tabbedArea;

	WorkLayer(Composite parent, int style) {
		theme = CmsTheme.getCmsTheme(parent);
		area = new SashForm(parent, SWT.HORIZONTAL);
		area.setLayoutData(CmsUiUtils.coversAll());

		if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
			editorArea = new Composite(area, SWT.BORDER);
			entryArea = new Composite(area, SWT.BORDER);
		} else {
			entryArea = new Composite(area, SWT.NONE);
			editorArea = new Composite(area, SWT.NONE);
		}
		int[] weights = new int[] { 2000, 8000 };
		area.setWeights(weights);
//			editorArea.setLayout(CmsUiUtils.noSpaceGridLayout());
		editorArea.setLayout(new GridLayout());

		tabbedArea = new TabbedArea(editorArea, SWT.NONE);
		tabbedArea.setBodyStyle(SuiteStyle.mainTabBody.toStyleClass());
		tabbedArea.setTabStyle(SuiteStyle.mainTab.toStyleClass());
		tabbedArea.setTabSelectedStyle(SuiteStyle.mainTabSelected.toStyleClass());
		tabbedArea.setCloseIcon(SuiteIcon.close.getSmallIcon(theme));
		tabbedArea.setLayoutData(CmsUiUtils.fillAll());
	}

	Composite getArea() {
		return area;
	}

	Composite getEntryArea() {
		return entryArea;
	}

	TabbedArea getTabbedArea() {
		return tabbedArea;
	}
}