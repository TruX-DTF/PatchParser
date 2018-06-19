package edu.lu.uni.serval;

import edu.lu.uni.serval.BugCommit.Distribution;
import edu.lu.uni.serval.BugCommit.parser.MultipleThreadsPatchesParser1;
import edu.lu.uni.serval.BugCommit.parser.MultipleThreadsPatchesParser2;

public class Main2 {

	public static void main(String[] args) {
		System.out.println("\n\n\n======================================================================================");
		System.out.println("Statistics of diff hunk sizes of code changes.");
		System.out.println("======================================================================================");
		new Distribution().statistics(Configuration.PATCH_COMMITS_PATH, Configuration.DIFFENTRY_SIZE_PATH);
		
		System.out.println("\n\n\n======================================================================================");
		System.out.println("Parse code changes of patches.");
		System.out.println("======================================================================================");
		new MultipleThreadsPatchesParser1().parse(Configuration.PATCH_COMMITS_PATH, Configuration.PARSE_RESULTS_PATH);
//		new MultipleThreadsPatchesParser2().parse(Configuration.PATCH_COMMITS_PATH, Configuration.PARSE_RESULTS_PATH);

	}

}
