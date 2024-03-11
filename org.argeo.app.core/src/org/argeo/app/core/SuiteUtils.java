package org.argeo.app.core;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ldap.LdapAttr;
import org.argeo.api.acr.ldap.LdapObj;
import org.argeo.api.app.EntityType;
import org.argeo.api.cms.auth.RoleNameUtils;

/** Utilities around the Argeo Suite APIs. */
public class SuiteUtils {
	public final static String USER_STATE_NODE_NAME = "state";
	public final static String USER_DEVICES_NODE_NAME = "devices";
	public final static String USER_SESSIONS_NODE_NAME = "sessions";

	public static String getUserNodePath(String userDn) {
		String uid = RoleNameUtils.getLastRdnValue(userDn);
		return EntityType.user.basePath() + '/' + uid;
	}

	public static Set<String> extractRoles(String[] semiColArr) {
		Set<String> res = new HashSet<>();
		// TODO factorize and make it more robust
		final String rolesPrefix = "roles:=\"";
		// first one is layer id
		for (int i = 1; i < semiColArr.length; i++) {
			if (semiColArr[i].startsWith(rolesPrefix)) {
				String rolesStr = semiColArr[i].substring(rolesPrefix.length());
				// remove last "
				rolesStr = rolesStr.substring(0, rolesStr.lastIndexOf('\"'));
				// TODO support AND (&) as well
				String[] roles = rolesStr.split("\\|");// OR (|)
				for (String role : roles) {
					res.add(role.trim());
				}
			}
		}
		return res;
	}

	synchronized static public long findNextId(Content hierarchyUnit, QName cclass) {
		if (!hierarchyUnit.hasContentClass(LdapObj.posixGroup.qName()))
			throw new IllegalArgumentException(hierarchyUnit + " is not a POSIX group");

		long min = hierarchyUnit.get(LdapAttr.gidNumber.qName(), Long.class).orElseThrow();
		long currentMax = 0l;
		for (Content childHu : hierarchyUnit) {
			if (!childHu.hasContentClass(LdapObj.organizationalUnit.qName()))
				continue;
			// FIXME filter out functional hierarchy unit
			for (Content role : childHu) {
				if (role.hasContentClass(cclass)) {

					if (LdapObj.posixAccount.qName().equals(cclass)) {
						Long id = role.get(LdapAttr.uidNumber.qName(), Long.class).orElseThrow();
						if (id > currentMax)
							currentMax = id;
					}
				}
			}
		}
		if (currentMax == 0l)
			return min;
		return currentMax + 1;
	}

	/** Singleton. */
	private SuiteUtils() {
	}
}
