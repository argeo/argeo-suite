package org.argeo.app.jcr.odk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.api.app.EntityType;
import org.argeo.api.cms.CmsLog;
import org.argeo.app.odk.OdkNames;
import org.argeo.app.odk.OrxListName;
import org.argeo.app.odk.OrxManifestName;
import org.argeo.cms.http.CommonMediaType;
import org.argeo.cms.util.DigestUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.JcrxApi;

/** Utilities around ODK. */
public class OdkJcrUtils {
	private final static CmsLog log = CmsLog.getLog(OdkJcrUtils.class);

	public static Node loadOdkForm(Node formBase, String name, InputStream in, InputStream... additionalNodes)
			throws RepositoryException, IOException {
		if (!formBase.isNodeType(EntityType.formSet.get()))
			throw new IllegalArgumentException(
					"Parent path " + formBase + " must be of type " + EntityType.formSet.get());
		Node form = JcrUtils.getOrAdd(formBase, name, OrxListName.xform.get(), NodeType.MIX_VERSIONABLE);

		String previousCsum = JcrxApi.getChecksum(form, JcrxApi.MD5);
		String previousFormId = Jcr.get(form, OrxListName.formID.get());
		String previousFormVersion = Jcr.get(form, OrxListName.version.get());

		Session s = formBase.getSession();
		s.importXML(form.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

		for (InputStream additionalIn : additionalNodes) {
			s.importXML(form.getPath(), additionalIn, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
		}
		s.save();

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
						String uuid;
						String mimeType;
						String encoding = StandardCharsets.UTF_8.name();
						String type = instanceUri.getHost();
						String path = instanceUri.getPath();
						if ("file".equals(type)) {
							if (!path.endsWith(".xml"))
								throw new IllegalArgumentException("File uri " + instanceUri + " must end with .xml");
							// Work around bug in ODK Collect not supporting paths
							// path = path.substring(0, path.length() - ".xml".length());
							// Node target = file.getSession().getNode(path);
							uuid = path.substring(1, path.length() - ".xml".length());
							mimeType = CommonMediaType.APPLICATION_XML.getType();
						} else if ("file-csv".equals(type)) {
							if (!path.endsWith(".csv"))
								throw new IllegalArgumentException("File uri " + instanceUri + " must end with .csv");
							// Work around bug in ODK Collect not supporting paths
							// path = path.substring(0, path.length() - ".csv".length());
							// Node target = file.getSession().getNode(path);
							uuid = path.substring(1, path.length() - ".csv".length());
							mimeType = CommonMediaType.TEXT_CSV.getType();
						} else {
							throw new IllegalArgumentException("Unsupported instance type " + type);
						}
						Node manifest = JcrUtils.getOrAdd(form, OrxManifestName.manifest.name(),
								OrxManifestName.manifest.get());
						Node file = JcrUtils.getOrAdd(manifest, instanceId);
						file.addMixin(NodeType.MIX_MIMETYPE);
						file.setProperty(Property.JCR_MIMETYPE, mimeType);
						file.setProperty(Property.JCR_ENCODING, encoding);
						Node target = file.getSession().getNodeByIdentifier(uuid);

//						if (target.isNodeType(NodeType.NT_QUERY)) {
//							Query query = target.getSession().getWorkspace().getQueryManager().getQuery(target);
//							query.setLimit(10);
//							QueryResult queryResult = query.execute();
//							RowIterator rit = queryResult.getRows();
//							while (rit.hasNext()) {
//								Row row = rit.nextRow();
//								for (Value value : row.getValues()) {
//									System.out.print(value.getString());
//									System.out.print(',');
//								}
//								System.out.print('\n');
//							}
//
//						}

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
				return form;
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
		return form;
	}

	/** Singleton. */
	private OdkJcrUtils() {

	}

}
