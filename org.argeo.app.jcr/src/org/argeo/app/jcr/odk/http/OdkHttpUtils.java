package org.argeo.app.jcr.odk.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.argeo.cms.http.CommonMediaType.TEXT_XML;

import org.argeo.cms.http.server.HttpServerUtils;

import com.sun.net.httpserver.HttpExchange;

/** Common patterns. */
class OdkHttpUtils {
	static void addOdkResponseHeaders(HttpExchange exchange) {
		HttpServerUtils.setContentType(exchange, TEXT_XML, UTF_8);
		// Date header should be set by the server
		// HttpServerUtils.setDateHeader(exchange);
		exchange.getResponseHeaders().set("X-OpenRosa-Version", "1.0");
	}

	/** singleton */
	private OdkHttpUtils() {
	}
}
