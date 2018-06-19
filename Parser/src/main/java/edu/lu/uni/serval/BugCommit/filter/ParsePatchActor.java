package edu.lu.uni.serval.BugCommit.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import akka.routing.RoundRobinPool;
import edu.lu.uni.serval.BugCommit.parser.MessageFile;
import edu.lu.uni.serval.BugCommit.parser.WorkMessage;
import edu.lu.uni.serval.BugCommit.parser.WorkerReturnMessage;
import edu.lu.uni.serval.utils.FileHelper;

public class ParsePatchActor extends UntypedActor {
	
//	private static Logger logger = LoggerFactory.getLogger(ParsePatchActor.class);

	private ActorRef mineRouter;
	private final int numberOfWorkers;
	private int counter = 0;
	int diff = 0;
	int hunks = 0;
	int zeroG = 0;
	int overRap = 0;
	private String rootPath;
	private String projectName;
	
	public ParsePatchActor(int numberOfWorkers, String rootPath, String projectName) {
		this.numberOfWorkers = numberOfWorkers;
		this.rootPath = rootPath;
		this.projectName = projectName;
		mineRouter = this.getContext().actorOf(new RoundRobinPool(numberOfWorkers)
				.props(ParsePatchWorker.props()), "patch-parse-router-" + projectName);
	}

	public static Props props(final int numberOfWorkers, final String rootPath, final String projectName) {
		
		return Props.create(new Creator<ParsePatchActor>() {

			private static final long serialVersionUID = 9207427376110704705L;

			@Override
			public ParsePatchActor create() throws Exception {
				return new ParsePatchActor(numberOfWorkers, rootPath, projectName);
			}
			
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof WorkMessage) {
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
			
//			logger.info(counter + " workers finished their work...");
			if (counter >= numberOfWorkers) {
//				logger.info(projectName + " patch parsing work is finished...");
				calculateCommitIds();
				this.getContext().stop(mineRouter);
				this.getContext().stop(getSelf());
				this.getContext().system().shutdown();
			}
		} else {
			unhandled(msg);
		}
	}

	private void calculateCommitIds() {
		List<String> keywordPatchCommitIds = readCommitIds(this.rootPath + "Keywords/" + projectName + "/DiffEntries/");
		List<String> linkedPatchCommitIds = readCommitIds(this.rootPath + "Linked/" + projectName + "/DiffEntries/");
//		List<String> unlinkedPatchCommitIds = readCommitIds(this.rootPath + "Unlinked/" + projectName + "/DiffEntries/");
//		System.out.println("Keyword Commits: " + keywordPatchCommitIds.size());
//		System.out.println("Linked Commits: " + linkedPatchCommitIds.size());
//		System.out.println("Unlinked Commits: " + unlinkedPatchCommitIds.size());
		System.out.println(projectName + " Identified patch-related commits: " + (keywordPatchCommitIds.size() + linkedPatchCommitIds.size()));
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

}
