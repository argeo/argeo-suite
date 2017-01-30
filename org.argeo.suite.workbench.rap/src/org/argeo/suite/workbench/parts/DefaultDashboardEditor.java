package org.argeo.suite.workbench.parts;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.workbench.rap.PeopleRapUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.suite.workbench.AsUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/** Argeo Suite Default Dashboard */
public class DefaultDashboardEditor extends AbstractSuiteDashboard {
	final static Log log = LogFactory.getLog(DefaultDashboardEditor.class);
	public final static String ID = AsUiPlugin.PLUGIN_ID + ".defaultDashboardEditor";

	// Default gadget dimensions
	private int wh = 300;
	private int hh = 350;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		parent.setLayout(new GridLayout());
		// Main Layout
		Composite body = getFormToolkit().createComposite(parent);
		body.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		GridLayout bodyLayout = new GridLayout(2, true);
		bodyLayout.horizontalSpacing = 20;
		bodyLayout.verticalSpacing = 20;
		body.setLayout(bodyLayout);

		// Project List
		Composite projectsGadget = createGadgetCmp(body, wh, hh);
		populateProjectsGadget(projectsGadget);

		// Contacts
		Composite contactGadget = createGadgetCmp(body, wh, hh);
		populateContactsGadget(contactGadget);

	}

	/** Links to the various projects */
	private void populateProjectsGadget(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		createGadgetTitleCmp(parent, "Projects");
		Composite bodyCmp = createGadgetBodyCmp(parent);

		// // TODO enhance this
		// NodeIterator nit = AoUtils.listNodesOfType(getSession(),
		// AoTypes.OFFICE_ACCOUNT,
		// getAoService().getBasePath(AoTypes.OFFICE_ACCOUNT));
		// while (nit.hasNext()) {
		// Node account = nit.nextNode();
		// PeopleRapUtils.createOpenEntityEditorLink(getAoWbService(), bodyCmp,
		// ConnectJcrUtils.get(account, Property.JCR_TITLE), account);
		// }
		//
		// PeopleWorkbenchService aoWbSrv = getAoWbService();
		// // Opens a lits of all projects
		//
		// PeopleRapUtils.createOpenSearchEditorLink(aoWbSrv, bodyCmp, "All
		// projects", TrackerTypes.TRACKER_PROJECT,
		// AoConstants.ACCOUNTS_BASE_PATH);
	}

	/** Links to the various contact search pages */
	private void populateContactsGadget(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		createGadgetTitleCmp(parent, "Contacts");
		Composite bodyCmp = createGadgetBodyCmp(parent);
		
		PeopleRapUtils.createOpenSearchEditorLink(getPeopleWorkbenchService(), bodyCmp, "Persons",
				PeopleTypes.PEOPLE_PERSON, getPeopleService().getBasePath(PeopleTypes.PEOPLE_PERSON));

		PeopleRapUtils.createOpenSearchEditorLink(getPeopleWorkbenchService(), bodyCmp, "Organisations",
				PeopleTypes.PEOPLE_ORG, getPeopleService().getBasePath(PeopleTypes.PEOPLE_ORG));

		Node tagParent = getPeopleService().getResourceService().getTagLikeResourceParent(getSession(),
				PeopleTypes.PEOPLE_MAILING_LIST);
		PeopleRapUtils.createOpenSearchEditorLink(getPeopleWorkbenchService(), bodyCmp, "Mailing lists",
				PeopleTypes.PEOPLE_MAILING_LIST, ConnectJcrUtils.getPath(tagParent));
		
		PeopleRapUtils.createOpenSearchEditorLink(getPeopleWorkbenchService(), bodyCmp, "Tasks",
				PeopleTypes.PEOPLE_TASK, getPeopleService().getBasePath(null));
		
		tagParent = getPeopleService().getResourceService().getTagLikeResourceParent(getSession(),
				PeopleConstants.RESOURCE_TAG);
		
		PeopleRapUtils.createOpenSearchEditorLink(getPeopleWorkbenchService(), bodyCmp, "Tags",
				PeopleTypes.PEOPLE_TAG_INSTANCE, ConnectJcrUtils.getPath(tagParent));

	}
}
