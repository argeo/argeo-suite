package org.argeo.suite.e4.rap;

import org.argeo.cms.e4.rap.AbstractRapE4App;

public class ArgeoOfficeRapE4App extends AbstractRapE4App {

	public ArgeoOfficeRapE4App() {
		setPageTitle("Argeo Office");
		setE4Xmi("org.argeo.suite.e4/e4xmi/argeo-office.e4xmi");
		setPath("/office");
		setLifeCycleUri("bundleclass://org.argeo.suite.e4.rap/org.argeo.suite.e4.rap.ArgeoSuiteLoginLifecycle");
	}

}
