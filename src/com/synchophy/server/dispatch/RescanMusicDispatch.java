package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ConfigManager;
import com.synchophy.server.scanner.AlbumArtScanner;
import com.synchophy.server.scanner.FileScanner;

public class RescanMusicDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String musicPath = ConfigManager.getInstance().getMusicPath();
		FileScanner fileScanner = new FileScanner(musicPath);
		fileScanner.scan();
		ExpandDispatch.clearCache();

		return Boolean.TRUE;
	}

}
