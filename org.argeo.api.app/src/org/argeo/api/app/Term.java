package org.argeo.api.app;

import java.util.List;

/**
 * A name within a {@link Typology}, used to qualify an entity (categories,
 * keywords, etc.).
 */
public interface Term {
	String getId();

	String getName();

//	String getRelativePath();

	Typology getTypology();

	List<? extends Term> getSubTerms();

	Term getParentTerm();

}
