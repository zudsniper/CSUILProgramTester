package cc.holstr.main;

import cc.holstr.main.model.UILTest;
import cc.holstr.main.model.UILTestResult;
import name.fraser.neil.plaintext.diff_match_patch;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jason McElhenney on 1/6/18.
 * zudsniper on GitHub.
 */
public class UILTestManager {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private List<UILTest> tests;
	private String path;

	public UILTestManager(String path) {
		this.path = path;
		tests = new ArrayList<>();
	}

	public void populateTests() throws IOException {
		tests.clear();
		File dir = new File(path);
		File[] files = dir.listFiles();
		if(files!=null) {
			for(File file : files) {
				String ext = getExt(file);
				UILTest test;
				if(ext.equals("java")) {
					String fileText = FileUtils.readFileToString(file);
					test = new UILTest(fileText);
					log.debug("fileText: \n" + fileText);
					test.setClassFile(file);
					test.setClassName(findInString("class ([\\w\\d]+)?", fileText,1));
					log.debug("ClassName: " + test.getClassName());
					String[] inputTemp = findInput(fileText, test.getClassName());
					String[] outputTemp = findOutput(fileText,test.getClassName(),inputTemp[1]);
					test.setInput(inputTemp[0]);
					test.setInputName(inputTemp[1]);
					test.setOutput(outputTemp[0]);
					test.setOutputName(outputTemp[1]);
					tests.add(test);
				} else {
					//ignore other files
				}
			}
		} else {
			//TODO: handle directory is empty
		}

		log.debug(tests.toString());
	}

	/**
	 * First checks for explicit definition, then for reference in code, then by className.
	 * @return The input data
	 *
	 */
	private String[] findInput(String fileText, String className) throws IOException {

		String[] out = new String[2];

		boolean input = true;
		String fileName = null;

		String explicitInput = findInString("\\/\\/(\\s+)?%i:(\\s+)?[\\w\\d]+\\.[\\w\\d]+",fileText);
		if(explicitInput != null) {
			explicitInput = explicitInput.replaceAll("//(\\s+)?%i:(\\s+)?", "");
		}
		log.debug("explicitInput: " + explicitInput);
		if(explicitInput == null) {
			String inputName = findInString("[\\w\\d]+\\.dat", fileText);
			if(inputName == null) {
				if(Arrays.stream(Paths.get(path).toFile().listFiles()).filter(f -> f.getName().equals(className + ".dat")).count()<1) {
					return out;
				} else {
					fileName = className + ".dat";
				}
			} else {
				fileName = inputName;
			}
		} else {
			fileName = explicitInput;
		}

		out[1] = fileName;
		out[0] = FileUtils.readFileToString(Paths.get(path, fileName).toFile());

		return out;
	}
	/**
	 * First checks for explicit definition, then for file of the same name as the input but with ".log" extension, then by className.
	 * @return The expected output
	 */
	private String[] findOutput(String fileText, String className, String inputName) throws IOException {

		String[] out = new String[2];

		String fileName = null;

		String explicitOutput = findInString("\\/\\/(\\s+)?%o:(\\s+)?[\\w\\d]+\\.[\\w\\d]+",fileText);
		if(explicitOutput != null) {
			explicitOutput = explicitOutput.replaceAll("//(\\s+)?%o:(\\s+)?", "");
		}
		log.debug("explicitOutput: " + explicitOutput);
		if(explicitOutput == null) {
			if(inputName == null) {
				fileName = className + ".log";
			} else {
				String outputName = inputName.replace(".dat",".log");
				if(Arrays.stream(Paths.get(path).toFile().listFiles()).filter(f -> f.getName().equals(outputName)).count()>=1) {
					fileName = outputName;
				}
			}
		} else {
			fileName = explicitOutput;
		}

		out[1] = fileName;
		out[0] = FileUtils.readFileToString(Paths.get(path, fileName).toFile());
		return out;
	}

	/**
	 * Convenience method for test(UILTest), runs all tests within tests ArrayList
	 * @return List of UILTestResults for all tests run
	 */
	public List<UILTestResult> testAll() throws IOException{
		List<UILTestResult> results = new ArrayList<>();
		for(UILTest test : tests) {
			results.add(test(test));
		}
		return results;
	}

	/**
	 * Convenience method for test(UILTest), finds UIlTest from list by className
	 * @param testClassName className to find UIlTest by
	 * @return The UILTestResult with populated differences
	 * @throws IOException
	 */
	public UILTestResult test(String testClassName) throws IOException {
		UILTest test = tests.stream().filter(t -> testClassName.equals(t.getClassName())).findFirst().get();
		return test(test);
	}

	/** run the specified UILTest class, capture output, and find the differences between it and the expected output.
	 *
	 * @param test The UILTest to run
	 * @return The UILTestResult with populated differences
	 * @throws IOException
	 */
	public UILTestResult test(UILTest test) throws IOException {

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

		String fileText = test.getRawClassCode();

		//change all relative path references for paths made with Files to absolute
		fileText = absolutify(fileText);

		JavaFileObject file = new JavaSourceFromString(test.getClassName(), fileText);

		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

		JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);

		boolean success = task.call();
		for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
			System.out.println(diagnostic.getCode());
			System.out.println(diagnostic.getKind());
			System.out.println(diagnostic.getPosition());
			System.out.println(diagnostic.getStartPosition());
			System.out.println(diagnostic.getEndPosition());
			System.out.println(diagnostic.getSource());
			System.out.println(diagnostic.getMessage(null));
		}

		if (success) {
			try {
				//get classFile, move it to the appropriate directory, create classLoader there for execution of class later
				File classLoaderDir = Paths.get(path).toFile();
				File classFile = Arrays.stream(Paths.get(System.getProperty("user.dir")).toFile().listFiles()).filter(f -> f.getName().equals(test.getClassName()+".class")).findFirst().get();
				File classFileDest = Paths.get(path,test.getClassName()+".class").toFile();
				classFileDest.delete();
				FileUtils.moveFile(classFile, classFileDest);
				URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {classLoaderDir.toURI().toURL()});

				//capture system.out for comparison to expected output
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				PrintStream old = System.out;

				System.setOut(ps);

				//run main method for testclass
				Class.forName(test.getClassName(), true, ucl).getDeclaredMethod("main", new Class[] { String[].class })
						.invoke(null, new Object[] { null });

				System.out.flush();
				System.setOut(old);

				String actualOutput = baos.toString();

				//get differences using google diff match patch
				diff_match_patch dmp = new diff_match_patch();
				LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(test.getOutput(),actualOutput);
				dmp.diff_cleanupSemantic(diffs);

				UILTestResult.Outcome outcome;

				if(diffs.stream().filter(d -> !d.text.matches("\\s+")).filter(d ->  !d.operation.equals(diff_match_patch.Operation.EQUAL)).count()>0) {
					outcome = UILTestResult.Outcome.FAIL;
				} else {
					outcome = UILTestResult.Outcome.PASS;
				}

				return new UILTestResult(test,actualOutput,diffs,outcome);

			} catch (ClassNotFoundException e) {
				log.debug("Class not found: " + e);
			} catch (NoSuchMethodException e) {
				log.debug("No such method: " + e);
			} catch (IllegalAccessException e) {
				log.debug("Illegal access: " + e);
			} catch (InvocationTargetException e) {
				log.debug("Invocation target: " + e);
				e.printStackTrace();
			} catch (IOException e) {
				log.debug("IOException: " + e);
			}
		}

		return null;
	}

	/**
	 * Gets a UILTest by specified className.
	 * @param testName
	 * @return UILTest with name testName
	 */
	public UILTest getTest(String testName) {
		return tests.stream().filter(t -> t.getClassName().equals(testName)).findAny().get();
	}

	/**
	 * Changes relative path references to absolute
	 * functions for File constructor
	 * @param fileText
	 * @return
	 */
	private String absolutify(String fileText) {
		//assumes reference doesn't begin with dir separator
		return fileText.replaceAll("(new File)\\(\"([\\w\\d\\.\\\\]+)\"\\)","new File(\"" + path + System.getProperty("file.separator") + "$2\")");
	}

	private String findInString(String exp, String str) throws IOException {
		return findInString(exp, str, 0);
	}

	private String findInString(String exp, String str,int groupPosition) throws IOException {
		Pattern pattern = Pattern.compile(exp);
		Matcher m = pattern.matcher(str);

		if(m.find()) {
			return m.group(groupPosition);
		} else {
			//TODO: handle multiple matches
		}
		return null;
	}

	//doesn't include the dot
	private String getExt(File f) {
		String name = f.getName();
		return name.substring(name.indexOf(".")+1);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<UILTest> getTestsList() {
		return tests;
	}

	class JavaSourceFromString extends SimpleJavaFileObject {
		final String code;

		JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}

}
