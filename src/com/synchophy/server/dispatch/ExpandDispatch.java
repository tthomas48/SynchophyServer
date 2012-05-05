package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;

public class ExpandDispatch extends AbstractDispatch {

	public static final int ITEMS_PER_PAGE = 1000;

	private Object getBody(String type, String value, String artist,
			String view, boolean filter, int page) {

		if (type.equals("letter")) {
			if (view.equals("artists")) {
				return expandArtistsLetter(filter, page);
			}
			return expandAlbumsLetter(filter, page);
		} else if (type.equals("artist")) {
			return expandArtists(filter, value, page);
		} else if (type.equals("album")) {
			return expandAlbums(filter, value, artist, page);
		}
		return null;

	}

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String type = getRequiredParameter(request, "t");
		String value = getOptionalParameter(request, "v");
		String artist = getOptionalParameter(request, "artist");
		String view = getOptionalParameter(request, "view");
		int page = Integer.parseInt(getOptionalParameter(request, "p", "0"));
		boolean filter = Boolean.valueOf(
				getRequiredParameter(request, "filter")).booleanValue();
		return getBody(type, value, artist, view, filter, page);

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

	private Object expandAlbums(boolean filter, String value, String artist,
			int page) {

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
				+ " order by upper(s.title_sort)"
				+ " limit "
				+ ITEMS_PER_PAGE
				+ " offset " + (ITEMS_PER_PAGE * page);

		// returns tracks
		List tracks = DatabaseManager.getInstance().query(query, params,
				new String[] { "name", "artist", "album", "sticky" });
		return tracks;
	}

	private Object expandArtists(boolean filter, String value, int page) {

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
						+ "order by upper(s.album_sort)"
						+ " limit "
						+ ITEMS_PER_PAGE + " offset " + (ITEMS_PER_PAGE * page),
						new String[] { value },
						new String[] { "name", "artist", "sticky" });
	}

	private Object expandArtistsLetter(boolean filter, int page) {

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
						+ "order by upper(s.artist_sort)" + " limit "
						+ ITEMS_PER_PAGE + " offset " + (ITEMS_PER_PAGE * page)

				, new String[0], new String[] { "name", "sticky" });
	}

	private Object expandAlbumsLetter(boolean filter, int page) {

		List albums = DatabaseManager
				.getInstance()
				.query("select album_sort, "
						+ "'',"
						/*+ "(select sss.artist_sort from song sss where sss.album_sort = s.album_sort limit 1), " */
						+ "coalesce(ss.stick, 0) "
						+ "from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
						+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
						+ "  and (s.title_sort = ss.name or ss.name = '*')) "
						+ getFilter(filter, "where")
						+ "group by album_sort, coalesce(ss.stick, 0) "
						+ "order by upper(album_sort)" + " limit "
						+ ITEMS_PER_PAGE + " offset " + (ITEMS_PER_PAGE * page)

				, new String[0], new String[] { "name", "artist", "sticky" });

		return albums;
	}

}
