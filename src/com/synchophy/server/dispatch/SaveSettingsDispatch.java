package com.synchophy.server.dispatch;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ControllerServlet;
import com.synchophy.server.User;
import com.synchophy.server.db.DatabaseManager;


public class SaveSettingsDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    User user = ControllerServlet.getCurrentUser(request);
    String key = getRequiredParameter(request, "n");
    String value = getRequiredParameter(request, "v");

    DatabaseManager.getInstance()
        .executeQuery("delete from settings where user_id = ? and key_name = ?", new Object[]{
            new Long(user.getId()), key
        });

    DatabaseManager.getInstance()
        .executeQuery("insert into settings (key_name, value, user_id) values (?, ?, ?)", new Object[]{
            key, value, new Long(user.getId()),
        });

    return Boolean.TRUE;
  }

}
