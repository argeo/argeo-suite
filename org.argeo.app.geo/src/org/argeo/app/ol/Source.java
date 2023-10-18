package org.argeo.app.ol;

public class Source extends AbstractOlObject {

	public Source(Object... args) {
		super(args);
	}

	public void refresh() {
		executeMethod(getMethodName());
	}
}
