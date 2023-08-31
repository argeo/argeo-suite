package org.argeo.app.internal.geo.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.argeo.api.acr.Content;
import org.argeo.api.acr.ContentSession;
import org.argeo.api.acr.spi.ProvidedRepository;
import org.argeo.app.geo.CqlUtils;
import org.argeo.cms.http.server.HttpServerUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GmlHttpHandler implements HttpHandler {
	private ProvidedRepository contentRepository;

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = HttpServerUtils.subPath(exchange);
		ContentSession session = HttpServerUtils.getContentSession(contentRepository, exchange);
//		Content content = session.get(path);
	}

	public void setContentRepository(ProvidedRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

}
