package org.argeo.suite.workbench.rap;

import org.argeo.cms.ui.workbench.rap.RapWorkbenchAdvisor;
import org.argeo.cms.ui.workbench.rap.RapWorkbenchLogin;
import org.argeo.cms.util.CmsUtils;
import org.argeo.cms.widgets.auth.CmsLogin;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This class controls all aspects of the application's execution and is
 * contributed through the plugin.xml.
 */
public class AsSecureEntryPoint extends RapWorkbenchLogin {

	/** Override to provide an application specific workbench advisor */
	protected RapWorkbenchAdvisor createRapWorkbenchAdvisor(String username) {
		return new AsWorkbenchAdvisor(username);
	}

	protected void createLoginPage(Composite parent, CmsLogin login) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// Main layout
		Composite bodyCmp = new Composite(parent, SWT.NO_FOCUS);
		bodyCmp.setLayoutData(EclipseUiUtils.fillAll());
		GridLayout gl = new GridLayout();
		gl.marginHeight = 25;
		gl.marginWidth = 40;
		bodyCmp.setLayout(gl);

		// Logo
		Label headerLbl = new Label(bodyCmp, SWT.WRAP);
		CmsUtils.markup(headerLbl);
		// Images are declared via the resources extension point in plugin.xml
		String headerStr = "<a href=\"http://argeo.org\" "
				+ "title=\"Smart Data Productivity Suite, by Argeo\" target=\"_blank\"> "
				+ "<img src=\"/ui/suite/img/logo.jpg\" width=\"200\" height=\"140\"></img> " + "</a>";
		headerLbl.setText(headerStr);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		headerLbl.setLayoutData(gd);

		// Title
		Label titleLbl = new Label(bodyCmp, SWT.WRAP | SWT.CENTER);
		CmsUtils.markup(titleLbl);
		String titleStr = "<big> Please sign in to your personal dashboard</big>";
		titleLbl.setText(titleStr);
		gd = new GridData(SWT.CENTER, SWT.BOTTOM, false, false);
		gd.verticalIndent = 80;
		titleLbl.setLayoutData(gd);

		// Login composite
		Composite loginCmp = login.createCredentialsBlock(bodyCmp);
		gd = new GridData(SWT.CENTER, SWT.TOP, true, true);
		gd.widthHint = 200;
		gd.verticalIndent = 15;
		loginCmp.setLayoutData(gd);

		// Footer
		Label footerLbl = new Label(bodyCmp, SWT.WRAP | SWT.CENTER);
		CmsUtils.markup(footerLbl);
		String footerStr = "<small>SDPS is a private service. <br/>"
				+ " Please <a href=\"mailto:contact@argeo.org\">contact us</a> if you have any question.</small>";
		footerLbl.setText(footerStr);
		footerLbl.setLayoutData(EclipseUiUtils.fillWidth());
	}
}
