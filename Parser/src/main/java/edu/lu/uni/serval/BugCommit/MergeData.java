package edu.lu.uni.serval.BugCommit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.utils.FileHelper;

// TODO
@Deprecated
public class MergeData {

	public static void main(String[] args) {
		mergeData("OUTPUT_DATA2/BugReportsBug/results/abstractStmtElements.csv", "OUTPUT_DATA2/Keywords_/results/abstractStmtElements.csv", "MergedData/abstractStmtElements.csv");
		mergeData("OUTPUT_DATA2/BugReportsBug/results/expElements.csv", "OUTPUT_DATA2/Keywords_/results/expElements.csv", "MergedData/expElements.csv");
		mergeData("OUTPUT_DATA2/BugReportsBug/results/expressions.csv", "OUTPUT_DATA2/Keywords_/results/expressions.csv", "MergedData/expressions.csv");
		mergeData("OUTPUT_DATA2/BugReportsBug/results/statements.csv", "OUTPUT_DATA2/Keywords_/results/statements.csv", "MergedData/statements.csv");
		mergeData("OUTPUT_DATA2/BugReportsBug/results/stmtElementTypes1.csv", "OUTPUT_DATA2/Keywords_/results/stmtElementTypes1.csv", "MergedData/stmtElementTypes1.csv");
	}

	private static void mergeData(String file1, String file2, String outputFile) {
		List<String> subTypes = new ArrayList<>();
		Map<String, Map<String, Integer>> data1 = readData(file1, subTypes);
		Map<String, Map<String, Integer>> data2 = readData(file2, subTypes);
		
		for (Map.Entry<String, Map<String, Integer>> entry : data2.entrySet()) {
			String key1 = entry.getKey();
			Map<String, Integer> numMap = entry.getValue();
			Map<String, Integer> numMap2 = data1.get(key1);
			if (numMap2 == null) {
				data1.put(key1, numMap);
			} else {
				for (Map.Entry<String, Integer> entry2 : numMap.entrySet()) {
					String key2 = entry2.getKey();
					int num = entry2.getValue();
					int num2 = numMap2.get(key2);
					numMap2.put(key2, num + num2);
				}
			}
		}
		
		StringBuilder builder = new StringBuilder("Type");
		for (String subType : subTypes) {
			builder.append(",").append(subType);
		}
		builder.append("\n");
		for (Map.Entry<String, Map<String, Integer>> entry : data1.entrySet()) {
			String key = entry.getKey();
			builder.append(key);
			Map<String, Integer> map = entry.getValue();
			for (String subType : subTypes) {
				builder.append(",").append(map.get(subType));
			}
			builder.append("\n");
		}
		FileHelper.outputToFile(outputFile, builder, false);
		
	}

	private static Map<String, Map<String, Integer>> readData(String file, List<String> subTypes) {
		Map<String, Map<String, Integer>> data = new HashMap<>();
		String content = FileHelper.readFile(file);
		
		try {
			BufferedReader reader = new BufferedReader(new StringReader(content));
			String line = null;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				String[] elements = line.split(",");
				if (i == 0) {
					for (int index = 1; index < elements.length; index ++) {
						String type = elements[index];
						if (!subTypes.contains(type)) subTypes.add(type);
					}
				} else {
					String type1 = elements[0];
					Map<String, Integer> numMap = new HashMap<>();
					for (int index = 1; index < elements.length; index ++) {
						int num = Integer.valueOf(elements[index]);
						numMap.put(subTypes.get(index - 1), num);
					}
					data.put(type1, numMap);
				}
				i ++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

}
