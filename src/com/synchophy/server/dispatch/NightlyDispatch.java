package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ConfigManager;
import com.synchophy.server.db.DatabaseManager;
import com.synchophy.server.scanner.FileScanner;

public class NightlyDispatch extends AbstractDispatch {

	private static boolean running;

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		if (running) {
			return Boolean.FALSE;
		}

		Thread thread = new Thread() {
			public void run() {
				running = true;

				FileScanner scanner = new FileScanner(ConfigManager
						.getInstance().getMusicPath(), ConfigManager
						.getInstance().getUploadPath(), true);
				scanner.scan();
				ExpandDispatch.clearCache();
				running = false;
			};

		};
		thread.setDaemon(true);
		thread.start();

		return Boolean.TRUE;
	}

	private void generate() {
		List rows = DatabaseManager.getInstance().query("select id from users",
				new Object[0], new String[] { "id" });
		Iterator i = rows.iterator();
		while (i.hasNext()) {
			Map map = (Map) i.next();
			Integer id = (Integer) map.get("id");
			System.err.println("Generating nightly for " + id);
		}

	}

	public static void main(String[] args) {

		try {
			NightlyDispatch dispatch = new NightlyDispatch();
			dispatch.generate();
		} finally {
			DatabaseManager.getInstance().shutdown();
		}

	}
}
