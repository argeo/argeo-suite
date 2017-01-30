package org.argeo.suite.workbench.parts;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.suite.workbench.AsUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/** Basic view to test plugin */
public class TestView extends ViewPart {
	public static final String ID = AsUiPlugin.PLUGIN_ID + ".testView";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Session session;

	@Override
	public void createPartControl(Composite parent) {
		// Finalise initialisation
		session = ConnectJcrUtils.login(repository);

		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		parent.setLayout(layout);

		new Label(parent, SWT.NONE).setText("Test view shown.");
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}
