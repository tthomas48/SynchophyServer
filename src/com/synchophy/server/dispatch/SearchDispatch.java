package com.synchophy.server.dispatch;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;


public class SearchDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String type = getRequiredParameter(request, "t");
    String query = getRequiredParameter(request, "q");

    if (type.equals("artist")) {
      return DatabaseManager.getInstance().query("select distinct(artist_sort) "
                                                     + "from song "
                                                     + "where artist_sort like ? "
                                                     + "order by upper(artist_sort)",
                                                 new String[]{
                                                   "%" + query + "%"
                                                 },
                                                 new String[]{
                                                   "name"
                                                 });
    } else if (type.equals("album")) {
      return DatabaseManager.getInstance().query("select album_sort, artist_sort "
                                                     + "from song "
                                                     + "where album_sort like ? "
                                                     + " group by album_sort, artist_sort "
                                                     + " order by upper(album_sort)",
                                                 new String[]{
                                                   "%" + query + "%"
                                                 },
                                                 new String[]{
                                                     "name", "artist"
                                                 });

    } else if (type.equals("track")) {
      return DatabaseManager.getInstance().query("select trim(LEADING '0' FROM title_sort), album_sort, artist_sort "
          + "from song "
          + "where title_sort like ? "
          + " group by title_sort, album_sort, artist_sort "
          + " order by upper(title_sort)",
      new String[]{
        "%" + query + "%"
      },
      new String[]{
          "name", "album", "artist"
      });

    }

    return null;
  }

}
