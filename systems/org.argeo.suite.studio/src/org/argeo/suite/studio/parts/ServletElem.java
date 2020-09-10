package org.argeo.suite.studio.parts;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.Servlet;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/** Site map element which is a plain servlet. */
class ServletElem extends SiteElem {
	private final ServiceReference<Servlet> serviceReference;

	public ServletElem(ServiceReference<Servlet> serviceReference) {
		super(extractPath(serviceReference));
		this.serviceReference = serviceReference;
	}
	
	

	public ServiceReference<Servlet> getServiceReference() {
		return serviceReference;
	}



	private static String extractPath(ServiceReference<Servlet> serviceReference) {
		Object servletPattern = serviceReference.getProperties()
				.get(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN);
		Object servletContextSelect = serviceReference.getProperties()
				.get(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT);
		String servletContextPath = "";
		if (servletContextSelect != null) {
			try {
				Collection<ServiceReference<ServletContextHelper>> scSr = serviceReference.getBundle()
						.getBundleContext()
						.getServiceReferences(ServletContextHelper.class, servletContextSelect.toString());
				if (scSr.size() > 0) {
					servletContextPath = scSr.iterator().next()
							.getProperty(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH).toString();
					if (servletContextPath.equals("/"))// default servlet context
						servletContextPath = "";
				}
			} catch (InvalidSyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}

		if (servletPattern instanceof String)
			return servletContextPath + servletPattern.toString();
		else if (servletPattern instanceof String[]) {
			String[] patterns = (String[]) servletPattern;
			if (patterns.length == 2 && patterns[1].equals(patterns[0] + "/*")) // RAP servlets
				return servletContextPath + patterns[0];
			return servletContextPath + Arrays.asList(patterns).toString();
		} else
			throw new IllegalArgumentException("Cannot interpret servlet pattern " + servletPattern.getClass());
	}
}
