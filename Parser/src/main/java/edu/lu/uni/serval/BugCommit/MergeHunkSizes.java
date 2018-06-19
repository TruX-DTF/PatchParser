package edu.lu.uni.serval.BugCommit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import edu.lu.uni.serval.utils.FileHelper;

//TODO merge data.
@Deprecated
public class MergeHunkSizes {

	public static void main(String[] args) throws IOException {
		StringBuilder builder = new StringBuilder("Hunk_Type,Size\n");
		StringBuilder rBuilder = readHunkSizes("OUTPUT_DATA2/BugReportsBug/dataset/DiffEntyRange.csv", "LP_");
		StringBuilder krBuilder = readHunkSizes("OUTPUT_DATA2/Keywords_/dataset/DiffEntyRange.csv", "UP_");
		builder.append(krBuilder);
		builder.append(rBuilder);
		FileHelper.outputToFile("OUTPUT_DATA2/HunkSizes2.csv", builder, false);
	}

	private static StringBuilder readHunkSizes(String sizesFile, String type) throws IOException {
		StringBuilder builder = new StringBuilder();
		String content = FileHelper.readFile(sizesFile);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			builder.append(type).append(line).append("\n");
		}
		reader.close();
		return builder;
	}

}
