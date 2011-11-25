package com.synchophy.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.synchophy.server.db.DatabaseManager;

public class User {

	private long id;
	private String username;
	private String token;

	public User(long id, String username) {

		this.id = id;
		this.username = username;
	}
	
	public static User load(String token) {
		List rows = DatabaseManager.getInstance().query(
				"select id, username from user where token = ?",
				new Object[] { token });
		if (rows.size() != 1) {
			return null;
		}
		Map row = (Map) rows.get(0);
		return new User(((Integer)row.get("ID")).longValue(), (String) row.get("USERNAME"));
	}

	public static User login(String username, String password) {

		List rows = DatabaseManager.getInstance().query(
				"select id, password from user where username = ?",
				new Object[] { username });
		if (rows.size() != 1) {
			throw new UserAuthException("Unable to load user.");
		}
		Map row = (Map) rows.get(0);
		if (row.get("PASSWORD").equals(password)) {
			Integer id = (Integer) row.get("ID");
			User user = new User(id.intValue(), username);
			String token = user.generateToken();
			// 1 week
			long expires = new Date().getTime() * (1000 * 60 * 60 * 24 * 7);
			DatabaseManager.getInstance().executeQuery(
					"update user set token = ?, expires = ? where id = ?",
					new Object[] { token, new Long(expires), id });
			return user;
		}
		throw new UserAuthException("Password does not match.");
	}

	public static User register(String username, String password) {

		DatabaseManager.getInstance().executeQuery(
				"insert into user (username, password) values (?, ?)",
				new Object[] { username, password });
		List rows = DatabaseManager.getInstance().query("CALL IDENTITY();");
		if (rows.size() != 1) {
			throw new UserAuthException("Unable to register user.");
		}
		Map row = (Map) rows.get(0);
		return new User(((Long) row.get("name")).longValue(), username);
	}

	public static boolean isUsernameUnique(String username) {

		List rows = DatabaseManager.getInstance().query(
				"select username from user where username = ?",
				new Object[] { username });
		if (rows.size() > 0) {
			return false;
		}
		return true;
	}

	public long getId() {

		return this.id;
	}

	public String generateToken() {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			byte[] tokenBytes = sha1.digest(("synchophy-" + new Date()
					.getTime()).getBytes());
			this.token = new String(Base64.encodeBase64(tokenBytes));
			return token;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unabel to generate user tokens.", e);
		}
	}	
}
