package com.synchophy.server.scanner.tag;

import java.io.File;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class FullTagProvider implements ITagProvider {

	private MP3File f;
	private Tag tag;

	public FullTagProvider(File file) {
		tag = null;
		try {
			f = new MP3File(file);
			tag = f.getTag();
		} catch (Exception e) {
			tag = null;
			e.printStackTrace();
		}
	}

	public boolean isParsed() {
		return tag != null;
	}

	public String getAlbum() {

		return tag.getFirst(FieldKey.ALBUM);
	}

	public String getArtist() {
		return tag.getFirst(FieldKey.ARTIST);
	}

	public String getTitle() {
		return tag.getFirst(FieldKey.TITLE);

	}

	public String getTrack() {
		return tag.getFirst(FieldKey.TRACK);
	}

	public void setAlbum(String album) {
		try {
			tag.setField(FieldKey.ALBUM, album);
			f.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setArtist(String artist) {
		try {
			tag.setField(FieldKey.ARTIST, artist);
			f.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTitle(String title) {
		try {
			tag.setField(FieldKey.TITLE, title);
			f.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTrack(String track) {
		try {
			tag.setField(FieldKey.TRACK, track);
			f.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String[] formats() {
		return new String[] {"MP3","M4A","MP4","MP4P","OGG","WMA"};
	}
}
