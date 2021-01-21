package org.argeo.cms.text;

import javax.jcr.Item;

/** Convert from/to data layer to/from presentation layer. */
public interface TextInterpreter {
	String raw(Item item);

	String read(Item item);

	String readSimpleHtml(Item item);

	void write(Item item, String content);
}
