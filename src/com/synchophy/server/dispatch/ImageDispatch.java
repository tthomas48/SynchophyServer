package com.synchophy.server.dispatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.MetricManager;
import com.synchophy.server.db.DatabaseManager;
import com.synchophy.util.StringUtils;

import de.umass.lastfm.Album;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.cache.FileSystemCache;

public class ImageDispatch extends AbstractDispatch {
	
	private MetricManager metricManager = new MetricManager();
	
	static {
		String musicPath = System.getProperty("music.path", "./Music");
		File cachedir = new File(musicPath, ".lastfm-cache");
		if (!cachedir.exists()) {
			cachedir.mkdirs();
		}
		Caller.getInstance().setCache(new FileSystemCache(cachedir));
	}

	private static final String[] coverFilenames = new String[] { "album.jpg",
			"album.png", "cover.jpg", "cover.png", "AlbumArtSmall.jpg",
			"AlbumArtSmall.png", "Folder.jpg", "Folder.png" };

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String album = getRequiredParameter(request, "album");
		String artist = getRequiredParameter(request, "artist");

		List files = DatabaseManager
				.getInstance()
				.query("select file from song where artist_sort = ? and album_sort = ?",
						new Object[] { artist, album }, new String[] { "file" });
		System.err.println("Found " + files.size() + " files.");

		String filepath = null;
		for (int i = 0; i < files.size(); i++) {
			String filename = (String) ((Map) files.get(i)).get("file");
			filepath = filename.substring(0,
					filename.lastIndexOf(File.separator));
			for (int j = 0; j < coverFilenames.length; j++) {
				System.err.println("Checking "
						+ new File(filepath, coverFilenames[j])
								.getAbsolutePath());
				if (new File(filepath, coverFilenames[j]).exists()) {
					return new File(filepath, coverFilenames[j]);

				}
			}
		}
		Album albumObj = Album.getInfo(
				StringUtils.unAlphabetizeLinguistically(artist),
				StringUtils.unAlphabetizeLinguistically(album), metricManager.getLastFmApiKey());
		if (albumObj != null && filepath != null) {
			InputStream is = new URL(albumObj.getImageURL(ImageSize.LARGE))
					.openStream();
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
				return npic;
			} finally {
				if (is != null) {
					is.close();
				}
			}

		}

		return null;
	}

	public void write(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		File filepath = null;
		try {
			filepath = (File) execute(request, response);
		} catch (RuntimeException e) {
			e.printStackTrace();
			response.setStatus(500);
			return;
		}
		if (filepath == null) {
			response.setStatus(404);
			return;
		}

		System.err.println("Writing out " + filepath);

		response.setHeader("Content-Type", "image/jpeg");
		int size = (int) filepath.length();
		response.setContentLength(size);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				filepath));
		try {
			BufferedOutputStream out = new BufferedOutputStream(
					response.getOutputStream());

			int read = -1;
			while ((read = in.read()) != -1) {
				out.write(read);

			}
			out.flush();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

}
