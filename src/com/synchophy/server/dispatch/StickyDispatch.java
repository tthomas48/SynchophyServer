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
    String artist = getRequiredParameter(request, "artist");
    String sticky = getRequiredParameter(request, "s");

    User user = ControllerServlet.getCurrentUser(request);

    DatabaseManager.getInstance()
        .executeQuery("DELETE FROM sticky WHERE name = ? and user_id = ? and artist = ? and album = ?",
                      new Object[]{
                          name, new Long(user.getId()), artist, album
                      });
    DatabaseManager.getInstance()
    .executeQuery("INSERT INTO sticky (name, stick, user_id, artist, album) values (?, ?, ?, ?, ?)",
                  new Object[]{
                      name, sticky, new Long(user.getId()), artist, album
                  });
    
    ControllerServlet.touchLastModified();
    return Boolean.TRUE;
  }

}
