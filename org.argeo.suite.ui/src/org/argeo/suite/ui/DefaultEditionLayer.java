package org.argeo.suite.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.widgets.TabbedArea;
import org.argeo.util.LangUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** An app layer based on an entry area and an editor area. */
public class DefaultEditionLayer implements SuiteLayer {
	private CmsUiProvider entryArea;
	private CmsUiProvider workArea;
	private List<String> weights = new ArrayList<>();
	private boolean startMaximized = false;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		if (entryArea != null) {
			SashFormEditionArea sashFormEditionArea = new SashFormEditionArea(parent, parent.getStyle());
			entryArea.createUi(sashFormEditionArea.getEntryArea(), context);
			if (this.workArea != null) {
				this.workArea.createUi(sashFormEditionArea.getEditorArea(), context);
			}
			return sashFormEditionArea;
		} else {
			if (this.workArea != null) {
				Composite area = new Composite(parent, SWT.NONE);
				this.workArea.createUi(area, context);
				return area;
			}
			CmsTheme theme = CmsTheme.getCmsTheme(parent);
			TabbedArea tabbedArea = createTabbedArea(parent, theme);
			return tabbedArea;
		}
	}

	@Override
	public void view(CmsUiProvider uiProvider, Composite workArea, Node context) {
		TabbedArea tabbedArea;
		if (workArea instanceof SashFormEditionArea) {
			tabbedArea = ((SashFormEditionArea) workArea).getTabbedArea();
		} else if (workArea instanceof TabbedArea) {
			tabbedArea = (TabbedArea) workArea;
		} else
			throw new IllegalArgumentException("Unsupported work area " + workArea.getClass().getName());
		tabbedArea.view(uiProvider, context);
	}

	@Override
	public void open(CmsUiProvider uiProvider, Composite workArea, Node context) {
		TabbedArea tabbedArea = ((SashFormEditionArea) workArea).getTabbedArea();
		tabbedArea.open(uiProvider, context);
	}

	public void init(Map<String, Object> properties) {
		weights = LangUtils.toStringList(properties.get(Property.weights.name()));
		startMaximized = properties.containsKey(Property.startMaximized.name())
				&& "true".equals(properties.get(Property.startMaximized.name()));
	}

	public void setEntryArea(CmsUiProvider entryArea) {
		this.entryArea = entryArea;
	}

	public void setWorkArea(CmsUiProvider workArea) {
		this.workArea = workArea;
	}

	TabbedArea createTabbedArea(Composite parent, CmsTheme theme) {
		TabbedArea tabbedArea = new TabbedArea(parent, SWT.NONE);
		tabbedArea.setBodyStyle(SuiteStyle.mainTabBody.style());
		tabbedArea.setTabStyle(SuiteStyle.mainTab.style());
		tabbedArea.setTabSelectedStyle(SuiteStyle.mainTabSelected.style());
		tabbedArea.setCloseIcon(SuiteIcon.close.getSmallIcon(theme));
		tabbedArea.setLayoutData(CmsUiUtils.fillAll());
		return tabbedArea;
	}

	/** A work area based on an entry area and and a tabbed area. */
	class SashFormEditionArea extends SashForm {
		private static final long serialVersionUID = 2219125778722702618L;
		private CmsTheme theme;
		private Composite entryArea;
		private Composite editorArea;
		private TabbedArea tabbedArea;

		SashFormEditionArea(Composite parent, int style) {
			super(parent, SWT.HORIZONTAL);
			theme = CmsTheme.getCmsTheme(parent);

			if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
				editorArea = new Composite(this, SWT.BORDER);
				entryArea = new Composite(this, SWT.BORDER);
			} else {
				entryArea = new Composite(this, SWT.NONE);
				editorArea = new Composite(this, SWT.NONE);
			}

			if (weights.size() != 0) {
				int[] actualWeight = new int[weights.size()];
				for (int i = 0; i < weights.size(); i++) {
					actualWeight[i] = Integer.parseInt(weights.get(i));
				}
				setWeights(actualWeight);
			} else {
				int[] actualWeights = new int[] { 3000, 7000 };
				setWeights(actualWeights);
			}
			if (startMaximized)
				setMaximizedControl(editorArea);
			editorArea.setLayout(new GridLayout());

			if (DefaultEditionLayer.this.workArea == null) {
				tabbedArea = createTabbedArea(editorArea, theme);
			}

		}

		Composite getEntryArea() {
			return entryArea;
		}

		TabbedArea getTabbedArea() {
			return tabbedArea;
		}

		Composite getEditorArea() {
			return editorArea;
		}

	}
}