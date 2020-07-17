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

/** Extends the CMS login lifecycle by managing the state of the current context. */
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
			if (state != null && state.startsWith("/")) {
				String path = state.substring(1);
				String workspace;
				if (path.equals("")) {
					workspace = null;
					path = "/";
				} else {
					int index = path.indexOf('/');
					if (index == 0) {
						log.error("Cannot interpret // " + state);
						getBrowserNavigation().pushState("~", null);
						return;
					} else if (index > 0) {
						workspace = path.substring(0, index);
						path = path.substring(index);
					} else {// index<0, assuming root node
						workspace = path;
						path = "/";
					}
				}
				Subject subject = getSubject();
				session = Subject.doAs(subject, new PrivilegedExceptionAction<Session>() {

					@Override
					public Session run() throws PrivilegedActionException {
						try {
							return repository.login(workspace);
						} catch (RepositoryException e) {
							throw new PrivilegedActionException(e);
						}
					}

				});
				Node node = session.getNode(path);
				systemWorkbenchService.openEntityEditor(node);
			}
		} catch (RepositoryException | PrivilegedActionException e) {
			log.error("Cannot load state " + state, e);
			getBrowserNavigation().pushState("~", null);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}
}
