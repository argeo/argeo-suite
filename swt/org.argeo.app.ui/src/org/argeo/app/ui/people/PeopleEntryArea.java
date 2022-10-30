package org.argeo.app.ui.people;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentRepository;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.cms.ux.CmsIcon;
import org.argeo.api.cms.ux.CmsView;
import org.argeo.app.ui.SuiteUxEvent;
import org.argeo.app.ui.SuiteIcon;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.auth.CmsRole;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.auth.UserAdminUtils;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.acr.SwtUiProvider;
import org.argeo.cms.swt.widgets.SwtGuidedFormDialog;
import org.argeo.cms.swt.widgets.SwtTableView;
import org.argeo.cms.swt.widgets.SwtTreeView;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ux.widgets.AbstractHierarchicalPart;
import org.argeo.cms.ux.widgets.Column;
import org.argeo.cms.ux.widgets.DefaultTabularPart;
import org.argeo.cms.ux.widgets.GuidedForm;
import org.argeo.cms.ux.widgets.HierarchicalPart;
import org.argeo.osgi.useradmin.UserDirectory;
import org.argeo.util.directory.HierarchyUnit;
import org.argeo.util.directory.ldap.IpaUtils;
import org.argeo.util.naming.LdapAttrs;
import org.argeo.util.naming.LdapObjs;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/** Entry to the admin area. */
public class PeopleEntryArea implements SwtUiProvider, CmsUiProvider {

	private CmsUserManager cmsUserManager;

	private ContentRepository contentRepository;

	@Override
	public Control createUiPart(Composite parent, Content context) {
		CmsSwtTheme theme = CmsSwtUtils.getCmsTheme(parent);
		CmsView cmsView = CmsSwtUtils.getCmsView(parent);
		parent.setLayout(new GridLayout());

		ContentSession contentSession = contentRepository.get();
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		CmsSwtUtils.fill(sashForm);

		// MODEL
//		List<UserDirectory> directories = new ArrayList<>();
//		// List<User> orgs = cmsUserManager.listGroups(null, true, false);
//		for (UserDirectory directory : cmsUserManager.getUserDirectories()) {
//			if (CurrentUser.implies(CmsRole.userAdmin, directory.getContext())) {
//				directories.add(directory);
//			}
//
//		}

		// VIEW
		HierarchicalPart<HierarchyUnit> hierarchyPart = new AbstractHierarchicalPart<>() {

			@Override
			public List<HierarchyUnit> getChildren(HierarchyUnit parent) {
				List<HierarchyUnit> visible = new ArrayList<>();
				if (parent != null) {
					for (HierarchyUnit hu : parent.getDirectHierarchyUnits(true)) {
						// if parent was visible, it is visible
						// TODO restrict more?

//						if (CurrentUser.implies(CmsRole.userAdmin, hu.getBase()) //
//						) // IPA
//						{
						visible.add(hu);
//						}
					}
				} else {
					for (UserDirectory directory : cmsUserManager.getUserDirectories()) {
						if (CurrentUser.implies(CmsRole.userAdmin, directory.getBase()) //
								|| CurrentUser.implies(CmsRole.userAdmin,
										IpaUtils.IPA_ACCOUNTS_RDN + "," + directory.getBase())) // IPA
						{
							// TODO show base level
						}
						for (HierarchyUnit hu : directory.getDirectHierarchyUnits(true)) {
							if (CurrentUser.implies(CmsRole.userAdmin, hu.getBase())) {
								visible.add(hu);
							}
						}

					}
				}
				return visible;
			}

			@Override
			public String getText(HierarchyUnit model) {
				return model.getHierarchyUnitLabel(CurrentUser.locale());
			}

			@Override
			public CmsIcon getIcon(HierarchyUnit model) {
				Content content = ContentUtils.hierarchyUnitToContent(contentSession, model);
				if (content.hasContentClass(LdapObjs.organization.qName()))
					return SuiteIcon.organisation;
				else if (content.hasContentClass(LdapObjs.posixGroup.qName()))
					return SuiteIcon.users;
				else
					return SuiteIcon.addressBook;
			}

		};
		SwtTreeView<HierarchyUnit> directoriesView = new SwtTreeView<>(sashForm, SWT.BORDER, hierarchyPart);

		DefaultTabularPart<HierarchyUnit, Content> usersPart = new DefaultTabularPart<>() {

			@Override
			protected List<Content> asList(HierarchyUnit hu) {
				List<Content> roles = new ArrayList<>();
				UserDirectory ud = (UserDirectory) hu.getDirectory();
				if (ud.getRealm().isPresent()) {
					for (Role r : ud.getHierarchyUnitRoles(ud, null, true)) {
						Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
						// if (r instanceof Person || r instanceof Organization)
						if (content.hasContentClass(LdapObjs.inetOrgPerson.qName(), LdapObjs.organization.qName()))
							roles.add(content);
					}

				} else {
					for (HierarchyUnit directChild : hu.getDirectHierarchyUnits(false)) {
						if (!directChild.isFunctional()) {
							for (Role r : ud.getHierarchyUnitRoles(directChild, null, false)) {
								Content content = ContentUtils.roleToContent(cmsUserManager, contentSession, r);
								// if (r instanceof Person || r instanceof Organization)
								if (content.hasContentClass(LdapObjs.inetOrgPerson.qName(),
										LdapObjs.organization.qName(), LdapObjs.groupOfNames.qName()))
									roles.add(content);
							}
						}
					}
				}
				return roles;
			}
		};
		usersPart.addColumn(new Column<Content>() {

			@Override
			public String getText(Content role) {
				if (role.isContentClass(LdapObjs.inetOrgPerson.qName()))
					return UserAdminUtils.getUserDisplayName(role.adapt(User.class));
				else if (role.isContentClass(LdapObjs.organization.qName()))
					return role.attr(LdapAttrs.o.qName());
				else if (role.isContentClass(LdapObjs.groupOfNames.qName()))
					return role.attr(LdapAttrs.cn.qName());
				else
					return null;
			}

			@Override
			public CmsIcon getIcon(Content role) {
				if (role.hasContentClass(LdapObjs.posixAccount.qName()))
					return SuiteIcon.user;
				else if (role.isContentClass(LdapObjs.inetOrgPerson.qName()))
					return SuiteIcon.person;
				else if (role.isContentClass(LdapObjs.organization.qName()))
					return SuiteIcon.organisationContact;
				else if (role.isContentClass(LdapObjs.groupOfNames.qName()))
					return SuiteIcon.group;
				else
					return null;
			}

			@Override
			public int getWidth() {
				return 300;
			}

		});
		usersPart.addColumn((Column<Content>) (role) -> role.attr(LdapAttrs.mail.qName()));

		SwtTableView<HierarchyUnit, Content> usersView = new SwtTableView<>(sashForm, SWT.BORDER, usersPart);

		// toolbar
		Composite bottom = new Composite(parent, SWT.NONE);
		bottom.setLayoutData(CmsSwtUtils.fillWidth());
		bottom.setLayout(CmsSwtUtils.noSpaceGridLayout());
		ToolBar bottomToolBar = new ToolBar(bottom, SWT.NONE);
		bottomToolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
		ToolItem deleteItem = new ToolItem(bottomToolBar, SWT.FLAT);
		deleteItem.setEnabled(false);
//		CmsUiUtils.style(deleteItem, SuiteStyle.recentItems);
		deleteItem.setImage(theme.getSmallIcon(SuiteIcon.delete));
		ToolItem addItem = new ToolItem(bottomToolBar, SWT.FLAT);
		addItem.setImage(theme.getSmallIcon(SuiteIcon.add));

		sashForm.setWeights(new int[] { 30, 70 });

		// CONTROLLER
		hierarchyPart.onSelected((o) -> {
			if (o instanceof HierarchyUnit) {
				HierarchyUnit hierarchyUnit = (HierarchyUnit) o;
				usersPart.setInput(hierarchyUnit);
				cmsView.sendEvent(SuiteUxEvent.refreshPart.topic(),
						SuiteUxEvent.eventProperties(ContentUtils.hierarchyUnitToContent(contentSession, hierarchyUnit)));
			}
		});

		usersPart.onSelected((o) -> {
			Content user = (Content) o;
			if (user != null) {
				cmsView.sendEvent(SuiteUxEvent.refreshPart.topic(), SuiteUxEvent.eventProperties(user));
				deleteItem.setEnabled(true);
			} else {
				deleteItem.setEnabled(false);
			}
		});

		usersPart.onAction((o) -> {
			Content user = (Content) o;
			if (user != null) {
				cmsView.sendEvent(SuiteUxEvent.openNewPart.topic(), SuiteUxEvent.eventProperties(user));
			}
		});

		addItem.addSelectionListener((Selected) (e) -> {
			HierarchyUnit hierarchyUnit = usersPart.getInput();
			Content huContent = ContentUtils.hierarchyUnitToContent(contentSession, hierarchyUnit);
			GuidedForm wizard = new NewUserForm(cmsUserManager, huContent);
			SwtGuidedFormDialog dialog = new SwtGuidedFormDialog(parent.getShell(), wizard);
			// WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() == Window.OK) {
				// TODO create
			}
		});

		directoriesView.refresh();
//		usersView.refresh();

		return sashForm;
	}

//	static String getProperty(Role role, LdapAttrs attr) {
//		Object value = role.getProperties().get(attr.name());
//		return value != null ? value.toString() : null;
//	}

//	private boolean isOrganisation(Role role) {
//		String[] objectClasses = role.getProperties().get(LdapAttrs.objectClasses.name()).toString().split("\\n");
//		for (String objectClass : objectClasses) {
//			if (LdapObjs.organization.name().equalsIgnoreCase(objectClass))
//				return true;
//		}
//		return false;
//	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		return createUiPart(parent, JcrContent.nodeToContent(context));
	}

	public void setContentRepository(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

}
