package org.argeo.api.app;

import java.util.List;
import java.util.Set;

/** Provides optimised access and utilities around terms typologies. */
public interface TermsManager {
	Typology getTypology(String typology);

	Set<Typology> getTypologies();

	Term getTerm(String id);

	List<Term> listAllTerms(String typology);

}
