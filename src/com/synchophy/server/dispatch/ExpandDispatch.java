package com.synchophy.server.dispatch;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;


public class ExpandDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String type = getRequiredParameter(request, "t");
    String value = getRequiredParameter(request, "v");
    String artist = getOptionalParameter(request, "artist");
    String view = getOptionalParameter(request, "view");

    if (type.equals("letter")) {
      if (view.equals("artists")) {
        return expandArtistsLetter(value);
      }
      return expandAlbumsLetter(value);
    } else if (type.equals("artist")) {
      return expandArtists(value);
    } else if (type.equals("album")) {
      return expandAlbums(value, artist);
    }
    return null;

  }


  private Object expandAlbums(String value, String artist) {

    // returns tracks
    return DatabaseManager.getInstance()
        .query("select trim(LEADING '0' FROM s.title_sort), s.artist_sort, s.album_sort, coalesce(ss.stick, 0) "
                   + "from song s left outer join sticky ss on ((s.album = ss.album or ss.album = '*') "
                   + "  and (s.artist = ss.artist or ss.artist = '*') "
                   + "  and (s.title = ss.name or ss.name = '*')) "
                   + " where s.album_sort = ? "
                   + "  and s.artist_sort = ? "
                   + " group by s.title_sort, s.artist_sort, s.album_sort, coalesce(ss.stick, 0)  "
                   + " order by upper(s.title_sort)",
               new String[]{
                   value, artist
               },
               new String[]{
                   "name", "artist", "album", "sticky"
               });
  }


  private Object expandArtists(String value) {

    // returns albums
    return DatabaseManager.getInstance()
        .query("select s.album_sort, s.artist_sort, coalesce(ss.stick, 0) "
                   + "from song s left outer join sticky ss on ((s.album = ss.album or ss.album = '*') "
                   + "  and (s.artist = ss.artist or ss.artist = '*') "
                   + "  and (s.title = ss.name or ss.name = '*')) "
                   + "where s.artist_sort = ? "
                   + "group by s.album_sort, s.artist_sort, coalesce(ss.stick, 0) "
                   + "order by upper(s.album_sort)",
               new String[]{
                 value
               },
               new String[]{
                   "name", "artist", "sticky"
               });
  }


  private Object expandArtistsLetter(String value) {

    return DatabaseManager.getInstance()
        .query("select s.artist_sort, coalesce(ss.stick, 0) "
                   + "from song s left outer join sticky ss on ((s.album = ss.album or ss.album = '*') "
                   + "  and (s.artist = ss.artist or ss.artist = '*') "
                   + "  and (s.title = ss.name or ss.name = '*')) "
                   + "where s.artist_key = ? "
                   + "group by s.artist_sort, coalesce(ss.stick, 0) "
                   + "order by upper(s.artist_sort)",
               new String[]{
                 value
               },
               new String[]{
                   "name", "sticky"
               });
  }


  private Object expandAlbumsLetter(String value) {

    return DatabaseManager.getInstance()
        .query("select album_sort, artist_sort, coalesce(ss.stick, 0) "
                   + "from song s left outer join sticky ss on ((s.album = ss.album or ss.album = '*') "
                   + "  and (s.artist = ss.artist or ss.artist = '*') "
                   + "  and (s.title = ss.name or ss.name = '*')) "
                   + "where album_key = ? "
                   + "group by album_sort, artist_sort, coalesce(ss.stick, 0) "
                   + "order by upper(album_sort)",
               new String[]{
                 value
               },
               new String[]{
                   "name", "artist", "sticky"
               });
  }

}
