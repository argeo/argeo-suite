package org.argeo.support.odk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.entity.EntityType;
import org.argeo.jcr.Jcr;
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
		Node form = JcrUtils.getOrAdd(formBase, name, OrxListName.xform.get(), NodeType.MIX_VERSIONABLE);

		String previousCsum = JcrxApi.getChecksum(form, JcrxApi.MD5);
		String previousFormId = Jcr.get(form, OrxListName.formID.get());
		String previousFormVersion = Jcr.get(form, OrxListName.version.get());

		Session s = formBase.getSession();
//		String res = "/odk/apafSession.odk.xml";
//		try (InputStream in = getClass().getClassLoader().getResourceAsStream(res)) {
		s.importXML(form.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
//		}

		// manage instances
		// NodeIterator instances =
		// form.getNodes("h:html/h:head/xforms:model/xforms:instance");
		NodeIterator instances = form.getNode("h:html/h:head/xforms:model").getNodes("xforms:instance");
		Node primaryInstance = null;
		while (instances.hasNext()) {
			Node instance = instances.nextNode();
			if (primaryInstance == null) {
				primaryInstance = instance;
			} else {// secondary instances
				String instanceId = instance.getProperty("id").getString();
				URI instanceUri = null;
				if (instance.hasProperty("src"))
					try {
						instanceUri = new URI(instance.getProperty("src").getString());
					} catch (URISyntaxException e) {
						throw new IllegalArgumentException("Instance " + instanceId + " has a badly formatted URI", e);
					}
				if (instanceUri != null) {
					if ("jr".equals(instanceUri.getScheme())) {
						String type = instanceUri.getHost();
						if ("file".equals(type)) {
							Node manifest = JcrUtils.getOrAdd(form, OrxManifestName.manifest.name(),
									OrxManifestName.manifest.get());
							Node file = JcrUtils.getOrAdd(manifest, instanceId);
							String path = instanceUri.getPath();
							if (!path.endsWith(".xml"))
								throw new IllegalArgumentException("File uri " + instanceUri + " must end with .xml");
							// Work around bug in ODK Collect not supporting paths
							// path = path.substring(0, path.length() - ".xml".length());
							// Node target = file.getSession().getNode(path);
							String uuid = path.substring(1, path.length() - ".xml".length());
							Node target = file.getSession().getNodeByIdentifier(uuid);
							// FIXME hard code terms path in order to test ODK Collect bug
							if (target.isNodeType(NodeType.MIX_REFERENCEABLE)) {
								file.setProperty(Property.JCR_ID, target);
								if (file.hasProperty(Property.JCR_PATH))
									file.getProperty(Property.JCR_PATH).remove();
							} else {
								file.setProperty(Property.JCR_PATH, target.getPath());
								if (file.hasProperty(Property.JCR_ID))
									file.getProperty(Property.JCR_ID).remove();
							}
						}
					}
				}
			}
		}

		if (primaryInstance == null)
			throw new IllegalArgumentException("No primary instance found in " + form);
		if (!primaryInstance.hasNodes())
			throw new IllegalArgumentException("No data found in primary instance of " + form);
		NodeIterator primaryInstanceChildren = primaryInstance.getNodes();
		Node data = primaryInstanceChildren.nextNode();
		if (primaryInstanceChildren.hasNext())
			throw new IllegalArgumentException("More than one data found in primary instance of " + form);
		String formId = data.getProperty("id").getString();
		if (previousFormId != null && !formId.equals(previousFormId))
			log.warn("Form id of " + form + " changed from " + previousFormId + " to " + formId);
		form.setProperty(OrxListName.formID.get(), formId);
		String formVersion = data.getProperty("version").getString();

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
			form.setProperty(OrxListName.version.get(), formVersion);
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
				if (formVersion.equals(previousFormVersion)) {
					s.refresh(false);
					throw new IllegalArgumentException("Form " + form + " has been changed but version " + formVersion
							+ " has not been changed, discarding changes...");
				}
				form.setProperty(OrxListName.version.get(), formVersion);
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
