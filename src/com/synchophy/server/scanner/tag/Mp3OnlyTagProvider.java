package com.synchophy.server.scanner.tag;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;

import org.farng.mp3.AbstractMP3Fragment;
import org.farng.mp3.AbstractMP3FragmentBody;
import org.farng.mp3.MP3File;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.id3.ID3v1_1;

public class Mp3OnlyTagProvider implements ITagProvider {
	private MP3File f;
	private ID3v1 tag;
	private AbstractID3v2 tag2;

	public Mp3OnlyTagProvider(File file) {
		tag = null;
		try {
			MP3File.logger.setLevel(Level.OFF);
			f = new MP3File(file);
			if (f.hasID3v1Tag()) {
				tag = f.getID3v1Tag();
			}
			if (f.hasID3v2Tag()) {
				tag2 = f.getID3v2TagAsv24();
			}
		} catch (Exception e) {
			tag = null;
			// tag2 = null;
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#isParsed()
	 */
	public boolean isParsed() {
		return tag != null || tag2 != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#getAlbum()
	 */
	public String getAlbum() {
		if (tag != null) {
			String album = tag.getAlbum();
			if (album != null && !album.trim().equals("")) {
				return album;
			}
		}

		if (tag2 != null) {
			Object frameObj = tag2.getFrame("TALB");
			if (frameObj != null && frameObj instanceof AbstractMP3Fragment) {
				AbstractMP3Fragment frame = (AbstractMP3Fragment) frameObj;
				AbstractMP3FragmentBody body = frame.getBody();
				if (body != null) {
					String bodyText = (String) body.getObjectValue("Text");
					if (bodyText != null) {
						return bodyText.trim();
					}
				}
			}
		}
		return "";
	}

	public String getOrchestra() {
		if (tag2 != null) {
			Object frameObj = tag2.getFrame("TPE2");
			if (frameObj != null && frameObj instanceof AbstractMP3Fragment) {
				AbstractMP3Fragment frame = (AbstractMP3Fragment) frameObj;
				AbstractMP3FragmentBody body = frame.getBody();
				if (body != null) {
					String bodyText = (String) body.getObjectValue("Text");
					if (bodyText != null) {
						return bodyText.trim();
					}
				}
			}
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#getArtist()
	 */
	public String getArtist() {

		if (tag != null) {
			String artist = tag.getArtist();
			if (artist != null && !artist.trim().equals("")) {
				return artist;
			}
		}

		if (tag2 != null) {
			Object frameObj = tag2.getFrame("TPE1");
			if (frameObj != null && frameObj instanceof AbstractMP3Fragment) {
				AbstractMP3Fragment frame = (AbstractMP3Fragment) frameObj;
				AbstractMP3FragmentBody body = frame.getBody();
				if (body != null) {
					String bodyText = (String) body.getObjectValue("Text");
					if (bodyText != null) {
						return bodyText.trim();
					}
				}
			}
		}
		return "";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#getTitle()
	 */
	public String getTitle() {

		if (tag != null) {
			String title = tag.getTitle();
			if (title != null && !title.trim().equals("")) {
				return title;
			}
		}

		if (tag2 != null) {
			Object frameObj = tag2.getFrame("TIT2");
			if (frameObj != null && frameObj instanceof AbstractMP3Fragment) {
				AbstractMP3Fragment frame = (AbstractMP3Fragment) frameObj;
				AbstractMP3FragmentBody body = frame.getBody();
				if (body != null) {
					String bodyText = (String) body.getObjectValue("Text");
					if (bodyText != null) {
						return bodyText.trim();
					}
				}
			}
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.scanner.tag.ITagProvider#getTrack()
	 */
	public String getTrack() {

		if (tag != null && tag instanceof ID3v1_1) {
			String track = ((ID3v1_1) tag).getTrack();
			if (track != null && !track.trim().equals("")) {
				return track;
			}
		}

		if (tag2 != null) {
			Object frameObj = tag2.getFrame("TRCK");
			if (frameObj != null && frameObj instanceof AbstractMP3Fragment) {
				AbstractMP3Fragment frame = (AbstractMP3Fragment) frameObj;
				AbstractMP3FragmentBody body = frame.getBody();
				if (body != null) {
					String bodyText = (String) body.getObjectValue("Text");
					if (bodyText != null) {
						return bodyText.trim();
					}
				}
			}
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

	public boolean writeArt(String path) {
		if (tag2 != null) {
			Object frameObj = tag2.getFrame("APIC");
			if (frameObj != null && frameObj instanceof AbstractMP3Fragment) {
				FileOutputStream output = null;
				try {
					try {

						AbstractMP3Fragment frame = (AbstractMP3Fragment) frameObj;
						AbstractMP3FragmentBody body = frame.getBody();
						if (body != null) {
							byte[] imageRawData = (byte[]) body
									.getObjectValue("PictureData");
							output = new FileOutputStream(new File(path));
							output.write(imageRawData);
							output.flush();

						}
					} finally {
						if (output != null) {
							output.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return new File(path).exists();
	}

	public static void main(String[] args) {
		Mp3OnlyTagProvider tag = new Mp3OnlyTagProvider(new File(
				"/home/tthomas/Music/Mona/Mona/04 - Teenager.mp3"));
		tag.writeArt("/tmp/album.jpg");
	}
}
