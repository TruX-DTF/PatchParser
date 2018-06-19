package edu.lu.uni.serval.BugCommit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.lu.uni.serval.git.exception.GitRepositoryNotFoundException;
import edu.lu.uni.serval.git.exception.NotValidGitRepositoryException;
import edu.lu.uni.serval.git.travel.CommitDiffEntry;
import edu.lu.uni.serval.git.travel.GitRepository;
import edu.lu.uni.serval.utils.FileHelper;

public class PatchRelatedCommits {
	
	public void collectCommits(String projectsPath, String outputPath, String urlPath) {
		File[] projects = new File(projectsPath).listFiles();
		FileHelper.deleteDirectory(outputPath);
		Map<String, String> map = new HashMap<>();
		map.put("commons-io", "IO-");
		map.put("commons-lang", "LANG-");
		map.put("mahout", "MAHOUT-");
		map.put("commons-math", "MATH-");
		map.put("derby", "DERBY-");
		for (File project : projects) {
			if (!project.isDirectory()) continue;
			String repoName = project.getName();
			String revisedFilesPath = "";
			String previousFilesPath = "";
			String gitRepoPath = project.getPath() + "/.git";
			GitRepository gitRepo = new GitRepository(gitRepoPath, revisedFilesPath, previousFilesPath);
			try {
				System.out.println("\nProject: " + repoName);
				gitRepo.open();
				List<RevCommit> commits = gitRepo.getAllCommits(false);
				System.out.println("All Commits: " + commits.size());
				List<RevCommit> keywordPatchCommits = gitRepo.filterCommits(commits); // searched by keywords.
//				System.out.println("Keywords Patch Commits: " + keywordPatchCommits.size());
				List<RevCommit> linkedPatchCommits;  // searched by bugId. "Bug"
				String bugId = map.get(repoName);
				if (bugId == null) {
					linkedPatchCommits = gitRepo.filterCommitsByBug(commits, urlPath, "LUCENE-", "SOLR-");
				} else {
					linkedPatchCommits = gitRepo.filterCommitsByBug(commits, urlPath, bugId);
				}
//				System.out.println("Bug-report Linked Patch Commits: " + linkedPatchCommits.size());
				List<RevCommit> keyworkAndLinkedPatchCommits;
				if (bugId == null) {
					keyworkAndLinkedPatchCommits = gitRepo.filterCommitsByBug(keywordPatchCommits, urlPath, "LUCENE-", "SOLR-");
				} else {
					keyworkAndLinkedPatchCommits = gitRepo.filterCommitsByBug(keywordPatchCommits, urlPath, bugId);
				}
				keywordPatchCommits.removeAll(keyworkAndLinkedPatchCommits);
//				System.out.println("Keywords-matching and unlinked Patch Commits: " + keywordPatchCommits.size());
				System.out.println("All collected patch-related Commits: " + (linkedPatchCommits.size() + keywordPatchCommits.size()));
				
				// previous java file vs. modified java file
//				System.out.println("Create revised Java files and previous Java files that contains code changes patch parsing with GumTree.");
				List<CommitDiffEntry> linkedPatchCommitDiffentries = gitRepo.getCommitDiffEntries(linkedPatchCommits);
				gitRepo.createFilesForGumTree(outputPath + "Linked/" + repoName + "/", linkedPatchCommitDiffentries);
				
				List<CommitDiffEntry> unlinkedPatchCommitDiffentries = gitRepo.getCommitDiffEntries(keywordPatchCommits);
				gitRepo.createFilesForGumTree(outputPath + "Keywords/" + repoName + "/", unlinkedPatchCommitDiffentries);
				gitRepo.outputCommitMessages(outputPath + "CommitMessage/" + repoName + "_Keywords.txt", keywordPatchCommits);
				gitRepo.outputCommitMessages(outputPath + "CommitMessage/" + repoName + "_Linked.txt", linkedPatchCommits);
				
				outputCommitIds(outputPath + "CommitIds/" + repoName + "_Keywords.txt", keywordPatchCommits);
				outputCommitIds(outputPath + "CommitIds/" + repoName + "_Linked.txt", linkedPatchCommits);
			} catch (GitRepositoryNotFoundException e) {
				e.printStackTrace();
			} catch (NotValidGitRepositoryException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoHeadException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			} finally {
				gitRepo.close();
			}
		}
	}

	private void outputCommitIds(String fileName, List<RevCommit> commits) {
		StringBuilder builder = new StringBuilder("Previous Commit Id ------ Patch Commit Id.");
		for (RevCommit commit : commits) {
			builder.append(commit.getParents()[0].getId().name()).append(" ------ ").append(commit.getId().name());
		}
		FileHelper.outputToFile(fileName, builder, false);
	}

}
