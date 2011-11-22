package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.scanner.FileScanner;


public class RescanMusicDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // TODO: Make file path a parameter
    FileScanner fileScanner = new FileScanner("/home/tthomas/MUSIC/");
    fileScanner.scan();
    return Boolean.TRUE;
  }

}
