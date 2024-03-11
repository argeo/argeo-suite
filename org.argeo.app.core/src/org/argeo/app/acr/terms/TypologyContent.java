package org.argeo.app.acr.terms;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.spi.ContentProvider;
import org.argeo.api.acr.spi.ProvidedSession;
import org.argeo.api.app.Typology;
import org.argeo.cms.acr.AbstractContent;

public class TypologyContent extends AbstractContent {
	private TermsContentProvider provider;
	private Typology typology;

	public TypologyContent(ProvidedSession session, TermsContentProvider provider, Typology typology) {
		super(session);
		this.provider = provider;
		this.typology = typology;
	}

	@Override
	public ContentProvider getProvider() {
		return provider;
	}

	@Override
	public QName getName() {
		return NamespaceUtils.unqualified(typology.getId());
	}

	@Override
	public Content getParent() {
		return provider.getRootContent(getSession());
	}

	@Override
	public Iterator<Content> iterator() {
		return typology.getSubTerms().stream().map((t) -> (Content) new TermContent(getSession(), provider, t))
				.iterator();
	}

}
