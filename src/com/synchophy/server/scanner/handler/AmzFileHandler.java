package com.synchophy.server.scanner.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.synchophy.server.ConfigManager;
import com.synchophy.server.scanner.FileScanner;

public class AmzFileHandler implements IFileHandler {

	public void handle(FileScanner scanner, File file) {
		String clamzPath = ConfigManager.getInstance()
				.getAmazonDownloaderPath();
		try {
			Process process = Runtime.getRuntime().exec(
					new String[] { clamzPath, file.getAbsolutePath(), "-d",
							file.getParentFile().getAbsolutePath() });
			/* handling the streams so that dead lock situation never occurs. */
			ProcessHandler inputStream = new ProcessHandler(
					process.getInputStream(), "STDIN");
			ProcessHandler errorStream = new ProcessHandler(
					process.getErrorStream(), "STDERR");
			inputStream.start();
			errorStream.start();

			process.waitFor();
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getExtension() {
		return "AMZ";
	}

	public class ProcessHandler extends Thread {

		InputStream inpStr;
		String strType;

		public ProcessHandler(InputStream inpStr, String strType) {
			this.inpStr = inpStr;
			this.strType = strType;
		}

		public void run() {
			try {
				InputStreamReader inpStrd = new InputStreamReader(inpStr);
				BufferedReader buffRd = new BufferedReader(inpStrd);
				String line = null;
				while ((line = buffRd.readLine()) != null) {
					System.out.println(strType + " —> " + line);
				}
				buffRd.close();

			} catch (Exception e) {
				System.out.println(e);
			}

		}
	}
}