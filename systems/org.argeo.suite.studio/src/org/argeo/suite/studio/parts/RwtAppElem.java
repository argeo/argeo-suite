package org.argeo.suite.studio.parts;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.osgi.framework.ServiceReference;

/** Site map element representing an RWT application. */
class RwtAppElem extends SiteElem {
	private final static String CONTEXT_NAME = "contextName";

	private final ServiceReference<ApplicationConfiguration> serviceReference;

	public RwtAppElem(ServiceReference<ApplicationConfiguration> serviceReference) {
		super(serviceReference.getProperties().get(CONTEXT_NAME).toString());
		this.serviceReference = serviceReference;
	}

	public ServiceReference<ApplicationConfiguration> getServiceReference() {
		return serviceReference;
	}

}
