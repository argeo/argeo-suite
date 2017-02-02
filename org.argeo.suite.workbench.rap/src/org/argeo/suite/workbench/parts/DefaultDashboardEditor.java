package org.argeo.suite.workbench.parts;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.workbench.rap.PeopleRapUtils;
import org.argeo.connect.ui.workbench.Refreshable;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.suite.workbench.AsUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/** Argeo Suite Default Dashboard */
public class DefaultDashboardEditor extends AbstractSuiteDashboard implements Refreshable {
	final static Log log = LogFactory.getLog(DefaultDashboardEditor.class);
	public final static String ID = AsUiPlugin.PLUGIN_ID + ".defaultDashboardEditor";

	// Default gadget dimensions
	private int wh = 300;
	private int hh = 350;

	private Composite projectsGadget;

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

		// Last updated doc List
		projectsGadget = createGadgetCmp(body, wh, hh);
		// refreshDocListGadget(projectsGadget);

		// Contacts
		Composite contactGadget = createGadgetCmp(body, wh, hh);
		populateContactsGadget(contactGadget);
	}

	@Override
	public void forceRefresh(Object object) {
		refreshDocListGadget();
	}

	@Override
	public void setFocus() {
		refreshDocListGadget();
	}

	/** Links to the various last updated docs */
	private void refreshDocListGadget() {
		EclipseUiUtils.clear(projectsGadget);
		projectsGadget.setLayout(EclipseUiUtils.noSpaceGridLayout());
		createGadgetTitleCmp(projectsGadget, "Last updated documents");
		Composite bodyCmp = createGadgetBodyCmp(projectsGadget);

		NodeIterator nit = getDocumentsService().getLastUpdatedDocuments(getSession());
		while (nit.hasNext()) {
			Node file = nit.nextNode();
			createOpenEntityEditorLink(getAppWorkbenchService(), bodyCmp, ConnectJcrUtils.getName(file), file);
		}
		projectsGadget.layout(true, true);
	}

	/** Links to the various contact search pages */
	private void populateContactsGadget(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		createGadgetTitleCmp(parent, "Contacts");
		Composite bodyCmp = createGadgetBodyCmp(parent);

		PeopleRapUtils.createOpenSearchEditorLink(getAppWorkbenchService(), bodyCmp, "Persons",
				PeopleTypes.PEOPLE_PERSON, getPeopleService().getBasePath(PeopleTypes.PEOPLE_PERSON));

		PeopleRapUtils.createOpenSearchEditorLink(getAppWorkbenchService(), bodyCmp, "Organisations",
				PeopleTypes.PEOPLE_ORG, getPeopleService().getBasePath(PeopleTypes.PEOPLE_ORG));

		Node tagParent = getPeopleService().getResourceService().getTagLikeResourceParent(getSession(),
				PeopleTypes.PEOPLE_MAILING_LIST);
		PeopleRapUtils.createOpenSearchEditorLink(getAppWorkbenchService(), bodyCmp, "Mailing lists",
				PeopleTypes.PEOPLE_MAILING_LIST, ConnectJcrUtils.getPath(tagParent));

		PeopleRapUtils.createOpenSearchEditorLink(getAppWorkbenchService(), bodyCmp, "Tasks", PeopleTypes.PEOPLE_TASK,
				getPeopleService().getBasePath(null));

		tagParent = getPeopleService().getResourceService().getTagLikeResourceParent(getSession(),
				PeopleConstants.RESOURCE_TAG);

		PeopleRapUtils.createOpenSearchEditorLink(getAppWorkbenchService(), bodyCmp, "Tags",
				PeopleTypes.PEOPLE_TAG_INSTANCE, ConnectJcrUtils.getPath(tagParent));

	}
}
