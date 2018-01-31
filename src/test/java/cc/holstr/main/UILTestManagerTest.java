package cc.holstr.main;

import cc.holstr.main.model.UILTest;
import cc.holstr.main.model.UILTestResult;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jason McElhenney on 1/24/18.
 * zudsniper on GitHub.
 */
public class UILTestManagerTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Rule
	public TemporaryFolder UILFileDir = new TemporaryFolder();

	private UILTestManager mgr;

	@Before
	public void setUp() throws Exception {
		mgr = new UILTestManager(UILFileDir.getRoot().getAbsolutePath());
		log.debug(UILFileDir.getRoot().getAbsolutePath());

	}

	@After
	public void tearDown() throws Exception {

	}

	public void makeUILTestFiles(String className, String inputName, String outputName,
								 String classText, String inputText, String outputText) throws IOException {
		if(className != null) {
			File classDir = UILFileDir.newFile(className);
			if(classText != null)
				FileUtils.writeStringToFile(classDir,classText);
		}
		if(inputName != null) {
			File classInput = UILFileDir.newFile(inputName);
			if(inputText != null)
				FileUtils.writeStringToFile(classInput,inputText);
		}
		if(outputName != null) {
			File classOutput = UILFileDir.newFile(outputName);
			if(outputText != null)
				FileUtils.writeStringToFile(classOutput, outputText);
		}
	}

	@Test
	public void populateTestsExplicitInputOutput() throws Exception {
		makeUILTestFiles("test.java","testInput.dat","testOutput.log",
				"import java.io.File;\n" +
						"import java.io.FileNotFoundException;\n" +
						"import java.util.Scanner;\n" +
						"\n" +
						"public class Test {\n" +
						"\n" +
						"\t//%o: testOutput.log\n" +
						"\t//%i: testInput.dat\n" +
						"\n" +
						"\tpublic static void main(String[] args) {\n" +
						"\n" +
						"\t\t" +
						"\n" +
						"\t\tSystem.out.println(\"output\");\n" +
						"\t}\n" +
						"}\n", "input","output");

		mgr.populateTests();
		UILTestResult result = mgr.test("test");

		assertEquals("testInput.dat",result.getTest().getInputName());
		assertEquals("testOutput.log",result.getTest().getOutputName());
	}

	@Test
	public void populateTestsFindOutputFromInput() throws Exception {
		makeUILTestFiles("test.java","testInput.dat","testInput.log",
				"import java.io.File;\n" +
				"import java.io.FileNotFoundException;\n" +
				"import java.util.Scanner;\n" +
				"\n" +
				"public class Test {\n" +
				"\n" +
				"\t//%i:testInput.dat" +
				"\n" +
				"\tpublic static void main(String[] args) {\n" +
				"\n" +
				"\t\t" +
				"\n" +
				"\t\tSystem.out.println(\"output\");\n" +
				"\t}\n" +
				"}\n", "input","output");

		mgr.populateTests();
		UILTestResult result = mgr.test("test");

		assertEquals("testInput.dat",result.getTest().getInputName());
		assertEquals("testInput.log",result.getTest().getOutputName());
	}


	@Test
	public void testAll() throws Exception {

	}

	@Test
	public void test() throws Exception {
		makeUILTestFiles(null, "input.dat",null, null, "input", null);

		UILTest test = new UILTest("import java.io.File;\n" +
				"import java.io.FileNotFoundException;\n" +
				"import java.util.Scanner;\n" +
				"\n" +
				"public class Test {\n" +
				"\n" +
				"\n" +
				"\tpublic static void main(String[] args) {\n" +
				"\n" +
				"\t Scanner scanner = new Scanner(new File(\"input.dat\"));" +
				"\t\t" +
				"\n" +
				"\t\tSystem.out.println(\"This is the output.\");\n" +
				"\t}\n" +
				"}\n");

		test.setInputName("input.dat");
		test.setInput("input");

		UILTestResult expected = new UILTestResult(test,"This is the output.", UILTestResult.Outcome.PASS);
		UILTestResult actual = mgr.test(test);

		assertEquals(expected.getActualOutput(),actual.getActualOutput());
		assertEquals(expected.getOutcome(),actual.getOutcome());

	}

	@Test
	public void test2() throws Exception {

	}

	@Test
	public void getTest() throws Exception {

	}

    @Test
    public void absolutify() {
	    UILTestManager mgr = new UILTestManager("test");
	    String relative = "new File(\"file.dat\");";
	    assertEquals("new File(\"" + Paths.get(mgr.getPath()).toFile().getAbsolutePath() + System.getProperty("file.separator") + "file.dat\");", mgr.absolutify(relative));

    }
}