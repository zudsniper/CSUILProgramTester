package cc.holstr.main;

import cc.holstr.gui.MainWindow;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Jason McElhenney on 1/6/18.
 * zudsniper on GitHub.
 */
public class Runner {

	private static final Logger log = LoggerFactory.getLogger(Runner.class);

	public static String path;

	public static void main(String[] args) throws IOException {

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
