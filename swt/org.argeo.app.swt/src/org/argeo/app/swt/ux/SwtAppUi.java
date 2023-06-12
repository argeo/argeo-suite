package org.argeo.app.swt.ux;

import java.util.HashMap;
import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsLog;
import org.argeo.app.ux.SuiteStyle;
import org.argeo.cms.Localized;
import org.argeo.cms.swt.CmsSwtUi;
import org.argeo.cms.swt.CmsSwtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/** The view for the default UX of Argeo Suite. */
public class SwtAppUi extends CmsSwtUi {
	private static final long serialVersionUID = 6207018859086689108L;
	private final static CmsLog log = CmsLog.getLog(SwtAppUi.class);

	private Localized title;
	private Composite header;
	private Composite footer;
	private Composite belowHeader;
	private Composite leadPane;
	private Composite sidePane;
	private Composite dynamicArea;

	private Content userDir;

	private Map<String, SwtAppLayer> layers = new HashMap<>();
	private Map<String, Composite> workAreas = new HashMap<>();
	private String currentLayerId = null;

	private boolean loginScreen = false;

	public SwtAppUi(Composite parent, int style) {
		super(parent, style);
		this.setLayout(CmsSwtUtils.noSpaceGridLayout());

		header = new Composite(this, SWT.NONE);
		header.setLayout(CmsSwtUtils.noSpaceGridLayout());
		CmsSwtUtils.style(header, SuiteStyle.header);
		header.setLayoutData(CmsSwtUtils.fillWidth());

		belowHeader = new Composite(this, SWT.NONE);
		belowHeader.setLayoutData(CmsSwtUtils.fillAll());

		footer = new Composite(this, SWT.NONE);
		footer.setLayout(CmsSwtUtils.noSpaceGridLayout());
		CmsSwtUtils.style(footer, SuiteStyle.footer);
		footer.setLayoutData(CmsSwtUtils.fillWidth());
	}

	public void refreshBelowHeader(boolean initApp) {
		CmsSwtUtils.clear(belowHeader);
		int style = getStyle();
		if (initApp) {
			belowHeader.setLayout(CmsSwtUtils.noSpaceGridLayout(3));

			if (SWT.RIGHT_TO_LEFT == (style & SWT.RIGHT_TO_LEFT)) {// arabic, hebrew, etc.
				sidePane = new Composite(belowHeader, SWT.NONE);
				sidePane.setLayout(CmsSwtUtils.noSpaceGridLayout());
				sidePane.setLayoutData(CmsSwtUtils.fillHeight());
				dynamicArea = new Composite(belowHeader, SWT.NONE);
				leadPane = new Composite(belowHeader, SWT.NONE);
			} else {
				leadPane = new Composite(belowHeader, SWT.NONE);
				dynamicArea = new Composite(belowHeader, SWT.NONE);
				sidePane = new Composite(belowHeader, SWT.NONE);
				sidePane.setLayout(CmsSwtUtils.noSpaceGridLayout());
				sidePane.setLayoutData(CmsSwtUtils.fillHeight());
			}
			leadPane.setLayoutData(CmsSwtUtils.fillHeight());
			leadPane.setLayout(CmsSwtUtils.noSpaceGridLayout());
			CmsSwtUtils.style(leadPane, SuiteStyle.leadPane);

			dynamicArea.setLayoutData(CmsSwtUtils.fillAll());
			dynamicArea.setLayout(new FormLayout());

		} else {
			belowHeader.setLayout(CmsSwtUtils.noSpaceGridLayout());
		}
	}

	/*
	 * LAYERS
	 */

	public Composite getCurrentWorkArea() {
		if (currentLayerId == null)
			throw new IllegalStateException("No current layer");
		return workAreas.get(currentLayerId);
	}

	public String getCurrentLayerId() {
		return currentLayerId;
	}

	private Composite getLayer(String id, Content context) {
		if (!layers.containsKey(id))
			return null;
		if (!workAreas.containsKey(id))
			initLayer(id, layers.get(id), context);
		return workAreas.get(id);
	}

	public Composite switchToLayer(String layerId, Content context) {
		Composite current = null;
		if (currentLayerId != null) {
			current = getCurrentWorkArea();
			if (currentLayerId.equals(layerId))
				return current;
		}
		if (context == null) {
			if (!getCmsView().isAnonymous())
				context = getUserDir();
		}
		Composite toShow = getLayer(layerId, context);
		if (toShow != null) {
			currentLayerId = layerId;
			if (!isDisposed()) {
				if (!toShow.isDisposed()) {
					toShow.moveAbove(null);
				} else {
					log.warn("Cannot show work area because it is disposed.");
					toShow = initLayer(layerId, layers.get(layerId), context);
					toShow.moveAbove(null);
				}
				dynamicArea.layout(true, true);
			}
			return toShow;
		} else {
			return current;
		}
	}

	public void switchToLayer(SwtAppLayer layer, Content context) {
		// TODO make it more robust
		for (String layerId : layers.keySet()) {
			SwtAppLayer l = layers.get(layerId);
			if (layer.getId().equals(l.getId())) {
				switchToLayer(layerId, context);
				return;
			}
		}
		throw new IllegalArgumentException("Layer is not registered.");
	}

	public void addLayer(String id, SwtAppLayer layer) {
		layers.put(id, layer);
	}

	public void removeLayer(String id) {
		layers.remove(id);
		if (workAreas.containsKey(id)) {
			Composite workArea = workAreas.remove(id);
			if (!workArea.isDisposed())
				workArea.dispose();
		}
	}

	protected Composite initLayer(String id, SwtAppLayer layer, Content context) {
		Composite workArea = getCmsView().doAs(() -> (Composite) layer.createUiPart(dynamicArea, context));
		CmsSwtUtils.style(workArea, SuiteStyle.workArea);
		workArea.setLayoutData(CmsSwtUtils.coverAll());
		workAreas.put(id, workArea);
		return workArea;
	}

	public synchronized void logout() {
		userDir = null;
		currentLayerId = null;
		workAreas.clear();
	}

	/*
	 * GETTERS / SETTERS
	 */

	public Composite getHeader() {
		return header;
	}

	public Composite getFooter() {
		return footer;
	}

	public Composite getLeadPane() {
		return leadPane;
	}

	public Composite getSidePane() {
		return sidePane;
	}

	public Composite getBelowHeader() {
		return belowHeader;
	}

	public Content getUserDir() {
		return userDir;
	}

	public void setUserDir(Content userDir) {
		this.userDir = userDir;
	}

	public Localized getTitle() {
		return title;
	}

	public void setTitle(Localized title) {
		this.title = title;
	}

	public boolean isLoginScreen() {
		return loginScreen;
	}

	public void setLoginScreen(boolean loginScreen) {
		this.loginScreen = loginScreen;
	}
}
