package edu.lu.uni.serval.BugCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.lu.uni.serval.Configuration;
import edu.lu.uni.serval.utils.FileHelper;

public class DownloadBugReports {

	public void collectBugReports(String projectName, String url, int stratId, int endId) throws IOException {
		for (int i = stratId; i <= endId; i ++) {
			String fileName = Configuration.BUG_REPORTS_PATH + projectName + i + ".txt";
			Document doc = Jsoup.connect(url + projectName + i).get();
			FileHelper.outputToFile(fileName, doc.html(), false);
		}
	}

	/*
	 * [Bug, Improvement, Sub-task, Task, New Feature, Wish, Test, Question, Documentation, Dependency upgrade, Epic, Blog - New Blog Request, New JIRA Project, Story, Proposal]
	 * [Closed, Open, Reopened, In Progress, Resolved, Patch Available]
	 * [Major, Trivial, Minor, Critical, Blocker, Fixed]
	 * [Fixed, Invalid, Won't Fix, Unresolved, Duplicate, Cannot Reproduce, Incomplete, Not A Problem, Later, Done, Not A Bug, Implemented, Workaround, Resolved, Won't Do, Pending Closed, 1.1, Information Provided, Works for Me, Delivered]
	 */
	int a = 0;
	int b = 0;
//	public List<String> types = new ArrayList<>();
//	public List<String> statuses = new ArrayList<>();
//	public List<String> priorities = new ArrayList<>();
//	public List<String> resolutions = new ArrayList<>();
	public List<String> results= new ArrayList<>();
	
	public void parseBugReport(File bugReportFile) throws IOException {
		try {
			Document doc = Jsoup.parse(bugReportFile, "utf-8");
			Element issueDetails = doc.getElementById("issuedetails"); 
			Elements e = issueDetails.children();
//    	System.out.println(e);// 0: Type, 1: Status, 2: Priority, 3: Resolution.
			String type = e.get(0).children().get(0).child(1).text();
			String status = e.get(1).children().get(0).child(1).text();
			String priority = e.get(2).children().get(0).child(1).text();
			String resolution = e.get(3).children().get(0).child(1).text();
//		System.out.println("Type: " + type + ", Status: " + status + ", Priority: " + priority + ", Resolution: " + resolution + ".");
//			if (!types.contains(type)) types.add(type);// Bug, Improvement
			if (type.equals("Bug")) {// || type.equals("Improvement")) {
				if (resolution.equals("Fixed")) {// || resolution.equals("Duplicate") || resolution.equals("Resolved")
//						|| resolution.equals("Implemented") || resolution.equals("Pending Closed")){// ||
//						status.equals("Resolved") || status.equals("Patch Available")) {
//					if (!statuses.contains(status)) statuses.add(status);// Resolved, Patch Available.  Closed
//					if (!priorities.contains(priority)) priorities.add(priority);// Major, Trivial, Minor, Critical, Blocker, Fixed.
//					if (!resolutions.contains(resolution)) resolutions.add(resolution);//Fixed, Duplicate, Done, Resolved, Implemented, Pending Closed. Incomplete, Unresolved
					String result = type + "+" + status + "+" + priority + "+" + resolution;// 522
					if (!results.contains(result)) results.add(result);
				} else {
					bugReportFile.delete();
				}
			} else {
				bugReportFile.delete();
			}
//			if (resolution.equals("Won't Fix") || resolution.equals("Cannot Reproduce") || resolution.equals("Not A Bug")
//					 || resolution.equals("Not A Problem") || resolution.equals("Incomplete")) return;
//			if (type.equals("Bug")) {
//				if (resolution.equals("Fixed") || resolution.equals("Duplicate") || resolution.equals("Resolved")
//						|| resolution.equals("Implemented") || resolution.equals("Pending Closed")){// ||
////						status.equals("Resolved") || status.equals("Patch Available")) {
//					a ++;
//					if (!statuses.contains(status)) statuses.add(status);// Resolved, Patch Available.  Closed
//					if (!priorities.contains(priority)) priorities.add(priority);// Major, Trivial, Minor, Critical, Blocker, Fixed.
//					if (!resolutions.contains(resolution)) resolutions.add(resolution);//Fixed, Duplicate, Done, Resolved, Implemented, Pending Closed. Incomplete, Unresolved
//					String result = type + "+" + status + "+" + priority + "+" + resolution;// 522
//					if (!results.contains(result)) results.add(result);
//				}
//			}
//			if (type.equals("Improvement")) {
//				if (resolution.equals("Fixed") || resolution.equals("Duplicate") || resolution.equals("Resolved")
//						|| resolution.equals("Implemented") || resolution.equals("Pending Closed")){// ||
////						status.equals("Resolved") || status.equals("Patch Available")) {
//					b++;
//					if (!statuses.contains(status)) statuses.add(status);// Resolved, Patch Available.  Closed
//					if (!priorities.contains(priority)) priorities.add(priority);// Major, Trivial, Minor, Critical, Blocker, Fixed.
//					if (!resolutions.contains(resolution)) resolutions.add(resolution);//Fixed, Duplicate, Done, Resolved, Implemented, Pending Closed. Incomplete, Unresolved
//					String result = type + "+" + status + "+" + priority + "+" + resolution;// 522
//					if (!results.contains(result)) results.add(result);
//				}
//			}
		} catch (Exception e) {
			bugReportFile.delete();
			e.printStackTrace();
		}
	}

}
