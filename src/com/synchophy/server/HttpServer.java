package com.synchophy.server;

import java.util.logging.Level;

import org.farng.mp3.LogFormatter;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.synchophy.server.db.DatabaseManager;

public class HttpServer {

	public static void main(String[] args) throws Exception {
		LogFormatter.getLogger().setLevel(Level.SEVERE);
		final ControllerServlet controller = new ControllerServlet();
		final Server server = new Server(8080);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run() {

				try {
					controller.shutdown();
					server.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};

		});
		
		// initiate database
		DatabaseManager.getInstance();
		
		// start our player thread
		PlayerManager.getInstance();

		Context root = new Context(server, "/", Context.SESSIONS);
		root.addServlet(new ServletHolder(new ControllerServlet()), "/*");
		server.start();
	}
}