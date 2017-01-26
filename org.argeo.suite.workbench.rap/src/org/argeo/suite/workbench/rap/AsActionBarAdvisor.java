package org.argeo.suite.workbench.rap;

import org.argeo.cms.ui.workbench.rap.RapActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/** Eclipse rap specific action bar advisor */
public class AsActionBarAdvisor extends RapActionBarAdvisor {

	public AsActionBarAdvisor(IActionBarConfigurer configurer, String username) {
		super(configurer, username);
	}
}
