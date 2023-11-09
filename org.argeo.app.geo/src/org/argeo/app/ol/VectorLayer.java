package org.argeo.app.ol;

public class VectorLayer extends Layer {
	public VectorLayer(Object... args) {
		super(args);
	}

	public VectorLayer(String name, Source source) {
		this(source);
		setName(name);
	}

	public VectorLayer(Source source) {
		setSource(source);
	}
}
