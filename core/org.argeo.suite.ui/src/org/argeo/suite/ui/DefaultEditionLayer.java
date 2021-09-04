package org.argeo.suite.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.Localized;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.jcr.JcrException;
import org.argeo.suite.ui.widgets.TabbedArea;
import org.argeo.util.LangUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

/** An app layer based on an entry area and an editor area. */
public class DefaultEditionLayer implements SuiteLayer {
	private CmsUiProvider entryArea;
	private CmsUiProvider defaultView;
	private CmsUiProvider workArea;
	private List<String> weights = new ArrayList<>();
	private boolean startMaximized = false;
	private boolean fixedEntryArea = false;
	private boolean singleTab = false;
	private Localized title = null;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		// TODO Factorize more, or split into more specialised classes?
		if (entryArea != null) {
			if (fixedEntryArea) {
				FixedEditionArea editionArea = new FixedEditionArea(parent, parent.getStyle());
				Control entryAreaC = entryArea.createUi(editionArea.getEntryArea(), context);
				CmsUiUtils.style(entryAreaC, SuiteStyle.entryArea);
				if (this.defaultView != null) {
					editionArea.getTabbedArea().view(defaultView, context);
				}
				return editionArea;
			} else {
				SashFormEditionArea editionArea = new SashFormEditionArea(parent, parent.getStyle());
				entryArea.createUi(editionArea.getEntryArea(), context);
				if (this.defaultView != null) {
					editionArea.getTabbedArea().view(defaultView, context);
				}
				return editionArea;
			}
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
	public void view(CmsUiProvider uiProvider, Composite workAreaC, Node context) {
		if (workArea != null) {
			try {
				CmsUiUtils.clear(workAreaC);
				workArea.createUi(workAreaC, context);
				workAreaC.layout(true, true);
				return;
			} catch (RepositoryException e) {
				throw new JcrException("Cannot rebuild work area", e);
			}
		}

		// tabbed area
		TabbedArea tabbedArea = findTabbedArea(workAreaC);
		if (tabbedArea == null)
			throw new IllegalArgumentException("Unsupported work area " + workAreaC.getClass().getName());
		if (uiProvider == null) {
			// reset
			tabbedArea.closeAllTabs();
			if (this.defaultView != null) {
				tabbedArea.view(defaultView, context);
			}
		} else {
			tabbedArea.view(uiProvider, context);
		}
	}

	@Override
	public Node getCurrentContext(Composite workArea) {
		TabbedArea tabbedArea = findTabbedArea(workArea);
		if (tabbedArea == null)
			return null;
		return tabbedArea.getCurrentContext();
	}

	private TabbedArea findTabbedArea(Composite workArea) {
		TabbedArea tabbedArea = null;
		if (workArea instanceof SashFormEditionArea) {
			tabbedArea = ((SashFormEditionArea) workArea).getTabbedArea();
		} else if (workArea instanceof FixedEditionArea) {
			tabbedArea = ((FixedEditionArea) workArea).getTabbedArea();
		} else if (workArea instanceof TabbedArea) {
			tabbedArea = (TabbedArea) workArea;
		}
		return tabbedArea;
	}

	@Override
	public void open(CmsUiProvider uiProvider, Composite workArea, Node context) {
		TabbedArea tabbedArea = ((SashFormEditionArea) workArea).getTabbedArea();
		tabbedArea.open(uiProvider, context);
	}

	@Override
	public Localized getTitle() {
		return title;
	}

	public void init(BundleContext bundleContext, Map<String, Object> properties) {
		weights = LangUtils.toStringList(properties.get(Property.weights.name()));
		startMaximized = properties.containsKey(Property.startMaximized.name())
				&& "true".equals(properties.get(Property.startMaximized.name()));
		fixedEntryArea = properties.containsKey(Property.fixedEntryArea.name())
				&& "true".equals(properties.get(Property.fixedEntryArea.name()));
		if (fixedEntryArea && weights.size() != 0) {
			throw new IllegalArgumentException("Property " + Property.weights.name() + " should not be set if property "
					+ Property.fixedEntryArea.name() + " is set.");
		}
		singleTab = properties.containsKey(Property.singleTab.name())
				&& "true".equals(properties.get(Property.singleTab.name()));

		String titleStr = (String) properties.get(SuiteLayer.Property.title.name());
		if (titleStr != null) {
			if (titleStr.startsWith("%")) {
				title = new Localized() {

					@Override
					public String name() {
						return titleStr;
					}

					@Override
					public ClassLoader getL10nClassLoader() {
						return bundleContext != null
								? bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader()
								: getClass().getClassLoader();
					}
				};
			} else {
				title = new Localized.Untranslated(titleStr);
			}
		}
	}

	public void destroy(BundleContext bundleContext, Map<String, String> properties) {

	}

	public void setEntryArea(CmsUiProvider entryArea) {
		this.entryArea = entryArea;
	}

	public void setWorkArea(CmsUiProvider workArea) {
		this.workArea = workArea;
	}

	public void setDefaultView(CmsUiProvider defaultView) {
		this.defaultView = defaultView;
	}

	TabbedArea createTabbedArea(Composite parent, CmsTheme theme) {
		TabbedArea tabbedArea = new TabbedArea(parent, SWT.NONE);
		tabbedArea.setSingleTab(singleTab);
		tabbedArea.setBodyStyle(SuiteStyle.mainTabBody.style());
		tabbedArea.setTabStyle(SuiteStyle.mainTab.style());
		tabbedArea.setTabSelectedStyle(SuiteStyle.mainTabSelected.style());
		tabbedArea.setCloseIcon(SuiteIcon.close.getSmallIcon(theme));
		tabbedArea.setLayoutData(CmsUiUtils.fillAll());
		return tabbedArea;
	}

//	/** A work area based on an entry area and and a tabbed area. */
	class SashFormEditionArea extends SashForm {
		private static final long serialVersionUID = 2219125778722702618L;
		private TabbedArea tabbedArea;
		private Composite entryC;

		SashFormEditionArea(Composite parent, int style) {
			super(parent, SWT.HORIZONTAL);
			CmsTheme theme = CmsTheme.getCmsTheme(parent);

			Composite editorC;
			if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
				editorC = new Composite(this, SWT.BORDER);
				entryC = new Composite(this, SWT.BORDER);
			} else {
				entryC = new Composite(this, SWT.NONE);
				editorC = new Composite(this, SWT.NONE);
			}

			// sash form specific
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
				setMaximizedControl(editorC);

			GridLayout editorAreaLayout = CmsUiUtils.noSpaceGridLayout();
//			editorAreaLayout.verticalSpacing = 0;
//			editorAreaLayout.marginBottom = 0;
//			editorAreaLayout.marginHeight = 0;
//			editorAreaLayout.marginLeft = 0;
//			editorAreaLayout.marginRight = 0;
			editorC.setLayout(editorAreaLayout);

			tabbedArea = createTabbedArea(editorC, theme);
		}

		TabbedArea getTabbedArea() {
			return tabbedArea;
		}

		Composite getEntryArea() {
			return entryC;
		}

	}

	class FixedEditionArea extends Composite {
		private static final long serialVersionUID = -5525672639277322465L;
		private TabbedArea tabbedArea;
		private Composite entryC;

		public FixedEditionArea(Composite parent, int style) {
			super(parent, style);
			CmsTheme theme = CmsTheme.getCmsTheme(parent);

			setLayout(CmsUiUtils.noSpaceGridLayout(2));

			Composite editorC;
			if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
				editorC = new Composite(this, SWT.NONE);
				entryC = new Composite(this, SWT.NONE);
			} else {
				entryC = new Composite(this, SWT.NONE);
				editorC = new Composite(this, SWT.NONE);
			}
			entryC.setLayoutData(CmsUiUtils.fillHeight());

			GridLayout editorAreaLayout = CmsUiUtils.noSpaceGridLayout();
//			editorAreaLayout.verticalSpacing = 0;
//			editorAreaLayout.marginBottom = 0;
//			editorAreaLayout.marginHeight = 0;
//			editorAreaLayout.marginLeft = 0;
//			editorAreaLayout.marginRight = 0;
			editorC.setLayout(editorAreaLayout);
			editorC.setLayoutData(CmsUiUtils.fillAll());

			tabbedArea = createTabbedArea(editorC, theme);
		}

		TabbedArea getTabbedArea() {
			return tabbedArea;
		}

		Composite getEntryArea() {
			return entryC;
		}
	}

}