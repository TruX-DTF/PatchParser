package edu.lu.uni.serval.BugCommit.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.actions.model.Action;

import edu.lu.uni.serval.gumtree.GumTreeComparer;
import edu.lu.uni.serval.gumtree.regroup.HierarchicalActionSet;
import edu.lu.uni.serval.gumtree.regroup.HierarchicalRegrouper;
import edu.lu.uni.serval.utils.ListSorter;

/**
 * Parse fix violations with GumTree in terms of multiple statements.
 * 
 * @author kui.liu
 *
 */
public class PatchParser {
	
	public void parsePatches(File prevFile, File revFile, File diffentryFile) {
		// GumTree results 
		List<HierarchicalActionSet> actionSets = parseChangedSourceCodeWithGumTree(prevFile, revFile);
		if (actionSets != null && actionSets.size() > 0) { 
			// Analyze the relationship between DiffEntry range and GumTree results.
		} else {
			prevFile.delete();
			revFile.delete();
			diffentryFile.delete();
		}
	}
	
	protected List<HierarchicalActionSet> parseChangedSourceCodeWithGumTree(File prevFile, File revFile) {
		List<HierarchicalActionSet> actionSets = new ArrayList<>();
		// GumTree results
		List<Action> gumTreeResults = new GumTreeComparer().compareTwoFilesWithGumTree(prevFile, revFile);
		if (gumTreeResults == null) {
			return null;
		} else if (gumTreeResults.size() == 0){
			return actionSets;
		} else {
			// Regroup GumTre results.
			List<HierarchicalActionSet> allActionSets = new HierarchicalRegrouper().regroupGumTreeResults(gumTreeResults);
			
			ListSorter<HierarchicalActionSet> sorter = new ListSorter<>(allActionSets);
			actionSets = sorter.sortAscending();
			
			return actionSets;
		}
	}

}

class RunnableParser implements Runnable {

	private File prevFile;
	private File revFile;
	private File diffentryFile;
	private PatchParser parser;
	
	public RunnableParser(File prevFile, File revFile, File diffentryFile, PatchParser parser) {
		this.prevFile = prevFile;
		this.revFile = revFile;
		this.diffentryFile = diffentryFile;
		this.parser = parser;
	}

	@Override
	public void run() {
		parser.parsePatches(prevFile, revFile, diffentryFile);
	}
}
