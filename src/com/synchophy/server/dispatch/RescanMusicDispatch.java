package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.scanner.FileScanner;


public class RescanMusicDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  
	String musicPath = System.getProperty("music.path", "./Music");
    FileScanner fileScanner = new FileScanner(musicPath);
    fileScanner.scan();
    return Boolean.TRUE;
  }

}
