package cc.holstr.main.model;

import name.fraser.neil.plaintext.diff_match_patch;

import java.util.LinkedList;

/**
 * Created by Jason McElhenney on 1/6/18.
 * zudsniper on GitHub.
 */
public class UILTestResult {

	private UILTest test;
	private String actualOutput;
	private LinkedList<diff_match_patch.Diff> differences;
	private Outcome outcome;

	public UILTestResult(UILTest test, String actualOutput, LinkedList<diff_match_patch.Diff> differences, Outcome outcome) {
		this.test = test;
		this.actualOutput = actualOutput;
		this.differences = differences;
		this.outcome = outcome;
	}

	public String getClassName() {
		return test.getClassName();
	}

	public UILTest getTest() {
		return test;
	}

	public String getActualOutput() {
		return actualOutput;
	}

	public LinkedList<diff_match_patch.Diff> getDifferences() {
		return differences;
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public enum Outcome {
		PASS,FAIL
	}
}
