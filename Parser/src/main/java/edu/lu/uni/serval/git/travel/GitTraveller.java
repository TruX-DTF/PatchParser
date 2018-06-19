package edu.lu.uni.serval.git.travel;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.git.exception.GitRepositoryNotFoundException;
import edu.lu.uni.serval.git.exception.NotValidGitRepositoryException;
import edu.lu.uni.serval.utils.FileHelper;

public class GitTraveller {
	private static Logger log = LoggerFactory.getLogger(GitTraveller.class);
	
	// input
	private String gitRepoPath;
	// output
	private String outputPath;
	private String revisedFilesPath = "/revFiles/";
	private String previousFilesPath = "/prevFiles/";
	private List<MyDiffEntry> allDiffEntries;
	
	public GitTraveller(String gitRepoPath, String outputPath) {
		super();
		this.gitRepoPath = gitRepoPath;
		this.outputPath = outputPath;
	}
	
	public void travelGitRepo() {
		String repoName = FileHelper.getRepositoryName(gitRepoPath);
		revisedFilesPath = outputPath + repoName + revisedFilesPath;
		previousFilesPath = outputPath + repoName + previousFilesPath;
		FileHelper.createDirectory(revisedFilesPath);
		FileHelper.createDirectory(previousFilesPath);
		
		GitRepository gitRepo = new GitRepository(gitRepoPath, revisedFilesPath, previousFilesPath);
		try {
			log.info("Project: " + repoName);
			gitRepo.open();
			List<RevCommit> commits = gitRepo.getAllCommits(false);
//			log.info("All Commits: " + commits.size());
////			gitRepo.createFilesOfAllCommits(commits);
//			List<RevCommit> bugRelatedCommits = gitRepo.filterCommits(commits);
			log.info("Bug-related Commits: " + commits.size());
			List<CommitDiffEntry> commitDiffentries = gitRepo.getCommitDiffEntries(commits);
			// previous java file vs. modified java file
			log.info("Create revised Java files and previous Java files...");
			gitRepo.createFilesForGumTree(commitDiffentries);
			
//			log.info("Get DiffEntries in all selected commits...");
//			allDiffEntries = gitRepo.getMyDiffEntriesWithContext(commitDiffentries);
//			log.info("Bug-related DiffEntries: " + allDiffEntries.size());
//			commitFiles = gitRepo.getCommitFiles();
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
	
	public String getRevisedFilesPath() {
		return revisedFilesPath;
	}

	public String getPreviousFilesPath() {
		return previousFilesPath;
	}

	public List<MyDiffEntry> getAllDiffEntries() {
		return allDiffEntries;
	}

}
