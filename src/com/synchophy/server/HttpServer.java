package com.synchophy.server;

import java.util.logging.Level;

import org.farng.mp3.LogFormatter;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.synchophy.server.db.DatabaseManager;

public class HttpServer {

	private static HttpServer instance = new HttpServer();
	private ControllerServlet controller;
	private Server server;

	private HttpServer() {

	}

	public static HttpServer getInstance() {
		return instance;
	}

	public void start() throws Exception {
		LogFormatter.getLogger().setLevel(Level.SEVERE);
		controller = new ControllerServlet();
		server = new Server(8080);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run() {
				instance.stop();
			};

		});

		// initiate database
		DatabaseManager.getInstance();

		// initiate player
		PlayerManager.getInstance();

		Context root = new Context(server, "/", Context.SESSIONS);
		root.addServlet(new ServletHolder(new ControllerServlet()), "/*");
		server.start();
	}

	public void stop() {
		try {
			controller.shutdown();
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void restart() {
		try {
			stop();
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		HttpServer.getInstance().start();
	}
}