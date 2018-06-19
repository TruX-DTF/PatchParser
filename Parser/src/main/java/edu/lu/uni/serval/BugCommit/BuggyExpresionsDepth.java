package edu.lu.uni.serval.BugCommit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import edu.lu.uni.serval.Configuration;
import edu.lu.uni.serval.utils.FileHelper;

@Deprecated
public class BuggyExpresionsDepth {

	public static void main(String[] args) throws IOException {
		String inputPath = Configuration.PARSE_RESULTS_PATH;
		String fileName1 = inputPath + "LinkedPatches/expDepth.txt";
		String fileName2 = inputPath + "LinkedPatches/expDepth.txt";

		StringBuilder depth = new StringBuilder("type,size\n");
		readDepth(depth, fileName1, "LP-exp-depth,");
		readDepth(depth, fileName2, "UP-exp-depth,");
		FileHelper.outputToFile(inputPath + "expDepth.csv", depth, false);
	}

	private static void readDepth(StringBuilder depth, String fileName, String type) throws IOException {
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] elements = line.split("_");
			depth.append(type).append(elements.length - 1).append("\n");
		}
		reader.close();
	}
}
