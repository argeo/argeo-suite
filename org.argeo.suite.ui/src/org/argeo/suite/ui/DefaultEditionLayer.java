package org.argeo.suite.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.widgets.TabbedArea;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** An app layer based on an entry area and an editor area. */
public class DefaultEditionLayer implements SuiteLayer {
	private CmsUiProvider entryArea;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		DefaultEditionArea workArea = new DefaultEditionArea(parent, parent.getStyle());
		if (entryArea != null) {
			entryArea.createUi(workArea.getEntryArea(), context);
		}
		return workArea;
	}

	@Override
	public void view(Composite workArea, Node context) {
		TabbedArea tabbedArea = ((DefaultEditionArea) workArea).getTabbedArea();
		CmsUiProvider uiProvider = null;
		tabbedArea.view(uiProvider, context);
	}

	@Override
	public void open(Composite workArea, Node context) {
		TabbedArea tabbedArea = ((DefaultEditionArea) workArea).getTabbedArea();
		CmsUiProvider uiProvider = null;
		tabbedArea.open(uiProvider, context);
	}

	public void setEntryArea(CmsUiProvider entryArea) {
		this.entryArea = entryArea;
	}

	class DefaultEditionArea extends SashForm {
		private static final long serialVersionUID = 2219125778722702618L;
		private CmsTheme theme;
//		private SashForm area;
		private Composite entryArea;
		private Composite editorArea;
		private TabbedArea tabbedArea;

		DefaultEditionArea(Composite parent, int style) {
			super(parent, SWT.HORIZONTAL);
			theme = CmsTheme.getCmsTheme(parent);
//			area = new SashForm(parent, SWT.HORIZONTAL);
//			area.setLayoutData(CmsUiUtils.coversAll());

			if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
				editorArea = new Composite(this, SWT.BORDER);
				entryArea = new Composite(this, SWT.BORDER);
			} else {
				entryArea = new Composite(this, SWT.NONE);
				editorArea = new Composite(this, SWT.NONE);
			}
			int[] weights = new int[] { 2000, 8000 };
			setWeights(weights);
//			editorArea.setLayout(CmsUiUtils.noSpaceGridLayout());
			editorArea.setLayout(new GridLayout());

			tabbedArea = new TabbedArea(editorArea, SWT.NONE);
			tabbedArea.setBodyStyle(SuiteStyle.mainTabBody.toStyleClass());
			tabbedArea.setTabStyle(SuiteStyle.mainTab.toStyleClass());
			tabbedArea.setTabSelectedStyle(SuiteStyle.mainTabSelected.toStyleClass());
			tabbedArea.setCloseIcon(SuiteIcon.close.getSmallIcon(theme));
			tabbedArea.setLayoutData(CmsUiUtils.fillAll());
		}

//		Composite getArea() {
//			return area;
//		}
//
		public Composite getEntryArea() {
			return entryArea;
		}

		public TabbedArea getTabbedArea() {
			return tabbedArea;
		}
	}
}