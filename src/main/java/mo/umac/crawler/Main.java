package mo.umac.crawler;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Main {

	public static Logger logger = Logger.getLogger(Main.class.getName());

	public static String LOG_PROPERTY_PATH = "./resources/log4j.xml";
	public static boolean debug = false;

	// testing synthetic
	// public final static String DB_NAME_D2 = "../data-experiment/synthetic/uniform-1000";
	// public final static String DB_NAME_SOURCE = "../data-experiment/synthetic/uniform-1000-dn";
	// public final static String DB_NAME_D2 = "../data-experiment/synthetic/skew-1000-0.3";
	// public final static String DB_NAME_SOURCE = "../data-experiment/synthetic/skew-1000-0.3-dn";
	// public final static String DB_NAME_TARGET = "../data-experiment/synthetic/target";
	// public final static int TOP_K = 10;
	// public final static int DIMENSION = 2;
	// public static double[] lowerBounds = { 0.0, 0.0 };
	// public static double[] upperBounds = { 1000.0, 1000.0 };
	// public static boolean hasBoundary = true;

	// testing yahoo
	public final static String DB_NAME_D2 = "../data-experiment/yahoo/ny-prun";
	public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ny-prun-dn";
	public final static String DB_NAME_TARGET = "../data-experiment/yahoo/target";
	public final static int TOP_K = 100;
	public final static int DIMENSION = 2;
	// NY: Env[-79.76259 : -71.777491, 40.477399 : 45.015865]
	public static double[] lowerBounds = { -79.76259, 40.477399 };
	public static double[] upperBounds = { -71.777491, 45.015865 };
	public static boolean hasBoundary = true;

	// public final static String DB_NAME_FILE = "../crawler-data/glass-data/glass/glasses.txt";
	// public final static String DB_NAME_SOURCE = "../crawler-data/glass-data/glass/glasses";
	// public final static String DB_NAME_TARGET = "../crawler-data/glass-data/glass/glasses_test";
	// public final static int TOP_K = 5;
	// public final static int DIMENSION = 4;

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
		Main.debug = false;
		initForServer(true);

		shutdownLogs(Main.debug);

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

	private static void shutdownLogs(boolean debug) {
		if (!debug) {
			Strategy.logger.setLevel(Level.INFO);
			ConcreteCrawler.logger.setLevel(Level.INFO);
			Memory.logger.setLevel(Level.INFO);
		} else {
			Strategy.logger.setLevel(Level.DEBUG);
			ConcreteCrawler.logger.setLevel(Level.DEBUG);
			Memory.logger.setLevel(Level.DEBUG);
		}
	}
}
