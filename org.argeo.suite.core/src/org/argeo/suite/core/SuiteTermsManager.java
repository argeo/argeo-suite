package org.argeo.suite.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.api.NodeConstants;
import org.argeo.cms.jcr.CmsJcrUtils;
import org.argeo.entity.EntityNames;
import org.argeo.entity.EntityType;
import org.argeo.entity.Term;
import org.argeo.entity.TermsManager;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;

/** Argeo Suite implementation of terms manager. */
public class SuiteTermsManager implements TermsManager {
	private final Map<String, SuiteTerm> terms = new HashMap<>();
	private final Map<String, SuiteTypology> typologies = new HashMap<>();

	// JCR
	private Repository repository;
	private Session adminSession;

	public void init() {
		adminSession = CmsJcrUtils.openDataAdminSession(repository, NodeConstants.SYS_WORKSPACE);
	}

	@Override
	public List<Term> listAllTerms(String typology) {
		List<Term> res = new ArrayList<>();
		SuiteTypology t = getTypology(typology);
		for (SuiteTerm term : t.getAllTerms()) {
			res.add(term);
		}
		return res;
	}

	@Override
	public SuiteTerm getTerm(String termId) {
		return terms.get(termId);
	}

	@Override
	public SuiteTypology getTypology(String typology) {
		SuiteTypology t = typologies.get(typology);
		if (t == null) {
			Node termsNode = Jcr.getNode(adminSession, "SELECT * FROM [{0}] WHERE NAME()=\"{1}\"",
					EntityType.terms.get(), typology);
			if (termsNode == null)
				throw new IllegalArgumentException("Typology " + typology + " not found.");
			t = loadTypology(termsNode);
		}
		return t;
	}

	SuiteTypology loadTypology(Node termsNode) {
		try {
			SuiteTypology typology = new SuiteTypology(termsNode);
			for (Node termNode : Jcr.iterate(termsNode.getNodes())) {
				if (termNode.isNodeType(EntityType.term.get())) {
					SuiteTerm term = loadTerm(typology, termNode, null);
					if (!term.getSubTerms().isEmpty())
						typology.markNotFlat();
					typology.getSubTerms().add(term);
				}
			}
			typologies.put(typology.getName(), typology);
			return typology;
		} catch (RepositoryException e) {
			throw new JcrException("Cannot load typology from " + termsNode, e);
		}
	}

	SuiteTerm loadTerm(SuiteTypology typology, Node termNode, SuiteTerm parentTerm) throws RepositoryException {
		String name = termNode.getProperty(EntityNames.NAME).getString();
		String relativePath = parentTerm == null ? name : parentTerm.getRelativePath() + '/' + name;
		SuiteTerm term = new SuiteTerm(typology, relativePath, parentTerm);
		terms.put(term.getId(), term);
		for (Node subTermNode : Jcr.iterate(termNode.getNodes())) {
			if (termNode.isNodeType(EntityType.term.get())) {
				SuiteTerm subTerm = loadTerm(typology, subTermNode, term);
				term.getSubTerms().add(subTerm);
			}
		}
		return term;
	}

	public void destroy() {
		Jcr.logout(adminSession);
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
