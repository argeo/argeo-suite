package org.argeo.app.api.geo;

/** Keys used for WFS KVP (key-value pair) encoding. */
public enum WfsKvp {
	CQL_FILTER("cql_filter"), //
	OUTPUT_FORMAT("outputFormat"), //
	TYPE_NAMES("typeNames"), //
	BBOX("bbox"), //
	FORMAT_OPTIONS("format_options"), //
	;

	public final static String FILENAME_ = "filename:";

	private final String key;

	private WfsKvp(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
