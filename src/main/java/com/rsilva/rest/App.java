package com.rsilva.rest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App extends ResourceConfig {

	public App() {
		packages("com.rsilva.rest");
		register(new AppBinder());
	}

	public static void main(String[] args) {

		Server server = new Server(8080);

		ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

		ctx.setContextPath("/");
		server.setHandler(ctx);

		ServletHolder serHol = ctx.addServlet(ServletContainer.class, "/money-transfer/*");
		serHol.setInitOrder(1);
		serHol.setInitParameter("jersey.config.server.provider.packages", "com.rsilva.rest, com.fasterxml.jackson.jaxrs.json");
		serHol.setInitParameter("javax.ws.rs.Application", "com.rsilva.rest.App");

		try {
			server.start();
			server.join();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {

			server.destroy();
		}
	}
}
