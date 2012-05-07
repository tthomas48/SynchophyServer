package com.synchophy.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.synchophy.server.db.DatabaseManager;
import com.synchophy.server.player.IMediaPlayer;

public class PlayerManager {

	private final Object lock = new Object();
	protected final Object pause = new Object();
	private static PlayerManager instance;
	private boolean running;
	private boolean playable;
	private boolean done;
	protected boolean paused;
	private boolean random;
	private boolean continuous;
	private boolean party;
	private Thread playThread;
	private int position;
	private IMediaPlayer player;
	private static Random generator = new Random();
	private String currentFilename;
	private MetricManager metricManager;

	private PlayerManager() {

		init();
	}

	private void init() {

		try {
			String playerClass = ConfigManager.getInstance().getPlayerProvider();
			Class clazz = Class.forName(playerClass);
			Object obj = clazz.newInstance();
			if (obj instanceof IMediaPlayer == false) {
				throw new RuntimeException("Invalid media player specified: "
						+ playerClass);
			}
			player = (IMediaPlayer) obj;
			metricManager = new MetricManager();

		} catch (Exception e) {
			throw new RuntimeException(
					"Could not initialize media player. Check your media.player command line option.",
					e);
		}

		// by default we play forever
		continuous = true;

		setPosition(0);
		running = true;
		done = true;
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
					if (!running) {
						continue;
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
		if (!party && random) {
			int next = generator.nextInt(queue.size());
			currentFilename = (String) ((Map) queue.get(next)).get("file");
			return currentFilename;
		}

		if (position > queue.size() && continuous) {
			position = 0;
		}

		if (queue.size() > position) {
			currentFilename = (String) ((Map) queue.get(position)).get("file");
			System.err.println("PARTY? " + party);
			if (party) {
				getNextFive(queue);
			}
			return currentFilename;
		}
		return null;
	}

	private Object nextFiveLock = new Object();

	private void getNextFive(List queue) {
		System.err.println("Attempting next five");
		synchronized (nextFiveLock) {
			if (position + 4 >= queue.size()) {
				System.err.println("Need at least 5 more.");
				final User user = this.getCurrentSongUser();
				final Map song = this.getCurrentScrobbleInfo();
				Thread thread = new Thread("NextFiveThread") {
					public void run() {
						synchronized (nextFiveLock) {
							System.err.println("In next five thread.");
							metricManager.getNextFiveAndInsert(user, song, 5);
						}
					}
				};
				thread.start();
			}

		}
	}

	public synchronized void addSong(User user, Map song, boolean fillParty) {
		List queue = DatabaseManager.getInstance().loadQueue();
		int position = DatabaseManager.getInstance().loadQueueMax() + 1;

		DatabaseManager.getInstance().executeQuery(
				"insert into queue (index, file, user_id) values (?, ?, ?)",
				new Object[] { new Integer(position), song.get("file"),
						new Long(user.getId()) });
		if (party && fillParty) {
			getNextFive(queue);
		}

	}

	public boolean isArtistInQueue(String artist) {

		List rows = DatabaseManager
				.getInstance()
				.query("select 'X' from queue q, song s where s.file = q.file and s.artist = ?",
						new Object[] { artist }, new String[] { "x" });
		if (rows.size() > 0) {
			return true;
		}
		return false;
	}
        private static final int ARTISTS_BEFORE_REPEAT = 200;	
	public List getArtistsInQueue() {

		List rows = DatabaseManager
				.getInstance()
				.query("select s.artist from queue q, song s where s.file = q.file limit " + ARTISTS_BEFORE_REPEAT,
						new Object[0], new String[] { "artist" });
		List artists = new ArrayList();
		Iterator i = rows.iterator();
		while(i.hasNext()) {
			Map song = (Map) i.next();
			artists.add(song.get("artist"));
		}
		return artists;
	}
	

	public void next() {
		metricManager.next(currentFilename);

		int p = this.position;
		p++;
		List queue = DatabaseManager.getInstance().loadQueueFiles();
		if (p > queue.size() - 1) {
			p = queue.size() - 1;
		}
		setPosition(p);
	}

	public void previous() {
		metricManager.previous(currentFilename);

		int p = this.position;
		p--;
		if (p < 0) {
			p = 0;
		}
		setPosition(p);
	}

	public void first() {
		metricManager.first(currentFilename);

		setPosition(0);
	}

	public void last() {
		metricManager.last(currentFilename);

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

		metricManager.select(currentFilename);
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
					User user = this.getCurrentSongUser();
					Map song = this.getCurrentScrobbleInfo();

					System.err.println("Playing " + filename);
					metricManager.started(filename);
					playable = player.notifyPlay(filename);

					metricManager.finished(filename, user, song);
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
		done = true;
	}

	public void play() {
		if (paused) {
			synchronized (pause) {
				player.notifyUnpause();
				pause.notifyAll();
				paused = false;
			}
			return;
		}

		synchronized (lock) {
			lock.notify();
		}
	}

	public void pause() {
		paused = true;
		player.notifyPause();
	}

	public void stop() {

		playable = false;
		player.notifyStop();
	}

	public void shutdown() {
		
		stop();

		playable = false;
		running = false;
		if (playThread != null) {
			try {
				synchronized (pause) {
					pause.notifyAll();
				}
				synchronized (lock) {
					lock.notify();
				}
				playThread.join();
			} catch (InterruptedException e) {

			}
		}
		waitToFinish();
		instance = null;
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

		if (currentFilename == null) {
			return null;
		}

		if (currentFilename.startsWith("http://")) {
			// TODO: It would be cool to be able to look up what the current
			// song is
			List list = DatabaseManager
					.getInstance()
					.query("select '', name, 'Internet Radio' from station where url = ?",
							new Object[] { currentFilename },
							new String[] { "name", "artist", "album" });
			if (list.size() > 0) {
				return (Map) list.get(0);
			}
			return null;
		}

		List list = DatabaseManager
				.getInstance()
				.query("select trim(LEADING '0' FROM title_sort), artist_sort, album_sort from song where file = ?",
						new Object[] { currentFilename },
						new String[] { "name", "artist", "album" });
		if (list.size() > 0) {
			return (Map) list.get(0);
		}
		return null;
	}

	public User getCurrentSongUser() {
		List queue = DatabaseManager.getInstance().loadQueueFiles();
		if (position > queue.size()) {
			return null;
		}
		Map song = (Map) queue.get(position);
		return User.load(((Integer) song.get("user_id")).intValue());
	}

	public Map getCurrentScrobbleInfo() {
		if (currentFilename == null) {
			return null;
		}

		if (currentFilename.startsWith("http://")) {
			return null;
		}

		List list = DatabaseManager.getInstance().query(
				"select title, artist, album from song where file = ?",
				new Object[] { currentFilename },
				new String[] { "name", "artist", "album" });
		if (list.size() > 0) {
			return (Map) list.get(0);
		}
		return null;

	}

	public Boolean isPlaying() {
		if (paused) {
			return Boolean.FALSE;
		}

		return Boolean.valueOf(playable);
	}

	public Integer getPosition() {

		return new Integer(position);
	}

	public void setPosition(int position) {
		this.position = position;
		player.notifyPositionChange();
	}

	public boolean isPaused() {
		return paused;
	}

	public Object getPauseLock() {
		return pause;
	}

	public Boolean isRandom() {
		return Boolean.valueOf(random);
	}

	public void toggleRandom() {
		this.random = !this.random;
	}

	public Boolean isContinuous() {
		return Boolean.valueOf(continuous);
	}

	public void toggleContinuous() {
		this.continuous = !continuous;
	}

	public Boolean isParty() {
		return Boolean.valueOf(party);
	}

	public void toggleParty() {
		this.party = !party;
	}

}
