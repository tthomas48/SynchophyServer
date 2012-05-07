package com.synchophy.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.synchophy.server.db.DatabaseManager;
import com.synchophy.util.FinallyUtils;

import de.umass.lastfm.Album;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleResult;
import edu.emory.mathcs.backport.java.util.Collections;

public class MetricManager {

	public static final String LASTFM_API_KEY = "9e91826464c3707512f7856dcd57e15f";
	public static final String LASTFM_API_SECRET = "d607f37300b75b3fe566b47088c62aff";

	public MetricManager() {

	}

	public String getLastFmApiKey() {
		return MetricManager.LASTFM_API_KEY;
	}

	private void saveMetric(String filename, String key) {

		DatabaseManager.getInstance().executeQuery(
				"insert into metric (" + "file, " + "track, " + "artist, "
						+ "artist_sort, " + "artist_key, " + "album, "
						+ "album_sort, " + "album_key, " + "title, "
						+ "title_sort, " + "title_key, " + "size, "
						+ "metric_type) " + "(select " + "file, " + "track, "
						+ "artist, " + "artist_sort, " + "artist_key, "
						+ "album, " + "album_sort, " + "album_key, "
						+ "title, " + "title_sort, " + "title_key, " + "size, "
						+ "? from song where file = ?) ",
				new String[] { key, filename });

	}

	private void scrobble(User user, Map song) {
		if (user == null || song == null) {
			return;
		}

		String lastfmUsername = DatabaseManager.getInstance().getSetting(user,
				"lastfmUsername");
		String lastfmPassword = DatabaseManager.getInstance().getSetting(user,
				"lastfmPassword");

		try {
			Session session = Authenticator.getMobileSession(lastfmUsername,
					lastfmPassword, LASTFM_API_KEY, LASTFM_API_SECRET);
			ScrobbleResult result = Track.updateNowPlaying(
					(String) song.get("artist"), (String) song.get("name"),
					session);
			System.out.println("ok: "
					+ (result.isSuccessful() && !result.isIgnored()));
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, Map<String, Object>> getTopAlbums(User user) {

		String lastfmUsername = DatabaseManager.getInstance().getSetting(user,
				"lastfmUsername");

		Collection<Album> albums = de.umass.lastfm.User.getTopAlbums(
				lastfmUsername, LASTFM_API_KEY);

		Map<String, Map<String, Object>> results = new HashMap<String, Map<String, Object>>();

		String sql = "select s.file, s.size "
				+ " from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
				+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
				+ "  and (s.title_sort = ss.name or ss.name = '*')"
				+ "  and ss.user_id = ?) "
				+ " where lower(s.album) = lower(?)"
				+ "  and lower(s.artist) = lower(?) "
				+ "  and coalesce(ss.stick, 0) >= 0"
				+ " order by coalesce(ss.stick, 0) desc, insert_timestamp desc ";
		PreparedStatement sth = null;
		try {
			sth = DatabaseManager.getInstance().prepare(sql);
			for (Album album : albums) {
				try {
					sth.setLong(1, user.getId());
					sth.setString(2, album.getArtist());
					sth.setString(3, album.getName());
					ResultSet rs = sth.executeQuery();
					while (rs.next()) {
						String file = rs.getString(1);
						Integer size = rs.getInt(2);
						Map<String, Object> song = new HashMap<String, Object>();
						song.put("file", file);
						song.put("size", size);
						results.put(file, song);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} finally {
			FinallyUtils.close(sth);
		}

		return results;

	}

	public Map<String, Map<String, Object>> getTopArtists(User user) {

		String lastfmUsername = DatabaseManager.getInstance().getSetting(user,
				"lastfmUsername");

		Collection<Artist> artists = de.umass.lastfm.User.getTopArtists(
				lastfmUsername, LASTFM_API_KEY);

		Map<String, Map<String, Object>> results = new HashMap<String, Map<String, Object>>();

		String albumSql = "select distinct(album) from song where lower(artist) = lower(?)";

		String sql = "select s.file, s.size "
				+ " from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
				+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
				+ "  and (s.title_sort = ss.name or ss.name = '*')"
				+ "  and ss.user_id = ?) "
				+ " where lower(s.album) = lower(?)"
				+ "  and lower(s.artist) = lower(?) "
				+ "  and coalesce(ss.stick, 0) >= 0"
				+ " order by coalesce(ss.stick, 0) desc, insert_timestamp desc ";
		PreparedStatement sth = null;
		PreparedStatement albumSth = null;
		try {
			sth = DatabaseManager.getInstance().prepare(sql);
			albumSth = DatabaseManager.getInstance().prepare(albumSql);
			for (Artist artist : artists) {
				try {
					List<String> albums = new ArrayList<String>();
					albumSth.setString(1, artist.getName());
					ResultSet rs = albumSth.executeQuery();
					while (rs.next()) {
						albums.add(rs.getString(1));
					}

					for (String album : albums) {
						sth.setLong(1, user.getId());
						sth.setString(2, artist.getName());
						sth.setString(3, album);
						rs = sth.executeQuery();
						while (rs.next()) {
							String file = rs.getString(1);
							Integer size = rs.getInt(2);
							Map<String, Object> song = new HashMap<String, Object>();
							song.put("file", file);
							song.put("size", size);
							results.put(file, song);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} finally {
			FinallyUtils.close(sth);
			FinallyUtils.close(albumSth);
		}

		return results;
	}

	public Map<String, Map<String, Object>> getTopTracks(User user) {

		String lastfmUsername = DatabaseManager.getInstance().getSetting(user,
				"lastfmUsername");

		Collection<Track> tracks = de.umass.lastfm.User.getTopTracks(
				lastfmUsername, LASTFM_API_KEY);

		Map<String, Map<String, Object>> results = new HashMap<String, Map<String, Object>>();

		String sql = "select s.file, s.size "
				+ " from song s left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
				+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
				+ "  and (s.title_sort = ss.name or ss.name = '*')"
				+ "  and ss.user_id = ?) "
				+ " where lower(s.artist) = lower(?) "
				+ "  and lower(s.title) = lower(?) "
				+ "  and coalesce(ss.stick, 0) >= 0"
				+ " order by coalesce(ss.stick, 0) desc, insert_timestamp desc ";
		PreparedStatement sth = null;
		try {
			sth = DatabaseManager.getInstance().prepare(sql);
			for (Track track : tracks) {
				try {
					sth.setLong(1, user.getId());
					sth.setString(2, track.getArtist());
					sth.setString(3, track.getName());
					ResultSet rs = sth.executeQuery();
					while (rs.next()) {
						String file = rs.getString(1);
						Integer size = rs.getInt(2);
						Map<String, Object> song = new HashMap<String, Object>();
						song.put("file", file);
						song.put("size", size);
						results.put(file, song);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} finally {
			FinallyUtils.close(sth);
		}

		return results;
	}

	// public Collection<Artist> getRecommendedArtists(User user) {
	//
	// String lastfmUsername = DatabaseManager.getInstance().getSetting(user,
	// "lastfmUsername");
	//
	// return de.umass.lastfm.User.getRecommendedArtists(lastfmUsername,
	// LASTFM_API_KEY);
	// }

	public void started(String file) {
		saveMetric(file, "s");
	}

	public void finished(String filename, User user, Map song) {
		saveMetric(filename, "f");
		scrobble(user, song);

	}

	public void next(String file) {
		saveMetric(file, "n");
	}

	public void previous(String file) {
		saveMetric(file, "p");
	}

	public void first(String file) {
		saveMetric(file, "1");
	}

	public void last(String file) {
		saveMetric(file, "l");

	}

	public void select(String file) {
		saveMetric(file, "i");
	}

	public void getNextFiveAndInsert(User user, Map song, int needed) {
		List nextFive = getNextFive(user, song, needed);
		if (nextFive == null) {
			return;
		}
		Iterator i = nextFive.iterator();
		while (i.hasNext()) {
			Map map = (Map) i.next();
			PlayerManager.getInstance().addSong(user, map, false);
		}
	}

	public List getNextFive(User user, Map song, int needed) {
		if (user == null || song == null) {
			return null;
		}

		// String lastfmUsername =
		// DatabaseManager.getInstance().getSetting(user,
		// "lastfmUsername");
		// String lastfmPassword =
		// DatabaseManager.getInstance().getSetting(user,
		// "lastfmPassword");
		//
		// if ("".equals(lastfmUsername.trim())
		// || "".equals(lastfmPassword.trim())) {
		// return;
		// }

		List similar = new LinkedList();
		try {

			Collection tracks = Track.getSimilar((String) song.get("artist"),
					(String) song.get("name"), LASTFM_API_KEY);
			System.err.println("Got back " + tracks.size()
					+ " suggestions for similar to "
					+ (String) song.get("name"));
			Iterator iterator = tracks.iterator();
			while (iterator.hasNext()) {
				Track track = (Track) iterator.next();

				List params = new ArrayList();

				String sql = "select s.file, s.artist, s.album, s.title, count(s.file) "
						+ "from song s "
						+ " left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
						+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
						+ "  and (s.title_sort = ss.name or ss.name = '*')"
						+ "  and ss.user_id = ?) "
						+ " left outer join metric m on ((s.album_sort = m.album_sort) "
						+ "  and (s.artist_sort = m.artist_sort) "
						+ "  and (s.title_sort = m.title_sort)) "
						+ " where s.artist != ?";
				// omit songs by the same artist
				params.add(new Long(user.getId()));
				params.add(song.get("artist"));

				String conjunction = "and";
				if (track.getName() != null) {
					sql += conjunction + " s.title = ? ";
					conjunction = "and";
					params.add(track.getName());
				}
				if (track.getArtist() != null) {
					sql += conjunction + " s.artist = ? ";
					conjunction = "and";
					params.add(track.getArtist());
				}
				if (track.getAlbum() != null) {
					sql += conjunction + " s.album = ? ";
					conjunction = "and";
					params.add(track.getAlbum());
				}
				sql += conjunction
						+ " coalesce(ss.stick, 0) >= 0 group by s.artist, s.album, s.title, s.file";

				List list = DatabaseManager.getInstance().query(
						sql,
						params.toArray(new Object[params.size()]),
						new String[] { "file", "artist", "album", "name",
								"count" });
				similar.addAll(list);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// now let's go ahead and order this by least plays to hopefully keep it
		// from repeating stuff

		Collections.sort(similar, new Comparator() {
			public int compare(Object o1, Object o2) {
				Map map1 = (Map) o1;
				Map map2 = (Map) o2;

				return ((Integer) map1.get("count")).compareTo((Integer) map2
						.get("count"));
			}
		});

		List toQueue = new ArrayList();
		List artistsSeen = PlayerManager.getInstance().getArtistsInQueue();
		for (int i = 0; i < similar.size(); i++) {
			if (toQueue.size() == needed) {
				break;
			}

			// TODO: We should make sure the track/artist is not already in the
			// queue
			Map map = (Map) similar.get(i);
			if (!artistsSeen.contains(map.get("artist"))) {
				toQueue.add(map);
				artistsSeen.add(map.get("artist"));
			}
		}
		long offset = 1;
		while (toQueue.size() != 5) {
			System.err.println("Getting more stuff");

			String qmarks = "";
			for (int i = 0; i < artistsSeen.size(); i++) {
				if (qmarks.length() != 0) {
					qmarks += ",";
				}
				qmarks += "?";
			}

			String sql = "select s.file, s.artist, s.album, s.title, count(s.file) "
					+ "from song s "
					+ " left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
					+ "  and (s.artist_sort = ss.artist or ss.artist = '*') "
					+ "  and (s.title_sort = ss.name or ss.name = '*')"
					+ "  and ss.user_id = ?) "
					+ " left outer join metric m on ((s.album_sort = m.album_sort) "
					+ "  and (s.artist_sort = m.artist_sort) "
					+ "  and (s.title_sort = m.title_sort)) "
					+ " where coalesce(ss.stick, 0) >= 0 ";
			if (qmarks.length() > 0) {
				sql += " and s.artist not in (" + qmarks + ")";
			}
			sql += "group by s.artist, s.album, s.title, s.file "
					+ "order by count(s.file) asc " + "limit 20 offset "
					+ offset;
			offset += 20;

			List params = new ArrayList();
			params.add(new Long(user.getId()));
			params.addAll(artistsSeen);

			List list = DatabaseManager.getInstance()
					.query(sql,
							params.toArray(new Object[params.size()]),
							new String[] { "file", "artist", "album", "name",
									"count" });
			if (list.size() == 0) {
				break;
			}
			Iterator i = list.iterator();
			while (i.hasNext()) {
				Map map = (Map) i.next();
				System.err.println("Looking at " + map);
				if (toQueue.size() == 5) {
					System.err.println("Got five in the queue.");
					break;
				}
				if (!artistsSeen.contains(map.get("artist"))) {
					System.err.println("Adding artist");
					toQueue.add(map);
					artistsSeen.add(map.get("artist"));
				}
			}
		}

		return toQueue;
	}

	public List getDownloadList(User user, Map song, int needed) {
		return null;
	}

	public static void main(String[] args) throws Exception {
		Map song = new HashMap();
		song.put("artist", "Depeche Mode");
		song.put("album", "Violator");
		song.put("name", "Personal Jesus");

		MetricManager manager = new MetricManager();
		User user = User.login("tim", "tim");

		try {
			for (int j = 0; j < 100; j++) {
				List nextFive = manager.getNextFive(user, song, 5);
				Iterator i = nextFive.iterator();
				while (i.hasNext()) {
					song = (Map) i.next();
					PlayerManager.getInstance().addSong(user, song, false);
					System.err.println(song);
				}
			}
		} finally {

			PlayerManager.getInstance().shutdown();
			DatabaseManager.getInstance().shutdown();
			System.err.println("Done");
		}

	}
}
