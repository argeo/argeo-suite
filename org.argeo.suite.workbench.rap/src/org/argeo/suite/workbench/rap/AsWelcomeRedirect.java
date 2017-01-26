package org.argeo.suite.workbench.rap;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AsWelcomeRedirect extends HttpServlet {
	private static final long serialVersionUID = 4359084312826596812L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.sendRedirect(resp.encodeRedirectURL("/ui/WelcomePage"));
	}
}
