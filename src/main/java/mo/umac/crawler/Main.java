package mo.umac.crawler;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Main {

	public static Logger logger = Logger.getLogger(Main.class.getName());

	public static String LOG_PROPERTY_PATH = "./resources/log4j.xml";

	public final static String DB_NAME_FILE = "../crawler-data/glass-data/glass/glasses.txt";
	public final static String DB_NAME_SOURCE = "../crawler-data/glass-data/glass/glasses";
	public final static String DB_NAME_TARGET = "../crawler-data/glass-data/glass/glasses_test";
	public final static int TOP_K = 20;
	public final static int DIMENSION = 4;

	// public final static String DB_NAME_FILE = "../crawler-data/glass-data/glass-small/small.txt";
	// public final static String DB_NAME_SOURCE = "../crawler-data/glass-data/glass-small/small";
	// public final static String DB_NAME_TARGET = "../crawler-data/glass-data/glass-small/small_test";
	// public final static int TOP_K = 1;
	// public final static int DIMENSION = 4;

	// for testing-2d
	// public final static String DB_NAME_SOURCE = "../crawler-data/glass-data/test-2d/source";;
	// public final static String DB_NAME_TARGET = "../crawler-data/glass-data/test-2d/target";;
	// public final static String DB_NAME_CRAWL = "../crawler-data/glass-data/test-2d/crawl";
	// public final static int TOP_K = 10;
	// public final static int DIMENSION = 2;

	// for testing-4d
	// public final static String DB_NAME_SOURCE = "../crawler-data/glass-data/test-4d/source";;
	// public final static String DB_NAME_TARGET = "../crawler-data/glass-data/test-4d/target";;
	// public final static String DB_NAME_CRAWL = "../crawler-data/glass-data/test-4d/crawl";
	// public final static int TOP_K = 3;
	// public final static int DIMENSION = 4;

	public static void main(String[] args) {
		initForServer(true);
		DOMConfigurator.configure(Main.LOG_PROPERTY_PATH);
		Strategy strategy = new ConcreteCrawler();

		ContextDn context = new ContextDn(strategy);

		context.callCrawling();
	}

	/**
	 * If packaging, then changing the destiny of paths of the configure files
	 * 
	 * @param packaging
	 */
	public static void initForServer(boolean packaging) {
		if (packaging) {
			// for packaging, set the resources folder as
			Main.LOG_PROPERTY_PATH = "target/log4j.xml";
		} else {
			// for debugging, set the resources folder as
			Main.LOG_PROPERTY_PATH = "./src/main/resources/log4j.xml";
		}
	}
}
