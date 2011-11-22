package com.synchophy.util;


import junit.framework.TestCase;


public class StringUtilsTest extends TestCase {

  public void testAlphabetizeLinguistically() {

    assertEquals("Cure, The", StringUtils.alphabetizeLinguistically("The Cure"));
    assertEquals("They Sing", StringUtils.alphabetizeLinguistically("They Sing"));
    assertEquals("Hawk and a Handsaw, A",
                 StringUtils.alphabetizeLinguistically("A Hawk and a Handsaw"));
  }


  public void testSortLetter() {

    assertEquals("C", StringUtils.sortLetter("The Cure"));
    assertEquals("T", StringUtils.sortLetter("They Sing"));
    assertEquals("H", StringUtils.sortLetter("A Hawk and a Handsaw"));
    assertEquals("#", StringUtils.sortLetter("+/-"));
    assertEquals("#", StringUtils.sortLetter(""));
  }
  
  public void testCleanTrack() {
    assertEquals("1", StringUtils.cleanTrack("01"));
    assertEquals("1", StringUtils.cleanTrack("1"));
    assertEquals("1", StringUtils.cleanTrack("1/14"));
    assertEquals("10", StringUtils.cleanTrack("10/14"));
    assertEquals("1", StringUtils.cleanTrack("01/14"));
  }
}
