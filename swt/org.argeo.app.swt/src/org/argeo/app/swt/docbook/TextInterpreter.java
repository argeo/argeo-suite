package org.argeo.app.swt.docbook;

import org.argeo.api.acr.Content;

/** Convert from/to data layer to/from presentation layer. */
public interface TextInterpreter {
	String raw(Content content);

	String read(Content content);

	String readSimpleHtml(Content content);

	void write(Content content, String txt);
}
