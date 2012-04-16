package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;

public class ExpandDispatch extends AbstractDispatch {
	
	private static Map cache = new HashMap();

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String type = getRequiredParameter(request, "t");
		String value = getOptionalParameter(request, "v");
		String artist = getOptionalParameter(request, "artist");
		String view = getOptionalParameter(request, "view");
		boolean filter = Boolean.valueOf(
				getRequiredParameter(request, "filter")).booleanValue();
		
		String cacheKey = getKey(type, value, artist, view, filter);
		if(cache.containsKey(cacheKey)) {
			return cache.get(cacheKey);
		}

		if (type.equals("letter")) {
			if (view.equals("artists")) {
				Object output = expandArtistsLetter(filter);
				cache.put(cacheKey, output);
				return output;
			}
			Object output = expandAlbumsLetter(filter);
			cache.put(cacheKey, output);
			return output;
		} else if (type.equals("artist")) {
			Object output = expandArtists(filter, value);
			cache.put(cacheKey, output);
			return output;
		} else if (type.equals("album")) {
			Object output = expandAlbums(filter, value, artist);
			cache.put(cacheKey, output);
			return output;
		}
		return null;
	}
	
	public static void clearCache() {
		cache.clear();
	}
	
	private String getKey(String type, String value, String artist, String view, boolean filter) {
		return type + "~" + value + "~" + artist + "~" + view + "~" + Boolean.toString(filter);
	}

	private String getFilter(boolean filter) {
		return getFilter(filter, "and");
	}

	private String getFilter(boolean filter, String conjunction) {
		if (!filter) {
			return "";
		}
		return "  " + conjunction + " coalesce(ss.stick, 0) >= 0 ";
	}

	private Object expandAlbums(boolean filter, String value, String artist) {

		String[] params = new String[] { value, artist };
		String artistFilter = "  and s.artist_sort = ? ";
		if (artist == null || artist.equals("")) {
			artistFilter = "";
			params = new String[] { value };
		}

		String query = "select trim(LEADING '0' FROM s.title_sort) name, s.artist_sort artist, s.album_sort album, coalesce(ss.stick, 0) sticky "
				+ "from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
				+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
				+ "  and (s.title_sort = ss.name or ss.name = '*')) "
				+ " where s.album_sort = ? "
				+ getFilter(filter)
				+ artistFilter

				+ " group by s.title_sort, s.artist_sort, s.album_sort, coalesce(ss.stick, 0)  "
				+ " order by upper(s.title_sort)";
		// returns tracks
		List tracks = DatabaseManager.getInstance().query(query, params,
				new String[] { "name", "artist", "album", "sticky" });
		return tracks;
	}

	private Object expandArtists(boolean filter, String value) {

		// returns albums
		return DatabaseManager
				.getInstance()
				.query("select s.album_sort, s.artist_sort, coalesce(ss.stick, 0) "
						+ "from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
						+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
						+ "  and (s.title_sort = ss.name or ss.name = '*')) "
						+ "where s.artist_sort = ? "
						+ getFilter(filter)
						+ "group by s.album_sort, s.artist_sort, coalesce(ss.stick, 0) "
						+ "order by upper(s.album_sort)",
						new String[] { value },
						new String[] { "name", "artist", "sticky" });
	}

	private Object expandArtistsLetter(boolean filter) {

		return DatabaseManager
				.getInstance()
				.query("select s.artist_sort, coalesce(ss.stick, 0) "
						+ "from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
						+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
						+ "  and (s.title_sort = ss.name or ss.name = '*')) "
						+ getFilter(filter, "where")
						// + " and exists (select 'X' "
						// + "from song s1 left outer join sticky ss1"
						// +
						// " on ((s1.album_sort = ss1.album or ss1.album = '*')"
						// +
						// " and (s1.artist_sort = ss1.artist or ss1.artist = '*')"
						// +
						// " and (s1.title_sort = ss1.name or ss1.name = '*'))"
						// + " where s.artist_sort = s1.artist_sort) "
						+ "group by s.artist_sort, coalesce(ss.stick, 0) "
						+ "order by upper(s.artist_sort)", new String[0],
						new String[] { "name", "sticky" });
	}

	private Object expandAlbumsLetter(boolean filter) {

		List albums = DatabaseManager
				.getInstance()
				.query("select album_sort, " +
						"(select sss.artist_sort from song sss where sss.album_sort = s.album_sort limit 1), " +
						"coalesce(ss.stick, 0) "
						+ "from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
						+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
						+ "  and (s.title_sort = ss.name or ss.name = '*')) "
						+ getFilter(filter, "where")
						+ "group by album_sort, coalesce(ss.stick, 0) "
						+ "order by upper(album_sort)", new String[0],
						new String[] { "name", "artist", "sticky" });
		
		return albums;
	}
}
