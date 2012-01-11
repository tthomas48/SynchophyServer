package com.synchophy.server.scanner.tag;

import java.io.File;
import java.util.logging.Level;

import org.farng.mp3.LogFormatter;
import org.farng.mp3.MP3File;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.id3.ID3v1_1;

public class Mp3OnlyTagProvider implements ITagProvider {
	private MP3File f;
	private ID3v1 tag;

	public Mp3OnlyTagProvider(File file) {
		tag = null;
		try {
			f = new MP3File(file);
			if (f.hasID3v1Tag()) {
				tag = f.getID3v1Tag();
			}
		} catch (Exception e) {
			tag = null;
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#isParsed()
	 */
	public boolean isParsed() {
		return tag != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#getAlbum()
	 */
	public String getAlbum() {
		return tag.getAlbum();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#getArtist()
	 */
	public String getArtist() {

		return tag.getArtist();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#getTitle()
	 */
	public String getTitle() {

		return tag.getTitle();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#getTrack()
	 */
	public String getTrack() {

		if (tag instanceof ID3v1_1) {
			return ((ID3v1_1) tag).getTrack();
		}
		return "0";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.synchophy.server.scanner.tag.ITagProvider#setAlbum(java.lang.String)
	 */
	public void setAlbum(String album) {
		try {
			tag.setAlbum(album);
			f.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.synchophy.server.scanner.tag.ITagProvider#setArtist(java.lang.String)
	 */
	public void setArtist(String artist) {
		try {
			tag.setArtist(artist);
			f.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.synchophy.server.scanner.tag.ITagProvider#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		try {
			tag.setTitle(title);
			f.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.synchophy.server.scanner.tag.ITagProvider#setTrack(java.lang.String)
	 */
	public void setTrack(String track) {
		try {
			if (tag instanceof ID3v1_1) {
				((ID3v1_1) tag).setTrack(track);
				f.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String[] formats() {
		// TODO Auto-generated method stub
		return new String[] { "MP3" };
	}
}
