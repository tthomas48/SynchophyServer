package com.synchophy.server.player;

import java.io.IOException;

public class CommandLineMediaPlayer implements IMediaPlayer {

	public boolean play(String filename) throws IOException {

		Runtime.getRuntime().exec(new String[] { "mpg123", "filename", "&" });

		return false;
	}

	public void stopOutput() {

		try {
			Runtime.getRuntime().exec(new String[] { "killall", "mpg123" });
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void afterPlay() throws IOException {

	}

}
