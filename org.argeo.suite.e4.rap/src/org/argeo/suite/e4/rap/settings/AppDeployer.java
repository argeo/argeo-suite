package org.argeo.suite.e4.rap.settings;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.e4.rap.AbstractRapE4App;
import org.argeo.util.LangUtils;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Managed service factory deploying Argeo RAP app based on OSGi configurations.
 */
public class AppDeployer implements ManagedServiceFactory {
	private final static Log log = LogFactory.getLog(AppDeployer.class);
	private BundleContext bundleContext;

	public void init(BundleContext bundleContext, Map<String, String> properties) {
		this.bundleContext = bundleContext;

		deploy(findBundle("org.argeo.suite.studio", null), properties);
		deploy(findBundle("org.argeo.suite.docs", null), properties);
	}

	public void destroy() {

	}

	@Override
	public String getName() {
		return "Argeo App Deployer";
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
		Bundle bundle = findBundle(pid, properties);
		deploy(bundle, LangUtils.dictToStringMap(properties));
	}

	protected void deploy(Bundle bundle, Map<String, String> properties) {
//		CmsTheme cmsTheme;
//		if (properties.containsKey(CmsTheme.CMS_THEME_BUNDLE_PROPERTY)) {
//			String cmsThemeBundle = properties.get(CmsTheme.CMS_THEME_BUNDLE_PROPERTY);
//			cmsTheme = new CmsTheme(bundleContext, cmsThemeBundle);
//		} else {
//			cmsTheme = new CmsTheme(bundleContext, CmsTheme.DEFAULT_CMS_THEME_BUNDLE);
//		}

		ArgeoRapApp app = new ArgeoRapApp(bundleContext, bundle, null);

		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(AbstractRapE4App.CONTEXT_NAME_PROPERTY, app.getContextName());
		bundleContext.registerService(ApplicationConfiguration.class, app, props);

		if (log.isDebugEnabled())
			log.debug("Deployed Argeo App " + bundle.getSymbolicName() + " to " + app.getContextName());
	}

	@Override
	public void deleted(String pid) {
	}

	protected Bundle findBundle(String pid, Dictionary<String, ?> properties) {
		Bundle bundle = null;
		for (Bundle b : bundleContext.getBundles()) {
			if (b.getSymbolicName().equals(pid)) {
				bundle = b;
				break;
			}
		}
		if (bundle == null)
			throw new IllegalStateException("Bundle " + pid + " not found");
		return bundle;
	}
}
