package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;

public class SearchDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// song
		// artist
		// album
		// tag
		// playlist

		String query = getRequiredParameter(request, "q");

		Map results = new HashMap();
		results.put(
				"artist",
				DatabaseManager.getInstance().query(
						"select distinct(artist_sort) " + "from song "
								+ "where artist_sort like ? "
								+ "order by upper(artist_sort)",
						new String[] { "%" + query + "%" },
						new String[] { "name" }));
		results.put(
				"album",
				DatabaseManager.getInstance().query(
						"select album_sort, artist_sort " + "from song "
								+ "where album_sort like ? "
								+ " group by album_sort, artist_sort "
								+ " order by upper(album_sort)",
						new String[] { "%" + query + "%" },
						new String[] { "name", "artist" }));

		results.put(
				"track",
				DatabaseManager
						.getInstance()
						.query("select trim(LEADING '0' FROM title_sort), album_sort, artist_sort "
								+ "from song "
								+ "where title_sort like ? "
								+ " group by title_sort, album_sort, artist_sort "
								+ " order by upper(title_sort)",
								new String[] { "%" + query + "%" },
								new String[] { "name", "album", "artist" }));

		results.put(
				"tag",
				DatabaseManager.getInstance().query(
						"select tag_name " + "from tag "
								+ "where tag_name like ? ",
						new String[] { "%" + query + "%" },
						new String[] { "name" }));

		results.put(
				"station",
				DatabaseManager.getInstance()
						.query("select name " + "from station "
								+ "where name like ? ",
								new String[] { "%" + query + "%" },
								new String[] { "name" }));
		return results;

	}

}
