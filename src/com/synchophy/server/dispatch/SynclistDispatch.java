package com.synchophy.server.dispatch;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.MetricManager;
import com.synchophy.server.User;
import com.synchophy.server.db.DatabaseManager;

public class SynclistDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		return generateSynclist(getCurrentUser(request).getId());

		/*
		 * String query = "select file, size " + " from song " + andClause;
		 * return DatabaseManager.getInstance().query(query, params.toArray(new
		 * Object[params.size()]), new String[] {"file", "size"});
		 */
	}

	public Collection<Map<String, Object>> generateSynclist(long user_id) {

		User user = User.load((int) user_id);
		long size = Long.parseLong(DatabaseManager.getInstance().getSetting(
				user, "syncSize"));

		size *= 1048576;
		System.err.println("Looking for " + size + " bytes of music.");

		Map<String, Map<String, Object>> list = new HashMap<String, Map<String, Object>>();

		long recentMusic = (long) (size * 0.20);
		long greenMusic = (long) (size * 0.80);

		System.err.println("Attempting to add " + recentMusic
				+ " bytes of recent music.");

		Calendar lastThirty = Calendar.getInstance();
		lastThirty.add(Calendar.DAY_OF_YEAR, -30);
		Date date = lastThirty.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		String sql = "select s.file, s.size "
				+ " from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
				+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
				+ "  and (s.title_sort = ss.name or ss.name = '*')"
				+ "  and ss.user_id = ?) "
				+ " where INSERT_TIMESTAMP > ? "
				+ "  and coalesce(ss.stick, 0) >= 0 "
				+ " order by coalesce(ss.stick, 0) desc, s.album_sort, insert_timestamp desc ";
		List<Map<String, Object>> songs = DatabaseManager.getInstance().query(
				sql, new Object[] { user_id, format.format(date) },
				new String[] { "file", "size" });
		for (Map<String, Object> song : songs) {
			int fileSize = (Integer) song.get("size");
			if (fileSize > recentMusic) {
				continue;
			}
			recentMusic -= fileSize;
			size -= fileSize;
			String file = (String) song.get("file");
			list.put(file, song);
		}

		System.err.println("Attempting to add " + greenMusic
				+ " bytes of top albums music.");
		MetricManager metricManager = new MetricManager();
		Map<String, Map<String, Object>> topAlbums = metricManager
				.getTopAlbums(user);
		for (Map.Entry<String, Map<String, Object>> entry : topAlbums
				.entrySet()) {
			Map<String, Object> song = entry.getValue();
			int fileSize = (Integer) song.get("size");
			if (fileSize > greenMusic) {
				continue;
			}
			String file = (String) song.get("file");
			if (list.containsKey(file)) {
				continue;
			}
			greenMusic -= fileSize;
			size -= fileSize;
			list.put(entry.getKey(), entry.getValue());
		}

		System.err.println("Attempting to add " + greenMusic
				+ " bytes of albums by top artists.");
		Map<String, Map<String, Object>> topArtists = metricManager
				.getTopArtists(user);
		for (Map.Entry<String, Map<String, Object>> entry : topArtists
				.entrySet()) {
			Map<String, Object> song = entry.getValue();
			int fileSize = (Integer) song.get("size");
			if (fileSize > greenMusic) {
				continue;
			}
			String file = (String) song.get("file");
			if (list.containsKey(file)) {
				continue;
			}
			greenMusic -= fileSize;
			size -= fileSize;
			list.put(entry.getKey(), entry.getValue());
		}

		System.err.println("Attempting to add " + greenMusic
				+ " bytes of albums of top tracks.");
		Map<String, Map<String, Object>> topTracks = metricManager
				.getTopTracks(user);
		for (Map.Entry<String, Map<String, Object>> entry : topTracks
				.entrySet()) {
			Map<String, Object> song = entry.getValue();
			int fileSize = (Integer) song.get("size");
			if (fileSize > greenMusic) {
				continue;
			}
			String file = (String) song.get("file");
			if (list.containsKey(file)) {
				continue;
			}
			greenMusic -= fileSize;
			size -= fileSize;
			list.put(entry.getKey(), entry.getValue());
		}
		

		System.err.println("Attempting to add " + size
				+ " bytes of other green music.");
		sql = "select s.file, s.size "
				+ " from song s, sticky ss where (s.album_sort = ss.album or ss.album = '*') "
				+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
				+ "  and (s.title_sort = ss.name or ss.name = '*') "
				+ "  and ss.user_id = ? and ss.stick = 1 "
				+ " order by s.album_sort, insert_timestamp desc ";
		songs = DatabaseManager.getInstance().query(sql,
				new Object[] { user_id }, new String[] { "file", "size" });
		System.err.println("Found " + songs.size() + " of other green music.");
		for (Map<String, Object> song : songs) {
			int fileSize = (Integer) song.get("size");
			if (fileSize > size) {
				continue;
			}
			String file = (String) song.get("file");
			if (list.containsKey(file)) {
				continue;
			}
			System.err.println(file);

			size -= fileSize;
			list.put(file, song);
		}

		System.err.println("Attempting to add " + size
				+ " bytes of random music.");
		sql = "select s.file, s.size "
				+ " from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
				+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
				+ "  and (s.title_sort = ss.name or ss.name = '*')"
				+ "  and ss.user_id = ?) "
				+ " where coalesce(ss.stick, 0) >= 0 "
				/* " where s.user_id = ? " + */
				+ " order by coalesce(ss.stick, 0) desc, s.album_sort, insert_timestamp desc ";
		songs = DatabaseManager.getInstance().query(sql,
				new Object[] { user_id }, new String[] { "file", "size" });
		for (Map<String, Object> song : songs) {
			int fileSize = (Integer) song.get("size");
			if (fileSize > size) {
				continue;
			}
			size -= fileSize;
			String file = (String) song.get("file");
			list.put(file, song);
		}

		System.err.println("Ended up with " + size + " bytes left over.");

		// so let's split it into 20% recent music, 80% green music, and random
		// for any leftover

		// first let's look for recent music

		// then let's look for green music

		return list.values();
	}

	public static void main(String... args) {

		SynclistDispatch dispatch = new SynclistDispatch();
		System.err.println(dispatch.generateSynclist(1));

	}

}
