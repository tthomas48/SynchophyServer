package com.synchophy.util;

import java.io.File;
import java.util.regex.Pattern;

public class StringUtils {

	private static final char[] caps = new char[] { 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	public static String alphabetizeLinguistically(String original) {
		String workingString = original.toUpperCase();
		if (workingString.startsWith("THE ")) {
			original = original.substring(4) + ", The";
		} else if (workingString.startsWith("A ")) {
			original = original.substring(2) + ", A";
		}
		return original;
	}

	public static String unAlphabetizeLinguistically(String original) {
		String workingString = original;
		if (workingString.endsWith(", The")) {
			original = "The " + original.substring(0, original.length() - 5);
		} else if (workingString.endsWith(", A")) {
			original = "A " + original.substring(0, original.length() - 3);
		}
		return original;
	}

	public static String sortLetter(String original) {
		original = alphabetizeLinguistically(original);
		if (original.length() == 0) {
			return "#";
		}
		for (int i = 0; i < caps.length; i++) {
			if (original.startsWith("" + caps[i])) {
				return original.substring(0, 1);
			}
		}
		return "#";
	}

	public static final char[] VALID_CHARS = new char[] { 'a', 'b', 'c', 'd',
			'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', '0', '-', ' ', 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	public static String cleanFilename(String filename) {

		String output = "";
		for (int i = 0; i < filename.length(); i++) {
			for (int j = 0; j < VALID_CHARS.length; j++) {
				if (VALID_CHARS[j] == filename.charAt(i)) {
					output += filename.charAt(i);
					break;
				}
			}
		}
		return output;
	}

	public static String removeDoubleSlashes(String filename) {
		String output = "";
		for (int i = 0; i < filename.length(); i++) {
			if (filename.charAt(i) == '/') {
				if (i + 1 < filename.length() && filename.charAt(i + 1) == '/') {
					continue;
				}
			}
			output += filename.charAt(i);
		}
		return output;
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
		for (int i = 0; i < add; i++) {
			string = character + string;
		}
		return string;
	}

	public static String formatTrack(String track) {
		track = cleanTrack(track);
		if (track.equals("0000000000")) {
			return "";
		}
		return track + " - ";
	}

	public static String extractTrack(String trackAndTitle) {
		if (Pattern.matches("^\\d+ - .*", trackAndTitle)) {
			return cleanTrack(trackAndTitle.substring(0,
					trackAndTitle.indexOf(" - ")));
		}
		return cleanTrack("");
	}

	public static String extractTitle(String trackAndTitle) {
		if (Pattern.matches("^\\d+ - .*", trackAndTitle)) {
			return trackAndTitle.substring(trackAndTitle.indexOf(" - ") + 3);
		}
		return trackAndTitle;
	}

	public static String getExtension(File file) {
		String filename = file.getName();
		int lastPeriod = filename.lastIndexOf('.');
		if (lastPeriod < 0) {
			return "";
		}

		return filename.substring(lastPeriod + 1).toUpperCase();
	}

	public static void main(String[] args) {
		System.err.println(StringUtils.extractTrack("My Track Name"));
		System.err.println(StringUtils.extractTrack("1 Little Indian"));
		System.err.println(StringUtils.extractTrack("1 - Little Indian"));
		System.err.println(StringUtils.extractTrack("14 - Little Indian"));
		System.err.println(StringUtils.extractTitle("My Track Name"));
		System.err.println(StringUtils.extractTitle("1 Little Indian"));
		System.err.println(StringUtils.extractTitle("1 - Little Indian"));
		System.err.println(StringUtils.extractTitle("14 - Little Indian"));
		// "abcDEF-349 39343kfd",
		System.err.println(StringUtils.cleanFilename("abcDEF-349 3934(*)3kfd"));
		System.err.println(StringUtils.removeDoubleSlashes("/foo//bar/"));

	}

}
