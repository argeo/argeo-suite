
package org.argeo.suite.e4.rap;

import org.argeo.cms.e4.rap.AbstractRapE4App;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.client.WebClient;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/** Argeo Office RAP application. */
public class ArgeoOfficeRapE4App extends AbstractRapE4App {
	private BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();

	public ArgeoOfficeRapE4App() {
//		setPageTitle("Argeo Office");
//		setE4Xmi("org.argeo.suite.e4/e4xmi/argeo-office.e4xmi");
//		setPath("/office");
		setLifeCycleUri("bundleclass://org.argeo.suite.e4.rap/org.argeo.suite.e4.rap.ArgeoSuiteLoginLifecycle");
	}

	@Override
	protected void addEntryPoints(Application application) {
//		CmsTheme cmsTheme = new CmsTheme(bc, "org.argeo.theme.argeo2");
//		cmsTheme.apply(application);

		String font = "<link rel='stylesheet' href='http://fonts.googleapis.com/css?family=Source+Sans+Pro'/>";
		getBaseProperties().put(WebClient.HEAD_HTML, font);

		addE4EntryPoint(application, "/office", "org.argeo.suite.e4/e4xmi/argeo-office.e4xmi", customise("Office"));
		addE4EntryPoint(application, "/docs", "org.argeo.suite.e4/e4xmi/argeo-docs.e4xmi", customise("Docs"));
	}

//	protected void addEntryPoint(Application application, E4ApplicationConfig config, Map<String, String> properties) {
//		// String theme ="org.argeo.theme.argeo2.office";
//		String theme = RWT.DEFAULT_THEME_ID;
//		Bundle themeBundle = findTheme("org.argeo.theme.argeo2");
//		// application.addStyleSheet(theme, "rap/office-rwt.css", new
//		// BundleResourceLoader(themeBundle));
//		application.addStyleSheet(theme, "rap/office.css", new BundleResourceLoader(themeBundle));
//		properties.put(WebClient.THEME_ID, theme);
//		String font = "<link rel='stylesheet' href='http://fonts.googleapis.com/css?family=Source+Sans+Pro'/>";
//		properties.put(WebClient.HEAD_HTML, font);
//		//super.addEntryPoint(application, config, properties);
//	}

}
