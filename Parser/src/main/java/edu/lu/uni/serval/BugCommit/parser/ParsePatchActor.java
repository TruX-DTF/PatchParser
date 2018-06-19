package edu.lu.uni.serval.BugCommit.parser;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import akka.routing.RoundRobinPool;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.ListSorter;

public class ParsePatchActor extends UntypedActor {
	
	private static Logger logger = LoggerFactory.getLogger(ParsePatchActor.class);

	private ActorRef mineRouter;
	private final int numberOfWorkers;
	private int counter = 0;
	int diff = 0;
	int hunks = 0;
	int zeroG = 0;
	int overRap = 0;
	private String outputPath;
	private String rootPath;
	private String projectName = null;
	
	public ParsePatchActor(int numberOfWorkers, String outputPath, int bugHunkSize, int fixHunkSize) {
		this.outputPath = outputPath;
		mineRouter = this.getContext().actorOf(new RoundRobinPool(numberOfWorkers)
				.props(ParsePatchWorker.props(outputPath, bugHunkSize, fixHunkSize)), "patch-parse-router");
		this.numberOfWorkers = numberOfWorkers;
	}

	public static Props props(final int numberOfWorkers, final String outputPath, final int bugHunkSize, final int fixHunkSize) {
		
		return Props.create(new Creator<ParsePatchActor>() {

			private static final long serialVersionUID = 9207427376110704705L;

			@Override
			public ParsePatchActor create() throws Exception {
				return new ParsePatchActor(numberOfWorkers, outputPath, bugHunkSize, fixHunkSize);
			}
			
		});
	}
	
	public ParsePatchActor(int numberOfWorkers, String outputPath, int bugHunkSize, int fixHunkSize, String rootPath, String projectName) {
		this.outputPath = outputPath;
		mineRouter = this.getContext().actorOf(new RoundRobinPool(numberOfWorkers)
				.props(ParsePatchWorker.props(outputPath, bugHunkSize, fixHunkSize)), "patch-parse-router");
		this.numberOfWorkers = numberOfWorkers;
		this.rootPath = rootPath;
		this.projectName = projectName;
	}

	public static Props props(final int numberOfWorkers, final String outputPath, final int bugHunkSize, final int fixHunkSize, 
			final String rootPath, final String projectName) {
		
		return Props.create(new Creator<ParsePatchActor>() {

			private static final long serialVersionUID = 9207427376110704705L;

			@Override
			public ParsePatchActor create() throws Exception {
				return new ParsePatchActor(numberOfWorkers, outputPath, bugHunkSize, fixHunkSize, rootPath, projectName);
			}
			
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof WorkMessage) {
			FileHelper.deleteDirectory(outputPath + "Patches/");
			FileHelper.deleteDirectory(outputPath + "examples/");
			List<MessageFile> files = ((WorkMessage) msg).getMsgFiles();
			int size = files.size();
			int average = (int) Math.round((double) size / numberOfWorkers);
			
			for (int i = 0; i < numberOfWorkers; i ++) {
				int fromIndex = i * average;
				int toIndex = (i + 1) * average;
				if (i == numberOfWorkers - 1) {
					toIndex = size;
				}
				
				List<MessageFile> filesOfWorkers = new ArrayList<>();
				filesOfWorkers.addAll(files.subList(fromIndex, toIndex));
				final WorkMessage workMsg = new WorkMessage(i + 1, filesOfWorkers);
				mineRouter.tell(workMsg, getSelf());
//				logger.info("Assign a task to worker #" + (i + 1) + "...");
			}
		} else if (msg instanceof WorkerReturnMessage) {
			counter ++;
			WorkerReturnMessage workerMsg = (WorkerReturnMessage) msg;
			this.diff += workerMsg.diffs;
			this.hunks += workerMsg.hunks;
			this.zeroG += workerMsg.zeroG;
			this.overRap += workerMsg.overRap;
			mergeData(workerMsg);
			
//			logger.info(counter + " workers finished their work...");
			if (counter >= numberOfWorkers) {
				exportData();
				if (projectName != null) {
					readSelectedCommits();
				}
				System.out.println("Selected bug-fix commits: " + this.patchCommitIds.size());
				logger.info("Patch parsing work is finished...");
				this.getContext().stop(mineRouter);
				this.getContext().stop(getSelf());
				this.getContext().system().shutdown();
			}
		} else {
			unhandled(msg);
		}
	}

	private void readSelectedCommits() {
		this.linkedPatchCommitIds = readCommitIds(this.rootPath + "Linked/" + projectName + "/DiffEntries/");
		this.unlinkedPatchCommitIds = readCommitIds(this.rootPath + "Keywords/" + projectName + "/DiffEntries/");
		System.out.println(projectName + " --- Linked Commits: " + linkedPatchCommitIds.size());
		System.out.println(projectName + " --- Keywords Commits: " + unlinkedPatchCommitIds.size());
		this.linkedPatchCommitIds.retainAll(patchCommitIds);
		this.unlinkedPatchCommitIds.retainAll(patchCommitIds);
		System.out.println(projectName + " --- Selected Linked Commits: " + linkedPatchCommitIds.size());
		System.out.println(projectName + " --- Selected Keywords Commits: " + unlinkedPatchCommitIds.size());
	}

	private List<String> readCommitIds(String directory) {
		List<String> commitIds = new ArrayList<>();
		List<File> files = FileHelper.getAllFiles(directory, ".txt");
		for (File file : files) {
			String fileName = file.getName();
			String commitId = fileName.substring(0, 6);
			if (!commitIds.contains(commitId)) commitIds.add(commitId);
		}
		return commitIds;
	}

	private Map<String, Map<String, Integer>> stmtMaps = new HashMap<>();
	private Map<String, Map<String, Integer>> elementsMaps = new HashMap<>();
	private Map<String, Map<String, Integer>> abstractElementsMaps = new HashMap<>();
	private Map<String, Map<String, Integer>> expElementMaps = new HashMap<>();
	private Map<String, Map<String, Integer>> expMaps = new HashMap<>();
	private Map<String, Map<String, Integer>> stmtBuggyElementTypesMaps = new HashMap<>();
	private List<String> patchCommitIds = new ArrayList<>();
	private List<String> linkedPatchCommitIds;
	private List<String> unlinkedPatchCommitIds;
	
	private void mergeData(WorkerReturnMessage workerMsg) {
		mergeData(this.stmtMaps, workerMsg.getStmtMaps());
		mergeData(this.elementsMaps, workerMsg.getElementsMaps());
		mergeData(this.abstractElementsMaps, workerMsg.abstractElementsMaps);
		mergeData(this.expElementMaps, workerMsg.expElementMaps);
		mergeData(this.expMaps, workerMsg.expMaps);
		mergeData(this.stmtBuggyElementTypesMaps, workerMsg.stmtBuggyElementTypesMaps);
		mergeData(this.patchCommitIds, workerMsg.patchCommitIds);
	}

	private void mergeData(Map<String, Map<String, Integer>> targetMap, Map<String, Map<String, Integer>> map) {
		for (Map.Entry<String, Map<String, Integer>> entry : map.entrySet()) {
			String key = entry.getKey();
			Map<String, Integer> value = entry.getValue();
			
			Map<String, Integer> targetValue = targetMap.get(key);
			if (targetValue == null) {
				targetMap.put(key, value);
			} else {
				for (Map.Entry<String, Integer> subEntry : value.entrySet()) {
					String subKey = subEntry.getKey();
					Integer subValue = subEntry.getValue();
					if (targetValue.containsKey(subKey)) {
						targetValue.put(subKey, targetValue.get(subKey) + subValue);
					} else {
						targetValue.put(subKey, subValue);
					}
				}
			}
		}
	}

	private void mergeData(List<String> targetList, List<String> list) {
		list.removeAll(targetList);
		targetList.addAll(list);
	}

	private void exportData() {
		FileHelper.deleteDirectory(this.outputPath + "results/");
		exportData(this.stmtMaps, this.outputPath + "results/statements.csv");
		exportData1(this.elementsMaps, this.outputPath + "results/stmtElements1.csv", this.outputPath + "results/stmtElements2.csv");
		exportData1(this.stmtBuggyElementTypesMaps, this.outputPath + "results/stmtElementTypes1.csv", this.outputPath + "results/stmtElementTypes2.csv");
		exportData(this.abstractElementsMaps, this.outputPath + "results/abstractStmtElements.csv");
		exportData(this.expElementMaps, this.outputPath + "results/expElements.csv");
		exportData(this.expMaps, this.outputPath + "results/expressions.csv");
		
		mergeExamples();
		mergeAllParsedPatches();
	}

	private static final DecimalFormat FORMAT = new DecimalFormat("0.0000");
	
	private void exportData(Map<String, Map<String, Integer>> dataMap, String outputFileName) {
		List<Result> results = new ArrayList<>(); 
		List<StringBuilder> builders = new ArrayList<>();
		int updStmt = 0;
		int delStmt = 0;
		int movStmt = 0;
		int insStmt = 0;
		
		int sum = 0;
		int update = 0;
		int delete = 0;
		int move = 0;
		int insert = 0;
		
		int totalSum = 0;
		List<Integer> sumList = new ArrayList<>();
		for (Map.Entry<String, Map<String, Integer>> entry : dataMap.entrySet()) {
			String type = entry.getKey();
			Map<String, Integer> actionMap = entry.getValue();
			Integer upd = actionMap.get("UPD");
			Integer ins = actionMap.get("INS");
			Integer del = actionMap.get("DEL");
			Integer mov = actionMap.get("MOV");
			upd = upd == null ? 0 : upd;
			ins = ins == null ? 0 : ins;
			del = del == null ? 0 : del;
			mov = mov == null ? 0 : mov;
			int total = upd + ins + del + mov;
			totalSum += total;
			
			if (type.endsWith("Declaration")) {
				StringBuilder b = new StringBuilder(type);
				b.append(".");//.append(FORMAT.format( (double) total / sum * 100).replace(".", ",")).append("%\n")
				builders.add(b);
				sumList.add(total);
				if (! type.equals("FieldDeclaration")) continue;
			} else {
				updStmt += upd;
				delStmt += del;
				movStmt += mov;
				insStmt += ins;
			}

			StringBuilder builder = new StringBuilder();
			builder.append(type).append(".").append(upd).append(".")
				   .append(del).append(".").append(mov).append(".")
				   .append(ins).append(".").append(total).append(".");
			
			Result result = new Result();
			result.total = total;
			result.upd = upd;
			result.ins = ins;
			result.del = del;
			result.mov = mov;
			result.result = builder.toString();
			results.add(result);

			sum += total;
			update += upd;
			delete += del;
			move += mov;
			insert += ins;
		}
		
		StringBuilder dataBuilder = new StringBuilder();
		dataBuilder.append("type.Ratio\n");
		for (int index = 0, size = sumList.size(); index < size; index ++) {
			dataBuilder.append(builders.get(index)).append(FORMAT.format( (double) sumList.get(index) / totalSum * 100).replace(".", ",")).append("%\n");
		}
		dataBuilder.append("Statement.").append(FORMAT.format( (double) (updStmt + delStmt + movStmt + insStmt) / totalSum * 100).replace(".", ",")).append("%\n\n\n");
		
		dataBuilder.append("type.Update.Delete.Move.Insert.Total.Ratio\n");
		ListSorter<Result> sorter = new ListSorter<>(results);
		results = sorter.sortAscending();
		for (int index =0, size = results.size(); index < size; index ++) {
			Result result = results.get(index);
			dataBuilder.append(result.result)
				.append(FORMAT.format( (double)result.total / sum * 100).replace(".", ",")).append("%\n");
		}
		dataBuilder.append("  .").append(update).append(".").append(delete).append(".").append(move).append(".").append(insert).append("\n");
		dataBuilder.append("  .").append(FORMAT.format( (double)update / sum * 100).replace(".", ",")).append("%")
				   .append("  .").append(FORMAT.format( (double)delete / sum * 100).replace(".", ",")).append("%")
				   .append("  .").append(FORMAT.format( (double)move / sum * 100).replace(".", ",")).append("%")
				   .append("  .").append(FORMAT.format( (double)insert / sum * 100).replace(".", ",")).append("%\n\n\n");
		dataBuilder.append("type.Update.Delete.Move.Insert.Total.Ratio\n");
		dataBuilder.append(topTenResults(results, sum));
		
		FileHelper.outputToFile(outputFileName, dataBuilder, false);
	}

	private StringBuilder topTenResults(List<Result> results, int sum) {
		int size = results.size() - 5;
		StringBuilder builder = new StringBuilder();
		if (size > 0) {
			builder.append("Others.");
			int update = 0;
			int delete = 0;
			int move = 0;
			int insert = 0;
			for (int i = 0; i < size; i ++) {
				Result result = results.get(i);
				update += result.upd;
				delete += result.del;
				move += result.mov;
				insert += result.ins;
			}
			builder.append(update).append(".").append(delete).append(".").append(move).append(".").append(insert).append(".").append(update + delete + move + insert).append(".")
			   	   .append(FORMAT.format( (double)(update + delete + move + insert) / sum * 100).replace(".", ",")).append("%\n");
		} else if (size < 0) {
			size = 0;
		}
		for (int i = size; i < results.size(); i ++) {
			Result result = results.get(i);
			builder.append(result.result).append(FORMAT.format( (double)result.total / sum * 100).replace(".", ",")).append("%\n");
		}
		return builder;
	}

	private void exportData1(Map<String, Map<String, Integer>> elementsMaps, String outputFileName1, String outputFileName2) {
		List<String> subKeys = new ArrayList<>();
		List<String> stmts = new ArrayList<>();

		StringBuilder builder1 = new StringBuilder();
		builder1.append("exp,");
		for (Map.Entry<String, Map<String, Integer>> entry : elementsMaps.entrySet()) {
			String stmt = entry.getKey();
			stmts.add(stmt);
			builder1.append(stmt).append(",");
			Map<String, Integer> subMap = entry.getValue();
			for (Map.Entry<String, Integer> subEntry : subMap.entrySet()) {
				String subKey = subEntry.getKey();
				if (!subKeys.contains(subKey)) subKeys.add(subKey);
			}
		}
		builder1.append("\n");
		
		StringBuilder builder2 = new StringBuilder();
		builder2.append("stmt,");
		for (String element : subKeys) {
			builder1.append(element).append(",");
			for (String stmt : stmts) {
				Map<String, Integer> subMap = elementsMaps.get(stmt);
				Integer value = subMap.get(element);
				if (value == null) {
					builder1.append("0,");
				} else {
					builder1.append(value).append(",");
				}
			}
			builder1.append("\n");
			builder2.append(element).append(",");
		}
		builder2.append("\n");
		
		for (Map.Entry<String, Map<String, Integer>> entry : elementsMaps.entrySet()) {
			builder2.append(entry.getKey()).append(",");
			Map<String, Integer> subMap = entry.getValue();
			for (String element : subKeys) {
				Integer value = subMap.get(element);
				if (value == null) {
					builder2.append("0,");
				} else {
					builder2.append(value).append(",");
				}
			}
			builder2.append("\n");
		}

		FileHelper.outputToFile(outputFileName1, builder1, false);
		FileHelper.outputToFile(outputFileName2, builder2, false);
	}

	private void mergeExamples() {
		File[] subDirectories = new File(this.outputPath + "examples/").listFiles();
		for (File directory : subDirectories) {
			if (directory.isDirectory()) {
				String fileName = directory.getName();
				String outputFileName = this.outputPath + "examples/" + fileName + ".txt";
				FileHelper.deleteFile(outputFileName);
				File[] files = directory.listFiles();
				for (File file : files) {
					String fName = file.getName();
					if (fName.endsWith(".txt")) {
						FileHelper.outputToFile(outputFileName, FileHelper.readFile(file), true);
					}
					file.delete();
				}
				directory.delete();
			}
		}
	}
	
	private void mergeAllParsedPatches() {
		String fileName = this.outputPath + "Patches/";
		String outputFileName = this.outputPath + "examples/AllPatches.txt";
		FileHelper.deleteFile(outputFileName);
		File[] files = new File(fileName).listFiles();
		for (File file : files) {
			String fName = file.getName();
			if (fName.endsWith(".txt")) {
				FileHelper.outputToFile(outputFileName, FileHelper.readFile(file), true);
			}
			file.delete();
		}
		FileHelper.deleteDirectory(fileName);
	}

}
