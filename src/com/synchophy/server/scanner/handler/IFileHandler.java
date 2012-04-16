package com.synchophy.server.scanner.handler;

import java.io.File;

import com.synchophy.server.scanner.FileScanner;

public interface IFileHandler {

	public void handle(FileScanner scanner, File file);
	
	public String getExtension();

}
