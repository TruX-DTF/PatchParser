package edu.lu.uni.serval.BugCommit.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;

import edu.lu.uni.serval.diffentry.DiffEntryHunk;
import edu.lu.uni.serval.diffentry.DiffEntryReader;
import edu.lu.uni.serval.gumtree.GumTreeComparer;
import edu.lu.uni.serval.gumtree.regroup.CUCreator;
import edu.lu.uni.serval.gumtree.regroup.HierarchicalActionSet;
import edu.lu.uni.serval.gumtree.regroup.HierarchicalRegrouper;
import edu.lu.uni.serval.gumtree.regroup.NodeChecker;
import edu.lu.uni.serval.utils.ListSorter;

/**
 * Parse fix violations with GumTree in terms of multiple statements.
 * 
 * @author kui.liu
 *
 */
public class PatchParser {
	
	private Map<DiffEntryHunk, List<HierarchicalActionSet>> patches = new HashMap<>();
	public int hunks = 0;
	public int diffs = 0;
	public int zeroG = 0;
	public int overRap = 0;
	
	public void parsePatches(File prevFile, File revFile, File diffentryFile, int bugHunkSize, int fixHunkSize) {
		// GumTree results 
		List<HierarchicalActionSet> actionSets = parseChangedSourceCodeWithGumTree(prevFile, revFile);
		if (actionSets != null && actionSets.size() > 0) { 
			// Analyze the relationship between DiffEntry range and GumTree results.
			List<DiffEntryHunk> diffentryHunks = new DiffEntryReader().readHunks3(diffentryFile);
			hunks = diffentryHunks.size();
			
			/**
			 * Select actions by the size of diffentry.
			 */
			CUCreator cuCreator = new CUCreator();
			CompilationUnit prevUnit = cuCreator.createCompilationUnit(prevFile);
			CompilationUnit revUnit = cuCreator.createCompilationUnit(revFile);
			if (prevUnit == null || revUnit == null) {
				return;
			}
			for (DiffEntryHunk hunk : diffentryHunks) {
				int buggyHunkSize = hunk.getBuggyHunkSize();
				int fixedHunkSize = hunk.getFixedHunkSize();
				if (buggyHunkSize <= bugHunkSize && fixedHunkSize <= fixHunkSize) { // The threshold of patch hunks.
					diffs ++;
					int buggyStart = hunk.getBugLineStartNum();
					int fixedStart = hunk.getFixLineStartNum();
					int buggyRange = hunk.getBugRange();
					int fixedRnage = hunk.getFixRange();
					int buggyEnd = buggyStart + (buggyRange == 0 ? 0 : (buggyRange - 1));
					int fixedEnd = fixedStart + (fixedRnage == 0 ? 0 : (fixedRnage - 1));
					
					HierarchicalActionSet singlePatch = new HierarchicalActionSet();
					singlePatch.setAstNodeType("");
					
					// Matching corresponding actionsets.
					for (HierarchicalActionSet actionSet : actionSets) {
						int actionBugStartLine = actionSet.getBugStartLineNum();
						if (actionBugStartLine == 0) {
							actionBugStartLine = setLineNumbers(actionSet, prevUnit, revUnit);
						} 
						int actionBugEndLine = actionSet.getBugEndLineNum();
						int actionFixStartLine = actionSet.getFixStartLineNum();
						int actionFixEndLine = actionSet.getFixEndLineNum();
						
						
						String actionStr = actionSet.getActionString();
						if (actionStr.startsWith("INS")) {
							if (fixedStart <= actionFixEndLine && actionFixStartLine <= fixedEnd) {
								singlePatch = addToPatchesMap(actionSet, singlePatch, hunk);
							}
						} else {//if (!actionStr.startsWith("MOV")){ // ignore move actions.
							if (buggyStart <= actionBugEndLine && actionBugStartLine <= buggyEnd) {
								singlePatch = addToPatchesMap(actionSet, singlePatch, hunk);
							}
						}
					}
					
					if (singlePatch.getSubActions().size() > 0) {
						addToPatchesMap(singlePatch, hunk);
					} else {
						zeroG ++;
					}
				}
			}
		}
	}
	
	private HierarchicalActionSet addToPatchesMap(HierarchicalActionSet actionSet, HierarchicalActionSet singlePatch, DiffEntryHunk hunk) {
		if (!isContained(actionSet)) {
			if (actionSet.getAstNodeType().equals("FieldDeclaration")) {
				if (singlePatch.getSubActions().size() > 0) {
					addToPatchesMap(singlePatch, hunk);
					singlePatch = new HierarchicalActionSet();
				}
				singlePatch.setAstNodeType("FieldDeclaration");
			} else {
				if (singlePatch.getAstNodeType().equals("FieldDeclaration")) {
					addToPatchesMap(singlePatch, hunk);
					singlePatch = new HierarchicalActionSet();
					singlePatch.setAstNodeType("");
				}
			}
			singlePatch.getSubActions().add(actionSet);
		} else overRap ++;
		
		return singlePatch;
	}

	private void addToPatchesMap(HierarchicalActionSet singlePatch, DiffEntryHunk hunk) {
		List<HierarchicalActionSet> patches = this.patches.get(hunk);
		if (patches == null) {
			patches = new ArrayList<>();
			patches.add(singlePatch);
			this.patches.put(hunk, patches);
		} else {
			patches.add(singlePatch);
		}
	}

	private boolean isContained(HierarchicalActionSet actionSet) {
		for (Map.Entry<DiffEntryHunk, List<HierarchicalActionSet>> entry : this.patches.entrySet()) {
			List<HierarchicalActionSet> patches = entry.getValue();
			for (HierarchicalActionSet patch : patches) {
				if (patch.getSubActions().contains(actionSet)) return true;
			}
		}
		return false;
	}

	private int setLineNumbers(HierarchicalActionSet actionSet, CompilationUnit prevUnit, CompilationUnit revUnit) {
		int actionBugStartLine;
		int actionBugEndLine;
		int actionFixStartLine;
		int actionFixEndLine;
		
		// position of buggy statements
		int bugStartPosition = 0;
		int bugEndPosition = 0;
		// position of fixed statements
		int fixStartPosition = 0;
		int fixEndPosition = 0;
		
		String actionStr = actionSet.getActionString();
		if (actionStr.startsWith("INS")) {
			ITree newTree = actionSet.getNode();
			fixStartPosition = newTree.getPos();
			fixEndPosition = fixStartPosition + newTree.getLength();

			List<Move> firstAndLastMov = getFirstAndLastMoveAction(actionSet);
			if (firstAndLastMov != null) {
				bugStartPosition = firstAndLastMov.get(0).getNode().getPos();
				ITree lastTree = firstAndLastMov.get(1).getNode();
				bugEndPosition = lastTree.getPos() + lastTree.getLength();
			}
		} else {
			ITree oldTree = actionSet.getNode();
			bugStartPosition = oldTree.getPos(); // range of actions
			bugEndPosition = bugStartPosition + oldTree.getLength();
			String astNodeType = actionSet.getAstNodeType();
			if ("TypeDeclaration".equals(astNodeType)) {
				bugEndPosition = getClassBodyStartPosition(oldTree);
			} else if ("MethodDeclaration".equals(astNodeType) || NodeChecker.withBlockStatement(oldTree.getType())) { //MethodDeclaration && Block-Statements
				List<ITree> children = oldTree.getChildren();
				bugEndPosition = getEndPosition(children);
			}
			if (bugEndPosition == 0) {
				bugEndPosition = bugStartPosition + oldTree.getLength();
			}
			
			if (actionStr.startsWith("UPD")) {
				Update update = (Update) actionSet.getAction();
				ITree newNode = update.getNewNode();
				fixStartPosition = newNode.getPos();
				fixEndPosition = fixStartPosition + newNode.getLength();
				
				if ("TypeDeclaration".equals(astNodeType)) {
					fixEndPosition = getClassBodyStartPosition(newNode);
				} else if ("MethodDeclaration".equals(astNodeType)) {
					List<ITree> newChildren = newNode.getChildren();
					fixEndPosition = getEndPosition(newChildren);
				}
				if (fixEndPosition == 0) {
					fixEndPosition = fixStartPosition + newNode.getLength();
				}
			}
		}
		actionBugStartLine = bugStartPosition == 0 ? 0 : prevUnit.getLineNumber(bugStartPosition);
		actionBugEndLine = bugEndPosition == 0 ? 0 : prevUnit.getLineNumber(bugEndPosition);
		actionFixStartLine = fixStartPosition == 0 ? 0 : revUnit.getLineNumber(fixStartPosition);
		actionFixEndLine = fixEndPosition == 0 ? 0 : revUnit.getLineNumber(fixEndPosition);
		actionSet.setBugStartLineNum(actionBugStartLine);
		actionSet.setBugEndLineNum(actionBugEndLine);
		actionSet.setFixStartLineNum(actionFixStartLine);
		actionSet.setFixEndLineNum(actionFixEndLine);
		actionSet.setBugEndPosition(bugEndPosition);
		actionSet.setFixEndPosition(fixEndPosition);
		
		return actionBugStartLine;
	}
	
	private int getEndPosition(List<ITree> children) {
		int endPosition = 0;
		for (int i = 0, size = children.size(); i < size; i ++) {
			ITree child = children.get(i);
			int type = child.getType();
			if (NodeChecker.isStatement2(type)) {
				if ( i > 0) {
					child = children.get(i - 1);
					endPosition = child.getPos() + child.getLength();
				} else {
					endPosition = child.getPos() - 1;
				}
				break;
			}
		}
		return endPosition;
	}
	
	private int getClassBodyStartPosition(ITree tree) {
		List<ITree> children = tree.getChildren();
		for (int i = 0, size = children.size(); i < size; i ++) {
			ITree child = children.get(i);
			int type = child.getType();
			// Modifier, NormalAnnotation, MarkerAnnotation, SingleMemberAnnotation
			if (type != 83 && type != 77 && type != 78 && type != 79
				&& type != 5 && type != 39 && type != 43 && type != 74 && type != 75
				&& type != 76 && type != 84 && type != 87 && type != 88 && type != 42) {
				// ArrayType, PrimitiveType, SimpleType, ParameterizedType, 
				// QualifiedType, WildcardType, UnionType, IntersectionType, NameQualifiedType, SimpleName
				if (i > 0) {
					child = children.get(i - 1);
					return child.getPos() + child.getLength() + 1;
				} else {
					return child.getPos() - 1;
				}
			}
		}
		return 0;
	}
	
	private List<Move> getFirstAndLastMoveAction(HierarchicalActionSet gumTreeResult) {
		List<Move> firstAndLastMoveActions = new ArrayList<>();
		List<HierarchicalActionSet> actions = new ArrayList<>();
		actions.addAll(gumTreeResult.getSubActions());
		if (actions.size() == 0) {
			return null;
		}
		Move firstMoveAction = null;
		Move lastMoveAction = null;
		while (actions.size() > 0) {
			List<HierarchicalActionSet> subActions = new ArrayList<>();
			for (HierarchicalActionSet action : actions) {
				subActions.addAll(action.getSubActions());
				if (action.toString().startsWith("MOV")) {
					if (firstMoveAction == null) {
						firstMoveAction = (Move) action.getAction();
						lastMoveAction = (Move) action.getAction();
					} else {
						int startPosition = action.getStartPosition();
						int length = action.getLength();
						int startPositionFirst = firstMoveAction.getPosition();
						int startPositionLast = lastMoveAction.getPosition();
						int lengthLast = lastMoveAction.getNode().getLength();
						if (startPosition < startPositionFirst || (startPosition == startPositionFirst && length > firstMoveAction.getLength())) {
							firstMoveAction = (Move) action.getAction();
						}
						if ((startPosition + length) > (startPositionLast + lengthLast)) {
							lastMoveAction = (Move) action.getAction();
						} 
					}
				}
			}
			
			actions.clear();
			actions.addAll(subActions);
		}
		if (firstMoveAction == null) {
			return null;
		}
		firstAndLastMoveActions.add(firstMoveAction);
		firstAndLastMoveActions.add(lastMoveAction);
		return firstAndLastMoveActions;
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

	public Map<DiffEntryHunk, List<HierarchicalActionSet>> getPatches() {
		return patches;
	}

}
