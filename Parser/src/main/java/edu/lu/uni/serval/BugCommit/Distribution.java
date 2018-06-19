package edu.lu.uni.serval.BugCommit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import edu.lu.uni.serval.Configuration;
import edu.lu.uni.serval.diffentry.DiffEntryHunk;
import edu.lu.uni.serval.diffentry.DiffEntryReader;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Get the distribution of project LOC.
 * Get the DiffEntries Distribution of bug-related commits in projects.
 * 
 * @author kui.liu
 *
 */
public class Distribution {
	
	public void countLOC(String subjectsPath) {
		File[] projects = new File(subjectsPath).listFiles();
		for (File project : projects) {
			if (project.isDirectory()) {
				List<File> allJavaFiles = FileHelper.getAllFiles(project.getPath(), ".java");

				int counter = 0;
				for (File file : allJavaFiles) {
					if (file.getPath().toLowerCase(Locale.ENGLISH).contains("test")) continue;
					String fileContent = FileHelper.readFile(file);
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new StringReader(fileContent));
						while (reader.readLine() != null) {
							counter ++;
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println(project.getName() + " LOC: " + counter);
			}
		}
	}
	
	public void statistics(String intputPath, String outputPath) {
		File[] dataTypes = new File(intputPath).listFiles();
		StringBuilder diffentryRangeBuilder = new StringBuilder();
		diffentryRangeBuilder.append("Hunk_Type,Size\n");
		List<Integer> buggyHunkSizes = new ArrayList<>();
		List<Integer> fixedHunkSizes = new ArrayList<>();
		for (File dataType : dataTypes) {
			if (dataType.isDirectory()) {
				File[] projects = dataType.listFiles();  // project folders
				for (File project : projects) {
					if (project.isDirectory()) {
						File[] diffentryFiles = new File(project.getPath() + "/DiffEntries/").listFiles();
						for (File diffentryFile : diffentryFiles) {
							if (diffentryFile.isFile() && diffentryFile.getName().endsWith(".txt")) {
								List<DiffEntryHunk> diffentryHunks = new DiffEntryReader().readHunks3(diffentryFile);
								for (DiffEntryHunk hunk : diffentryHunks) {
									int bugRange = hunk.getBugRange();
									int fixRange = hunk.getFixRange();
									buggyHunkSizes.add(Integer.valueOf(bugRange));
									fixedHunkSizes.add(Integer.valueOf(fixRange));
									diffentryRangeBuilder.append("Buggy_Hunk,").append(bugRange).append("\n");
									diffentryRangeBuilder.append("Fixed_Hunk,").append(fixRange).append("\n");
								}
							}
						}
					}
				}
			}
		}
		FileHelper.outputToFile(outputPath + "/DiffEntyRange.csv", diffentryRangeBuilder, false);
		
		summary(buggyHunkSizes, "buggy hunk");
		summary(fixedHunkSizes, "fixed hunk");
	}
	
	private void summary(List<Integer> sizes, String type) {
		
		Collections.sort(sizes, (s1, s2) -> Integer.compare(s1, s2));
		
		int size = sizes.size();
		int firstQuarterIndex = (int)(0.25 * size);
		int firstQuarter = sizes.get(firstQuarterIndex);
		int thirdQuarterIndex = (int)(0.75 * size);
		int thirdQuarter = sizes.get(thirdQuarterIndex);
		int upperWhisker = thirdQuarter + (int) (1.5 * (thirdQuarter - firstQuarter));
		int maxSize = sizes.get(size - 1);
		upperWhisker = upperWhisker > maxSize ? maxSize : upperWhisker;
		
		System.out.println("Summary " + type + " sizes:");
		System.out.println("Min: " + sizes.get(0));
		System.out.println("First quartile: " + firstQuarter);
		System.out.println("Mean: " + mean(sizes));
		System.out.println("Third quartile: " + thirdQuarter);
		System.out.println("Upper whisker: " + upperWhisker);
		System.out.println("Max: " + maxSize);
		Configuration.sizeThreshold.put(type, upperWhisker);
	}

	private double mean(List<Integer> sizes) {
		int sum = 0;
		for (int size : sizes) {
			sum += size;
		}
		
		return (double) sum / sizes.size();
	}
	
}
