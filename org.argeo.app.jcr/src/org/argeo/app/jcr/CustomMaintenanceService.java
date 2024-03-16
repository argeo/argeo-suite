package org.argeo.app.jcr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.api.app.EntityType;
import org.argeo.api.cms.CmsLog;
import org.argeo.jcr.JcrUtils;
import org.argeo.maintenance.AbstractMaintenanceService;

/** Base for custom initialisations. */
public abstract class CustomMaintenanceService extends AbstractMaintenanceService {
	private final static CmsLog log = CmsLog.getLog(AbstractMaintenanceService.class);

	protected List<String> getTypologies() {
		return new ArrayList<>();
	}

	protected String getTypologiesLoadBase() {
		return "";
	}

	protected void loadTypologies(Node customBaseNode) throws RepositoryException, IOException {
		List<String> typologies = getTypologies();
		if (!typologies.isEmpty()) {
			Node termsBase = JcrUtils.getOrAdd(customBaseNode, EntityType.terms.name(), EntityType.typologies.get());
			for (String terms : typologies) {
				loadTerms(termsBase, terms);
			}
			// TODO do not save here, so that upper layers can decide when to save
			termsBase.getSession().save();
		}
	}

	protected void loadTerms(Node termsBase, String name) throws IOException, RepositoryException {
		try {
//			if (termsBase.hasNode(name))
//				return;
			String typologiesLoadBase = getTypologiesLoadBase();
			if (typologiesLoadBase.contains("/") && !typologiesLoadBase.endsWith("/"))
				typologiesLoadBase = typologiesLoadBase + "/";
			String termsLoadPath = typologiesLoadBase + name + ".xml";
			URL termsUrl = getClass().getResource(termsLoadPath);
			if (termsUrl == null)
				throw new IllegalArgumentException("Terms '" + name + "' not found.");
			try (InputStream in = termsUrl.openStream()) {
				termsBase.getSession().importXML(termsBase.getPath(), in,
						ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
			} catch (ItemExistsException e) {
				log.warn("Terms " + name + " exists with another UUID, removing it...");
				if (termsBase.hasNode(name))
					termsBase.getNode(name).remove();
				try (InputStream in = termsUrl.openStream()) {
					termsBase.getSession().importXML(termsBase.getPath(), in,
							ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
				}
			}
			if (log.isDebugEnabled())
				log.debug("Terms '" + name + "' loaded.");
			// TODO do not save here, so that upper layers can decide when to save
			termsBase.getSession().save();
		} catch (RepositoryException | IOException e) {
			log.error("Cannot load terms '" + name + "': " + e.getMessage());
			throw e;
		}
	}

}
