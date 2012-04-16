package com.synchophy.server;

import java.io.IOException;
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
	public String getUploadPath() {
		return properties.getProperty("upload.path", "/Upload");
	}
	

	public String getPlayerProvider() {
		return properties.getProperty("player.provider",
				CommandLineMediaPlayer.class.getName());
	}

	public String getTagProvider() {
		return properties.getProperty("tag.provider",
				Mp3OnlyTagProvider.class.getName());
	}

	public String getMediaPlayerPath() {
		return properties.getProperty("media.player.path", "/opt/bin/mpg123");
	}
	
	public String getAmazonDownloaderPath() {
		return properties.getProperty("amazon.downloader.path", "/opt/bin/clamz");
	}
	
	public List getFileHandlers() {
		List handlers = new ArrayList();
		for(int i = 1; i < Integer.MAX_VALUE; i++) {
			if(properties.containsKey("file.handler." + i)) {
				handlers.add(properties.get("file.handler." + i));
				continue;
			}
			break; 
		}
		return handlers;
	}
}
