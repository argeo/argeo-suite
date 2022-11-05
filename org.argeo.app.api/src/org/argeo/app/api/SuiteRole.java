package org.argeo.app.api;

import javax.xml.namespace.QName;

import org.argeo.api.acr.ArgeoNamespace;
import org.argeo.api.acr.ContentName;
import org.argeo.api.acr.ldap.LdapAttrs;
import org.argeo.api.cms.CmsConstants;
import org.argeo.cms.auth.SystemRole;

/** Standard suite system roles. */
public enum SuiteRole implements SystemRole {
	/** An external person who has read access to part of the information. */
	observer,
	/** An active coworker. */
	coworker,
	/** Someone who is allowed validate and publish information. */
	publisher,
	/** Someone with manager status within an organisation. Does not necessarily give more rights. */
	manager,
	//
	;

	private final static String QUALIFIER = "app.";

	private final ContentName name;

	SuiteRole() {
		name = new ContentName(ArgeoNamespace.ROLE_NAMESPACE_URI, QUALIFIER + name());
	}

	@Override
	public QName getName() {
		return name;
	}

	@Deprecated
	private String getRolePrefix() {
		return "org.argeo.suite";
	}

	@Deprecated
	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(getRolePrefix()).append(".").append(name())
				.append(",").append(CmsConstants.SYSTEM_ROLES_BASEDN).toString();
	}
}
