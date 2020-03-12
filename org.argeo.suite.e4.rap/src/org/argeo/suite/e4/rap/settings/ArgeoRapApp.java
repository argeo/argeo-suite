package org.argeo.suite.e4.rap.settings;

import java.util.Enumeration;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.e4.rap.AbstractRapE4App;
import org.argeo.cms.ui.util.CmsTheme;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.client.WebClient;
import org.osgi.framework.Bundle;

/** Argeo RAP app. */
public class ArgeoRapApp extends AbstractRapE4App {
	private final static Log log = LogFactory.getLog(ArgeoRapApp.class);

	private Bundle bundle;
	private CmsTheme cmsTheme;
	private String baseE4xmi = "/e4xmi";

	public ArgeoRapApp(Bundle bundle, CmsTheme cmsTheme) {
		this.bundle = bundle;
		this.cmsTheme = cmsTheme;
		setLifeCycleUri("bundleclass://org.argeo.suite.e4.rap/org.argeo.suite.e4.rap.ArgeoSuiteLoginLifecycle");
		String contextName = "argeo/" + FilenameUtils.getExtension(bundle.getSymbolicName());
		setContextName(contextName);
	}

	@Override
	protected void addEntryPoints(Application application) {
		if (cmsTheme != null)
			cmsTheme.apply(application);

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
