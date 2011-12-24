package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ControllerServlet;
import com.synchophy.server.User;
import com.synchophy.server.db.DatabaseManager;

public class TagDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String action = getRequiredParameter(request, "a");
		if (action.equals("add")) {

			String name = getRequiredParameter(request, "n");
			String album = getRequiredParameter(request, "album");
			String artist = getRequiredParameter(request, "artist");
			String tag = getRequiredParameter(request, "t");

			User user = ControllerServlet.getCurrentUser(request);

			DatabaseManager
					.getInstance()
					.executeQuery(
							"DELETE FROM tag WHERE tag_name = ? and name = ? and user_id = ? and artist = ? and album = ?",
							new Object[] { tag, name, new Long(user.getId()),
									artist, album });
			DatabaseManager
					.getInstance()
					.executeQuery(
							"INSERT INTO tag (tag_name, name, user_id, artist, album) values (?, ?, ?, ?, ?)",
							new Object[] { tag, name, new Long(user.getId()),
									artist, album });

			// what would be nice would be to have a list of current tags
			// matching the current file
			// then have the ability to enter one to add.
			// the current ones would be available to delete
		} else if (action.equals("delete")) {

			String name = getRequiredParameter(request, "n");
			String album = getRequiredParameter(request, "album");
			String artist = getRequiredParameter(request, "artist");
			String tag = getRequiredParameter(request, "t");
			User user = ControllerServlet.getCurrentUser(request);

			DatabaseManager
					.getInstance()
					.executeQuery(
							"DELETE FROM tag WHERE tag_name = ?, name = ? and user_id = ? and artist = ? and album = ?",
							new Object[] { tag, name, new Long(user.getId()),
									artist, album });

		} else if (action.equals("track-tags")) {

			// a list of all tags for a given item

			String name = getRequiredParameter(request, "n");
			String album = getRequiredParameter(request, "album");
			String artist = getRequiredParameter(request, "artist");

			return DatabaseManager.getInstance().query(
					"select distinct(t.tag_name) " + "from tag t "
							+ "   where (t.album = ? or t.album = '*') "
							+ "  and (t.artist = ? or t.artist = '*') "
							+ "  and (t.name = ? or t.name = '*') "
							+ " order by upper(t.tag_name)",
					new Object[] { album, artist, name },
					new String[] { "tag_name" });
		} else if (action.equals("list-tags")) {
			// a list of all tags

			return DatabaseManager.getInstance().query(
					"select distinct(tag_name) from tag", new Object[0],
					new String[] { "tag_name" });

		} else if (action.equals("list-tracks")) {
			// a lsit of tracks that match a tag

			String tag = getRequiredParameter(request, "t");
			return DatabaseManager.getInstance().loadTracksForTag(tag);

		}

		return Boolean.TRUE;
	}

}
