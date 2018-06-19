package edu.lu.uni.serval;

import java.util.HashMap;
import java.util.Map;

public class Configuration {
	
	public static final String BUG_REPORT_URL = "https://issues.apache.org/jira/browse/";
	public static final String SUBJECTS_PATH = "../subjects/";
	private static final String OUTPUT_PATH = "../data/";
	public static final String BUG_REPORTS_PATH = OUTPUT_PATH + "BugReports/";
	public static final String PATCH_COMMITS_PATH = OUTPUT_PATH + "PatchCommits/";
	public static final String DIFFENTRY_SIZE_PATH = OUTPUT_PATH + "DiffentrySizes/";
	public static final String PARSE_RESULTS_PATH = OUTPUT_PATH + "ParseResults/";
	public static final long TIMEOUT_THRESHOLD = 1800L;

	public static Map<String, Integer> numOfWorkers = new HashMap<>();
	public static Map<String, Integer> sizeThreshold = new HashMap<>();
	static {
		numOfWorkers.put("commons-io", 1);
		numOfWorkers.put("commons-lang", 1);
		numOfWorkers.put("commons-math", 10);
		numOfWorkers.put("derby", 20);
		numOfWorkers.put("lucene-solr", 50);
		numOfWorkers.put("mahout", 10);
		
		sizeThreshold.put("buggy hunk", 8);
		sizeThreshold.put("fixed hunk", 10);
	}
}
