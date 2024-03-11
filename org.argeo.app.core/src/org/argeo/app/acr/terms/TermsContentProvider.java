package org.argeo.app.acr.terms;

import java.util.Iterator;
import java.util.List;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentNotFoundException;
import org.argeo.api.acr.spi.ProvidedContent;
import org.argeo.api.acr.spi.ProvidedSession;
import org.argeo.api.app.EntityType;
import org.argeo.api.app.Term;
import org.argeo.api.app.TermsManager;
import org.argeo.api.app.Typology;
import org.argeo.cms.acr.AbstractSimpleContentProvider;
import org.argeo.cms.acr.ContentUtils;

public class TermsContentProvider extends AbstractSimpleContentProvider<TermsManager> {

	public TermsContentProvider() {
		super(EntityType.ENTITY_NAMESPACE_URI, EntityType.ENTITY_DEFAULT_PREFIX);
	}

	@Override
	protected Iterator<Content> firstLevel(ProvidedSession session) {
		return getService().getTypologies().stream().map((t) -> (Content) new TypologyContent(session, this, t))
				.iterator();
	}

	@Override
	public ProvidedContent get(ProvidedSession session, List<String> segments) {
		String typologyName = segments.get(0);
		Typology typology = getService().getTypology(typologyName);
		if (segments.size() == 1)
			return new TypologyContent(session, this, typology);
		Term currTerm = null;
		terms: for (Term term : typology.getSubTerms()) {
			if (term.getName().equals(segments.get(1))) {
				currTerm = term;
				break terms;
			}
		}
		if (currTerm == null)
			throw new ContentNotFoundException(session,
					getMountPath() + "/" + ContentUtils.toPath(segments) + " cannot be found");
		if (segments.size() == 1)
			return new TermContent(session, this, currTerm);

		for (int i = 2; i < segments.size(); i++) {
			String termName = segments.get(i);
			Term nextTerm = null;
			terms: for (Term term : currTerm.getSubTerms()) {
				if (term.getName().equals(termName)) {
					nextTerm = term;
					break terms;
				}
			}
			if (nextTerm == null)
				throw new ContentNotFoundException(session,
						getMountPath() + "/" + ContentUtils.toPath(segments) + " cannot be found");
			currTerm = nextTerm;
		}
		return new TermContent(session, this, currTerm);
	}

	ServiceContent getRootContent(ProvidedSession session) {
		return new ServiceContent(session);
	}

}
