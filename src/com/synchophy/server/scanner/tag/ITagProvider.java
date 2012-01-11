package com.synchophy.server.scanner.tag;

public interface ITagProvider {

	public abstract boolean isParsed();

	public abstract String getAlbum();

	public abstract String getArtist();

	public abstract String getTitle();

	public abstract String getTrack();

	public abstract void setAlbum(String album);

	public abstract void setArtist(String artist);

	public abstract void setTitle(String title);

	public abstract void setTrack(String track);

}