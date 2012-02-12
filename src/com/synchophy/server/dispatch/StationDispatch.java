package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ControllerServlet;
import com.synchophy.server.User;
import com.synchophy.server.db.DatabaseManager;

public class StationDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String action = getRequiredParameter(request, "a");
		if (action.equals("add")) {
			String name = getRequiredParameter(request, "n");
			String url = getRequiredParameter(request, "u");
			User user = ControllerServlet.getCurrentUser(request);

			DatabaseManager
					.getInstance()
					.executeQuery(
							"insert into station (name, url, user_id) values (?, ?, ?)",
							new Object[] { name, url, new Long(user.getId()) });

		} else if (action.equals("delete")) {
			String id = getRequiredParameter(request, "id");

			DatabaseManager.getInstance().executeQuery(
					"delete from station where id = ?",
					new Object[] { new Integer(id) });
		} else if (action.equals("list")) {
			return DatabaseManager.getInstance().query(
					"select id, name, url from station order by name", new Object[0],
					new String[] { "id", "name", "url" });

		} else {
			throw new RuntimeException("Invalid value for the parameter 'a'.");
		}
		return Boolean.TRUE;
	}

}
