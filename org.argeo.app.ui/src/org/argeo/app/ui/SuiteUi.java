package org.argeo.app.ui;

import java.util.HashMap;
import java.util.Map;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.ux.CmsUi;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.cms.Localized;
import org.argeo.cms.swt.CmsSwtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/** The view for the default ergonomics of Argeo Suite. */
class SuiteUi extends Composite implements CmsUi {
	private static final long serialVersionUID = 6207018859086689108L;
	private final static CmsLog log = CmsLog.getLog(SuiteUi.class);

	private Localized title;
	private Composite header;
	private Composite footer;
	private Composite belowHeader;
	private Composite leadPane;
	private Composite sidePane;
	private Composite dynamicArea;

//	private Session sysSession;
//	private Session homeSession;
	private Content userDir;

	private Map<String, SuiteLayer> layers = new HashMap<>();
	private Map<String, Composite> workAreas = new HashMap<>();
	private String currentLayerId = null;

	private CmsView cmsView;

	public SuiteUi(Composite parent, int style) {
		super(parent, style);
		cmsView = CmsSwtUtils.getCmsView(parent);
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

	Composite getCurrentWorkArea() {
		if (currentLayerId == null)
			throw new IllegalStateException("No current layer");
		return workAreas.get(currentLayerId);
	}

	String getCurrentLayerId() {
		return currentLayerId;
	}

	private Composite getLayer(String id, Content context) {
		if (!layers.containsKey(id))
			return null;
		if (!workAreas.containsKey(id))
			initLayer(id, layers.get(id), context);
		return workAreas.get(id);
	}

	Composite switchToLayer(String layerId, Content context) {
		Composite current = null;
		if (currentLayerId != null) {
			current = getCurrentWorkArea();
			if (currentLayerId.equals(layerId))
				return current;
		}
		if (context == null) {
			if (!cmsView.isAnonymous())
				context = getUserDirNode();
		}
		Composite toShow = getLayer(layerId, context);
		if (toShow != null) {
			currentLayerId = layerId;
			if (!isDisposed()) {
//				getDisplay().syncExec(() -> {
				if (!toShow.isDisposed()) {
					toShow.moveAbove(null);
				} else {
					log.warn("Cannot show work area because it is disposed.");
					toShow = initLayer(layerId, layers.get(layerId), context);
					toShow.moveAbove(null);
				}
				dynamicArea.layout(true, true);
//				});
			}
			return toShow;
		} else {
			return current;
		}
	}

	void switchToLayer(SuiteLayer layer, Content context) {
		// TODO make it more robust
		for (String layerId : layers.keySet()) {
			SuiteLayer l = layers.get(layerId);
			if (layer.getId().equals(l.getId())) {
				switchToLayer(layerId, context);
				return;
			}
		}
		throw new IllegalArgumentException("Layer is not registered.");
	}

	void addLayer(String id, SuiteLayer layer) {
		layers.put(id, layer);
	}

	void removeLayer(String id) {
		layers.remove(id);
		if (workAreas.containsKey(id)) {
			Composite workArea = workAreas.remove(id);
			if (!workArea.isDisposed())
				workArea.dispose();
		}
	}

	protected Composite initLayer(String id, SuiteLayer layer, Content context) {
		Composite workArea = cmsView.doAs(() -> (Composite) layer.createUiPart(dynamicArea, context));
		CmsSwtUtils.style(workArea, SuiteStyle.workArea);
		workArea.setLayoutData(CmsSwtUtils.coverAll());
		workAreas.put(id, workArea);
		return workArea;
	}

	synchronized void logout() {
		userDir = null;
//		Jcr.logout(sysSession);
//		Jcr.logout(homeSession);
		currentLayerId = null;
		workAreas.clear();
	}

	/*
	 * GETTERS / SETTERS
	 */

	Composite getHeader() {
		return header;
	}

	Composite getFooter() {
		return footer;
	}

	Composite getLeadPane() {
		return leadPane;
	}

	Composite getSidePane() {
		return sidePane;
	}

	Composite getBelowHeader() {
		return belowHeader;
	}

//	Session getSysSession() {
//		return sysSession;
//	}
//
//	synchronized void initSessions(Repository repository, String userDirPath) throws RepositoryException {
//		this.sysSession = repository.login();
//		this.homeSession = repository.login(CmsConstants.HOME_WORKSPACE);
//		userDir = sysSession.getNode(userDirPath);
//		addDisposeListener((e) -> {
//			Jcr.logout(sysSession);
//			Jcr.logout(homeSession);
//		});
//	}

	@Deprecated
	Content getUserDirNode() {
		if (userDir == null)
			return null;
		return userDir;
	}

	Content getUserDir() {
		return userDir;
	}

	void setUserDir(Content userDir) {
		this.userDir = userDir;
	}

//	Session getSysSession() {
//		return sysSession;
//	}

//	Session getSession(String workspaceName) {
//		if (workspaceName == null)
//			return sysSession;
//		if (CmsConstants.SYS_WORKSPACE.equals(workspaceName))
//			return sysSession;
//		else if (CmsConstants.HOME_WORKSPACE.equals(workspaceName))
//			return homeSession;
//		else
//			throw new IllegalArgumentException("Unknown workspace " + workspaceName);
//	}

	public CmsView getCmsView() {
		return cmsView;
	}

	public Localized getTitle() {
		return title;
	}

	public void setTitle(Localized title) {
		this.title = title;
	}

}
