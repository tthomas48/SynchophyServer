package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.PlayerManager;
import com.synchophy.server.db.DatabaseManager;

public class PlaylistDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String action = getRequiredParameter(request, "a");
		if (action.equals("add")) {

			int startPosition = DatabaseManager.getInstance().loadQueueMax();

			String artist = getOptionalParameter(request, "artist");
			if (artist == null || artist.equals("")) {
				artist = "*";
			}
			String album = getRequiredParameter(request, "album");
			String title = getRequiredParameter(request, "title");
			boolean play = Boolean.valueOf(
					getRequiredParameter(request, "play")).booleanValue();

			String conjunction = "where";
			List params = new ArrayList();
			String sql = "select file from song ";
			String order = "order by artist_sort, album_sort, title_sort";

			if (artist.equals("*") == false) {
				sql += conjunction + " artist_sort = ? ";
				params.add(artist);
				conjunction = "and";
			}
			if (album.equals("*") == false) {
				sql += conjunction + " album_sort = ? ";
				params.add(album);
				conjunction = "and";

			}
			if (title.equals("*") == false) {
				sql += conjunction + " trim(LEADING '0' FROM title_sort) = ? ";
				params.add(title);
			}

			int index = startPosition + 1;

			List toAdd = DatabaseManager.getInstance().query(sql + order,
					params.toArray(new String[params.size()]),
					new String[] { "file" });
			for (int i = 0; i < toAdd.size(); i++) {
				System.err.println("Inserting into queue ["
						+ new Integer(index + i) + "]"
						+ (String) ((Map) toAdd.get(i)).get("file"));
				DatabaseManager.getInstance().executeQuery(
						"insert into queue (index, file) values (?, ?)",
						new Object[] { new Integer(index + i),
								(String) ((Map) toAdd.get(i)).get("file") });

			}
			if (play) {
				PlayerManager.getInstance().select(startPosition);
				PlayerManager.getInstance().play();
			}
		} else if (action.equals("add-tag")) {
			String tag = getRequiredParameter(request, "tag");
			boolean play = Boolean.valueOf(
					getRequiredParameter(request, "play")).booleanValue();
			
			List tracks = DatabaseManager.getInstance().loadTracksForTag(tag);

			int startPosition = DatabaseManager.getInstance().loadQueueMax();
			int index = startPosition + 1;

			for (int i = 0; i < tracks.size(); i++) {
				System.err.println("Inserting into queue ["
						+ new Integer(index + i) + "]"
						+ (String) ((Map) tracks.get(i)).get("file"));
				DatabaseManager.getInstance().executeQuery(
						"insert into queue (index, file) values (?, ?)",
						new Object[] { new Integer(index + i),
								(String) ((Map) tracks.get(i)).get("file") });

			}
			if (play) {
				PlayerManager.getInstance().select(startPosition);
				PlayerManager.getInstance().play();
			}

		} else if (action.equals("remove")) {
			String index = getRequiredParameter(request, "i");

			System.err.println("Deleting from queue [" + new Integer(index)
					+ "]");

			DatabaseManager.getInstance().executeQuery(
					"delete from queue where index = ?",
					new Object[] { new Integer(index) });

		} else if (action.equals("list")) {
			Map list = new HashMap();
			list.put("queue", DatabaseManager.getInstance().loadQueue());
			list.put("current", PlayerManager.getInstance().getPosition());
			return list;

		} else if (action.equals("clear")) {
			DatabaseManager.getInstance().executeQuery("delete from queue",
					new Object[0]);

		} else if (action.equals("save")) {
			String name = getRequiredParameter(request, "n");

		} else if (action.equals("load")) {
			String name = getRequiredParameter(request, "n");

		} else {
			throw new RuntimeException("Invalid value for the parameter 'a'.");
		}
		return Boolean.TRUE;
	}

}
