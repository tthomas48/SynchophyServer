package com.synchophy.server.dispatch;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ControllerServlet;
import com.synchophy.server.User;
import com.synchophy.server.db.DatabaseManager;


public class LoadSettingsDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    User user = ControllerServlet.getCurrentUser(request);

    return DatabaseManager.getInstance().query("select key, value from settings where user_id = ?",
                                               new Object[]{
                                                 new Long(user.getId())

                                               },
                                               new String[]{
                                                   "key", "value"
                                               });
  }

}
