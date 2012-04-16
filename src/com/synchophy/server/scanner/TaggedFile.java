package com.synchophy.server.scanner;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.synchophy.server.ConfigManager;
import com.synchophy.server.scanner.tag.ITagProvider;
import com.synchophy.server.scanner.tag.Mp3OnlyTagProvider;

public class TaggedFile {
	private static Constructor tagConstructor;
	public static String[] supportedExts = new String[0];
	static {
		String tagProviderClassname = ConfigManager.getInstance()
				.getTagProvider();

		try {
			Class tagProviderClass = Class.forName(tagProviderClassname);
			tagConstructor = tagProviderClass
					.getConstructor(new Class[] { File.class });
			Method formats = tagProviderClass
					.getMethod("formats", new Class[0]);
			supportedExts = (String[]) formats.invoke(null, new Object[0]);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to load tag provider "
					+ tagProviderClassname);

		}
	}

	private ITagProvider tag;

	public TaggedFile(String filename) {
		this(new File(filename));
	}

	public TaggedFile(File file) {
		try {
			tag = (ITagProvider) tagConstructor
					.newInstance(new Object[] { file });
		} catch (Exception e) {
			throw new RuntimeException("Unable to construct tag provider.", e);
		}
	}

	public boolean isParsed() {
		return tag.isParsed();
	}

	public String getAlbum() {
		return tag.getAlbum();
	}

	public String getArtist() {

		return tag.getArtist();
	}

	public String getTitle() {

		return tag.getTitle();

	}

	public String getTrack() {
		return tag.getTrack();
	}

	public String getOrchestra() {
		return tag.getOrchestra();
	}

	public void setAlbum(String album) {
		tag.setAlbum(album);
	}

	public void setArtist(String artist) {
		tag.setArtist(artist);
	}

	public void setTitle(String title) {
		tag.setTitle(title);
	}

	public void setTrack(String track) {
		tag.setTrack(track);
	}

	public static String[] formats() {
		return supportedExts;
	}
	
	public boolean writeArt(String path) {
		return tag.writeArt(path);
	}

	public static void main(String[] args) {
		TaggedFile file = new TaggedFile(

				"/home/tthomas/Music/Lil Wayne Bruno Mars/Tha Carter IV/17 - Mirror Feat Bruno Mars.mp3");
		System.err.println(file.getOrchestra());
		System.err.println(file.getArtist());

	}
}
