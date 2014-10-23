package mo.umac.crawler;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Main {

	public static Logger logger = Logger.getLogger(Main.class.getName());

	public static String LOG_PROPERTY_PATH = "./resources/log4j.xml";
	public static boolean debug = false;

	// testing synthetic
	// public final static String DB_NAME_D2 = "../data-experiment/synthetic/uniform-2000";
	// public final static String DB_NAME_SOURCE = "../data-experiment/synthetic/uniform-2000-dn";
	// public final static String DB_NAME_D2 = "../data-experiment/synthetic/skew-2000-0.3";
	// public final static String DB_NAME_SOURCE = "../data-experiment/synthetic/skew-2000-0.3-dn";

//	public final static String DB_NAME_D2 = "";
//	public final static String DB_NAME_SOURCE = "../data-experiment/synthetic/1d-skewed/1000";
//	public final static String DB_NAME_TARGET = "../data-experiment/synthetic/target";
//	public final static int TOP_K = 100;
//	public final static int DIMENSION = 1;
//	public static final int OVERLAP_NUM = TOP_K;
//	public static double[] lowerBounds = { 0.0, 0.0, 0.0, 0.0, 0.0 };
//	public static double[] upperBounds = { 1000.0, 1000.0, 1000.0, 1000.0, 1000.0 };
//	public static boolean hasBoundary = true;

	public final static String DB_NAME_D2 = "";
	public final static String DB_NAME_SOURCE = "../data-experiment/glass-data/glass/glasses";
	public final static String DB_NAME_TARGET = "../data-experiment/glass-data/glass/glasses_test";
	public final static int TOP_K = 10;
	public final static int DIMENSION = 4;
	public static final int OVERLAP_NUM = 5;
	public static double[] lowerBounds = { 40, 10, 9, 115 };
	public static double[] upperBounds = { 64, 49, 24, 155 };
	public static boolean hasBoundary = true;

	public static void main(String[] args) {
		Main.debug = false;
		initForServer(false);

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
