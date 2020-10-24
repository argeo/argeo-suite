package org.argeo.suite.ui;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.api.NodeConstants;
import org.argeo.api.NodeUtils;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.jcr.Jcr;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/** The {@link CmsView} for the work ergonomics of Argeo Suite. */
class SuiteUi extends Composite {
	private static final long serialVersionUID = 6207018859086689108L;

	private Composite header;
	private Composite belowHeader;
	private Composite leadPane;
	private Composite dynamicArea;

	private Session sysSession;
	private Session homeSession;
	private Node userHome;

	private Map<String, SuiteLayer> layers = new HashMap<>();
	private Map<String, Composite> workAreas = new HashMap<>();
	private String currentLayerId = null;

	private CmsView cmsView;

	public SuiteUi(Composite parent, int style) {
		super(parent, style);
		cmsView = CmsView.getCmsView(parent);
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
			throw new IllegalArgumentException("No layer " + id + " is available.");
		if (!workAreas.containsKey(id))
			initLayer(id, layers.get(id), context);
		return workAreas.get(id);
	}

	Composite switchToLayer(String layer, Node context) {
		if (currentLayerId != null) {
			Composite current = getCurrentWorkArea();
			if (currentLayerId.equals(layer))
				return current;
		}
		if (context == null) {
			if (!cmsView.isAnonymous())
				context = userHome;
		}
		Composite toShow = getLayer(layer, context);
		getDisplay().syncExec(() -> {
			toShow.moveAbove(null);
			dynamicArea.layout(true, true);
		});
		currentLayerId = layer;
		return toShow;
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
		workArea.setLayoutData(CmsUiUtils.coverAll());
		workAreas.put(id, workArea);
		return workArea;
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

//	Session getSysSession() {
//		return sysSession;
//	}
//
	void initSessions(Repository repository) throws RepositoryException {
		this.sysSession = repository.login();
		this.homeSession = repository.login(NodeConstants.HOME_WORKSPACE);
		userHome = NodeUtils.getUserHome(homeSession);
		addDisposeListener((e) -> {
			Jcr.logout(sysSession);
			Jcr.logout(homeSession);
		});
	}

	Node getUserHome() {
		return userHome;
	}

	Session getSysSession() {
		return sysSession;
	}

}
