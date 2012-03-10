package com.synchophy.server.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

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

	public void scan() {
		List albums = DatabaseManager
				.getInstance()
				.query(" select s.artist, s.album "
						+ "  from song s "
						+ " group by s.artist_sort, s.artist, s.album_sort, s.album "
						+ " order by s.artist_sort, s.album_sort",
						new Object[0], new String[] { "artist", "album" });

	}

	private void scan(String artist, String album) throws IOException {

		System.err.println("Looking up image for " + artist + ":" + album);

		List files = DatabaseManager
				.getInstance()
				.query("select file from song where artist_sort = ? and album_sort = ?",
						new Object[] { artist, album }, new String[] { "file" });
		System.err.println("Found " + files.size() + " files.");

		String filepath = null;
		for (int i = 0; i < files.size(); i++) {
			String filename = (String) ((Map) files.get(i)).get("file");
			filepath = AlbumArtScanner.getFilePath(filename);
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
		}
		Album albumObj = Album.getInfo(StringUtils
				.unAlphabetizeLinguistically(StringUtils
						.unAlphabetizeLinguistically(artist)), StringUtils
				.unAlphabetizeLinguistically(StringUtils
						.unAlphabetizeLinguistically(album)), metricManager
				.getLastFmApiKey());
		if (albumObj != null && filepath != null) {
			InputStream is = new URL(albumObj.getImageURL(ImageSize.LARGE))
					.openStream();
			writeAlbumJpeg(filepath, is);
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
}
