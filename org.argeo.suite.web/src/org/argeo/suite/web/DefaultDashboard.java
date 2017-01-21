package org.argeo.suite.web;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.web.pages.OrgPage;
import org.argeo.connect.people.web.parts.PeopleSearchCmp;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Default dashboard layout for Argeo Suite */
public class DefaultDashboard implements CmsUiProvider {

	private PeopleService peopleService;
	private Map<String, String> peopleIconPaths;

	// Local UI Providers
	private CmsUiProvider orgPage;

	public DefaultDashboard(PeopleService peopleService, Map<String, String> peopleIconPaths) {
		this.peopleService = peopleService;
		this.peopleIconPaths = peopleIconPaths;

		orgPage = new OrgPage(peopleService);
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		form.setLayoutData(EclipseUiUtils.fillAll());
		Composite leftPannelCmp = new Composite(form, SWT.NO_FOCUS);
		Composite rightPannelCmp = new Composite(form, SWT.NO_FOCUS);
		form.setWeights(new int[] { 2, 5 });

		// A search on the left and the display on the right
		populateSearch(leftPannelCmp, context, rightPannelCmp);
		populateDefaultDisplay(rightPannelCmp, context);

		return form;
	}

	public Viewer populateSearch(Composite parent, Node context, final Composite targetComposite)
			throws RepositoryException {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite titleCmp = new Composite(parent, SWT.NO_FOCUS);
		titleCmp.setLayoutData(EclipseUiUtils.fillWidth());
		titleCmp.setLayout(new GridLayout());
		Label titleLbl = new Label(titleCmp, SWT.CENTER);
		titleLbl.setLayoutData(EclipseUiUtils.fillWidth());
		titleLbl.setText("My Tasks");
		titleLbl.setFont(EclipseUiUtils.getBoldFont(titleCmp));

		PeopleSearchCmp searchComp = new PeopleSearchCmp(parent, SWT.NO_FOCUS, peopleService, peopleIconPaths);
		searchComp.populate(context, true);
		searchComp.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer = searchComp.getViewer();
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object firstObj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				try {
					Node node = (Node) firstObj;
					String path = node.getPath();
					CmsUtils.getCmsView().navigateTo(path);
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to refresh display for " + context, e);
				}
			}
		});
		return null;
	}

	public Control populateDefaultDisplay(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText("Implement a default display");
		return lbl;
	}
}
