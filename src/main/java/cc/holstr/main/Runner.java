package cc.holstr.main;

import cc.holstr.gui.MainWindow;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Jason McElhenney on 1/6/18.
 * zudsniper on GitHub.
 */
public class Runner {

	public static final boolean DEBUG = true;
	private static final Logger log = LoggerFactory.getLogger(Runner.class);

	private static String path;
	public static String version;

	public static void main(String[] args) throws IOException {
		version = new BufferedReader(new InputStreamReader(Runner.class.getClassLoader().getResourceAsStream("version.txt"))).readLine();

		OptionParser parser = new OptionParser("d:");
		OptionSet options = parser.parse(args);
		if(options.has("d") && options.hasArgument("d")) {
			path = (String)options.valueOf("d");
		} else {
			path = System.getProperty("user.dir");
		}
		log.info("PATH: " + path);

		UILTestManager mgr = new UILTestManager(path);
		MainWindow mainWindow = new MainWindow(mgr);
	}
}
