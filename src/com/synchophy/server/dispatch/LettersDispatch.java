package com.synchophy.server.dispatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LettersDispatch extends AbstractDispatch {

	private static final char[] letters = new char[] { '#', 'A', 'B', 'C', 'D',
			'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	public Object execute(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		List chars = new ArrayList();
		for (int i = 0; i < letters.length; i++) {
			Map map = new HashMap();
			map.put("name", Character.toString(letters[i]));
			chars.add(map);
		}
		System.err.print(chars);
		return chars;
	}

}
