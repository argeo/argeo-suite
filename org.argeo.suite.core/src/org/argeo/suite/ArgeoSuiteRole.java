package org.argeo.suite;

import org.argeo.naming.LdapAttrs;
import org.argeo.node.NodeConstants;

/** Argeo Office specific roles used in the code */
public enum ArgeoSuiteRole {
	coworker, manager;

	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(SuiteConstants.SUITE_APP_PREFIX).append(".")
				.append(name()).append(",").append(NodeConstants.ROLES_BASEDN).toString();
	}
}
