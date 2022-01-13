package org.argeo.app.api;

import java.util.List;

/** Provides optimised access and utilities around terms typologies. */
public interface TermsManager {
	Typology getTypology(String typology);
	
	Term getTerm(String id);

	List<Term> listAllTerms(String typology);

}
