package com.synchophy.server.player;

import java.io.IOException;

public interface IMediaPlayer {

	public boolean notifyPlay(String filename) throws IOException;

	public void notifyAfterPlay() throws IOException;
	
	public void notifyPositionChange();
	
	public void notifyStop();
	
	public void shutdown();
	
	public void notifyPause();
}