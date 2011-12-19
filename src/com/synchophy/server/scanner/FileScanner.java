package com.synchophy.server.scanner;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.farng.mp3.MP3File;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

import com.synchophy.server.db.DatabaseManager;
import com.synchophy.util.StringUtils;


public class FileScanner {

  private static final int MAX_BATCH_SIZE = 1000;

  private String basePath;
  private PreparedStatement sth;
  private PreparedStatement errorSth;
  private int batchSize = 0;
  private int errorBatchSize = 0;


  public FileScanner(String basePath) {

    this.basePath = basePath;
  }


  public void scan() {

    DatabaseManager.getInstance().executeQuery("delete from import");
    DatabaseManager.getInstance().executeQuery("delete from import_error");
    sth = DatabaseManager.getInstance()
        .prepare("insert into import (file, track, artist, artist_sort, artist_key, album, album_sort, album_key, title, title_sort, title_key,  size) "
                 + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    if (sth == null) {
      return;
    }
    errorSth = DatabaseManager.getInstance()
        .prepare("insert into import_error (file, message) values (?, ?)");

    scan(new File(this.basePath));

    executeBatch();
    executeErrorBatch();
    String insertSQL = "INSERT INTO song (file, track, artist, artist_sort, artist_key, album, album_sort, album_key, title, title_sort, title_key,  size) " 
    	+ " (SELECT file, track, artist, artist_sort, artist_key, album, album_sort, album_key, title, title_sort, title_key, size from import i where not exists (select 'x' from song where song.file = i.file))";
    DatabaseManager.getInstance().executeQuery(insertSQL);
//    String updateSQL = "UPDATE song set song.artist = (select artist from import where song.file = import.file), song.artist_sort = (select artist_sort from import where song.file = import.file), song.artist_key = (select artist_key from import where song.file = import.file), "
//                      + "song.album = (select album from import where song.file = import.file), song.album_sort = (select album_sort from import where song.file = import.file), song.album_key = (select album_key from import where song.file = import.file), "
//                      + "song.title = (select title from import where song.file = import.file), song.title_sort = (select title_sort from import where song.file = import.file), song.title_key = (select title_key from import where song.file = import.file), "
//                      + "song.size = (select size from import where song.file = import.file)";
//    DatabaseManager.getInstance().executeQuery(updateSQL);
    
    insertSQL = "INSERT INTO bad_song (file, message) (select file, message from import_error where not exists (select 'x' from bad_song where bad_song.file = import_error.file))";
    DatabaseManager.getInstance().executeQuery(insertSQL);
    
//    updateSQL = "update bad_song set message = (select message from import_error where bad_song.file = import_error.file)";
//    DatabaseManager.getInstance().executeQuery(updateSQL);
    
    
//    String updateSQL = "update song set song.track = vals.track"
//                      + " song.artist = vals.artist, song.artist_sort = vals.artist_sort, song.artist_key = vals.artist_key, "
//                      + "song.album = vals.album, song.album_sort = vals.album_sort, song.album_key = vals.album_key, "
//                      + "song.title = vals.title, song.title_sort = vals.title_sort, song.title_key = vals.album_key, "
//                      + "song.size = vals.size "
//    DatabaseManager.getInstance()
//        .executeQuery("merge into song using (select file, track, artist, artist_sort, artist_key, album, album_sort, album_key, title, title_sort, title_key, size from import) as vals(file, track, artist, artist_sort, artist_key, album, album_sort, album_key, title, title_sort, title_key, size)"
//                      + " on song.file = vals.file"
//                      + " when matched then"
//                      + " update set song.track = vals.track, "
//                      + " song.artist = vals.artist, song.artist_sort = vals.artist_sort, song.artist_key = vals.artist_key, "
//                      + "song.album = vals.album, song.album_sort = vals.album_sort, song.album_key = vals.album_key, "
//                      + "song.title = vals.title, song.title_sort = vals.title_sort, song.title_key = vals.album_key, "
//                      + "song.size = vals.size"
//                      + " when not matched then insert values(null, vals.file, vals.track, vals.artist, vals.artist_sort, vals.artist_key, "
//                      + "vals.album, vals.album_sort, vals.album_key, "
//                      + "vals.title, vals.title_sort, vals.title_key, vals.size)");
//    DatabaseManager.getInstance()
//        .executeQuery("merge into bad_song using (select file, message from import_error) as vals(file, message)"
//                      + " on bad_song.file = vals.file"
//                      + " when matched then"
//                      + " update set bad_song.message = vals.message"
//                      + " when not matched then insert values(null, vals.file, vals.message)");
    DatabaseManager.getInstance().executeQuery("delete from import");
    DatabaseManager.getInstance().executeQuery("delete from import_error");
    
    cleanup();
  }


  private void cleanup() {

    List files = DatabaseManager.getInstance().query("select file from song", new Object[0], new String[] {"song"});
    Iterator iterator = files.iterator();
    while (iterator.hasNext()) {
      String filename = (String) ((Map) iterator.next()).get("song");
      if (new File(filename).exists() == false) {
        DatabaseManager.getInstance().executeQuery("delete from song where file = ?", new Object[]{
          filename
        });
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
        insertFile(file);
      }
    }
  }


  private void insertFile(File file) {

    try {
      String filename = file.getName();
      int lastPeriod = filename.lastIndexOf('.');
      if (lastPeriod < 0) {
        insertFileError(file, "Unsupported file name");
        return;
      }

      String ext = filename.substring(lastPeriod).toUpperCase();
      if (ext.equals(".MP3") == false) {
        insertFileError(file, "Unsupported file type");
        return;
      }

      MP3File mp3file = new MP3File(file);
      AbstractID3v2 tag2 = mp3file.getID3v2Tag();
      if (tag2 != null) {
        insertFile(file, tag2);
        return;
      }
      ID3v1 tag = mp3file.getID3v1Tag();
      if (tag != null) {
        insertFile(file, tag);
        return;
      }
      insertFileError(file, "No tag found in " + filename);
    } catch (Exception e) {
      insertFileError(file, e.getMessage());
    }
  }


  private void insertFile(File file, AbstractID3v2 tag) {

    try {
      // :file, :track, :artist, :album, :title, :size
      sth.setString(1, file.getAbsolutePath());
      sth.setString(2, StringUtils.cleanTrack(tag.getTrackNumberOnAlbum()));
      sth.setString(3, tag.getLeadArtist());
      sth.setString(4, StringUtils.alphabetizeLinguistically(tag.getLeadArtist()));
      sth.setString(5, StringUtils.sortLetter(tag.getLeadArtist()));
      sth.setString(6, tag.getAlbumTitle());
      sth.setString(7, StringUtils.alphabetizeLinguistically(tag.getAlbumTitle()));
      sth.setString(8, StringUtils.sortLetter(tag.getAlbumTitle()));
      sth.setString(9, tag.getSongTitle());
      sth.setString(10, StringUtils.formatTrack(tag.getTrackNumberOnAlbum()) + tag.getSongTitle());
      sth.setString(11, StringUtils.sortLetter(tag.getSongTitle()));
      sth.setLong(12, file.length());
      sth.addBatch();
      batchSize++;
      if (batchSize > MAX_BATCH_SIZE) {
        executeBatch();
      }
    } catch (SQLException e) {
      insertFileError(file, e.getMessage());
    }
  }


  private void insertFile(File file, ID3v1 tag) {

    try {
      // :file, :track, :artist, :album, :title, :size
      sth.setString(1, file.getAbsolutePath());
      sth.setString(2, StringUtils.cleanTrack(tag.getTrackNumberOnAlbum()));
      sth.setString(3, tag.getLeadArtist());
      sth.setString(4, StringUtils.alphabetizeLinguistically(tag.getLeadArtist()));
      sth.setString(5, StringUtils.sortLetter(tag.getLeadArtist()));
      sth.setString(6, tag.getAlbumTitle());
      sth.setString(7, StringUtils.alphabetizeLinguistically(tag.getAlbumTitle()));
      sth.setString(8, StringUtils.sortLetter(tag.getAlbumTitle()));
      sth.setString(9, tag.getSongTitle());
      sth.setString(10, StringUtils.formatTrack(tag.getTrackNumberOnAlbum()) + tag.getSongTitle());
      sth.setString(11, StringUtils.sortLetter(tag.getSongTitle()));
      sth.setLong(12, file.length());
      sth.addBatch();
      batchSize++;
      if (batchSize > MAX_BATCH_SIZE) {
        executeBatch();
      }
    } catch (SQLException e) {
      insertFileError(file, e.getMessage());
    }
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
  


  public static void main(String[] args) {

    try {
      Date start = new Date();
      FileScanner scanner = new FileScanner("/home/tthomas/MUSIC/Republica/");
      scanner.scan();
      System.err.println("Scanned in " + ((new Date().getTime() - start.getTime()) / 1000) + "secs");
    } finally {
      DatabaseManager.getInstance().shutdown();
    }
  }
}
