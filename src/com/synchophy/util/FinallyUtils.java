package com.synchophy.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FinallyUtils {

	public static void close(PreparedStatement stmt) {
		try {
			if (stmt != null && stmt.isClosed() == false) {
				stmt.close();

			}
		} catch (SQLException e) {

		}
	}
}
