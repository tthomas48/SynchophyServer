package com.synchophy.server;


import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;


public class HttpServer {

  public static void main(String[] args) throws Exception {

    ControllerServlet controller = null;
    try {
      controller = new ControllerServlet();

      Server server = new Server(8080);

      Context root = new Context(server, "/", Context.SESSIONS);
      root.addServlet(new ServletHolder(new ControllerServlet()), "/*");
      server.start();
    } finally {
      controller.shutdown();
    }
  }
}