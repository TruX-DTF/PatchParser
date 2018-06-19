package edu.lu.uni.serval.BugCommit.parser;

import java.io.File;

public class RunnableParser implements Runnable {

	private File prevFile;
	private File revFile;
	private File diffentryFile;
	private PatchParser parser;
	private int bugHunkSize;
	private int fixHunkSize;
	
	public RunnableParser(File prevFile, File revFile, File diffentryFile, PatchParser parser, int bugHunkSize, int fixHunkSize) {
		this.prevFile = prevFile;
		this.revFile = revFile;
		this.diffentryFile = diffentryFile;
		this.parser = parser;
		this.bugHunkSize = bugHunkSize;
		this.fixHunkSize = fixHunkSize;
	}

	@Override
	public void run() {
		parser.parsePatches(prevFile, revFile, diffentryFile, bugHunkSize, fixHunkSize);
	}
}
