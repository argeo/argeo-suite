package org.argeo.app.ol;

public class TileLayer extends Layer {
	public TileLayer(Object... args) {
		super(args);
	}

	public TileLayer(Source source) {
		setSource(source);
	}
}
