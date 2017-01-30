package org.argeo.suite;

/** Argeo Suite specific exception. Wraps a usual RuntimeException */
public class SuiteException extends RuntimeException {
	private static final long serialVersionUID = 9048360857209165816L;

	public SuiteException(String message) {
		super(message);
	}

	public SuiteException(String message, Throwable e) {
		super(message, e);
	}
}
