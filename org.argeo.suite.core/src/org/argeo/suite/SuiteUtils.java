package org.argeo.suite;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;
import javax.naming.ldap.LdapName;

import org.argeo.cms.auth.CmsSession;
import org.argeo.entity.EntityType;
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
			} else {
				userNode = usersBase.getNode(uid);
			}
			return userNode;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot create user node for " + userDn, e);
		}
	}

	public static Node getOrCreateSessionDir(Session adminSession, CmsSession cmsSession) {
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
			Node userDir;
			if (!userNode.hasNode(cmsSessionUuid)) {
				userDir = userNode.addNode(cmsSessionUuid, NodeType.NT_UNSTRUCTURED);
				userDir.addMixin(NodeType.MIX_CREATED);
				adminSession.save();
				JcrUtils.addPrivilege(adminSession, userDir.getPath(), cmsSession.getUserDn().toString(),
						Privilege.JCR_ALL);
			} else {
				userDir = userNode.getNode(cmsSessionUuid);
			}
			return userDir;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot create session dir for " + cmsSession, e);
		}
	}

	/** Singleton. */
	private SuiteUtils() {

	}

}
