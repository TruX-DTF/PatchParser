package edu.lu.uni.serval.BugCommit.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import edu.lu.uni.serval.Configuration;

/**
 * Separate by projects, linked and unlinked patches.
 * 
 * @author kui.liu
 *
 */
public class MultipleThreadsPatchesParser2 {
	
	private static Logger log = LoggerFactory.getLogger(MultipleThreadsPatchesParser2.class);
//	private static final String ROOT_PATH = "OUTPUT_1/";
//	private static String outputPath = "OUTPUT_DATA_1/";

	@SuppressWarnings("deprecation")
	public void parse(String patchPath, String outputPath) {
		File[] typeFiles = new File(patchPath).listFiles();
		for (File typeFile : typeFiles) {
			if (typeFile.isDirectory()) {
				String dataType = typeFile.getName();
				File[] projectFiles = typeFile.listFiles();
				for (File projectFile : projectFiles) {
					if (projectFile.isDirectory()) {
						String projectName = projectFile.getName();
						int bugHunkSize = Configuration.sizeThreshold.get("buggy hunk");
						int fixHunkSize = Configuration.sizeThreshold.get("fixed hunk");
						final List<MessageFile> msgFiles = readMessageFiles(projectFile.getPath());
						ActorSystem system = null;
						ActorRef parsingActor = null;
						int numberOfWorkers = Configuration.numOfWorkers.get(projectName);
						final WorkMessage msg = new WorkMessage(0, msgFiles);
						outputPath = outputPath + projectName + "_" + dataType + "/";
						try {
//							log.info("Parsing begins...");
							system = ActorSystem.create("Parsing-Patches-System");
							parsingActor = system.actorOf(ParsePatchActor.props(numberOfWorkers, outputPath, bugHunkSize, fixHunkSize, patchPath, projectName), "patch-parser-actor");
							parsingActor.tell(msg, ActorRef.noSender());
						} catch (Exception e) {
							system.shutdown();
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private List<MessageFile> readMessageFiles(String path) {
		List<MessageFile> msgFiles = new ArrayList<>();
		File patchsFile = new File(path);
		msgFiles.addAll(getMessageFiles(patchsFile.getPath()));
		return msgFiles;
	}

	private List<MessageFile> getMessageFiles(String filePath) {
		List<MessageFile> msgFiles = new ArrayList<>();
		File revFilesPath = new File(filePath + "/revFiles/");
		File[] revFiles = revFilesPath.listFiles();   // project folders
		
		for (File revFile : revFiles) {
			if (revFile.getName().endsWith(".java")) {
				String fileName = revFile.getName();
				File prevFile = new File(filePath + "/prevFiles/prev_" + fileName);// previous file
				fileName = fileName.replace(".java", ".txt");
				File diffentryFile = new File(filePath + "/DiffEntries/" + fileName); // DiffEntry file
				MessageFile msgFile = new MessageFile(revFile, prevFile, diffentryFile);
				msgFiles.add(msgFile);
			}
		}
		return msgFiles;
	}
	
}
