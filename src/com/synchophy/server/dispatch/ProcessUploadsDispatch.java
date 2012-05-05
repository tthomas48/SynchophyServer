package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ConfigManager;
import com.synchophy.server.ControllerServlet;
import com.synchophy.server.db.DatabaseManager;
import com.synchophy.server.scanner.FileScanner;

public class ProcessUploadsDispatch extends AbstractDispatch {
	
	private static boolean running;

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub

		if(running) {
			return Boolean.FALSE;
		}
		
		Thread thread = new Thread() {
			public void run() {
				running = true;
				FileScanner scanner = new FileScanner(ConfigManager
						.getInstance().getMusicPath(), ConfigManager
						.getInstance().getUploadPath(),
						true);
				scanner.scan();
				ControllerServlet.touchLastModified();
				running = false;
			};

		};
		thread.setDaemon(true);
		thread.start();

		return Boolean.TRUE;
	}

	public static void main(String[] args) {

		try {
			FileScanner scanner = new FileScanner(ConfigManager.getInstance()
					.getMusicPath(), ConfigManager.getInstance()
					.getUploadPath());
			scanner.scan();
		} finally {
			DatabaseManager.getInstance().shutdown();
		}

	}

}
