package org.argeo.suite.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A single term. Helper to optimise {@link SuiteTermsManager} implementation.
 */
class SuiteTerm {
	private final String name;
	private final String relativePath;
	private final SuiteTypology typology;
	private final String id;

	private final SuiteTerm parentTerm;
	private final List<SuiteTerm> subTerms = new ArrayList<>();

	SuiteTerm(SuiteTypology typology, String relativePath, SuiteTerm parentTerm) {
		this.typology = typology;
		this.parentTerm = parentTerm;
		this.relativePath = relativePath;
		int index = relativePath.lastIndexOf('/');
		if (index > 0) {
			this.name = relativePath.substring(index);
		} else {
			this.name = relativePath;
		}
		id = typology.getName() + '/' + relativePath;
	}

	public String getName() {
		return name;
	}

	public String getRelativePath() {
		return relativePath;
	}

	SuiteTypology getTypology() {
		return typology;
	}

	public String getId() {
		return id;
	}

	List<SuiteTerm> getSubTerms() {
		return subTerms;
	}

	SuiteTerm getParentTerm() {
		return parentTerm;
	}

}
