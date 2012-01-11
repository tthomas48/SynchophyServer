package com.synchophy.server.dispatch;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.farng.mp3.AbstractMP3Tag;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

import com.synchophy.server.db.DatabaseManager;
import com.synchophy.server.scanner.TaggedFile;
import com.synchophy.util.StringUtils;

public class Id3Dispatch extends AbstractDispatch {

	private PreparedStatement updateSongSong;
	private PreparedStatement updateSongAlbum;
	private PreparedStatement updateSongArtist;
	private PreparedStatement updateStickySong;
	private PreparedStatement updateStickyAlbum;
	private PreparedStatement updateStickyArtist;

	public Id3Dispatch() {
		updateSongSong = DatabaseManager
				.getInstance()
				.prepare(
						"update song set track = ?, title = ?, title_sort = ?, title_key = ? where file = ?");
		updateSongAlbum = DatabaseManager
				.getInstance()
				.prepare(
						"update song set album = ?, album_sort = ?, album_key = ? where file = ?");
		updateSongArtist = DatabaseManager
				.getInstance()
				.prepare(
						"update song set artist = ?, artist_sort = ?, artist_key = ? where file = ?");
		updateStickySong = DatabaseManager.getInstance().prepare(
				"update sticky set name = ? where name = ?");
		updateStickyAlbum = DatabaseManager.getInstance().prepare(
				"update sticky set album = ? where album = ?");
		updateStickyArtist = DatabaseManager.getInstance().prepare(
				"update sticky set artist = ? where artist = ?");
	}

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String type = this.getRequiredParameter(request, "t");
		String value = this.getRequiredParameter(request, "v");

		String artist = this.getRequiredParameter(request, "artist");
		String album = this.getRequiredParameter(request, "album");
		String name = this.getRequiredParameter(request, "name");

		List params = new ArrayList();
		String andClause = "";
		String conjunction = " where ";
		if (artist.equals("*") == false) {
			andClause += conjunction + " artist_sort = ? ";
			conjunction = " and ";
			params.add(artist);
		}
		if (album.equals("*") == false) {
			andClause += conjunction + " album_sort = ? ";
			conjunction = " and ";
			params.add(album);
		}
		if (name.equals("*") == false) {
			andClause += conjunction
					+ " trim(LEADING '0' FROM title_sort) = ? ";
			conjunction = " and ";
			params.add(name);
		}
		String track = StringUtils.extractTrack(value);
		value = StringUtils.extractTitle(value);

		String query = "select file, artist_sort, album_sort, title_sort "
				+ " from song " + andClause;
		List files = DatabaseManager.getInstance().query(query,
				params.toArray(new Object[params.size()]),
				new String[] { "file", "artist", "album", "title" });
		if (files.size() == 0) {
			return "File not found.";
		}
		for (Iterator i = files.iterator(); i.hasNext();) {
			try {
				Map file = (Map) i.next();
				String filename = (String) file.get("file");
				tagFile(file, type, value, track);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Boolean.TRUE;
	}

	private void tagFile(Map fileMap, String type, String value, String track)
			throws Exception {
		TaggedFile tfile = new TaggedFile((String) fileMap.get("file"));
		if (tfile.isParsed()) {
			if ("album".equals(type)) {
				tfile.setAlbum(value);

			} else if ("track".equals(type)) {
				tfile.setTitle(value);
				tfile.setTrack(track);

			} else if ("artist".equals(type)) {
				tfile.setArtist(value);
			}
		}

		updateDB(fileMap, type, value, track);
	}

	private void updateDB(Map fileMap, String type, String value, String track)
			throws SQLException {
		if ("album".equals(type)) {
			updateSongAlbum.setString(1, value);
			updateSongAlbum.setString(2,
					StringUtils.alphabetizeLinguistically(value));
			updateSongAlbum.setString(3, StringUtils.sortLetter(value));
			updateSongAlbum.setString(4, (String) fileMap.get("file"));
			updateSongAlbum.execute();

			updateStickyAlbum.setString(1, value);
			updateStickyAlbum.setString(2, (String) fileMap.get("album"));
			updateStickyAlbum.execute();

		} else if ("track".equals(type)) {

			updateSongSong.setString(1, track);
			updateSongSong.setString(2, value);
			updateSongSong.setString(3, StringUtils.formatTrack(track) + value);
			updateSongSong.setString(4, StringUtils.sortLetter(value));
			updateSongSong.setString(5, (String) fileMap.get("file"));
			updateSongSong.execute();

			updateStickySong.setString(1, value);
			updateStickySong.setString(2, (String) fileMap.get("title"));
			updateStickySong.execute();

		} else if ("artist".equals(type)) {
			updateSongArtist.setString(1, value);
			updateSongArtist.setString(2,
					StringUtils.alphabetizeLinguistically(value));
			updateSongArtist.setString(3, StringUtils.sortLetter(value));
			updateSongArtist.setString(4, (String) fileMap.get("file"));
			updateSongArtist.execute();

			updateStickyArtist.setString(1, value);
			updateStickyArtist.setString(2, (String) fileMap.get("artist"));
			updateStickyArtist.execute();

		}

	}

	public static void main(String[] args) throws Exception {
		TaggedFile tfile = new TaggedFile(
				"/home/tthomas/Music/Broken Bells/Broken Bells/1 - The High Road.mp3");
		System.err.println(tfile.getTrack());

	}

}
