package org.argeo.suite.studio.parts;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.Servlet;

import org.argeo.eclipse.ui.AbstractTreeContentProvider;
import org.eclipse.e4.core.di.extensions.OSGiBundle;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/** Overview of the URL paths provides by the system. */
@SuppressWarnings("restriction")
public class SiteMapPart {
//	private final static BundleContext bc = FrameworkUtil.getBundle(SiteMapPart.class).getBundleContext();

	private TreeViewer viewer;

	@Inject
	@OSGiBundle
	BundleContext bc;

	@PostConstruct
	public void createPartControl(Composite parent) {
//		new Label(parent, SWT.NONE).setText("TEST");
//		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		viewer.getTree().setHeaderVisible(true);

//		viewer.setLabelProvider(new ColumnLabelProvider());

		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Path");
		column.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = -3122136344359358605L;

			public String getText(Object element) {
				return ((SiteElem) element).getPath();
			}

			@Override
			public Image getImage(Object element) {
				return super.getImage(element);
			}

		});

		viewer.setContentProvider(new SitePathContentProvider());
		viewer.setInput(bc);

	}

	class SitePathContentProvider extends AbstractTreeContentProvider {
		private static final long serialVersionUID = -5650173256183322051L;

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof BundleContext) {
				BundleContext bundleContext = (BundleContext) inputElement;
				Map<String, SiteElem> siteElems = new TreeMap<>();
				try {
					Collection<ServiceReference<ApplicationConfiguration>> rwtApps = bundleContext
							.getServiceReferences(ApplicationConfiguration.class, null);
					for (ServiceReference<ApplicationConfiguration> sr : rwtApps) {
						RwtAppElem elem = new RwtAppElem(sr);
						siteElems.put(elem.getPath(), elem);
					}
					Collection<ServiceReference<Servlet>> plainServlets = bundleContext
							.getServiceReferences(Servlet.class, null);
					for (ServiceReference<Servlet> sr : plainServlets) {
						ServletElem elem = new ServletElem(sr);
						siteElems.put(elem.getPath(), elem);
					}
				} catch (InvalidSyntaxException e) {
					throw new IllegalArgumentException(e);
				}
				return siteElems.values().toArray();
			}
			return null;
		}

	}
}
