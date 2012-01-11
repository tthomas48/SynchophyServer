package com.synchophy.server.dispatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;

public class SyncDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String filepath = getRequiredParameter(request, "f");

		List files = DatabaseManager.getInstance().query(
				"select file from song where file = ?",
				new Object[] { filepath }, new String[] { "file" });
		System.err.println("Found " + files.size() + " files.");
		if (files.size() == 0) {
			return null;
		}
		File file = new File(filepath);
		if (file.exists() == false) {
			return null;
		}
		return file;
	}

	public void write(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		File filepath = null;
		try {
			filepath = (File) execute(request, response);
		} catch (RuntimeException e) {
			e.printStackTrace();
			response.setStatus(500);
			return;
		}
		if (filepath == null) {
			response.setStatus(404);
			return;
		}

		System.err.println("Writing out " + filepath);

		response.setHeader("Content-Type", "application/octet-stream");
		int size = (int) filepath.length();
		response.setContentLength(size);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				filepath));
		try {
			BufferedOutputStream out = new BufferedOutputStream(
					response.getOutputStream());

			int read = -1;
			while ((read = in.read()) != -1) {
				out.write(read);

			}
			out.flush();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
}
