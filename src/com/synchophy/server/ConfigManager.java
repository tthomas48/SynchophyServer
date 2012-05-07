package com.synchophy.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.synchophy.server.player.CommandLineMediaPlayer;
import com.synchophy.server.scanner.tag.Mp3OnlyTagProvider;

public class ConfigManager {

	private static ConfigManager instance;
	private Properties properties = new Properties();

	private ConfigManager() {

		init();
	}

	private void init() {
		try {
			properties.load(ConfigManager.class
					.getResourceAsStream("/synchophy.server.properties"));
		} catch (IOException e) {
			throw new RuntimeException(
					"Unable to load synchophy.server.properties file.");
		}
	}

	public static synchronized ConfigManager getInstance() {

		if (instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	public String getMusicPath() {
		return properties.getProperty("music.path", "/Music");
	}

	public void setMusicPath(String path) {
		properties.setProperty("music.path", path);
	}

	public String getUploadPath() {
		return properties.getProperty("upload.path", "/Upload");
	}

	public void setUploadPath(String path) {
		properties.setProperty("upload.path", path);
	}

	public String getNightlyPath() {
		return properties.getProperty("nightly.path", "/Nightly");
	}

	public void setNightlyPath(String path) {
		properties.setProperty("nightly.path", path);
	}

	public String getPlayerProvider() {
		return properties.getProperty("player.provider",
				CommandLineMediaPlayer.class.getName());
	}

	public void setPlayerProvider(String provider) {
		properties.setProperty("player.provider", provider);
	}

	public String getTagProvider() {
		return properties.getProperty("tag.provider",
				Mp3OnlyTagProvider.class.getName());
	}

	public void setTagProvider(String provider) {
		properties.setProperty("tag.provider", provider);
	}

	public String getMediaPlayerPath() {
		return properties.getProperty("media.player.path", "/opt/bin/mpg123");
	}

	public String getAmazonDownloaderPath() {
		return properties.getProperty("amazon.downloader.path",
				"/opt/bin/clamz");
	}

	public List<String> getFileHandlers() {
		List<String> handlers = new ArrayList<String>();
		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			if (properties.containsKey("file.handler." + i)) {
				handlers.add((String) properties.get("file.handler." + i));
				continue;
			}
			break;
		}
		return handlers;
	}

	public void save() throws IOException {
		URL url = ConfigManager.class
				.getResource("/synchophy.server.properties");
		File file = new File(url.getFile());
		FileWriter out = new FileWriter(file);
		properties.store(out, "Synchopy Properties");
	}
}
