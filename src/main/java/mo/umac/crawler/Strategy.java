package mo.umac.crawler;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.wlu.cs.levy.CG.KeySizeException;

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
		// for debugging
		checkUncrawledPoints();

		// After crawling:
		// logger.info("removing duplicate records in the external db");
		// dbExternal.removeDuplicate();
		// logger.info("begin updating the external db");
		// dbInMemory.updataExternalDB();
		// logger.info("end updating the external db");
		//
		endData();
		/**************************************************************************/
		long after = System.currentTimeMillis();
		logger.info("Stop at: " + after);
		logger.info("time for crawling = " + (after - before) / 1000 + "s");
		//
		logger.info("countNumQueries = " + countNumQueries);
		logger.info("number of points crawled = " + dbInMemory.poisIDs.size());
		// logger.info(Memory.poisCrawledTimes.toString());
		logger.info("Finished ! Oh ! Yeah! ");
	}

	private void checkUncrawledPoints() {
		logger.info("checking not crawled points");
		Iterator it = Memory.pois.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			int key = (Integer) entry.getKey();
			if (!Memory.poisIDs.contains(key)) {
				logger.info(key + ": " + Memory.pois.get(key).toString());
				double[] newKey = new double[Main.DIMENSION + 1];
				for (int i = 0; i < Main.DIMENSION; i++) {
					newKey[i] = Memory.pois.get(key).getValueOfADimension(i);
				}
				newKey[Main.DIMENSION] = key * Memory.EPSILON;
				try {
					int id = (Integer) Memory.kdtree.search(newKey);
					logger.info("in kdtree: " + id + ": " + Utils.ArrayToString(newKey));
				} catch (KeySizeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected abstract void crawl(DSpace dSpace);

	private static DSpace prepareData() {
		logger.info("preparing data...");
		dbExternal = new H2DB(Main.DB_NAME_SOURCE, Main.DB_NAME_TARGET);
		//
		dbInMemory = new Memory();
		// read point from external h2db, update the lower bounds and the upper bounds
		dbInMemory.readFromExtenalDB(Main.DIMENSION);
		dbInMemory.pruning(Main.OVERLAP_NUM);
		// expandBoundary();
		// dbInMemory.poisCrawledTimes = new HashMap<Integer, Integer>();
		dbInMemory.index();
		logger.info("There are in total " + dbInMemory.pois.size() + " points.");
		logger.info("There are in total " + Memory.kdtree.size() + " points in the KD tree");
		// target database
		dbExternal.createTables(Main.DB_NAME_TARGET);
		// add at 2014-4-1
		if (Main.hasBoundary) {
			dbInMemory.lowerBounds = Main.lowerBounds;
			dbInMemory.upperBounds = Main.upperBounds;
		}
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
	 * 
	 * @see mo.umac.crawler.YahooLocalCrawlerStrategy#endData() shut down the connection
	 */
	private static void endData() {
		H2DB.distroyConn();
	}
}
