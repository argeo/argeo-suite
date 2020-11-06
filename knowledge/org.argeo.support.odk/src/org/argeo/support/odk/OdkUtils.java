package org.argeo.support.odk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.entity.EntityType;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.JcrxApi;
import org.argeo.util.DigestUtils;

/** Utilities around ODK. */
public class OdkUtils {
	private final static Log log = LogFactory.getLog(OdkUtils.class);

	public static void loadOdkForm(Node formBase, String name, InputStream in) throws RepositoryException, IOException {
		if (!formBase.isNodeType(EntityType.formSet.get()))
			throw new IllegalArgumentException(
					"Parent path " + formBase + " must be of type " + EntityType.formSet.get());
		Node form = JcrUtils.getOrAdd(formBase, name, OrxListType.xform.get(), NodeType.MIX_SIMPLE_VERSIONABLE);

		String previousCsum = JcrxApi.getChecksum(form, JcrxApi.MD5);

		Session s = formBase.getSession();
//		String res = "/odk/apafSession.odk.xml";
//		try (InputStream in = getClass().getClassLoader().getResourceAsStream(res)) {
		s.importXML(form.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
//		}

		if (previousCsum == null)// save before checksuming
			s.save();
		String newCsum;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			s.exportDocumentView(form.getPath() + "/" + OdkNames.H_HTML, out, true, false);
			newCsum = DigestUtils.digest(DigestUtils.MD5, out.toByteArray());
		}
		if (previousCsum == null) {
			JcrxApi.addChecksum(form, newCsum);
			JcrUtils.updateLastModified(form);
			s.save();
			s.getWorkspace().getVersionManager().checkpoint(form.getPath());
			if (log.isDebugEnabled())
				log.debug("New form " + form);
		} else {
			if (newCsum.equals(previousCsum)) {
				// discard
				s.refresh(false);
				if (log.isDebugEnabled())
					log.debug("Unmodified form " + form);
				return;
			} else {
				JcrxApi.addChecksum(form, newCsum);
				JcrUtils.updateLastModified(form);
				s.save();
				s.getWorkspace().getVersionManager().checkpoint(form.getPath());
				if (log.isDebugEnabled()) {
					log.debug("Updated form " + form);
					log.debug("Previous csum " + previousCsum);
					log.debug("New csum " + newCsum);
				}
			}
		}
	}

	/** Singleton. */
	private OdkUtils() {

	}

}
