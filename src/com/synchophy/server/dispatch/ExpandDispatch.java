package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;

public class ExpandDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String type = getRequiredParameter(request, "t");
		String value = getRequiredParameter(request, "v");
		String artist = getOptionalParameter(request, "artist");
		String view = getOptionalParameter(request, "view");
		boolean filter = Boolean.valueOf(getRequiredParameter(request,
				"filter")).booleanValue();

		if (type.equals("letter")) {
			if (view.equals("artists")) {
				return expandArtistsLetter(filter, value);
			}
			return expandAlbumsLetter(filter, value);
		} else if (type.equals("artist")) {
			return expandArtists(filter, value);
		} else if (type.equals("album")) {
			return expandAlbums(filter, value, artist);
		}
		return null;

	}

	private String getFilter(boolean filter) {
		if (!filter) {
			return "";
		}
		return "  and coalesce(ss.stick, 0) >= 0 ";
	}

	private Object expandAlbums(boolean filter, String value, String artist) {

		String[] params = new String[] { value, artist };
		String artistFilter = "  and s.artist_sort = ? ";
		if (artist == null || artist.equals("")) {
			artistFilter = "";
			params = new String[] { value };
		}
		
		String query = "select trim(LEADING '0' FROM s.title_sort), s.artist_sort, s.album_sort, coalesce(ss.stick, 0) "
			+ "from song s left outer join sticky ss on ((s.album = ss.album or ss.album = '*') "
			+ "  and (s.artist = ss.artist or ss.artist = '*') "
			+ "  and (s.title = ss.name or ss.name = '*')) "
			+ " where s.album_sort = ? "
			+ artistFilter
			+ getFilter(filter)
			+ " group by s.title_sort, s.artist_sort, s.album_sort, coalesce(ss.stick, 0)  "
			+ " order by upper(s.title_sort)";
		System.err.println(query);
		System.err.println(params);
		

		// returns tracks
		return DatabaseManager
				.getInstance()
				.query(query, params,
						new String[] { "name", "artist", "album", "sticky" });
	}

	private Object expandArtists(boolean filter, String value) {

		// returns albums
		return DatabaseManager
				.getInstance()
				.query("select s.album_sort, s.artist_sort, coalesce(ss.stick, 0) "
						+ "from song s left outer join sticky ss on ((s.album = ss.album or ss.album = '*') "
						+ "  and (s.artist = ss.artist or ss.artist = '*') "
						+ "  and (s.title = ss.name or ss.name = '*')) "
						+ "where s.artist_sort = ? "
						+ getFilter(filter)
						+ "group by s.album_sort, s.artist_sort, coalesce(ss.stick, 0) "
						+ "order by upper(s.album_sort)",
						new String[] { value },
						new String[] { "name", "artist", "sticky" });
	}

	private Object expandArtistsLetter(boolean filter, String value) {

		return DatabaseManager
				.getInstance()
				.query("select s.artist_sort, coalesce(ss.stick, 0) "
						+ "from song s left outer join sticky ss on ((s.album = ss.album or ss.album = '*') "
						+ "  and (s.artist = ss.artist or ss.artist = '*') "
						+ "  and (s.title = ss.name or ss.name = '*')) "
						+ "where s.artist_key = ? " + getFilter(filter)
						+ "group by s.artist_sort, coalesce(ss.stick, 0) "
						+ "order by upper(s.artist_sort)",
						new String[] { value },
						new String[] { "name", "sticky" });
	}

	private Object expandAlbumsLetter(boolean filter, String value) {

		return DatabaseManager
				.getInstance()
				.query("select album_sort, '', coalesce(ss.stick, 0) "
						+ "from song s left outer join sticky ss on ((s.album = ss.album or ss.album = '*') "
						+ "  and (s.artist = ss.artist or ss.artist = '*') "
						+ "  and (s.title = ss.name or ss.name = '*')) "
						+ "where album_key = ? " + getFilter(filter)
						+ "group by album_sort, coalesce(ss.stick, 0) "
						+ "order by upper(album_sort)", new String[] { value },
						new String[] { "name", "artist", "sticky" });
	}

}
