package com.synchophy.server.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.synchophy.server.PlayerManager;

public class CommandLineMediaPlayer implements IMediaPlayer {
	private boolean advance;
	private Thread remoteThread;
	private OutputStream out;
	private BufferedReader input;
	// at some point we might want to be able to handle this
	private String elapsed;
	private boolean positionChanged;

	public CommandLineMediaPlayer() {
		System.err.println("Attempting to start mpg123 remote.");

		final String playerBinary = System.getProperty("media.player.path",
				"/opt/bin/mpg123");

		Thread remoteThread = new Thread() {
			public void run() {
				try {
					Process process = Runtime.getRuntime().exec(
							new String[] { playerBinary, "-R" });
					out = process.getOutputStream();
					input = new BufferedReader(new InputStreamReader(
							process.getInputStream()));
					process.waitFor();
				} catch (Exception e) {
					System.err.println("Remote control stopped.");
					e.printStackTrace();

				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {

						}
					}
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {

						}
					}

				}
			}
		};
		remoteThread.start();

	}

	private void sendCommand(String command) {
		System.err.println(command);
		if (out == null) {
			return;
		}
		try {
			out.write((command + "\n").getBytes());
			out.flush();

		} catch (IOException e) {
			System.err.println("Unable to write command " + command);
			e.printStackTrace();
			remoteThread.interrupt();
			try {
				remoteThread.join();
			} catch (InterruptedException ie) {

			}
		}
	}

	public boolean notifyPlay(String filename) throws IOException {

		advance = true;
		// skip to end of stream?
		sendCommand("L " + filename);
		String in = null;

		positionChanged = false;
		boolean started = false;
		while (Boolean.TRUE.equals(PlayerManager.getInstance().isPlaying())
				&& !positionChanged && (in = input.readLine()) != null) {
			if (PlayerManager.getInstance().isPaused()) {
				synchronized (PlayerManager.getInstance().getPauseLock()) {
					try {
						PlayerManager.getInstance().getPauseLock().wait();
					} catch (InterruptedException e) {
					}
					continue;
				}
			}
			if (started && in.equals("@P 0")) {
				System.err.println("Done playing " + filename);
				break;
			}

			if (in.substring(0, 2).equals("@I")) {
				// loaded info about the current song
				started = true;
			}

			if (in.substring(0, 2).equals("@F")) {
				elapsed = in;
			}
		}
		if (advance) {
			PlayerManager.getInstance().setPosition(
					PlayerManager.getInstance().getPosition().intValue() + 1);
		}
		return true;
	}

	public void notifyAfterPlay() throws IOException {

	}

	public void notifyPositionChange() {
		advance = false;
		positionChanged = true;
	}

	public void notifyStop() {
		advance = false;
		sendCommand("stop");
	}

	public void shutdown() {
		sendCommand("quit");
	}

	public void notifyPause() {
		sendCommand("pause");
	}

	public void notifyUnpause() {
		sendCommand("pause");
	}
}
