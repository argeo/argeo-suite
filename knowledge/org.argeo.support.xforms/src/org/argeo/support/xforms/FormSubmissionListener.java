package org.argeo.support.xforms;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/** Called when a user has received a new form submission. */
public interface FormSubmissionListener {
	/** Called after a form submission has been stored in the user area. */
	void formSubmissionReceived(Node node) throws RepositoryException;
}
