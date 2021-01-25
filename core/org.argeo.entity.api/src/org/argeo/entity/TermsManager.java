package org.argeo.entity;

import java.util.List;

/** Provides optimised access and utilities around terms typologies. */
public interface TermsManager {
	List<String> listAllTerms(String typology);
}
