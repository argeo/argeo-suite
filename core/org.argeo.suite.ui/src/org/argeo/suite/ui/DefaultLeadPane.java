package org.argeo.suite.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.LocaleUtils;
import org.argeo.cms.Localized;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsIcon;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.suite.RankedObject;
import org.argeo.suite.SuiteUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Constants;

/** Side pane listing various perspectives. */
public class DefaultLeadPane implements CmsUiProvider {
	private final static Log log = LogFactory.getLog(DefaultLeadPane.class);

	public static enum Property {
		defaultLayers, adminLayers;
	}

	private Map<String, RankedObject<SuiteLayer>> layers = Collections.synchronizedSortedMap(new TreeMap<>());
	private List<String> defaultLayers;
	private List<String> adminLayers;

	@Override
	public Control createUi(Composite parent, Node node) throws RepositoryException {
		CmsView cmsView = CmsView.getCmsView(parent);
		parent.setLayout(CmsUiUtils.noSpaceGridLayout());
		Composite appLayersC = new Composite(parent, SWT.NONE);
		CmsUiUtils.style(appLayersC, SuiteStyle.leadPane);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		appLayersC.setLayout(layout);
		appLayersC.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		Composite adminLayersC = new Composite(parent, SWT.NONE);
		CmsUiUtils.style(adminLayersC, SuiteStyle.leadPane);
		GridLayout adminLayout = new GridLayout();
		adminLayout.verticalSpacing = 10;
		adminLayout.marginBottom = 10;
		adminLayout.marginLeft = 10;
		adminLayout.marginRight = 10;
		adminLayersC.setLayout(adminLayout);
		adminLayersC.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, true));

//		boolean isAdmin = cmsView.doAs(() -> CurrentUser.isInRole(NodeConstants.ROLE_USER_ADMIN));
		Set<String> userRoles = cmsView.doAs(() -> CurrentUser.roles());
		Button first = null;
		layers: for (String layerDef : defaultLayers) {
			layerDef = layerDef.trim();
			if ("".equals(layerDef))
				continue layers;// skip empty lines
			String[] semiColArr = layerDef.split(";");
			String layerId = semiColArr[0];
			Set<String> layerRoles = SuiteUtils.extractRoles(semiColArr);
			if (layers.containsKey(layerId)) {
				if (!layerRoles.isEmpty()) {
					Set<String> intersection = new HashSet<String>(layerRoles);
					intersection.retainAll(userRoles);
					if (intersection.isEmpty())
						continue layers;// skip unauthorized layer
				}
				RankedObject<SuiteLayer> layerObj = layers.get(layerId);

				Localized title = null;
				if (!adminLayers.contains(layerId)) {
					String titleStr = (String) layerObj.getProperties().get(SuiteLayer.Property.title.name());
					if (titleStr != null) {
						if (titleStr.startsWith("%")) {
							// LocaleUtils.local(titleStr, getClass().getClassLoader());
							title = () -> titleStr;
						} else {
							title = new Localized.Untranslated(titleStr);
						}
					}
				}

				String iconName = (String) layerObj.getProperties().get(SuiteLayer.Property.icon.name());
				SuiteIcon icon = null;
				if (iconName != null)
					icon = SuiteIcon.valueOf(iconName);

				Composite buttonParent;
				if (adminLayers.contains(layerId))
					buttonParent = adminLayersC;
				else
					buttonParent = appLayersC;
				Button b = createLayerButton(buttonParent, layerId, title, icon);
				if (first == null)
					first = b;
			}
		}

//		if (isAdmin && adminLayers != null)
//			for (String layerId : adminLayers) {
//				if (layers.containsKey(layerId)) {
//					RankedObject<SuiteLayer> layerObj = layers.get(layerId);
//
//					String titleStr = (String) layerObj.getProperties().get(SuiteLayer.Property.title.name());
//					Localized title = null;
//					if (titleStr != null)
//						title = new Localized.Untranslated(titleStr);
//
//					String iconName = (String) layerObj.getProperties().get(SuiteLayer.Property.icon.name());
//					SuiteIcon icon = null;
//					if (iconName != null)
//						icon = SuiteIcon.valueOf(iconName);
//
//					Button b = SuiteUiUtils.createLayerButton(parent, layerId, title, icon);
//					if (first == null)
//						first = b;
//				}
//			}

//		Button dashboardB = createButton(parent, SuiteMsg.dashboard.name(), SuiteMsg.dashboard, SuiteIcon.dashboard);
		if (!cmsView.isAnonymous()) {
//			createButton(parent, SuiteMsg.documents.name(), SuiteMsg.documents, SuiteIcon.documents);
//			createButton(parent, SuiteMsg.people.name(), SuiteMsg.people, SuiteIcon.people);
//			createButton(parent, SuiteMsg.locations.name(), SuiteMsg.locations, SuiteIcon.location);
		}
		return first;
	}

	protected void processLayer(String layerDef) {

	}

	public void init(Map<String, Object> properties) {
		String[] defaultLayers = (String[]) properties.get(Property.defaultLayers.toString());
		if (defaultLayers == null)
			throw new IllegalArgumentException("Default layers must be set.");
		this.defaultLayers = Arrays.asList(defaultLayers);
		if (log.isDebugEnabled())
			log.debug("Default layers: " + Arrays.asList(defaultLayers));
		String[] adminLayers = (String[]) properties.get(Property.adminLayers.toString());
		if (adminLayers != null) {
			this.adminLayers = Arrays.asList(adminLayers);
			if (log.isDebugEnabled())
				log.debug("Admin layers: " + Arrays.asList(adminLayers));
		}
	}

	public void addLayer(SuiteLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			RankedObject.putIfHigherRank(layers, pid, layer, properties);
		}
	}

	public void removeLayer(SuiteLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			if (layers.containsKey(pid)) {
				if (layers.get(pid).equals(new RankedObject<SuiteLayer>(layer, properties))) {
					layers.remove(pid);
				}
			}
		}
	}

	protected Button createLayerButton(Composite parent, String layer, Localized msg, CmsIcon icon) {
		CmsTheme theme = CmsTheme.getCmsTheme(parent);
		Button button = new Button(parent, SWT.PUSH);
		CmsUiUtils.style(button, SuiteStyle.leadPane);
		if (icon != null)
			button.setImage(icon.getBigIcon(theme));
		button.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));
		// button.setToolTipText(msg.lead());
		if (msg != null) {
			Label lbl = new Label(parent, SWT.CENTER);
			CmsUiUtils.style(lbl, SuiteStyle.leadPane);
			// CmsUiUtils.markup(lbl);
			ClassLoader l10nClassLoader = getClass().getClassLoader();
			String txt = LocaleUtils.lead(msg, l10nClassLoader);
//			String txt = msg.lead();
			lbl.setText(txt);
			lbl.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
		}
		CmsUiUtils.sendEventOnSelect(button, SuiteEvent.switchLayer.topic(), SuiteEvent.LAYER, layer);
		return button;
	}
}
