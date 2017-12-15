package org.argeo.suite.workbench.rap;

import org.argeo.cms.ui.workbench.rap.RapWindowAdvisor;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.workbench.commands.OpenDefaultEditor;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

/** Eclipse RAP specific window advisor */
public class AsWindowAdvisor extends RapWindowAdvisor {

	private String username;

	public AsWindowAdvisor(IWorkbenchWindowConfigurer configurer, String username) {
		super(configurer, username);
		this.username = username;
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new AsActionBarAdvisor(configurer, username);
	}

	@Override
	public void preWindowOpen() {
		super.preWindowOpen();
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowStatusLine(true);
		configurer.setShowPerspectiveBar(false);
		configurer.setShowMenuBar(false);
	}

	@Override
	public void postWindowOpen() {
		super.postWindowOpen();
		try {
			CommandUtils.callCommand(OpenDefaultEditor.ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
