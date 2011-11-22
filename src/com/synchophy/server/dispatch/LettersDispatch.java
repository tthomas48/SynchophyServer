package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.db.DatabaseManager;


public class LettersDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    return DatabaseManager.getInstance().query("select distinct(coalesce(upper(regexp_substring(artist, '^[A-Z]')), '#')) letter from song order by letter");
  }

}
