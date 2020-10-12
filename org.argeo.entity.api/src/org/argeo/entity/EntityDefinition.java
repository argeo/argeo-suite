package org.argeo.entity;

import javax.jcr.Node;

/** The definition of an entity, a composite configurable data structure. */
public interface EntityDefinition {
	String getEditorId(Node entity);

	String getType();
}
