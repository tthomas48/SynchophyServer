package com.synchophy.util;


import junit.framework.TestCase;


public class StringUtilsTest extends TestCase {

  public void testAlphabetizeLinguistically() {

    assertEquals("Cure, The", StringUtils.alphabetizeLinguistically("The Cure"));
    assertEquals("They Sing", StringUtils.alphabetizeLinguistically("They Sing"));
    assertEquals("Hawk and a Handsaw, A",
                 StringUtils.alphabetizeLinguistically("A Hawk and a Handsaw"));
  }
  
  public void testunAlphabetizeLinguistically() {

	    assertEquals("The Cure", StringUtils.unAlphabetizeLinguistically("Cure, The"));
	    assertEquals("They Sing", StringUtils.unAlphabetizeLinguistically("They Sing"));
	    assertEquals("A Hawk and a Handsaw",
	                 StringUtils.unAlphabetizeLinguistically("Hawk and a Handsaw, A"));
	  }
  


  public void testSortLetter() {

    assertEquals("C", StringUtils.sortLetter("The Cure"));
    assertEquals("T", StringUtils.sortLetter("They Sing"));
    assertEquals("H", StringUtils.sortLetter("A Hawk and a Handsaw"));
    assertEquals("#", StringUtils.sortLetter("+/-"));
    assertEquals("#", StringUtils.sortLetter(""));
  }
  
  public void testCleanTrack() {
    assertEquals("0000000001", StringUtils.cleanTrack("01"));
    assertEquals("0000000001", StringUtils.cleanTrack("1"));
    assertEquals("0000000001", StringUtils.cleanTrack("1/14"));
    assertEquals("0000000010", StringUtils.cleanTrack("10/14"));
    assertEquals("0000000001", StringUtils.cleanTrack("01/14"));
  }
}
