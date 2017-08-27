package org.argeo.suite.workbench;

public class SuiteWorkbenchException extends RuntimeException {
	private static final long serialVersionUID = 5276857785523513563L;

	public SuiteWorkbenchException(String message) {
		super(message);
	}

	public SuiteWorkbenchException(Throwable cause) {
		super(cause);
	}

	public SuiteWorkbenchException(String message, Throwable cause) {
		super(message, cause);
	}
}
