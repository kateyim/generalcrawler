package mo.umac.crawler;

import org.apache.log4j.Logger;

public abstract class Strategy {

	protected static Logger logger = Logger.getLogger(Strategy.class.getName());

	public static H2DB dbExternal;

	public static Memory dbInMemory;

	public static int countNumQueries = 0;

	public static int rectangleId;

	public void callCrawling() {
		long before = System.currentTimeMillis();
		logger.info("Start at : " + before);

		DSpace dSpace = prepareData();
		crawl(dSpace);

		// After crawling:
		logger.info("removing duplicate records in the external db");
		dbExternal.removeDuplicate();
		logger.info("begin updating the external db");
		dbInMemory.updataExternalDB();
		logger.info("end updating the external db");
		//
		endData();
		/**************************************************************************/
		long after = System.currentTimeMillis();
		logger.info("Stop at: " + after);
		logger.info("time for crawling = " + (after - before) / 1000);
		//
		logger.info("countNumQueries = " + countNumQueries);
		logger.info("number of points crawled = " + dbInMemory.poisIDs.size());
		logger.info(Memory.poisCrawledTimes.toString());
		logger.info("Finished ! Oh ! Yeah! ");
	}

	protected abstract void crawl(DSpace dSpace);

	private static DSpace prepareData() {
		logger.info("preparing data...");
		dbExternal = new H2DB(Main.DB_NAME_SOURCE, Main.DB_NAME_TARGET);
		//
		dbInMemory = new Memory();
		// read point from external h2db, update the lowerbounds and the upper bounds
		dbInMemory.readFromExtenalDB();
		// expandBoundary();
		// dbInMemory.poisCrawledTimes = new HashMap<Integer, Integer>();
		dbInMemory.index();
		logger.info("There are in total " + dbInMemory.pois.size() + " points.");
		// target database
		dbExternal.createTables(Main.DB_NAME_TARGET);

		DSpace dSpace = dbInMemory.space();
		logger.info("The dSpace is " + dSpace.toString());
		return dSpace;
	}

	private static void expandBoundary() {
		for (int i = 0; i < Main.DIMENSION; i++) {
			Memory.lowerBounds[i] = Memory.lowerBounds[i] - 0.1f;
			Memory.upperBounds[i] = Memory.upperBounds[i] - 0.1f;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see mo.umac.crawler.YahooLocalCrawlerStrategy#endData()
	 * shut down the connection
	 */
	private static void endData() {
		H2DB.distroyConn();
	}
}
