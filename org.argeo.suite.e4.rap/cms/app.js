// Standard CMS App
APP.webPath = 'suite'

// Common
APP.pageTitle = 'Argeo Suite';
APP.favicon = 'icons/argeo-e.png';
APP.theme = new org.argeo.cms.script.Theme(BC)

// Office
APP.ui['office'] = new org.argeo.cms.script.AppUi(APP,
		new org.argeo.suite.e4.rap.ArgeoSuiteEntryPoint(
				'org.argeo.suite.e4/e4xmi/argeo-office.e4xmi'));
APP.ui['office'].pageTitle = 'Office';
