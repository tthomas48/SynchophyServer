package com.synchophy.server.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.synchophy.server.User;

public class DatabaseManager {
	static {
		try {
			Class.forName("org.hsqldb.jdbcDriver");

		} catch (Exception e) {
			throw new RuntimeException("Could not load hsqldb driver.");
		}

	}

	ThreadLocal connection = new ThreadLocal();

	private static DatabaseManager instance;

	private DatabaseManager() {

		init();
	}

	private void init() {
		Connection conn = getConnection();
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			if (!tableExists(dbmd, "song")) {
				executeQuery("create table song (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, file varchar(2000), track varchar(50), artist varchar(2000), artist_sort varchar(2000), artist_key char(1), album varchar(2000), album_sort varchar(2000), album_key char(1), title varchar(2000), title_sort varchar(2000), title_key char(1), size integer, unique(file))");
			} else {
				// migrations
				if (!columnExists(dbmd, "song", "orchestra")) {

					executeQuery("ALTER TABLE song ADD COLUMN orchestra VARCHAR(2000)");
					executeQuery("ALTER TABLE song ADD COLUMN orchestra_sort VARCHAR(2000)");
					executeQuery("ALTER TABLE song ADD COLUMN orchestra_key char(1)");
				}
				if (!columnExists(dbmd, "song", "insert_timestamp")) {
					System.err.println("HERE3");
					executeQuery("ALTER TABLE song ADD COLUMN insert_timestamp TIMESTAMP default 'now'");
				}
			}

			if (!tableExists(dbmd, "import")) {
				executeQuery("create table import (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, file varchar(2000), track varchar(50), artist varchar(2000), artist_sort varchar(2000), artist_key char(1), album varchar(2000), album_sort varchar(2000), album_key char(1), title varchar(2000), title_sort varchar(2000), title_key char(1), size integer, unique(file))");
			} else {
				// migrations
				if (!columnExists(dbmd, "import", "orchestra")) {

					executeQuery("ALTER TABLE import ADD COLUMN orchestra VARCHAR(2000)");
					executeQuery("ALTER TABLE import ADD COLUMN orchestra_sort VARCHAR(2000)");
					executeQuery("ALTER TABLE import ADD COLUMN orchestra_key char(1)");
				}
			}

			if (!tableExists(dbmd, "bad_song")) {
				executeQuery("create table bad_song (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, file varchar(2000), message varchar(2000), unique(file))");
			}
			if (!tableExists(dbmd, "import_error")) {
				executeQuery("create table import_error (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, file varchar(2000), message varchar(2000), unique(file))");
			}
			if (!tableExists(dbmd, "sticky")) {
				executeQuery("CREATE TABLE sticky (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, artist VARCHAR(2000), album VARCHAR(2000), name VARCHAR(2000), type VARCHAR(25), stick INTEGER, user_id INTEGER)");
			}
			if (!indexExists(dbmd, "STICKY", "STICKY_STICK_IX")) {
				executeQuery("create index STICKY_STICK_IX on STICKY (STICK);");
			}
			if (!tableExists(dbmd, "settings")) {
				executeQuery("CREATE TABLE settings (key_name VARCHAR(100) PRIMARY KEY, value VARCHAR(500), user_id INTEGER)");
			}
			if (!tableExists(dbmd, "users")) {
				executeQuery("CREATE TABLE users (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, username varchar(100), password varchar(100), token varchar(100), expires BIGINT, unique(username))");
			}
			if (!tableExists(dbmd, "queue")) {
				executeQuery("CREATE TABLE queue (index INTEGER PRIMARY KEY, file varchar(2000), user_id INTEGER)");
			} else {
				// migrations
				if (!columnExists(dbmd, "queue", "user_id")) {

					executeQuery("ALTER TABLE queue ADD COLUMN user_id INTEGER");
				}
			}
			if (!tableExists(dbmd, "tag")) {
				executeQuery("CREATE TABLE tag (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, tag_name VARCHAR(2000), artist VARCHAR(2000), album VARCHAR(2000), name VARCHAR(2000), user_id INTEGER)");
			}
			if (!tableExists(dbmd, "station")) {
				executeQuery("CREATE TABLE station (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, name varchar(2000), url varchar(2000), user_id INTEGER)");
			}
			if (!tableExists(dbmd, "metric")) {
				executeQuery("create table metric (id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, file varchar(2000), track varchar(50), artist varchar(2000), artist_sort varchar(2000), artist_key char(1), album varchar(2000), album_sort varchar(2000), album_key char(1), title varchar(2000), title_sort varchar(2000), title_key char(1), size integer, metric_timestamp timestamp default CURRENT_TIMESTAMP, metric_type char(1))");
			}

		} catch (SQLException e) {
			throw new RuntimeException("Unable to init database.", e);
		}
	}

	private boolean indexExists(DatabaseMetaData dbmd, String tableName,
			String indexName) throws SQLException {
		ResultSet rs = dbmd.getIndexInfo(null, null, tableName.toUpperCase(),
				false, false);
		try {
			while (rs.next()) {
				if (indexName.equals(rs.getString("INDEX_NAME"))) {
					return true;
				}
			}

		} finally {
			rs.close();
		}
		return false;

	}

	private boolean tableExists(DatabaseMetaData dbmd, String name)
			throws SQLException {
		ResultSet rs = dbmd.getTables(null, null, name.toUpperCase(), null);
		try {
			return rs.next();
		} finally {
			rs.close();
		}
	}

	private boolean columnExists(DatabaseMetaData dbmd, String tableName,
			String name) throws SQLException {
		ResultSet rs = dbmd.getColumns(null, null, tableName.toUpperCase(),
				name.toUpperCase());
		try {
			return rs.next();
		} finally {
			rs.close();
		}
	}

	public void executeQuery(String sql) {

		executeQuery(sql, new Object[0], false);
	}

	public void executeQuery(String sql, boolean failQuietly) {

		executeQuery(sql, new Object[0], failQuietly);
	}

	public void executeQuery(String sql, Object[] params) {
		executeQuery(sql, params, false);
	}

	public void executeQuery(String sql, Object[] params, boolean failQuietly) {

		try {
			Connection c = getConnection();
			PreparedStatement sth = c.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				sth.setObject(i + 1, params[i]);
			}

			sth.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			if (!failQuietly) {
				throw new RuntimeException("Unable to execute query " + sql, e);
			}
		}
	}

	public List query(String sql) {

		return query(sql, new Object[0]);

	}

	public List query(String sql, Object[] params) {

		return query(sql, params, new String[0]);
	}

	public List query(String sql, Object[] params, String[] outputKeys) {

		try {
			Connection c = getConnection();
			PreparedStatement sth = c.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				sth.setObject(i + 1, params[i]);
			}

			List result = new ArrayList();

			ResultSet rs = sth.executeQuery();
			if (outputKeys.length == 0) {
				ResultSetMetaData md = rs.getMetaData();
				int columnCount = md.getColumnCount();
				if (outputKeys.length != columnCount) {
					outputKeys = new String[columnCount];
					for (int i = 0; i < columnCount; i++) {
						outputKeys[i] = md.getColumnName(i + 1);
					}
				}
			}

			while (rs.next()) {
				Map row = new LinkedHashMap();
				for (int i = 0; i < outputKeys.length; i++) {
					row.put(outputKeys[i], rs.getObject(i + 1));
				}
				result.add(row);
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to execute query " + sql, e);
		}

	}

	public PreparedStatement prepare(String sql) {

		try {
			Connection c = getConnection();
			PreparedStatement sth = c.prepareStatement(sql);
			return sth;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Connection getConnection() {

		try {
			if (connection.get() == null) {

				connection.set(DriverManager.getConnection(
						"jdbc:hsqldb:file:music", "sa", ""));
			}
			return (Connection) connection.get();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void close() {

		executeQuery("SHUTDOWN;");
		try {
			Connection c = (Connection) connection.get();
			if (c != null) {
				c.close();
			}
			connection.set(null);
		} catch (SQLException e) {
			// TODO log warning
		}
	}

	public void shutdown() {

		close();
	}

	public static synchronized DatabaseManager getInstance() {

		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

	public List loadQueueFiles() {

		return DatabaseManager.getInstance().query(
				"select file, user_id from queue order by index",
				new Object[0], new String[] { "file", "user_id" });
	}

	public int loadQueueMax() {

		List list = DatabaseManager.getInstance().query(
				"select coalesce(max(index), 0) from queue", new Object[0],
				new String[] { "max" });
		if (list.size() == 0) {
			return 0;
		}
		Map row = (Map) list.get(0);
		return ((Integer) row.get("max")).intValue();
	}

	public List loadQueue() {

		return DatabaseManager
				.getInstance()
				.query("select q.index, "
						+ "      coalesce(trim(LEADING '0' FROM title_sort), st.name), "
						+ "      coalesce(artist_sort, 'Internet Radio'), "
						+ "      coalesce(album_sort, '') "
						+ " from queue q"
						+ "      left outer join song s on (s.file = q.file)"
						+ "      left outer join station st on (st.url = q.file) "
						+ "order by index", new Object[0],
						new String[] { "index", "name", "artist", "album" });
	}

	public List loadTracksForTag(String tag) {
		return DatabaseManager
				.getInstance()
				.query("select trim(LEADING '0' FROM s.title_sort) name, s.artist_sort artist, s.album_sort album, s.file "
						+ "from song s, tag t where t.tag_name = ?"
						+ "   and (s.album = t.album or t.album = '*') "
						+ "  and (s.artist = t.artist or t.artist = '*') "
						+ "  and (s.title = t.name or t.name = '*') "
						+ " group by s.title_sort, s.artist_sort, s.album_sort, s.file  "
						+ " order by upper(s.album_sort), upper(s.title_sort), upper(s.artist_sort) ",
						new Object[] { tag },
						new String[] { "name", "artist", "album", "file" });

	}

	public String getSetting(User user, String key) {
		List rows = DatabaseManager
				.getInstance()
				.query("select value from settings where user_id = ? and key_name = ?",
						new Object[] { new Long(user.getId()), key

						}, new String[] { "value" });
		if (rows.size() < 1) {
			return null;
		}
		Map row = (Map) rows.get(0);
		return (String) row.get("value");

	}
}
