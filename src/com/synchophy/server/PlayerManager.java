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

		try {
			String playerClass = System.getProperty("media.player",
					CommandLineMediaPlayer.class.getName());
			Class clazz = Class.forName(playerClass);
			Object obj = clazz.newInstance();
			if (obj instanceof IMediaPlayer == false) {
				throw new RuntimeException("Invalid media player specified: "
						+ playerClass);
			}
			player = (IMediaPlayer) obj;

		} catch (Exception e) {
			throw new RuntimeException(
					"Could not initialize media player. Check your media.player command line option.",
					e);
		}

		setPosition(0);
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

		int p = this.position;
		p++;
		List queue = DatabaseManager.getInstance().loadQueueFiles();
		if (p > queue.size() - 1) {
			p = queue.size() - 1;
		}
		setPosition(p);
	}

	public void previous() {

		int p = this.position;
		p--;
		if (p < 0) {
			p = 0;
		}
		setPosition(p);
	}

	public void first() {

		setPosition(0);
	}

	public void last() {

		List queue = DatabaseManager.getInstance().loadQueueFiles();
		setPosition(queue.size() - 1);
	}

	public void select(int index) {

		List queue = DatabaseManager.getInstance().loadQueueFiles();
		int p = index;
		if (p < 0) {
			p = 0;
		}
		if (p > queue.size() - 1) {
			p = queue.size() - 1;
		}
		setPosition(p);
	}

	private void playList() {

		done = false;
		try {
			playable = true;
			while (playable) {

				try {
					String filename = getNextFilename();
					if (filename == null) {
						System.err.println("No more files to play.");
						playable = false;
						continue;
					}
					System.err.println("Playing " + filename);
					playable = player.notifyPlay(filename);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				player.notifyAfterPlay();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		playable = false;
		player.notifyStop();
		done = true;
	}

	public void play() {

		synchronized (lock) {
			lock.notify();
		}
	}

	public void pause() {
		player.notifyPause();
	}

	public void stop() {

		playable = false;
		player.notifyStop();
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
		player.notifyPositionChange();
	}
}