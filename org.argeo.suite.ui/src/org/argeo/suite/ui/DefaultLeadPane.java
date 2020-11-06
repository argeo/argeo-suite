package org.argeo.suite.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.Localized;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsIcon;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.suite.RankedObject;
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
		defaultLayers;
	}

	private Map<String, RankedObject<SuiteLayer>> layers = Collections.synchronizedSortedMap(new TreeMap<>());
	private String[] defaultLayers;

	@Override
	public Control createUi(Composite parent, Node node) throws RepositoryException {
		CmsView cmsView = CmsView.getCmsView(parent);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		parent.setLayout(layout);

		Button first = null;
		for (String layerId : defaultLayers) {
			if (layers.containsKey(layerId)) {
				RankedObject<SuiteLayer> layerObj = layers.get(layerId);

				// TODO deal with i10n
				String titleStr = (String) layerObj.getProperties().get(SuiteLayer.Property.title.name());
				Localized title = null;
				if (titleStr != null)
					title = new Localized.Untranslated(titleStr);

				String iconName = (String) layerObj.getProperties().get(SuiteLayer.Property.icon.name());
				SuiteIcon icon = null;
				if (iconName != null)
					icon = SuiteIcon.valueOf(iconName);

				Button b = createButton(parent, layerId, title, icon);
				if (first == null)
					first = b;
			}
		}

//		Button dashboardB = createButton(parent, SuiteMsg.dashboard.name(), SuiteMsg.dashboard, SuiteIcon.dashboard);
		if (!cmsView.isAnonymous()) {
//			createButton(parent, SuiteMsg.documents.name(), SuiteMsg.documents, SuiteIcon.documents);
//			createButton(parent, SuiteMsg.people.name(), SuiteMsg.people, SuiteIcon.people);
//			createButton(parent, SuiteMsg.locations.name(), SuiteMsg.locations, SuiteIcon.location);
		}
		return first;
	}

	protected Button createButton(Composite parent, String layer, Localized msg, CmsIcon icon) {
		CmsTheme theme = CmsTheme.getCmsTheme(parent);
		Button button = new Button(parent, SWT.PUSH);
		CmsUiUtils.style(button, SuiteStyle.leadPane);
		if (icon != null)
			button.setImage(icon.getBigIcon(theme));
		button.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));
		// button.setToolTipText(msg.lead());
		if (msg != null) {
			Label lbl = new Label(parent, SWT.NONE);
			CmsUiUtils.style(lbl, SuiteStyle.leadPane);
			lbl.setText(msg.lead());
			lbl.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
		}
		CmsUiUtils.sendEventOnSelect(button, SuiteEvent.switchLayer.topic(), SuiteEvent.LAYER, layer);
		return button;
	}

	public void init(Map<String, Object> properties) {
		defaultLayers = (String[]) properties.get(Property.defaultLayers.toString());
		if (defaultLayers == null)
			throw new IllegalArgumentException("Default layers must be set.");
		if (log.isDebugEnabled())
			log.debug("Default layers: " + Arrays.asList(defaultLayers));
	}

	public void addLayer(SuiteLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			RankedObject.putIfHigherRank(layers, pid, layer, properties);
		}
	}

}
