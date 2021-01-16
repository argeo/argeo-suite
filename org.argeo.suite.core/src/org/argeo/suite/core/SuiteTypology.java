package org.argeo.suite.core;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.argeo.jcr.Jcr;

/** A typology. Helper to optimise {@link SuiteTermsManager} implementation. */
class SuiteTypology {
	private final String name;
	private final Node node;
	private boolean isFlat = true;

	private final List<SuiteTerm> subTerms = new ArrayList<>();

	public SuiteTypology(Node node) {
		this.node = node;
		this.name = Jcr.getName(this.node);
	}

	public String getName() {
		return name;
	}

	public Node getNode() {
		return node;
	}

	void markNotFlat() {
		if (isFlat)
			isFlat = false;
	}

	public boolean isFlat() {
		return isFlat;
	}

	public List<SuiteTerm> getSubTerms() {
		return subTerms;
	}

	public List<SuiteTerm> getAllTerms() {
		if (isFlat)
			return subTerms;
		else {
			List<SuiteTerm> terms = new ArrayList<>();
			for (SuiteTerm subTerm : subTerms) {
				terms.add(subTerm);
				collectSubTerms(terms, subTerm);
			}
			return terms;
		}
	}

	private void collectSubTerms(List<SuiteTerm> terms, SuiteTerm term) {
		for (SuiteTerm subTerm : term.getSubTerms()) {
			terms.add(subTerm);
			collectSubTerms(terms, subTerm);
		}
	}

}
