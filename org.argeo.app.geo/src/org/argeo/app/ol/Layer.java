package org.argeo.app.ol;

import java.util.Objects;

public abstract class Layer extends AbstractOlObject {

	public Layer(Object... args) {
		super(args);
	}

	public void setOpacity(double opacity) {
		if (opacity < 0 || opacity > 1)
			throw new IllegalArgumentException("Opacity must be between 0 and 1");
		if (isNew())
			getNewOptions().put("opacity", opacity);
		else
			executeMethod(getMethodName(), opacity);
	}

	public void setSource(Source source) {
		Objects.requireNonNull(source);
		if (isNew())
			getNewOptions().put("source", source);
		else
			executeMethod(getMethodName(), source);
	}

	public void setMinResolution(long minResolution) {
		if (isNew())
			getNewOptions().put("minResolution", minResolution);
		else
			executeMethod(getMethodName(), minResolution);
	}

	public void setMaxResolution(long maxResolution) {
		if (isNew())
			getNewOptions().put("maxResolution", maxResolution);
		else
			executeMethod(getMethodName(), maxResolution);
	}

}
