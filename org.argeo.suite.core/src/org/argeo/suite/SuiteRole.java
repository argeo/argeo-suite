package org.argeo.suite;

import org.argeo.api.cms.CmsConstants;
import org.argeo.util.naming.Distinguished;
import org.argeo.util.naming.LdapAttrs;

/** Office specific roles used in the code */
public enum SuiteRole implements Distinguished {
	coworker, manager;

	public String getRolePrefix() {
		return "org.argeo.suite";
	}

	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(getRolePrefix()).append(".").append(name())
				.append(",").append(CmsConstants.ROLES_BASEDN).toString();
	}
}
