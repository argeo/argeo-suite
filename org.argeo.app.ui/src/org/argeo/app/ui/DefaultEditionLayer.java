package org.argeo.app.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.cms.Localized;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.SwtTabbedArea;
import org.argeo.cms.ui.CmsUiProvider;
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
	public Control createUiPart(Composite parent, Content context) {
		// TODO Factorize more, or split into more specialised classes?
		if (entryArea != null) {
			if (fixedEntryArea) {
				FixedEditionArea editionArea = new FixedEditionArea(parent, parent.getStyle());
				Control entryAreaC = entryArea.createUiPart(editionArea.getEntryArea(), context);
				CmsSwtUtils.style(entryAreaC, SuiteStyle.entryArea);
				if (this.defaultView != null) {
					editionArea.getTabbedArea().view(defaultView, context);
				}
				return editionArea;
			} else {
				SashFormEditionArea editionArea = new SashFormEditionArea(parent, parent.getStyle());
				entryArea.createUiPart(editionArea.getEntryArea(), context);
				if (this.defaultView != null) {
					editionArea.getTabbedArea().view(defaultView, context);
				}
				return editionArea;
			}
		} else {
			if (this.workArea != null) {
				Composite area = new Composite(parent, SWT.NONE);
				this.workArea.createUiPart(area, context);
				return area;
			}
			CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);
			SwtTabbedArea tabbedArea = createTabbedArea(parent, theme);
			return tabbedArea;
		}
	}

	@Override
	public void view(SwtUiProvider uiProvider, Composite workAreaC, Content context) {
		if (workArea != null) {
			CmsSwtUtils.clear(workAreaC);
			workArea.createUiPart(workAreaC, context);
			workAreaC.layout(true, true);
			return;
		}

		// tabbed area
		SwtTabbedArea tabbedArea = findTabbedArea(workAreaC);
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
	public Content getCurrentContext(Composite workArea) {
		SwtTabbedArea tabbedArea = findTabbedArea(workArea);
		if (tabbedArea == null)
			return null;
		return tabbedArea.getCurrentContext();
	}

	private SwtTabbedArea findTabbedArea(Composite workArea) {
		SwtTabbedArea tabbedArea = null;
		if (workArea instanceof SashFormEditionArea) {
			tabbedArea = ((SashFormEditionArea) workArea).getTabbedArea();
		} else if (workArea instanceof FixedEditionArea) {
			tabbedArea = ((FixedEditionArea) workArea).getTabbedArea();
		} else if (workArea instanceof SwtTabbedArea) {
			tabbedArea = (SwtTabbedArea) workArea;
		}
		return tabbedArea;
	}

	@Override
	public void open(SwtUiProvider uiProvider, Composite workArea, Content context) {
		SwtTabbedArea tabbedArea = ((SashFormEditionArea) workArea).getTabbedArea();
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

	SwtTabbedArea createTabbedArea(Composite parent, CmsSwtTheme theme) {
		SwtTabbedArea tabbedArea = new SwtTabbedArea(parent, SWT.NONE);
		tabbedArea.setSingleTab(singleTab);
		tabbedArea.setBodyStyle(SuiteStyle.mainTabBody.style());
		tabbedArea.setTabStyle(SuiteStyle.mainTab.style());
		tabbedArea.setTabSelectedStyle(SuiteStyle.mainTabSelected.style());
		tabbedArea.setCloseIcon(theme.getSmallIcon(SuiteIcon.close));
		tabbedArea.setLayoutData(CmsSwtUtils.fillAll());
		return tabbedArea;
	}

//	/** A work area based on an entry area and and a tabbed area. */
	class SashFormEditionArea extends SashForm {
		private static final long serialVersionUID = 2219125778722702618L;
		private SwtTabbedArea tabbedArea;
		private Composite entryC;

		SashFormEditionArea(Composite parent, int style) {
			super(parent, SWT.HORIZONTAL);
			CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);

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

			GridLayout editorAreaLayout = CmsSwtUtils.noSpaceGridLayout();
//			editorAreaLayout.verticalSpacing = 0;
//			editorAreaLayout.marginBottom = 0;
//			editorAreaLayout.marginHeight = 0;
//			editorAreaLayout.marginLeft = 0;
//			editorAreaLayout.marginRight = 0;
			editorC.setLayout(editorAreaLayout);

			tabbedArea = createTabbedArea(editorC, theme);
		}

		SwtTabbedArea getTabbedArea() {
			return tabbedArea;
		}

		Composite getEntryArea() {
			return entryC;
		}

	}

	class FixedEditionArea extends Composite {
		private static final long serialVersionUID = -5525672639277322465L;
		private SwtTabbedArea tabbedArea;
		private Composite entryC;

		public FixedEditionArea(Composite parent, int style) {
			super(parent, style);
			CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);

			setLayout(CmsSwtUtils.noSpaceGridLayout(2));

			Composite editorC;
			if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
				editorC = new Composite(this, SWT.NONE);
				entryC = new Composite(this, SWT.NONE);
			} else {
				entryC = new Composite(this, SWT.NONE);
				editorC = new Composite(this, SWT.NONE);
			}
			entryC.setLayoutData(CmsSwtUtils.fillHeight());

			GridLayout editorAreaLayout = CmsSwtUtils.noSpaceGridLayout();
//			editorAreaLayout.verticalSpacing = 0;
//			editorAreaLayout.marginBottom = 0;
//			editorAreaLayout.marginHeight = 0;
//			editorAreaLayout.marginLeft = 0;
//			editorAreaLayout.marginRight = 0;
			editorC.setLayout(editorAreaLayout);
			editorC.setLayoutData(CmsSwtUtils.fillAll());

			tabbedArea = createTabbedArea(editorC, theme);
		}

		SwtTabbedArea getTabbedArea() {
			return tabbedArea;
		}

		Composite getEntryArea() {
			return entryC;
		}
	}

}