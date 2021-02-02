package org.argeo.entity;

import java.util.List;

/** A structured and exhaustive set of {@link Term}s. */
public interface Typology {

	String getId();

	boolean isFlat();

	List<? extends Term> getSubTerms();

}
