package cc.holstr.main.model;

import java.io.File;

/**
 * Created by Jason McElhenney on 1/6/18.
 * zudsniper on GitHub.
 */
public class UILTest {

	private String className;
	private File classFile;

	private String inputName;
	private String outputName;

	private String input;
	private String output;

	private String rawClassCode;

	public UILTest(String rawClassCode) {
		this.rawClassCode = rawClassCode;
	}

	public UILTest(String className, File classFile, String rawClassCode) {
		this.className = className;
		this.classFile = classFile;
		this.rawClassCode = rawClassCode;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}


	public File getClassFile() {
		return classFile;
	}

	public void setClassFile(File classFile) {
		this.classFile = classFile;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public String getRawClassCode() {
		return rawClassCode;
	}

	public String shortToString() {
		return "UILTest{" +
				"className='" + className + '\'' +
				", classFile=" + classFile +
				", inputName='" + inputName + '\'' +
				", outputName='" + outputName + '\'' + '}';
	}

	@Override
	public String toString() {
		return "UILTest{" +
				"className='" + className + '\'' +
				", classFile=" + classFile +
				", inputName='" + inputName + '\'' +
				", outputName='" + outputName + '\'' +
				", input='" + input + '\'' +
				", output='" + output + '\'' +
				", rawClassCode='" + rawClassCode + '\'' +
				'}';
	}
}
