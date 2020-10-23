package org.argeo.suite.ui;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;

import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.widgets.TabbedArea;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/** The {@link CmsView} for the work ergonomics of Argeo Suite. */
public class ArgeoSuiteUi extends Composite {
	private static final long serialVersionUID = 6207018859086689108L;

	public final static String DASHBOARD_LAYER = "dashboard";
	private Composite header;
	private Composite belowHeader;
	private Composite leadPane;
	private Composite dynamicArea;

	private Session session;

	private Map<String, WorkLayer> layers = new HashMap<>();
	private String currentLayer = DASHBOARD_LAYER;

	public ArgeoSuiteUi(Composite parent, int style) {
		super(parent, style);
		this.setLayout(CmsUiUtils.noSpaceGridLayout());

		header = new Composite(this, SWT.NONE);
		CmsUiUtils.style(header, SuiteStyle.header);
		header.setLayoutData(CmsUiUtils.fillWidth());

		belowHeader = new Composite(this, SWT.NONE);
		belowHeader.setLayoutData(CmsUiUtils.fillAll());
	}

	public void refreshBelowHeader(boolean initApp) {
		CmsUiUtils.clear(belowHeader);
		int style = getStyle();
		if (initApp) {
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

		} else {
			belowHeader.setLayout(CmsUiUtils.noSpaceGridLayout());
		}
	}

	/*
	 * LAYERS
	 */

	Composite getCurrentLayer() {
		if (currentLayer == null)
			throw new IllegalStateException("No current layer");
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

	void addLayer(String layer) {
		WorkLayer workLayer = new WorkLayer(dynamicArea, getStyle());
		layers.put(layer, workLayer);
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

	TabbedArea getTabbedArea() {
		return layers.get(currentLayer).getTabbedArea();
	}

	Session getSession() {
		return session;
	}

	void setSession(Session session) {
		this.session = session;
	}

	

}
