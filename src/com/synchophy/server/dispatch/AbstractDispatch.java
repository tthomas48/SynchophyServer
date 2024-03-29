package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.synchophy.server.ControllerServlet;
import com.synchophy.server.ParamException;
import com.synchophy.server.User;

public abstract class AbstractDispatch {

	public abstract Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException;

	public void write(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		Object output = null;
		try {
			output = execute(request, response);
		} catch (RuntimeException e) {
			e.printStackTrace();
			output = new HashMap();
			response.setStatus(500);
			((HashMap) output).put("error", e.getMessage());
		}
		writeJSON(output, response);
	}

	protected void writeJSON(Object output, HttpServletResponse response)
			throws IOException {
		Gson gson = new Gson();
		String json = gson.toJson(output);

		response.setHeader("Content-Type", "application/json");
		response.getWriter().write(json);
		response.getWriter().flush();
	}

	public String getRequiredParameter(HttpServletRequest request, String name) {

		String value = request.getParameter(name);
		if (value == null || value.trim().length() == 0) {
			throw new ParamException(
					"You must specify a value for the parameter '" + name
							+ "'.");
		}
		return value;
	}

	public String getOptionalParameter(HttpServletRequest request, String name) {

		return getOptionalParameter(request, name, "");
	}

	public String getOptionalParameter(HttpServletRequest request, String name,
			String defaultValue) {

		String value = request.getParameter(name);
		if (value == null || value.trim().length() == 0) {
			return defaultValue;
		}
		return value;
	}

	public User getCurrentUser(HttpServletRequest request) {

		return ControllerServlet.getCurrentUser(request);
	}

}
