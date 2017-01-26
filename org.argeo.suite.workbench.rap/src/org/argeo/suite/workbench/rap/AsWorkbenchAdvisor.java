package org.argeo.suite.workbench.rap;

import org.argeo.cms.ui.workbench.rap.RapWorkbenchAdvisor;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/** Eclipse RAP specific workbench advisor */
public class AsWorkbenchAdvisor extends RapWorkbenchAdvisor {

	private String username;

	@Override
	public void postStartup() {
		super.postStartup();
		// ExitConfirmation confirmation = RWT.getClient().getService(
		// ExitConfirmation.class);
		// confirmation.setMessage("Are you sure you want to leave the page? "
		// + "All un-saved information will be lost.");
	}

	public void postShutdown() {
		super.postShutdown();
		// ExitConfirmation confirmation = RWT.getClient().getService(
		// ExitConfirmation.class);
		// if (confirmation != null)
		// confirmation.setMessage(null);
	}

	public AsWorkbenchAdvisor(String username) {
		super(username);
		this.username = username;
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new AsWindowAdvisor(configurer, username);
	}
}
