package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.PlayerManager;

public class PlayerDispatch extends AbstractDispatch {

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String action = getRequiredParameter(request, "a");
		if (action.equals("play")) {
			PlayerManager.getInstance().play();
		} else if (action.equals("pause")) {
			PlayerManager.getInstance().pause();
		} else if (action.equals("stop")) {
			PlayerManager.getInstance().stop();
		} else if (action.equals("next")) {
			PlayerManager.getInstance().next();
		} else if (action.equals("previous")) {
			PlayerManager.getInstance().previous();
		} else if (action.equals("first")) {
			PlayerManager.getInstance().first();
		} else if (action.equals("last")) {
			PlayerManager.getInstance().last();
		} else if (action.equals("select")) {
			int index = Integer.parseInt(getRequiredParameter(request, "i"));
			PlayerManager.getInstance().select(index);
			if(!PlayerManager.getInstance().isPlaying().booleanValue()) {
				PlayerManager.getInstance().play();
			}
		} else if (action.equals("random")) {
			PlayerManager.getInstance().toggleRandom();
		} else if (action.equals("continuous")) {
			PlayerManager.getInstance().toggleContinuous();
		} else if (action.equals("party")) {
			PlayerManager.getInstance().toggleParty();
		}

		return getStatus();
	}

	private Map getStatus() {
		Map status = new HashMap();
		status.put("current", PlayerManager.getInstance().getCurrentSong());
		status.put("playing", PlayerManager.getInstance().isPlaying());
		status.put("position", PlayerManager.getInstance().getPosition());
		status.put("random", PlayerManager.getInstance().isRandom());
		status.put("continuous", PlayerManager.getInstance().isContinuous());
		status.put("party", PlayerManager.getInstance().isParty());
		return status;
	}
}
