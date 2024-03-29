package com.synchophy.server;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;
import com.synchophy.server.dispatch.AbstractDispatch;

public class ControllerServlet extends HttpServlet {
	private Map dispatchCache = new HashMap();

	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		execute(req, resp);
	}

	private void execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String dispatchKey = request.getPathInfo();
		if (dispatchKey.equals("/player") == false) {
			System.err.println(dispatchKey);
		}
		dispatchKey = dispatchKey.replace('/', ' ').trim();
		// dispatchKey = dispatchKey.replaceFirst("/", "");

		if (!isLoggedIn(dispatchKey, request)) {
			do401(dispatchKey, request, response);
			return;
		}

		AbstractDispatch dispatchAction = getDispatch(dispatchKey);
		if (dispatchAction != null) {
			dispatchAction.write(request, response);
			return;
		}
		do404(dispatchKey, request, response);

	}

	public boolean isLoggedIn(String dispatchKey, HttpServletRequest request) {
System.err.println(dispatchKey);
		if (dispatchKey.equals("login") || dispatchKey.equals("register") || dispatchKey.equals("image")) {
			return true;
		}
		return getCurrentUser(request) != null;
	}

	public static User getCurrentUser(HttpServletRequest request) {
		if (request.getParameter("k") != null) {
			User user = User.load(request.getParameter("k"));
			if (user != null) {
				return user;
			}
		}
		return null;
	}

	public void do404(String dispatchKey, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		response.sendError(404, "Unable to find dispatch " + dispatchKey);
	}

	public void do401(String dispatchKey, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		response.sendError(401, "Unauthorized to access " + dispatchKey);
	}

	public void shutdown() {

		DatabaseManager.getInstance().shutdown();
		PlayerManager.getInstance().shutdown();
	}

	private AbstractDispatch getDispatch(String dispatchKey) {

		dispatchKey = dispatchKey.substring(0, 1).toUpperCase()
				+ dispatchKey.substring(1);
		while (dispatchKey.indexOf('-') >= 0) {
			dispatchKey = dispatchKey.substring(0, dispatchKey.indexOf('-'))
					+ dispatchKey.substring(dispatchKey.indexOf('-') + 1,
							dispatchKey.indexOf('-') + 2).toUpperCase()
					+ dispatchKey.substring(dispatchKey.indexOf('-') + 2);
		}

		if (dispatchCache.containsKey(dispatchKey)) {
			return (AbstractDispatch) dispatchCache.get(dispatchKey);
		}

		try {
			Class clazz = Class.forName("com.synchophy.server.dispatch."
					+ dispatchKey + "Dispatch");
			AbstractDispatch dispatch = (AbstractDispatch) clazz.newInstance();
			dispatchCache.put(dispatchKey, dispatch);
			return dispatch;
		} catch (ClassNotFoundException e) {

		} catch (IllegalAccessException e) {

		} catch (InstantiationException e) {
		}
		return null;
	}

	private static Long lastModified;
	protected long getLastModified(HttpServletRequest req) {
		if(ControllerServlet.lastModified == null) {
			touchLastModified();
			
		}
		return lastModified.longValue();
	}
	
	public static void touchLastModified() {
		lastModified = new Long(new Date().getTime());
	}

}