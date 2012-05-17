package com.synchophy.server.scanner.tag;

import java.io.File;
import java.io.FileOutputStream;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;

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

	public String getOrchestra() {
		// TODO Auto-generated method stub
		return tag.getFirst(ID3v24Frames.FRAME_ID_ACCOMPANIMENT);
	}

	public static String[] formats() {
		//return new String[] { "MP3", "M4A", "MP4", "MP4P", "OGG", "WMA" };
		// currently only play mp3
		return new String[] { "MP3" };
	}

	public boolean writeArt(String path) {

		FileOutputStream output = null;
		try {
			try {
				System.err.println(tag.getFields(FieldKey.COVER_ART).size());

				TagField imageField = (TagField) tag.getFields(
						FieldKey.COVER_ART).get(0);
				if (imageField instanceof AbstractID3v2Frame) {
					FrameBodyAPIC imageFrameBody = (FrameBodyAPIC) ((AbstractID3v2Frame) imageField)
							.getBody();
					if (!imageFrameBody.isImageUrl()) {
						byte[] imageRawData = (byte[]) imageFrameBody
								.getObjectValue(DataTypes.OBJ_PICTURE_DATA);
						output = new FileOutputStream(new File(path));
						output.write(imageRawData);
						output.flush();
					}
				}
			} finally {
				if (output != null) {
					output.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new File(path).exists();
	}

	public static void main(String[] args) {
		FullTagProvider tag = new FullTagProvider(new File(
				"/home/tthomas/Music/Grimes/Genesis/01 - Genesis.mp3"));
		tag.writeArt("/tmp/album.jpg");
	}

}
