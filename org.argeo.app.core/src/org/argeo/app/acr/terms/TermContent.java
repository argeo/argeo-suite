package org.argeo.app.acr.terms;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.spi.ContentProvider;
import org.argeo.api.acr.spi.ProvidedSession;
import org.argeo.api.app.Term;
import org.argeo.cms.acr.AbstractContent;

public class TermContent extends AbstractContent {
	private TermsContentProvider provider;
	private Term term;

	public TermContent(ProvidedSession session, TermsContentProvider provider, Term term) {
		super(session);
		this.provider = provider;
		this.term = term;
	}

	@Override
	public Iterator<Content> iterator() {
		return term.getSubTerms().stream().map((t) -> (Content) new TermContent(getSession(), provider, t)).iterator();
	}

	@Override
	public ContentProvider getProvider() {
		return provider;
	}

	@Override
	public QName getName() {
		return NamespaceUtils.unqualified(term.getName());
	}

	@Override
	public Content getParent() {
		Term parentTerm = term.getParentTerm();
		return parentTerm == null ? new TypologyContent(getSession(), provider, term.getTypology())
				: new TermContent(getSession(), provider, parentTerm);
	}

}
