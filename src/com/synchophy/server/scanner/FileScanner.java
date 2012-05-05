package com.synchophy.server.scanner;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.synchophy.server.ConfigManager;
import com.synchophy.server.db.DatabaseManager;
import com.synchophy.server.scanner.handler.IFileHandler;
import com.synchophy.util.StringUtils;

public class FileScanner {

	private static final int MAX_BATCH_SIZE = 100;

	private String basePath;
	private String scanPath;
	private boolean insertOnly;
	private PreparedStatement sth;
	private PreparedStatement errorSth;
	private int batchSize = 0;
	private int errorBatchSize = 0;
	private static final String[] supportedExts = TaggedFile.formats();
	private static Map handlers = new HashMap();

	public FileScanner(String basePath) {
		this(basePath, basePath);
	}

	public FileScanner(String basePath, String scanPath) {
		this(basePath, scanPath, false);
	}

	public FileScanner(String basePath, String scanPath, boolean insertOnly) {
		this.basePath = basePath;
		this.scanPath = scanPath;
		this.insertOnly = insertOnly;
		init();
	}

	private void init() {
		List handlers = ConfigManager.getInstance().getFileHandlers();
		Iterator i = handlers.iterator();
		while (i.hasNext()) {

			String handlerClass = (String) i.next();
			try {

				Class clazz = Class.forName(handlerClass);
				Object obj = clazz.newInstance();
				if (obj instanceof IFileHandler == false) {
					throw new RuntimeException(
							"Invalid file handler specified: " + handlerClass);
				}
				IFileHandler fileHandler = (IFileHandler) obj;
				FileScanner.registerHandler(fileHandler.getExtension(),
						fileHandler);
			} catch (Exception e) {
				throw new RuntimeException("Could not register file handler: "
						+ handlerClass);

			}
		}

	}

	public static void registerHandler(String extension, IFileHandler handler) {
		handlers.put(extension, handler);
	}

	public void scan() {

		DatabaseManager.getInstance().executeQuery("delete from import");
		DatabaseManager.getInstance().executeQuery("delete from import_error");
		sth = DatabaseManager
				.getInstance()
				.prepare(
						"insert into import (file, track, artist, artist_sort, artist_key, album, album_sort, album_key, title, title_sort, title_key,  size, orchestra, orchestra_sort, orchestra_key) "
								+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		if (sth == null) {
			return;
		}
		errorSth = DatabaseManager.getInstance().prepare(
				"insert into import_error (file, message) values (?, ?)");

		scan(new File(this.scanPath));

		executeBatch();
		executeErrorBatch();

//		AlbumArtScanner scanner = new AlbumArtScanner();
//		List rows = DatabaseManager
//				.getInstance()
//				.query("select artist_sort, album_sort from import group by artist_sort, album_sort",
//						new Object[0], new String[] { "artist", "album" });
//		Iterator i = rows.iterator();
//		while (i.hasNext()) {
//			Map row = (Map) i.next();
//			try {
//				scanner.scan("import", (String) row.get("artist"),
//						(String) row.get("album"));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}

		if (!insertOnly) {
			String deleteSQL = "DELETE FROM song where file not in (select file from IMPORT)";
			DatabaseManager.getInstance().executeQuery(deleteSQL);
		}

		String insertSQL = "INSERT INTO song (file, track, artist, artist_sort, artist_key, album, album_sort, album_key, title, title_sort, title_key,  size, orchestra, orchestra_sort, orchestra_key) "
				+ " (SELECT file, track, artist, artist_sort, artist_key, album, album_sort, album_key, title, title_sort, title_key, size, orchestra, orchestra_sort, orchestra_key from import where file not in (select file from song))";
		DatabaseManager.getInstance().executeQuery(insertSQL);

		if (!insertOnly) {
			String deleteSQL = "DELETE FROM bad_song where file not in (select file from import_error)";
			DatabaseManager.getInstance().executeQuery(deleteSQL);
		}

		insertSQL = "INSERT INTO bad_song (file, message) (select file, message from import_error where file not in (select file from bad_song))";
		DatabaseManager.getInstance().executeQuery(insertSQL);

		DatabaseManager.getInstance().executeQuery("delete from import");
		DatabaseManager.getInstance().executeQuery("delete from import_error");

		cleanup();
	}

	private void cleanup() {

		List files = DatabaseManager.getInstance()
				.query("select file from song", new Object[0],
						new String[] { "song" });
		Iterator iterator = files.iterator();
		while (iterator.hasNext()) {
			String filename = (String) ((Map) iterator.next()).get("song");
			if (new File(filename).exists() == false) {
				DatabaseManager.getInstance().executeQuery(
						"delete from song where file = ?",
						new Object[] { filename });
			}
		}

	}

	private void scan(File path) {

		if (path.exists() == false || path.isDirectory() == false) {
			return;
		}
		String[] fileNames = path.list();
		for (int i = 0; i < fileNames.length; i++) {
			String fileName = fileNames[i];
			File file = new File(path, fileName);
			if (file.exists()) {
				if (file.isDirectory()) {
					scan(file);
					continue;
				}
				if (hasHandler(file)) {
					handle(file);
				}
			}
		}
		fileNames = path.list();
		for (int i = 0; i < fileNames.length; i++) {
			String fileName = fileNames[i];
			File file = new File(path, fileName);
			if (file.exists() && !file.isDirectory()) {
				insertFile(file);
			}
		}
	}

	private boolean hasHandler(File file) {
		String ext = StringUtils.getExtension(file);
		return handlers.containsKey(ext);
	}

	private void handle(File file) {
		String ext = StringUtils.getExtension(file);

		IFileHandler handler = (IFileHandler) handlers.get(ext);
		handler.handle(this, file);
	}

	private void insertFile(File file) {

		try {
			System.err.println("Inserting " + file.getAbsolutePath());

			String ext = StringUtils.getExtension(file);
			if ("".equals(ext)) {
				insertFileError(file, "Unsupported file name");
				return;
			}

			boolean supportedFile = false;
			for (int i = 0; i < supportedExts.length; i++) {
				if (ext.equals(supportedExts[i]) == true) {
					supportedFile = true;
				}
			}
			if (!supportedFile) {
				insertFileError(file, "Unsupported file type");
				return;
			}

			TaggedFile tfile = new TaggedFile(file);
			try {
				if (tfile.isParsed()) {
					insertFile(file, tfile);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			insertFileError(file, "No tag found in " + file.getAbsolutePath());
		} catch (Exception e) {
			insertFileError(file, e.getMessage());
		}
	}

	private void insertFile(File file, TaggedFile tfile) {

		try {
			file = moveFile(file, tfile);

			// :file, :track, :artist, :album, :title, :size
			sth.setString(1, file.getAbsolutePath());
			sth.setString(2, StringUtils.cleanTrack(tfile.getTrack()));
			sth.setString(3, tfile.getArtist());
			sth.setString(4,
					StringUtils.alphabetizeLinguistically(tfile.getArtist()));
			sth.setString(5, StringUtils.sortLetter(tfile.getArtist()));
			sth.setString(6, tfile.getAlbum());
			sth.setString(7,
					StringUtils.alphabetizeLinguistically(tfile.getAlbum()));
			sth.setString(8, StringUtils.sortLetter(tfile.getAlbum()));
			sth.setString(9, tfile.getTitle());
			sth.setString(
					10,
					StringUtils.formatTrack(tfile.getTrack())
							+ tfile.getTitle());
			sth.setString(11, StringUtils.sortLetter(tfile.getTitle()));
			sth.setLong(12, file.length());
			sth.setString(13, tfile.getOrchestra());
			sth.setString(14,
					StringUtils.alphabetizeLinguistically(tfile.getOrchestra()));
			sth.setString(15, StringUtils.sortLetter(tfile.getOrchestra()));

			sth.addBatch();
			batchSize++;
			if (batchSize > MAX_BATCH_SIZE) {
				executeBatch();
			}
		} catch (SQLException e) {
			insertFileError(file, e.getMessage());
		}
	}

	private File moveFile(File file, TaggedFile tfile) {

		String artist = StringUtils.cleanFilename(tfile.getOrchestra());
		if ("".equals(artist)) {
			artist = StringUtils.cleanFilename(tfile.getArtist());
		}
		String toPath = basePath + "//" + artist + "/" + tfile.getAlbum() + "/"
				+ file.getName();
		toPath = StringUtils.removeDoubleSlashes(toPath);

		if (!toPath.equals(file.getAbsolutePath())) {
			System.err.println("I am moving " + file.getAbsolutePath() + " to "
					+ toPath);
			File toFile = new File(toPath);
			File parent = toFile.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			if (parent.exists()) {
				if (file.renameTo(toFile)) {
					file.delete();
					return toFile;
				} else {
					System.err.println("Could not delete "
							+ file.getAbsolutePath());
				}
			} else {
				System.err.println("Could not mkdirs "
						+ toFile.getParentFile().getAbsolutePath());
			}
		}
		return file;
	}

	private void insertFileError(File file, String message) {

		try {
			// :file, :track, :artist, :album, :title, :size
			errorSth.setString(1, file.getAbsolutePath());
			errorSth.setString(2, message);
			errorSth.addBatch();
			errorBatchSize++;
			if (errorBatchSize > MAX_BATCH_SIZE) {
				executeErrorBatch();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void executeBatch() {

		try {
			batchSize = 0;
			sth.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.gc();
	}

	private void executeErrorBatch() {

		try {
			errorBatchSize = 0;
			errorSth.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.gc();
	}

	public static void main(String[] args) throws Exception {
		// does not have track info. Attempt filename parsing
		// /home/tthomas/MUSIC/Clinic/Winchester Cathedral/07 - clinic -
		// winchester_cathedral -.mp3'
		Logger.global.setLevel(Level.SEVERE);

		try {
			String musicPath = ConfigManager.getInstance().getMusicPath();
			Date start = new Date();
			FileScanner scanner = new FileScanner(musicPath);
			scanner.scan();
			System.err.println("Scanned music "
					+ ((new Date().getTime() - start.getTime()) / 1000)
					+ "secs");
		} finally {
			DatabaseManager.getInstance().shutdown();
		}
	}
}
