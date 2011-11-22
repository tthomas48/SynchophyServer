package com.synchophy.server.dispatch;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ControllerServlet;
import com.synchophy.server.User;
import com.synchophy.server.db.DatabaseManager;


public class StickyDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String name = getRequiredParameter(request, "n");
    String album = getRequiredParameter(request, "album");
    String artist = getOptionalParameter(request, "artist");
    String type = getRequiredParameter(request, "t");
    String sticky = getRequiredParameter(request, "s");
    String view = getRequiredParameter(request, "v");

    if (view.equals("albums") && "album".equals(type)) {
      artist = "*";
    }
    
    User user = ControllerServlet.getCurrentUser(request);

    DatabaseManager.getInstance()
        .executeQuery("DELETE FROM sticky WHERE name = ? and type = ? and user_id = ? and artist = ? and album = ?",
                      new Object[]{
                          name, type, new Long(user.getId()), artist, album
                      });
    DatabaseManager.getInstance()
    .executeQuery("INSET INTO sticky (id, name, type, stick, user, artist, album) values (null, ?, ?, ?, ?, ?, ?)",
                  new Object[]{
                      name, type, sticky, new Long(user.getId()), artist, album
                  });
    return Boolean.TRUE;
  }

}
