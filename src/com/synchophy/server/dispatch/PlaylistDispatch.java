package com.synchophy.server.dispatch;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.PlayerManager;
import com.synchophy.server.db.DatabaseManager;


public class PlaylistDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String action = getRequiredParameter(request, "a");
    if (action.equals("add")) {
      
      int startPosition = DatabaseManager.getInstance().loadQueueFiles().size();
      
      String artist = getRequiredParameter(request, "artist");
      String album = getRequiredParameter(request, "album");
      String title = getRequiredParameter(request, "title");
      boolean play = Boolean.parseBoolean(getOptionalParameter(request, "play"));

      List params = new ArrayList();
      String sql = "select file from song where artist_sort = ?";
      String order = "order by artist_sort, album_sort, title_sort";
      params.add(artist);
      if (album.equals("*") == false) {
        sql += " and album_sort = ?";
        params.add(album);
        
      }
      if (title.equals("*") == false) {
        sql += " and trim(LEADING '0' FROM title_sort) = ?";
        params.add(title);
      }

      List queue = DatabaseManager.getInstance().loadQueueFiles();
      int index = queue.size();

      List toAdd = DatabaseManager.getInstance().query(sql + order,
                                                       params.toArray(new String[params.size()]),
                                                       new String[]{
                                                         "file"
                                                       });
      for (int i = 0; i < toAdd.size(); i++) {
        DatabaseManager.getInstance().executeQuery("insert into queue (index, file) values (?, ?)",
                                                   new Object[]{
                                                       new Integer(index + i),
                                                       (String) ((Map) toAdd.get(i)).get("file")
                                                   });

      }
      if(play) {
        PlayerManager.getInstance().select(startPosition);
        PlayerManager.getInstance().play();
      }
    } else if (action.equals("remove")) {
      String index = getRequiredParameter(request, "i");

      DatabaseManager.getInstance().executeQuery("delete from queue where index = ?", new Object[]{
        Integer.getInteger(index)
      });

    } else if (action.equals("list")) {
      Map list = new HashMap();
      list.put("queue", DatabaseManager.getInstance().loadQueue());
      list.put("current", PlayerManager.getInstance().getPosition());
      return list;

    } else if (action.equals("clear")) {
      DatabaseManager.getInstance().executeQuery("delete from queue", new Object[0]);

    } else if (action.equals("save")) {
      String name = getRequiredParameter(request, "n");

    } else if (action.equals("load")) {
      String name = getRequiredParameter(request, "n");

    } else {
      throw new RuntimeException("Invalid value for the parameter 'a'.");
    }
    return Boolean.TRUE;
  }

}
