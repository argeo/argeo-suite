package org.argeo.app.jcr;

import static org.argeo.app.core.SuiteUtils.USER_DEVICES_NODE_NAME;
import static org.argeo.app.core.SuiteUtils.USER_SESSIONS_NODE_NAME;
import static org.argeo.app.core.SuiteUtils.USER_STATE_NODE_NAME;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;
import javax.security.auth.x500.X500Principal;

import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.cms.CmsConstants;
import org.argeo.api.cms.CmsSession;
import org.argeo.app.api.AppUserState;
import org.argeo.app.api.EntityType;
import org.argeo.app.core.SuiteUtils;
import org.argeo.cms.RoleNameUtils;
import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrUtils;

/** JCR utilities. */
public class SuiteJcrUtils {
	/** @deprecated Use {@link AppUserState} instead. */
	@Deprecated
	public static Node getOrCreateUserNode(Session adminSession, String userDn) {
		try {
			Node usersBase = adminSession.getNode(EntityType.user.basePath());
			String uid = RoleNameUtils.getLastRdnValue(userDn);
			Node userNode;
			if (!usersBase.hasNode(uid)) {
				userNode = usersBase.addNode(uid, NodeType.NT_UNSTRUCTURED);
				userNode.addMixin(EntityType.user.get());
				userNode.addMixin(NodeType.MIX_CREATED);
				userNode.setProperty(LdapAttr.distinguishedName.get(), userDn.toString());
				userNode.setProperty(LdapAttr.uid.get(), uid);
			} else {
				userNode = usersBase.getNode(uid);
			}

			if (!userNode.hasNode(USER_SESSIONS_NODE_NAME)) {
				// Migrate existing user node
				Node sessionsNode = userNode.addNode(USER_SESSIONS_NODE_NAME, NodeType.NT_UNSTRUCTURED);
				oldSessions: for (NodeIterator nit = userNode.getNodes(); nit.hasNext();) {
					Node child = nit.nextNode();
					if (USER_SESSIONS_NODE_NAME.equals(child.getName()) || child.getName().startsWith("rep:")
							|| child.getName().startsWith("jcr:"))
						continue oldSessions;
					Node target = sessionsNode.addNode(child.getName());
					JcrUtils.copy(child, target);
				}

				Node userStateNode = userNode.addNode(USER_STATE_NODE_NAME, NodeType.NT_UNSTRUCTURED);
				Node userDevicesNode = userNode.addNode(USER_DEVICES_NODE_NAME, NodeType.NT_UNSTRUCTURED);

				adminSession.save();
//				JackrabbitSecurityUtils.denyPrivilege(adminSession, userNode.getPath(), SuiteRole.coworker.dn(),
//						Privilege.JCR_READ);
				JcrUtils.addPrivilege(adminSession, userNode.getPath(), new X500Principal(userDn.toString()).getName(),
						Privilege.JCR_READ);
				JcrUtils.addPrivilege(adminSession, userNode.getPath(), CmsConstants.ROLE_USER_ADMIN,
						Privilege.JCR_ALL);

				JcrUtils.addPrivilege(adminSession, userStateNode.getPath(), userDn, Privilege.JCR_ALL);
				JcrUtils.addPrivilege(adminSession, userDevicesNode.getPath(), userDn, Privilege.JCR_ALL);
			}
			return userNode;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot create user node for " + userDn, e);
		}
	}

	/** @deprecated Use {@link AppUserState} instead. */
	@Deprecated
	public static Node getCmsSessionNode(Session session, CmsSession cmsSession) {
		try {
			return session.getNode(SuiteUtils.getUserNodePath(cmsSession.getUserDn()) + '/' + USER_SESSIONS_NODE_NAME
					+ '/' + cmsSession.uuid().toString());
		} catch (RepositoryException e) {
			throw new JcrException("Cannot get session dir for " + cmsSession, e);
		}
	}

	/** @deprecated Use {@link AppUserState} instead. */
	@Deprecated
	public static Node getOrCreateCmsSessionNode(Session adminSession, CmsSession cmsSession) {
		try {
			String userDn = cmsSession.getUserDn();
			Node userNode = getOrCreateUserNode(adminSession, userDn);
			Node sessionsNode = userNode.getNode(USER_SESSIONS_NODE_NAME);
			String cmsSessionUuid = cmsSession.uuid().toString();
			Node cmsSessionNode;
			if (!sessionsNode.hasNode(cmsSessionUuid)) {
				cmsSessionNode = sessionsNode.addNode(cmsSessionUuid, NodeType.NT_UNSTRUCTURED);
				cmsSessionNode.addMixin(NodeType.MIX_CREATED);
				adminSession.save();
				JcrUtils.addPrivilege(adminSession, cmsSessionNode.getPath(), cmsSession.getUserRole(),
						Privilege.JCR_ALL);
			} else {
				cmsSessionNode = sessionsNode.getNode(cmsSessionUuid);
			}
			return cmsSessionNode;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot create session dir for " + cmsSession, e);
		}
	}

	/** singleton */
	private SuiteJcrUtils() {
	}
}
