package com.synchophy.server;


import java.util.List;
import java.util.Map;

import com.synchophy.server.db.DatabaseManager;


public class User {

  private long id;
  private String username;


  public User(long id, String username) {

    this.id = id;
    this.username = username;
  }


  public static User login(String username, String password) {

    List rows = DatabaseManager.getInstance()
        .query("select id, password from user where username = ?", new Object[]{
          username
        });
    if (rows.size() != 1) {
      throw new UserAuthException("Unable to load user.");
    }
    Map row = (Map) rows.get(0);
    if (row.get("PASSWORD").equals(password)) {
      Integer id = (Integer) row.get("ID");
      return new User(id.intValue(), username);
    }
    throw new UserAuthException("Password does not match.");
  }


  public static User register(String username, String password) {

    DatabaseManager.getInstance()
        .executeQuery("insert into user (username, password) values (?, ?)", new Object[]{
            username, password
        });
    List rows = DatabaseManager.getInstance().query("CALL IDENTITY();");
    if (rows.size() != 1) {
      throw new UserAuthException("Unable to register user.");
    }
    Map row = (Map) rows.get(0);
    return new User(((Long) row.get("name")).longValue(), username);
  }


  public static boolean isUsernameUnique(String username) {

    List rows = DatabaseManager.getInstance().query("select username from user where username = ?",
                                                    new Object[]{
                                                      username
                                                    });
    if (rows.size() > 0) {
      return false;
    }
    return true;
  }


  public long getId() {

    return this.id;
  }

}
