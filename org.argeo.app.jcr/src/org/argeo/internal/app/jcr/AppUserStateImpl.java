package org.argeo.internal.app.jcr;

import javax.jcr.Node;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentRepository;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.app.AppUserState;
import org.argeo.api.cms.CmsConstants;
import org.argeo.api.cms.CmsSession;
import org.argeo.app.jcr.SuiteJcrUtils;
import org.argeo.cms.acr.ContentUtils;
import org.argeo.cms.jcr.acr.JcrContentProvider;
import org.argeo.jcr.Jcr;

public class AppUserStateImpl implements AppUserState {
	private ContentRepository contentRepository;
	private JcrContentProvider jcrContentProvider;

	@SuppressWarnings("deprecation")
	@Override
	public Content getOrCreateSessionDir(CmsSession session) {
		Node userDirNode = jcrContentProvider.doInAdminSession((adminSession) -> {
			Node node = SuiteJcrUtils.getOrCreateCmsSessionNode(adminSession, session);
			return node;
		});		
		ContentSession contentSession = ContentUtils.openSession(contentRepository, session); 
		Content userDir = contentSession.get(Content.ROOT_PATH + CmsConstants.SYS_WORKSPACE + Jcr.getPath(userDirNode));
		return userDir;
	}

	public void setJcrContentProvider(JcrContentProvider jcrContentProvider) {
		this.jcrContentProvider = jcrContentProvider;
	}

	public void setContentRepository(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

}
