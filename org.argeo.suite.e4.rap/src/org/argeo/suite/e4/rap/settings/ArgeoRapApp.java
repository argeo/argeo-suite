package org.argeo.suite.e4.rap.settings;

import java.util.Enumeration;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.e4.rap.AbstractRapE4App;
import org.argeo.cms.ui.CmsTheme;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.client.WebClient;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/** Argeo RAP app. */
public class ArgeoRapApp extends AbstractRapE4App {
	private final static Log log = LogFactory.getLog(ArgeoRapApp.class);

	private CmsTheme cmsTheme;
	private String baseE4xmi = "/e4xmi";
	private Bundle bundle;

	public ArgeoRapApp(BundleContext bundleContext, Bundle bundle, CmsTheme cmsTheme) {
		setBundleContext(bundleContext);
		this.cmsTheme = cmsTheme;
		this.bundle = bundle;
		setLifeCycleUri("bundleclass://org.argeo.suite.e4.rap/org.argeo.suite.e4.rap.ArgeoSuiteLoginLifecycle");
		String contextName = "apps/" + FilenameUtils.getExtension(bundle.getSymbolicName());
		setContextName(contextName);
	}

	public ArgeoRapApp() {
		setLifeCycleUri("bundleclass://org.argeo.suite.e4.rap/org.argeo.suite.e4.rap.ArgeoSuiteLoginLifecycle");
	}

	@Override
	public void init(BundleContext bundleContext, Map<String, Object> properties) {
		super.init(bundleContext, properties);
		// super must be first
//		if (getBaseProperties().containsKey(CmsTheme.CMS_THEME_BUNDLE_PROPERTY)) {
//			String cmsThemeBundle = getBaseProperties().get(CmsTheme.CMS_THEME_BUNDLE_PROPERTY);
//			cmsTheme = new CmsTheme(getBundleContext(), cmsThemeBundle);
//		} else {
//			cmsTheme = new CmsTheme(getBundleContext(), CmsTheme.DEFAULT_CMS_THEME_BUNDLE);
//		}
		bundle = bundleContext.getBundle();
	}

	@Override
	protected void addEntryPoints(Application application) {
//		if (cmsTheme != null)
//			cmsTheme.apply(application);

		String font = "<link rel='stylesheet' href='http://fonts.googleapis.com/css?family=Source+Sans+Pro'/>";
		getBaseProperties().put(WebClient.HEAD_HTML, font);

		Enumeration<String> paths = bundle.getEntryPaths(baseE4xmi);
		while (paths.hasMoreElements()) {
			String p = paths.nextElement();
			if (p.endsWith(".e4xmi")) {
				String e4xmiPath = bundle.getSymbolicName() + '/' + p;
				String name = '/' + FilenameUtils.removeExtension(FilenameUtils.getName(p));
				addE4EntryPoint(application, name, e4xmiPath, getBaseProperties());
				if (log.isDebugEnabled())
					log.debug("Registered " + e4xmiPath + " as " + getContextName() + name);
			}
		}
	}
}
