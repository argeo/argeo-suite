package org.argeo.publishing.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.argeo.api.cms.CmsTheme;

/** Serves fonts locally. */
public class FontsServlet extends HttpServlet {
	private static final long serialVersionUID = 6009572962850708537L;
	private Map<String, CmsTheme> themes = Collections.synchronizedMap(new HashMap<>());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String font = req.getPathInfo();
		font = font.substring(1, font.length());
		for (CmsTheme theme : themes.values()) {
			for (String fontPath : theme.getFontsPaths()) {
				if (fontPath.endsWith(font)) {
					if (font.endsWith(".woff"))
						resp.setContentType("font/woff");
					else if (font.endsWith(".woff2"))
						resp.setContentType("font/woff2");
					try (InputStream in = theme.loadPath(fontPath)) {
						IOUtils.copy(in, resp.getOutputStream());
						return;
					}
				}
			}
		}
		resp.setStatus(404);
	}

	public void addTheme(CmsTheme theme, Map<String, String> properties) {
		themes.put(theme.getThemeId(), theme);
	}

	public void removeTheme(CmsTheme theme, Map<String, String> properties) {
		themes.remove(theme.getThemeId());
	}

}
