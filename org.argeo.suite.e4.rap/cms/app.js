// Standard CMS App
APP.webPath = 'suite'

// Common
APP.pageTitle = 'Argeo Suite';
APP.favicon = 'icons/argeo-e.png';
APP.theme = new org.argeo.cms.ui.script.Theme(BC, 'org.argeo.theme.argeo2')

// Office
APP.ui['office'] = new org.argeo.cms.ui.script.AppUi(APP,
		new org.argeo.suite.e4.rap.ArgeoSuiteEntryPoint(
				'org.argeo.suite.e4/e4xmi/argeo-office.e4xmi'));
APP.ui['office'].pageTitle = 'Office';

// Docs
APP.ui['docs'] = new org.argeo.cms.ui.script.AppUi(APP,
		new org.argeo.suite.e4.rap.ArgeoSuiteEntryPoint(
				'org.argeo.suite.e4/e4xmi/argeo-docs.e4xmi'));
APP.ui['docs'].pageTitle = 'Docs';
