package org.argeo.internal.app.jcr;

import javax.jcr.Node;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.cms.CmsConstants;
import org.argeo.api.cms.CmsSession;
import org.argeo.app.api.AppUserState;
import org.argeo.app.jcr.SuiteJcrUtils;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.jcr.acr.JcrContentProvider;
import org.argeo.jcr.Jcr;

public class AppUserStateImpl implements AppUserState {
	private JcrContentProvider jcrContentProvider;

	@SuppressWarnings("deprecation")
	@Override
	public Content getOrCreateSessionDir(ContentSession contentSession, CmsSession session) {
		Node userDirNode = jcrContentProvider.doInAdminSession((adminSession) -> {
			Node node = SuiteJcrUtils.getOrCreateCmsSessionNode(adminSession, session);
			return node;
		});
		Content userDir = contentSession
				.get(ContentUtils.SLASH + CmsConstants.SYS_WORKSPACE + Jcr.getPath(userDirNode));
		return userDir;
	}

	public void setJcrContentProvider(JcrContentProvider jcrContentProvider) {
		this.jcrContentProvider = jcrContentProvider;
	}

}
