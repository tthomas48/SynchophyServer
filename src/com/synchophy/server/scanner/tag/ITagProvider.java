package com.synchophy.server.scanner.tag;

public interface ITagProvider {

	public boolean isParsed();

	public String getAlbum();

	public String getArtist();

	public String getTitle();

	public String getTrack();

	public void setAlbum(String album);

	public void setArtist(String artist);

	public void setTitle(String title);

	public void setTrack(String track);

}