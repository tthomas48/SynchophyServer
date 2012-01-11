package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;

public class DownloadDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String artist = this.getRequiredParameter(request, "artist");
		String album = this.getRequiredParameter(request, "album");
		String name = this.getRequiredParameter(request, "name");
		
		List params = new ArrayList();
		String andClause = "";
		String conjunction = " where ";
		if(artist.equals("*") == false) {
			andClause += conjunction + " artist_sort = ? ";
			conjunction = " and ";
			params.add(artist);
		}
		if(album.equals("*") == false) {
			andClause += conjunction + " album_sort = ? ";
			conjunction = " and ";
			params.add(album);
		}
		if(name.equals("*") == false) {
			andClause += conjunction + " title_sort = ? ";
			conjunction = " and ";
			params.add(name);
		}
		
		
		String query = "select file, size "
				+ " from song "
				+ andClause;
		return DatabaseManager.getInstance().query(query, params.toArray(new Object[params.size()]), new String[] {"file", "size"});
	}

}
