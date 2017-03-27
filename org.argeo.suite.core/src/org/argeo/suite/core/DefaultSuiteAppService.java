package org.argeo.suite.core;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.AppService;
import org.argeo.connect.SystemAppService;
import org.argeo.suite.SuiteConstants;
import org.argeo.suite.SuiteException;

public class DefaultSuiteAppService implements SystemAppService {

	// Injected known AppWorkbenchServices: order is important, first found
	// result will be returned by the various methods.
	private List<AppService> knownAppServices;

	@Override
	public Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(nodeType))
				return appService.publishEntity(parent, nodeType, srcNode, removeSrcNode);
		}
		return null;
	}

	@Override
	public String getAppBaseName() {
		return SuiteConstants.SUITE_APP_BASE_NAME;
	}

	@Override
	public String getBaseRelPath(String nodeType) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(nodeType))
				return appService.getBaseRelPath(nodeType);
		}
		return null;
		// return getAppBaseName();
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(entity))
				return appService.getDefaultRelPath(entity);
		}
		return null;
	}

	@Override
	public String getDefaultRelPath(Session session, String nodetype, String id) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(nodetype))
				return appService.getDefaultRelPath(session, nodetype, id);
		}
		return null;
	}

	/** Insures the correct service is called on save */
	@Override
	public Node saveEntity(Node entity, boolean publish) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(entity))
				return appService.saveEntity(entity, publish);
		}
		throw new SuiteException("Unknown NodeType for " + entity + ". Cannot save");
		// return AppService.super.saveEntity(entity, publish);
	}

	@Override
	public boolean isKnownType(Node entity) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(entity))
				return true;
		}
		return false;
	}

	@Override
	public boolean isKnownType(String nodeType) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(nodeType))
				return true;
		}
		return false;
	}

	public void setKnownAppServices(List<AppService> knownAppServices) {
		this.knownAppServices = knownAppServices;
	}

}
