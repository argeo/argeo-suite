package org.argeo.app.ui.people;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.cms.directory.CmsUserManager;
import org.argeo.api.cms.directory.HierarchyUnit;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.dialogs.CmsMessageDialog;
import org.argeo.cms.swt.widgets.SwtTableView;
import org.argeo.cms.swt.widgets.SwtTreeView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/** Pick up a user within a hierarchy. */
public class ChooseUserDialog extends CmsMessageDialog {
	private ContentSession contentSession;
	private CmsUserManager cmsUserManager;
	private HierarchyUnit defaultHierarchyUnit;

	private Content selected;

	public ChooseUserDialog(Shell parentShell, String message, ContentSession contentSession,
			CmsUserManager cmsUserManager, HierarchyUnit defaultHierarchyUnit) {
		super(parentShell, message, CmsMessageDialog.QUESTION);
		this.contentSession = contentSession;
		this.cmsUserManager = cmsUserManager;
		this.defaultHierarchyUnit = defaultHierarchyUnit;
	}

	@Override
	protected Control createInputArea(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		CmsSwtUtils.fill(sashForm);

		HierarchyUnitPart hierarchyPart = new HierarchyUnitPart(contentSession, cmsUserManager);
		SwtTreeView<HierarchyUnit> directoriesView = new SwtTreeView<>(sashForm, SWT.BORDER, hierarchyPart);

		UsersPart usersPart = new UsersPart(contentSession, cmsUserManager);

		SwtTableView<?, ?> usersView = new SwtTableView<>(sashForm, SWT.BORDER, usersPart);
		usersView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// CONTROLLER
		hierarchyPart.onSelected((o) -> {
			if (o instanceof HierarchyUnit) {
				HierarchyUnit hierarchyUnit = (HierarchyUnit) o;
				usersPart.setInput(hierarchyUnit);
			}
		});

		usersPart.onSelected((o) -> {
			Content user = (Content) o;
			selected = user;
		});

		hierarchyPart.refresh();

		sashForm.setWeights(new int[] { 40, 60 });
		return sashForm;
	}

	public Content getSelected() {
		return selected;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

}
