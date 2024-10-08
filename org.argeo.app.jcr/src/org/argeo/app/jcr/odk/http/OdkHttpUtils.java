package org.argeo.app.jcr.odk.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.argeo.cms.http.CommonMediaType.TEXT_CSV;

import org.argeo.cms.http.server.HttpServerUtils;

import com.sun.net.httpserver.HttpExchange;

class OdkHttpUtils {

	static void addOdkResponseHeaders(HttpExchange exchange) {
		HttpServerUtils.setContentType(exchange, TEXT_CSV, UTF_8);
		HttpServerUtils.setDateHeader(exchange);
		exchange.getResponseHeaders().set("X-OpenRosa-Version", "1.0");
	}

	/** singleton */
	private OdkHttpUtils() {
	}
}
