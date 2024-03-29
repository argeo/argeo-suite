package org.argeo.app.swt.ux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.argeo.api.acr.Content;
import org.argeo.api.app.RankedObject;
import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.ux.CmsIcon;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.core.SuiteUtils;
import org.argeo.app.ux.SuiteIcon;
import org.argeo.app.ux.SuiteStyle;
import org.argeo.app.ux.SuiteUxEvent;
import org.argeo.cms.CurrentUser;
import org.argeo.cms.LocaleUtils;
import org.argeo.cms.Localized;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleWiring;

/** Side pane listing various perspectives. */
public class DefaultLeadPane implements SwtUiProvider {
	private final static CmsLog log = CmsLog.getLog(DefaultLeadPane.class);

	public static enum Property {
		defaultLayers, adminLayers;
	}

	private Map<String, RankedObject<SwtAppLayer>> layers = Collections.synchronizedSortedMap(new TreeMap<>());
	private List<String> defaultLayers;
	private List<String> adminLayers = new ArrayList<>();

	private ClassLoader l10nClassLoader;

	@Override
	public Control createUiPart(Composite parent, Content node) {
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		parent.setLayout(CmsSwtUtils.noSpaceGridLayout());
		Composite appLayersC = new Composite(parent, SWT.NONE);
		CmsSwtUtils.style(appLayersC, SuiteStyle.leadPane);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		appLayersC.setLayout(layout);
		appLayersC.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		Composite adminLayersC;
		if (!adminLayers.isEmpty()) {
			adminLayersC = new Composite(parent, SWT.NONE);
			CmsSwtUtils.style(adminLayersC, SuiteStyle.leadPane);
			GridLayout adminLayout = new GridLayout();
			adminLayout.verticalSpacing = 10;
			adminLayout.marginBottom = 10;
			adminLayout.marginLeft = 10;
			adminLayout.marginRight = 10;
			adminLayersC.setLayout(adminLayout);
			adminLayersC.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, true));
		} else {
			adminLayersC = null;
		}

//		boolean isAdmin = cmsView.doAs(() -> CurrentUser.isInRole(NodeConstants.ROLE_USER_ADMIN));
		// Set<String> userRoles = cmsView.doAs(() -> CurrentUser.roles());
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
					boolean authorized = false;
					authorized = cmsView.doAs(() -> {
						for (String layerRole : layerRoles) {
							if (CurrentUser.implies(layerRole, null)) {
								return true;
							}
						}
						return false;
					});
					if (!authorized)
						continue layers;// skip unauthorized layer
//					Set<String> intersection = new HashSet<String>(layerRoles);
//					intersection.retainAll(userRoles);
//					if (intersection.isEmpty())
//						continue layers;// skip unauthorized layer
				}
				RankedObject<SwtAppLayer> layerObj = layers.get(layerId);

				Localized title = null;
				if (!adminLayers.contains(layerId)) {
					String titleStr = (String) layerObj.getProperties().get(SwtAppLayer.Property.title.name());
					if (titleStr != null) {
						if (titleStr.startsWith("%")) {
							// LocaleUtils.local(titleStr, getClass().getClassLoader());
							title = () -> titleStr;
						} else {
							title = new Localized.Untranslated(titleStr);
						}
					}
				}

				String iconName = (String) layerObj.getProperties().get(SwtAppLayer.Property.icon.name());
				SuiteIcon icon = null;
				if (iconName != null)
					icon = SuiteIcon.valueOf(iconName);

				Composite buttonParent;
				if (adminLayers.contains(layerId))
					buttonParent = adminLayersC;
				else
					buttonParent = appLayersC;
				Button b = createLayerButton(buttonParent, layerId, title, icon, l10nClassLoader);
				if (first == null)
					first = b;
			}
		}
		return first;
	}

	protected Button createLayerButton(Composite parent, String layer, Localized msg, CmsIcon icon,
			ClassLoader l10nClassLoader) {
		CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);
		Button button = new Button(parent, SWT.PUSH);
		CmsSwtUtils.style(button, SuiteStyle.leadPane);
		if (icon != null)
			button.setImage(theme.getBigIcon(icon));
		button.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));
		// button.setToolTipText(msg.lead());
		if (msg != null) {
			Label lbl = new Label(parent, SWT.CENTER);
			CmsSwtUtils.style(lbl, SuiteStyle.leadPane);
			String txt = LocaleUtils.lead(msg, l10nClassLoader);
//			String txt = msg.lead();
			lbl.setText(txt);
			lbl.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
		}
		CmsSwtUtils.sendEventOnSelect(button, SuiteUxEvent.switchLayer.topic(), SuiteUxEvent.LAYER, layer);
		return button;
	}

	public void init(BundleContext bundleContext, Map<String, Object> properties) {
		l10nClassLoader = bundleContext != null ? bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader()
				: getClass().getClassLoader();

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

	public void destroy(BundleContext bundleContext, Map<String, String> properties) {

	}

	public void addLayer(SwtAppLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			RankedObject.putIfHigherRank(layers, pid, layer, properties);
		}
	}

	public void removeLayer(SwtAppLayer layer, Map<String, Object> properties) {
		if (properties.containsKey(Constants.SERVICE_PID)) {
			String pid = (String) properties.get(Constants.SERVICE_PID);
			if (layers.containsKey(pid)) {
				if (layers.get(pid).equals(new RankedObject<SwtAppLayer>(layer, properties))) {
					layers.remove(pid);
				}
			}
		}
	}

//	protected Button createLayerButton(Composite parent, String layer, Localized msg, CmsIcon icon) {
//		CmsTheme theme = CmsTheme.getCmsTheme(parent);
//		Button button = new Button(parent, SWT.PUSH);
//		CmsUiUtils.style(button, SuiteStyle.leadPane);
//		if (icon != null)
//			button.setImage(icon.getBigIcon(theme));
//		button.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));
//		// button.setToolTipText(msg.lead());
//		if (msg != null) {
//			Label lbl = new Label(parent, SWT.CENTER);
//			CmsUiUtils.style(lbl, SuiteStyle.leadPane);
//			// CmsUiUtils.markup(lbl);
//			ClassLoader l10nClassLoader = getClass().getClassLoader();
//			String txt = LocaleUtils.lead(msg, l10nClassLoader);
////			String txt = msg.lead();
//			lbl.setText(txt);
//			lbl.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
//		}
//		CmsUiUtils.sendEventOnSelect(button, SuiteEvent.switchLayer.topic(), SuiteEvent.LAYER, layer);
//		return button;
//	}
}
