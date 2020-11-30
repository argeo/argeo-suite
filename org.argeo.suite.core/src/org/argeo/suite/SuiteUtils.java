package org.argeo.suite;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;
import javax.naming.ldap.LdapName;
import javax.security.auth.x500.X500Principal;

import org.argeo.cms.auth.CmsSession;
import org.argeo.entity.EntityType;
import org.argeo.jackrabbit.security.JackrabbitSecurityUtils;
import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrUtils;
import org.argeo.naming.LdapAttrs;

/** Utilities around the Argeo Suite APIs. */
public class SuiteUtils {

	public static String getUserNodePath(LdapName userDn) {
		String uid = userDn.getRdn(userDn.size() - 1).getValue().toString();
		return EntityType.user.basePath() + '/' + uid;
	}

	public static Node getOrCreateUserNode(Session adminSession, LdapName userDn) {
		try {
			Node usersBase = adminSession.getNode(EntityType.user.basePath());
			String uid = userDn.getRdn(userDn.size() - 1).getValue().toString();
			Node userNode;
			if (!usersBase.hasNode(uid)) {
				userNode = usersBase.addNode(uid, NodeType.NT_UNSTRUCTURED);
				userNode.addMixin(EntityType.user.get());
				userNode.addMixin(NodeType.MIX_CREATED);
				userNode.setProperty(LdapAttrs.distinguishedName.property(), userDn.toString());
				userNode.setProperty(LdapAttrs.uid.property(), uid);
				adminSession.save();
				JackrabbitSecurityUtils.denyPrivilege(adminSession, userNode.getPath(), SuiteRole.coworker.dn(),
						Privilege.JCR_READ);
				JcrUtils.addPrivilege(adminSession, userNode.getPath(), new X500Principal(userDn.toString()).getName(),
						Privilege.JCR_ALL);
			} else {
				userNode = usersBase.getNode(uid);
			}
			return userNode;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot create user node for " + userDn, e);
		}
	}

	public static Node getCmsSessionNode(Session session, CmsSession cmsSession) {
		try {
			return session.getNode(getUserNodePath(cmsSession.getUserDn()) + '/' + cmsSession.getUuid().toString());
		} catch (RepositoryException e) {
			throw new JcrException("Cannot get session dir for " + cmsSession, e);
		}
	}

	public static Node getOrCreateCmsSessionNode(Session adminSession, CmsSession cmsSession) {
		try {
			LdapName userDn = cmsSession.getUserDn();
//			String uid = userDn.get(userDn.size() - 1);
			Node userNode = getOrCreateUserNode(adminSession, userDn);
//			if (!usersBase.hasNode(uid)) {
//				userNode = usersBase.addNode(uid, NodeType.NT_UNSTRUCTURED);
//				userNode.addMixin(EntityType.user.get());
//				userNode.addMixin(NodeType.MIX_CREATED);
//				usersBase.setProperty(LdapAttrs.uid.property(), uid);
//				usersBase.setProperty(LdapAttrs.distinguishedName.property(), userDn.toString());
//				adminSession.save();
//			} else {
//				userNode = usersBase.getNode(uid);
//			}
			String cmsSessionUuid = cmsSession.getUuid().toString();
			Node cmsSessionNode;
			if (!userNode.hasNode(cmsSessionUuid)) {
				cmsSessionNode = userNode.addNode(cmsSessionUuid, NodeType.NT_UNSTRUCTURED);
				cmsSessionNode.addMixin(NodeType.MIX_CREATED);
				adminSession.save();
				JcrUtils.addPrivilege(adminSession, cmsSessionNode.getPath(), cmsSession.getUserRole(),
						Privilege.JCR_ALL);
			} else {
				cmsSessionNode = userNode.getNode(cmsSessionUuid);
			}
			return cmsSessionNode;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot create session dir for " + cmsSession, e);
		}
	}

	/** Singleton. */
	private SuiteUtils() {

	}

}
