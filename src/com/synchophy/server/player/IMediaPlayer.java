package com.synchophy.server.player;

import java.io.IOException;

public interface IMediaPlayer {

	public abstract boolean play(String filename) throws IOException;

	public abstract void stopOutput();

	public void afterPlay() throws IOException;

}