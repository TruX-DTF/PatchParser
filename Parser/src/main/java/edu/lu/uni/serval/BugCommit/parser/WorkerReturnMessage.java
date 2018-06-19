package edu.lu.uni.serval.BugCommit.parser;

import java.util.List;
import java.util.Map;

public class WorkerReturnMessage {
	private int numOfPatches;
	private Map<String, Map<String, Integer>> stmtMaps;
	private Map<String, Map<String, Integer>> elementsMaps;
	public int diffs = 0;
	public int hunks = 0;
	public int zeroG = 0;
	public int overRap = 0;
	public Map<String, Map<String, Integer>> abstractElementsMaps;
	public Map<String, Map<String, Integer>> expElementMaps;
	public Map<String, Map<String, Integer>> expMaps;
	public Map<String, Map<String, Integer>> stmtBuggyElementTypesMaps;
	public List<String> patchCommitIds;
	public int pureDelRootNodes;

	public WorkerReturnMessage(int numOfPatches, Map<String, Map<String, Integer>> stmtMaps, Map<String, Map<String, Integer>> elementsMaps) {
		this.numOfPatches = numOfPatches;
		this.stmtMaps = stmtMaps;
		this.elementsMaps = elementsMaps;
	}
	
	public int getNumOfPatches() {
		return numOfPatches;
	}
	
	public Map<String, Map<String, Integer>> getStmtMaps() {
		return stmtMaps;
	}

	public Map<String, Map<String, Integer>> getElementsMaps() {
		return elementsMaps;
	}
	
}
