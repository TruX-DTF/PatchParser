package edu.lu.uni.serval.BugCommit.filter;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import edu.lu.uni.serval.BugCommit.parser.MessageFile;
import edu.lu.uni.serval.BugCommit.parser.WorkMessage;
import edu.lu.uni.serval.BugCommit.parser.WorkerReturnMessage;

public class ParsePatchWorker extends UntypedActor {
	
	public ParsePatchWorker() {
	}

	public static Props props() {
		return Props.create(new Creator<ParsePatchWorker>() {

			private static final long serialVersionUID = -7615153844097275009L;

			@Override
			public ParsePatchWorker create() throws Exception {
				return new ParsePatchWorker();
			}
			
		});
	}

	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof WorkMessage) {
			WorkMessage workMsg = (WorkMessage) msg;
			List<MessageFile> msgFiles = workMsg.getMsgFiles();
			
			for (MessageFile msgFile : msgFiles) {
				File revFile = msgFile.getRevFile();
				File prevFile = msgFile.getPrevFile();
				File diffentryFile = msgFile.getDiffEntryFile();
				
				PatchParser parser =  new PatchParser();
				
				final ExecutorService executor = Executors.newSingleThreadExecutor();
				// schedule the work
				final Future<?> future = executor.submit(new RunnableParser(prevFile, revFile, diffentryFile, parser));
				try {
					// wait for task to complete
					future.get(600L, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					future.cancel(true);
					System.err.println("#Timeout: " + revFile.getName());
				} catch (InterruptedException e) {
					System.err.println("#TimeInterrupted: " + revFile.getName());
					e.printStackTrace();
				} catch (ExecutionException e) {
					System.err.println("#TimeAborted: " + revFile.getName());
					e.printStackTrace();
				} finally {
					executor.shutdownNow();
				}
			}
			
			WorkerReturnMessage workerMsg = new WorkerReturnMessage(0, null, null);
			this.getSender().tell(workerMsg, getSelf());
		} else {
			unhandled(msg);
		}
	}
	
}
