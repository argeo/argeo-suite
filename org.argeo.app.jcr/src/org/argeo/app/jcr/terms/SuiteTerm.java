package org.argeo.app.jcr.terms;

import java.util.ArrayList;
import java.util.List;

import org.argeo.api.app.Term;

/**
 * A single term. Helper to optimise {@link SuiteTermsManager} implementation.
 */
class SuiteTerm implements Term {
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
			this.name = relativePath.substring(index + 1);
		} else {
			this.name = relativePath;
		}
		id = typology.getName() + '/' + relativePath;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getRelativePath() {
		return relativePath;
	}

	@Override
	public SuiteTypology getTypology() {
		return typology;
	}

	@Override
	public List<SuiteTerm> getSubTerms() {
		return subTerms;
	}

	@Override
	public SuiteTerm getParentTerm() {
		return parentTerm;
	}

}
