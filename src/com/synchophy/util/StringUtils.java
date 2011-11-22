package com.synchophy.util;


public class StringUtils {
  
  public static String alphabetizeLinguistically(String original) {
    String workingString = original.toUpperCase();
    if(workingString.startsWith("THE ")) {
      original = original.replaceFirst("(?i)^The ", "") + ", The";
    }
    else if(workingString.startsWith("A ")) {
      original = original.replaceFirst("(?i)^A ", "") + ", A";
    }
    return original;
  }
  
  public static String sortLetter(String original) {
    original = alphabetizeLinguistically(original);
    if(original.length() == 0) {
      return "#";
    }
    if(original.substring(0, 1).matches("[A-Z]")) {
      return original.substring(0, 1);
    }
    return "#";
  }
  
  public static String cleanTrack(String trackName) {

    while (trackName.length() > 0 && trackName.charAt(0) == '0') {
      trackName = trackName.substring(1);
    }

    int slash = trackName.lastIndexOf('/');
    if (slash < 0) {
      return lpad(trackName, '0', 10);
    }
    return lpad(trackName.substring(0, slash), '0', 10);
  }
  
  public static String lpad(String string, char character, int count) {
    int add = (count - string.length() > 0 ? count - string.length() : 0);
    for(int i = 0; i < add; i++) {
      string = character + string;
    }
    return string;
  }
  
  public static String formatTrack(String track) {
    track = cleanTrack(track);
    if(track.equals("0000000000")) {
      return "";
    }
    return track + " - ";
  }


  

}
