package org.argeo.suite.e4.rap;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.e4.rap.CmsLoginLifecycle;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;

public class ArgeoSuiteLoginLifecycle extends CmsLoginLifecycle {
	private final static Log log = LogFactory.getLog(ArgeoSuiteLoginLifecycle.class);
	@Inject
	SystemWorkbenchService systemWorkbenchService;

	@Inject
	Repository repository;

	@Override
	protected void startupComplete() {
		loadState();
	}

	@Override
	protected void stateChanged() {
		loadState();
	}

	private void loadState() {
		String state = getState();
		// for the time being we systematically open a session, in order to make sure
		// that home is initialised
		Session session = null;
		try {
			Subject subject = getSubject();
			session = Subject.doAs(subject, new PrivilegedExceptionAction<Session>() {

				@Override
				public Session run() throws PrivilegedActionException {
					try {
						return repository.login(NodeConstants.HOME);
					} catch (RepositoryException e) {
						throw new PrivilegedActionException(e);
					}
				}

			});
			if (state != null && state.startsWith("/")) {
				if (state.startsWith("/")) {
					Node node = session.getNode(state);
					systemWorkbenchService.openEntityEditor(node);
				}
			}
		} catch (RepositoryException | PrivilegedActionException e) {
			log.error("Cannot load state " + state, e);
			getBrowserNavigation().pushState("~", null);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}
}
