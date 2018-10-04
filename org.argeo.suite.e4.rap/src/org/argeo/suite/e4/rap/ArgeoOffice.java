package org.argeo.suite.e4.rap;

import org.argeo.cms.e4.rap.CmsE4EntryPointFactory;

public class ArgeoOffice extends CmsE4EntryPointFactory {

	public ArgeoOffice(String e4Xmi, String lifeCycleUri) {
		super(e4Xmi, lifeCycleUri);
	}

	public ArgeoOffice() {
		super("org.argeo.suite.e4/e4xmi/argeo-office.e4xmi",
				"bundleclass://org.argeo.suite.e4.rap/org.argeo.suite.e4.rap.ArgeoSuiteLoginLifecycle");
	}

}
