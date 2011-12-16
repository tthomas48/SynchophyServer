package com.synchophy.server;

import java.util.List;
import java.util.Map;

import com.synchophy.server.db.DatabaseManager;
import com.synchophy.server.player.CommandLineMediaPlayer;
import com.synchophy.server.player.IMediaPlayer;

public class PlayerManager {

	private final Object lock = new Object();
	private static PlayerManager instance;
	private boolean running;
	private boolean playable;
	private boolean done;
	private Thread playThread;
	private int position;
	private IMediaPlayer player;

	private PlayerManager() {

		init();
	}

	private void init() {
		
		System.getProperty("media.player", CommandLineMediaPlayer.class.getName());

		position = 0;
		running = true;
		playThread = new Thread("PlayThread") {

			public void run() {

				while (running) {
					synchronized (lock) {
						try {
							lock.wait();
						} catch (Exception e) {
							running = false;
						}
					}
					playList();
				}
			}
		};
		playThread.start();
	}

	public static synchronized PlayerManager getInstance() {

		if (instance == null) {
			instance = new PlayerManager();
		}
		return instance;
	}

	private String getNextFilename() {
		List queue = DatabaseManager.getInstance().loadQueueFiles();
		if (queue.size() > position) {
			String filename = (String) ((Map) queue.get(position)).get("file");
			return filename;
		}
		return null;
	}

	public void next() {

		position++;
		List queue = DatabaseManager.getInstance().loadQueueFiles();
		if (position > queue.size() - 1) {
			position = queue.size() - 1;
		}
	}

	public void previous() {

		position--;
		if (position < 0) {
			position = 0;
		}
	}

	public void first() {

		position = 0;
	}

	public void last() {

		List queue = DatabaseManager.getInstance().loadQueueFiles();
		position = queue.size() - 1;
	}

	public void select(int index) {

		List queue = DatabaseManager.getInstance().loadQueueFiles();
		position = index;
		if (position < 0) {
			position = 0;
		}
		if (position > queue.size() - 1) {
			position = queue.size() - 1;
		}
	}

	private void playList() {

		done = false;
		try {
			playable = true;
			while (playable) {

				while (playable) {
					try {
						playable = player.play(getNextFilename());
					} catch (Exception e) {
						// e.printStackTrace();
						break;
					}
				}
				player.afterPlay();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		playable = false;
		player.stopOutput();
		done = true;
	}

	public void play() {

		synchronized (lock) {
			lock.notify();
		}
	}

	public void stop() {

		playable = false;
	}

	public void shutdown() {

		playable = false;
		running = false;
		if (playThread != null) {
			try {
				synchronized (lock) {
					lock.notify();
				}
				playThread.join();
			} catch (InterruptedException e) {

			}
		}
		waitToFinish();
	}

	private void waitToFinish() {

		while (!done) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}

	}

	public Map getCurrentSong() {

		List queue = DatabaseManager.getInstance().loadQueueFiles();
		if (position >= queue.size() - 1) {
			return null;
		}
		String song = (String) ((Map) queue.get(position)).get("file");
		List list = DatabaseManager
				.getInstance()
				.query("select trim(LEADING '0' FROM title_sort), artist_sort, album_sort from song where file = ?",
						new Object[] { song },
						new String[] { "name", "artist", "album" });
		if (list.size() > 0) {
			return (Map) list.get(0);
		}
		return null;
	}

	public Boolean isPlaying() {

		return Boolean.valueOf(playable);
	}

	public Integer getPosition() {

		return new Integer(position);
	}

	public void setPosition(int position) {
		this.position = position;

	}

}
