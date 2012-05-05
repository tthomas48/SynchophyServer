package com.synchophy.server.dispatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ConfigManager;
import com.synchophy.server.db.DatabaseManager;
import com.synchophy.server.scanner.AlbumArtScanner;

import de.umass.lastfm.Caller;
import de.umass.lastfm.cache.FileSystemCache;

public class ImageDispatch extends AbstractDispatch {

	static {
		String musicPath = ConfigManager.getInstance().getMusicPath();
		File cachedir = new File(musicPath, ".lastfm-cache");
		if (!cachedir.exists()) {
			cachedir.mkdirs();
		}
		Caller.getInstance().setCache(new FileSystemCache(cachedir));
	}

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String action = getRequiredParameter(request, "a");
		if ("view".equals(action)) {
			String album = getRequiredParameter(request, "album");
			String artist = getOptionalParameter(request, "artist");

			String andClause = "";
			String[] params = new String[] { album };
			if ("".equals(artist) == false) {
				andClause += " and artist_sort = ? ";
				params = new String[] { album, artist };
			}

			List files = DatabaseManager.getInstance().query(
					"select file from song where album_sort = ?" + andClause,
					params, new String[] { "file" });
			for (int i = 0; i < files.size(); i++) {
				String filename = (String) ((Map) files.get(i)).get("file");
				String filepath = AlbumArtScanner.getFilePath(filename);
				File albumJpeg = new File(filepath, "album.jpg");
				if (albumJpeg.exists()) {
					return albumJpeg;
				}
			}
			return new File("./image/nocover.png");
		} else if ("list".equals(action)) {
			boolean filter = Boolean.valueOf(
					getRequiredParameter(request, "filter")).booleanValue();

			return DatabaseManager
					.getInstance()
					.query(" select s.artist_sort, s.album_sort "
							+ "  from song s"
							+ "       left outer join sticky ss on ((s.album_sort = ss.album or ss.album = '*') "
							+ "       and (s.artist_sort = ss.artist or ss.artist = '*')"
							+ "       and (s.title_sort = ss.name or ss.name = '*')"
							+ getJoinFilter(filter) + ")" + getFilter(filter)
							+ " group by s.artist_sort, s.album_sort "
							+ " order by s.artist_sort, s.album_sort",
							new Object[0], new String[] { "artist", "album" });
		}

		return null;
	}

	private String getFilter(boolean filter) {
		if (!filter) {
			return "";
		}
		return "  where ss.stick is null or ss.stick >= 0 ";
	}

	private String getJoinFilter(boolean filter) {
		if (!filter) {
			return "";
		}
		return "  and ss.stick < 0 ";
	}

	public void write(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		File filepath = null;
		try {
			Object obj = execute(request, response);
			if (obj instanceof File == false) {
				writeJSON(obj, response);
				return;
			}
			filepath = (File) obj;
		} catch (RuntimeException e) {
			e.printStackTrace();
			response.setStatus(500);
			return;
		}
		if (filepath == null) {
			response.setStatus(404);
			return;
		}

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
