package org.argeo.suite.ui;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.NodeConstants;
import org.argeo.cms.Localized;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.jcr.Jcr;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/** The view for the default ergonomics of Argeo Suite. */
class SuiteUi extends Composite {
	private static final long serialVersionUID = 6207018859086689108L;
	private final static Log log = LogFactory.getLog(SuiteUi.class);

	private Localized title;
	private Composite header;
	private Composite footer;
	private Composite belowHeader;
	private Composite leadPane;
	private Composite dynamicArea;

	private Session sysSession;
	private Session homeSession;
	private Node userDir;

	private Map<String, SuiteLayer> layers = new HashMap<>();
	private Map<String, Composite> workAreas = new HashMap<>();
	private String currentLayerId = null;

	private CmsView cmsView;

	public SuiteUi(Composite parent, int style) {
		super(parent, style);
		cmsView = CmsView.getCmsView(parent);
		this.setLayout(CmsUiUtils.noSpaceGridLayout());

		header = new Composite(this, SWT.NONE);
		header.setLayout(CmsUiUtils.noSpaceGridLayout());
		CmsUiUtils.style(header, SuiteStyle.header);
		header.setLayoutData(CmsUiUtils.fillWidth());

		belowHeader = new Composite(this, SWT.NONE);
		belowHeader.setLayoutData(CmsUiUtils.fillAll());

		footer = new Composite(this, SWT.NONE);
		footer.setLayout(CmsUiUtils.noSpaceGridLayout());
		CmsUiUtils.style(footer, SuiteStyle.footer);
		footer.setLayoutData(CmsUiUtils.fillWidth());
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
			leadPane.setLayout(CmsUiUtils.noSpaceGridLayout());
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

	Composite getCurrentWorkArea() {
		if (currentLayerId == null)
			throw new IllegalStateException("No current layer");
		return workAreas.get(currentLayerId);
	}

	String getCurrentLayerId() {
		return currentLayerId;
	}

	private Composite getLayer(String id, Node context) {
		if (!layers.containsKey(id))
			return null;
		if (!workAreas.containsKey(id))
			initLayer(id, layers.get(id), context);
		return workAreas.get(id);
	}

	Composite switchToLayer(String layerId, Node context) {
		Composite current = null;
		if (currentLayerId != null) {
			current = getCurrentWorkArea();
			if (currentLayerId.equals(layerId))
				return current;
		}
		if (context == null) {
			if (!cmsView.isAnonymous())
				context = userDir;
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

	Composite switchToLayer(SuiteLayer layer, Node context) {
		// TODO make it more robust
		for (String layerId : layers.keySet()) {
			SuiteLayer l = layers.get(layerId);
			if (layer == l) {
				return switchToLayer(layerId, context);
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

	protected Composite initLayer(String id, SuiteLayer layer, Node context) {
		Composite workArea = cmsView.doAs(() -> (Composite) layer.createUiPart(dynamicArea, context));
		CmsUiUtils.style(workArea, SuiteStyle.workArea);
		workArea.setLayoutData(CmsUiUtils.coverAll());
		workAreas.put(id, workArea);
		return workArea;
	}

	synchronized void logout() {
		userDir = null;
		Jcr.logout(sysSession);
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

	Composite getBelowHeader() {
		return belowHeader;
	}

//	Session getSysSession() {
//		return sysSession;
//	}
//
	synchronized void initSessions(Repository repository, String userDirPath) throws RepositoryException {
		this.sysSession = repository.login();
		this.homeSession = repository.login(NodeConstants.HOME_WORKSPACE);
		userDir = sysSession.getNode(userDirPath);
		addDisposeListener((e) -> {
			Jcr.logout(sysSession);
			Jcr.logout(homeSession);
		});
	}

	Node getUserDir() {
		return userDir;
	}

	Session getSysSession() {
		return sysSession;
	}

	Session getSession(String workspaceName) {
		if (workspaceName == null)
			return sysSession;
		if (NodeConstants.SYS_WORKSPACE.equals(workspaceName))
			return sysSession;
		else if (NodeConstants.HOME_WORKSPACE.equals(workspaceName))
			return homeSession;
		else
			throw new IllegalArgumentException("Unknown workspace " + workspaceName);
	}

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
