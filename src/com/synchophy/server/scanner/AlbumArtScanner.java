package com.synchophy.server.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.synchophy.server.ConfigManager;
import com.synchophy.server.MetricManager;
import com.synchophy.server.db.DatabaseManager;
import com.synchophy.util.StringUtils;

import de.umass.lastfm.Album;
import de.umass.lastfm.ImageSize;

public class AlbumArtScanner {

	private MetricManager metricManager = new MetricManager();

	private static final String[] coverFilenames = new String[] { "album.jpg",
			"album.png", "cover.jpg", "cover.png", "AlbumArtSmall.jpg",
			"AlbumArtSmall.png", "Folder.jpg", "Folder.png" };

	public AlbumArtScanner() {
	}

	public void scan(String startLetter) throws IOException {

		Object[] params = new Object[0];
		String whereClause = "";
		if (startLetter != null) {
			whereClause = " where upper(s.artist_sort) like ? ";
			params = new Object[] { startLetter.toUpperCase() + "%" };
		}
		List albums = DatabaseManager
				.getInstance()
				.query(" select s.artist_sort, s.album_sort "
						+ "  from song s "
						+ whereClause
						+ " group by s.artist_sort, s.artist, s.album_sort, s.album "
						+ " order by s.artist_sort, s.album_sort",
						new Object[0], new String[] { "artist", "album" });

		for (Iterator i = albums.iterator(); i.hasNext();) {
			Map row = (Map) i.next();
			scan("song", (String) row.get("artist"), (String) row.get("album"));
		}

	}

	protected void scan(String table, String artist, String album)
			throws IOException {

		System.err.println("Looking up image for " + artist + ":" + album);

		List files = DatabaseManager.getInstance().query(
				"select file from " + table
						+ " where artist_sort = ? and album_sort = ?",
				new Object[] { artist, album }, new String[] { "file" });
		System.err.println("Found " + files.size() + " files.");

		String filepath = null;
		for (int i = 0; i < (files.size() > 0 ? 1 : 0); i++) {
			String filename = (String) ((Map) files.get(i)).get("file");

			filepath = AlbumArtScanner.getFilePath(filename);

			File filenotfound = new File(filepath, ".file-notfound");
			if (filenotfound.exists()) {
				break;
			}
			for (int j = 0; j < coverFilenames.length; j++) {
				System.err.println("Checking "
						+ new File(filepath, coverFilenames[j])
								.getAbsolutePath());
				if (new File(filepath, coverFilenames[j]).exists()) {
					if (coverFilenames[j].equals("album.jpg") == false) {
						writeAlbumJpeg(filepath, new FileInputStream(new File(
								filepath, coverFilenames[j])));
					}
					return;

				}
			}
			filenotfound.createNewFile();

			// second try inside the file
			File file = new File(filepath);
			TaggedFile tfile = new TaggedFile(filename);
			try {
				if (tfile.isParsed()) {
					if (tfile.writeArt(filepath + "/album.jpg")) {
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// finally last.fm
		File lastfmnotfound = new File(filepath, ".lastfm-notfound");
		if (lastfmnotfound.exists()) {
			return;
		}
		Album albumObj = null;
		try {
			albumObj = Album.getInfo(StringUtils
					.unAlphabetizeLinguistically(StringUtils
							.unAlphabetizeLinguistically(artist)), StringUtils
					.unAlphabetizeLinguistically(StringUtils
							.unAlphabetizeLinguistically(album)), metricManager
					.getLastFmApiKey());
		} catch (Exception e) {
			e.printStackTrace();

		}
		if (albumObj != null && filepath != null
				&& albumObj.getImageURL(ImageSize.LARGE) != null) {
			try {
				System.err.println("Attempting to lookup: "
						+ albumObj.getImageURL(ImageSize.LARGE));
				InputStream is = new URL(albumObj.getImageURL(ImageSize.LARGE))
						.openStream();
				writeAlbumJpeg(filepath, is);
			} catch (MalformedURLException e) {
				System.err.println("Invalid url: "
						+ albumObj.getImageURL(ImageSize.LARGE));

			}
		} else {
			lastfmnotfound.createNewFile();
		}
	}

	public static String getFilePath(String filename) {
		return filename.substring(0, filename.lastIndexOf(File.separator));

	}

	private void writeAlbumJpeg(String filepath, InputStream is)
			throws IOException {
		try {
			System.err.println("Attempting to write last.fm image to "
					+ filepath + "/album.jpg");
			File npic = new File(filepath + "/album.jpg");
			npic.createNewFile();
			FileOutputStream fos = new FileOutputStream(npic);
			int in = -1;
			while ((in = is.read()) != -1) {
				fos.write(in);
			}
			fos.close();
			return;
		} finally {
			if (is != null) {
				is.close();
			}
		}

	}

	public static void main(String[] args) throws Exception {
		// does not have track info. Attempt filename parsing
		// /home/tthomas/MUSIC/Clinic/Winchester Cathedral/07 - clinic -
		// winchester_cathedral -.mp3'
		Logger.global.setLevel(Level.SEVERE);

		try {
			String letter = System.getProperty("letter");
			System.err.println("Scanning for letter: " + letter);
			Date start = new Date();
			AlbumArtScanner scanner = new AlbumArtScanner();
			scanner.scan(letter);
			System.err.println("Scanned in "
					+ ((new Date().getTime() - start.getTime()) / 1000)
					+ "secs");
		} finally {
			DatabaseManager.getInstance().shutdown();
		}
	}

}
