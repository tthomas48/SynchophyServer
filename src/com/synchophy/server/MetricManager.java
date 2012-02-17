package com.synchophy.server;

import java.util.Map;

import com.synchophy.server.db.DatabaseManager;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleResult;

public class MetricManager {

	private static final String LASTFM_API_KEY = "9e91826464c3707512f7856dcd57e15f";
	private static final String LASTFM_API_SECRET = "d607f37300b75b3fe566b47088c62aff";

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

}
