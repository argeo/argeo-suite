package org.argeo.suite.ui;

import static org.argeo.suite.ui.SuiteIcon.dashboard;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;

import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FormLayout;
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
	private Composite dynamicArea;

	private CmsTheme theme;

	private Session session;

	private Map<String, WorkLayer> layers = new HashMap<>();
	private String currentLayer = "dashboard";

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
			dynamicArea = new Composite(belowHeader, SWT.NONE);
			leadPane = new Composite(belowHeader, SWT.NONE);
		} else {
			leadPane = new Composite(belowHeader, SWT.NONE);
			dynamicArea = new Composite(belowHeader, SWT.NONE);
		}
		leadPane.setLayoutData(CmsUiUtils.fillHeight());
		CmsUiUtils.style(leadPane, SuiteStyle.leadPane);
		dynamicArea.setLayoutData(CmsUiUtils.fillAll());

		dynamicArea.setLayout(new FormLayout());

		layers.put("dashboard", new WorkLayer(dynamicArea, style));
		layers.put("documents", new WorkLayer(dynamicArea, style));
		layers.put("locations", new WorkLayer(dynamicArea, style));
		layers.put("people", new WorkLayer(dynamicArea, style));
	}

	Composite getCurrentLayer() {
		return layers.get(currentLayer).getArea();
	}

	Composite getLayer(String id) {
		if (!layers.containsKey(id))
			throw new IllegalArgumentException("No layer " + id + " is available.");
		return layers.get(id).getArea();
	}

	Composite switchToLayer(String layer) {
		Composite current = getCurrentLayer();
		if (currentLayer.equals(layer))
			return current;
		Composite toShow = getLayer(layer);
		getDisplay().syncExec(() -> toShow.moveAbove(current));
		currentLayer = layer;
		return toShow;
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

	Composite getBelowHeader() {
		return belowHeader;
	}

	Composite getEntryArea() {
		return layers.get(currentLayer).getEntryArea();
	}

	Composite getDefaultBody() {
		return layers.get(currentLayer).getDefaultBody();
	}

	Session getSession() {
		return session;
	}

	void setSession(Session session) {
		this.session = session;
	}

	class WorkLayer {
		private SashForm area;
		private Composite entryArea;
		private Composite editorArea;
		private CTabFolder editorTabFolder;

		private Composite defaultBody;

		WorkLayer(Composite parent, int style) {
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

		}

		Composite getArea() {
			return area;
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

}
