package edu.lu.uni.serval.BugCommit.parser;

public class Result implements Comparable<Result> {

	public Integer total;
	public int upd, ins, del, mov;
	public String result;
	
	@Override
	public int compareTo(Result o) {
		return this.total.compareTo(o.total);
	}
	
}
