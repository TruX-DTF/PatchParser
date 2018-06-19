package edu.lu.uni.serval;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.lu.uni.serval.BugCommit.Distribution;
import edu.lu.uni.serval.BugCommit.DownloadBugReports;
import edu.lu.uni.serval.BugCommit.PatchRelatedCommits;
import edu.lu.uni.serval.BugCommit.filter.MultipleThreadsPatchCommitsFilter;
import edu.lu.uni.serval.utils.FileHelper;

public class Main {

	public static void main(String[] args) throws IOException {
		System.out.println("======================================================================================");
		System.out.println("Statistics of project LOC.");
		System.out.println("======================================================================================");
		new Distribution().countLOC(Configuration.SUBJECTS_PATH);
		
		
		System.out.println("\n\n\n======================================================================================");
		System.out.println("Download fixed bug reports from JIRA.");
		System.out.println("======================================================================================");
		if (! new File(Configuration.BUG_REPORTS_PATH).exists()) {
			DownloadBugReports dlbr = new DownloadBugReports();
			dlbr.collectBugReports("IO-", Configuration.BUG_REPORT_URL, 1, 573);
			dlbr.collectBugReports("LANG-", Configuration.BUG_REPORT_URL, 1, 1386);
			dlbr.collectBugReports("MATH-", Configuration.BUG_REPORT_URL, 1, 1453);
			dlbr.collectBugReports("MAHOUT-", Configuration.BUG_REPORT_URL, 1, 2030);
			dlbr.collectBugReports("DERBY-", Configuration.BUG_REPORT_URL, 1, 6985);
			dlbr.collectBugReports("LUCENE-", Configuration.BUG_REPORT_URL, 1, 8202);
			dlbr.collectBugReports("SOLR-", Configuration.BUG_REPORT_URL, 1, 12036);
			List<File> bugReportFiles = FileHelper.getAllFiles(Configuration.BUG_REPORTS_PATH, ".txt");
			for (File bugReportFile : bugReportFiles) {
				dlbr.parseBugReport(bugReportFile);
			}
		}
		
		
		System.out.println("\n\n\n======================================================================================");
		System.out.println("Collect bug-fix-related commits with bugID of bug reports and bug-related keywords,\n"
				+ "and Fileter out test Java code changes.");
		System.out.println("======================================================================================");
		PatchRelatedCommits prc = new PatchRelatedCommits();
		prc.collectCommits(Configuration.SUBJECTS_PATH, Configuration.PATCH_COMMITS_PATH, Configuration.BUG_REPORTS_PATH);
		
		
		System.out.println("\n\n\n======================================================================================");
		System.out.println("Filter out non-Java code changes (e.g., Javadoc).");
		System.out.println("======================================================================================");
		new MultipleThreadsPatchCommitsFilter().filter(Configuration.SUBJECTS_PATH, Configuration.PATCH_COMMITS_PATH);
		
	}

}
