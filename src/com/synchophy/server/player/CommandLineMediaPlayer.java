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

	public CommandLineMediaPlayer() {
		System.err.println("Attempting to start mpg123 remote.");
		Thread remoteThread = new Thread() {
			public void run() {
				try {
					Process process = Runtime.getRuntime().exec(
							new String[] { "/opt/bin/mpg123", "-R" });
					out = process.getOutputStream();
					input = new BufferedReader(new InputStreamReader(
							process.getInputStream()));
					process.waitFor();
				} catch (Exception e) {
					System.err.println("Remote control stopped.");

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
		sendCommand(command, true);
	}

	private void sendCommand(String command, boolean wait) {
		System.err.println(command);
		if (out == null) {
			return;
		}
		try {
			out.write((command + "\n").getBytes());
			out.flush();

			// wait until we see our input echoed back to us
			if (wait) {
				String in = null;
				while ((in = input.readLine()) != null) {
					if (in.equals(command)) {
						break;
					}
				}
			}
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

		while (Boolean.TRUE.equals(PlayerManager.getInstance().isPlaying())
				&& (in = input.readLine()) != null) {
			if (in.equals("@P 0")) {
				System.err.println("Done playing " + filename);
				break;
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
		notifyStop();
	}

	public void notifyStop() {
		advance = false;
		sendCommand("stop", false);
	}

	public void shutdown() {
		sendCommand("quit", false);
	}

	public void notifyPause() {
		sendCommand("pause", false);
	}
}
