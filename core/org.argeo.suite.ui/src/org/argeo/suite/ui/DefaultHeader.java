package org.argeo.suite.ui;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.Localized;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.CmsView;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

/** Header of a standard Argeo Suite application. */
public class DefaultHeader implements CmsUiProvider {
	public final static String TITLE_PROPERTY = "argeo.suite.ui.header.title";
	private Localized title = null;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		CmsView cmsView = CmsView.getCmsView(parent);
		CmsTheme theme = CmsTheme.getCmsTheme(parent);

		parent.setLayout(CmsUiUtils.noSpaceGridLayout(new GridLayout(3, true)));

		// TODO right to left
		Composite lead = new Composite(parent, SWT.NONE);
		CmsUiUtils.style(lead, SuiteStyle.header);
		lead.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, true, false));
		lead.setLayout(new GridLayout());
		Label lbl = new Label(lead, SWT.NONE);
//		String title = properties.get(TITLE_PROPERTY);
//		// TODO expose the localized
//		lbl.setText(LocaleUtils.isLocaleKey(title) ? LocaleUtils.local(title, getClass().getClassLoader()).toString()
//				: title);
		lbl.setText(title.lead());
		CmsUiUtils.style(lbl, SuiteStyle.headerTitle);
		lbl.setLayoutData(CmsUiUtils.fillWidth());

		Composite middle = new Composite(parent, SWT.NONE);
		CmsUiUtils.style(middle, SuiteStyle.header);
		middle.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		middle.setLayout(new GridLayout());

		Composite end = new Composite(parent, SWT.NONE);
		CmsUiUtils.style(end, SuiteStyle.header);
		end.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));

		if (!cmsView.isAnonymous()) {
			end.setLayout(new GridLayout(2, false));
			Label userL = new Label(end, SWT.NONE);
			CmsUiUtils.style(userL, SuiteStyle.header);
			userL.setText(CurrentUser.getDisplayName());
			Button logoutB = new Button(end, SWT.FLAT);
//			CmsUiUtils.style(logoutB, SuiteStyle.header);
			logoutB.setImage(SuiteIcon.logout.getSmallIcon(theme));
			logoutB.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 7116760083964201233L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					cmsView.logout();
				}

			});
		} else {
			end.setLayout(new GridLayout(1, false));
			// required in order to avoid wrong height after logout
			new Label(end, SWT.NONE).setText("");

		}
		return lbl;
	}

	public void init(BundleContext bundleContext, Map<String, String> properties) {
		String titleStr = (String) properties.get(TITLE_PROPERTY);
		if (titleStr != null) {
			if (titleStr.startsWith("%")) {
				title = new Localized() {

					@Override
					public String name() {
						return titleStr;
					}

					@Override
					public ClassLoader getL10nClassLoader() {
						return bundleContext != null
								? bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader()
								: getClass().getClassLoader();
					}
				};
			} else {
				title = new Localized.Untranslated(titleStr);
			}
		}
	}

	public void destroy(BundleContext bundleContext, Map<String, String> properties) {

	}

	public Localized getTitle() {
		return title;
	}

}
