package org.argeo.suite.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.entity.EntityType;
import org.argeo.jcr.JcrUtils;
import org.argeo.maintenance.AbstractMaintenanceService;

/** Base for custom initialisations. */
public abstract class CustomMaintenanceService extends AbstractMaintenanceService {
	private final static Log log = LogFactory.getLog(AbstractMaintenanceService.class);

	protected List<String> getTypologies() {
		return new ArrayList<>();
	}

	protected String getTypologiesLoadBase() {
		return "/sys/terms";
	}

	protected void loadTypologies(Node customBaseNode) throws RepositoryException, IOException {
		List<String> typologies = getTypologies();
		if (!typologies.isEmpty()) {
			Node termsBase = JcrUtils.getOrAdd(customBaseNode, EntityType.terms.name(), EntityType.typologies.get());
			for (String terms : typologies) {
				loadTerms(termsBase, terms);
			}
			termsBase.getSession().save();
		}
	}

	protected void loadTerms(Node termsBase, String name) throws IOException, RepositoryException {
		try {
			String termsLoadPath = getTypologiesLoadBase() + '/' + name + ".xml";
			URL termsUrl = getClass().getClassLoader().getResource(termsLoadPath);
			if (termsUrl == null)
				throw new IllegalArgumentException("Terms '" + name + "' not found.");
			try (InputStream in = termsUrl.openStream()) {
				termsBase.getSession().importXML(termsBase.getPath(), in,
						ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
			} catch (ItemExistsException e) {
				log.warn("Terms " + name + " exists with another UUID, removing it...");
				termsBase.getNode(name).remove();
				try (InputStream in = termsUrl.openStream()) {
					termsBase.getSession().importXML(termsBase.getPath(), in,
							ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
				}
			}
			if (log.isDebugEnabled())
				log.debug("Terms '" + name + "' loaded.");
			termsBase.getSession().save();
		} catch (RepositoryException | IOException e) {
			log.error("Cannot load terms '" + name + "': " + e.getMessage());
			throw e;
		}
	}

}
