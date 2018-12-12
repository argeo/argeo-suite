package org.argeo.suite.e4.rap;

import org.argeo.cms.e4.rap.CmsE4EntryPointFactory;

public class ArgeoSuiteEntryPoint extends CmsE4EntryPointFactory {

	public ArgeoSuiteEntryPoint(String e4Xmi) {
		super(e4Xmi, "bundleclass://org.argeo.suite.e4.rap/org.argeo.suite.e4.rap.ArgeoSuiteLoginLifecycle");
	}

}
