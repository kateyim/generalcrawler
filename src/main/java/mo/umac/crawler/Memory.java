package mo.umac.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.infomatiq.jsi.Point;

import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;

public class Memory {

	protected static Logger logger = Logger.getLogger(Memory.class.getName());

	/**
	 * the lower bounds of the space. The length is d
	 */
	public static double[] lowerBounds = new double[Main.DIMENSION];
	/**
	 * the upper bounds of the space. The length is d
	 */
	public static double[] upperBounds = new double[Main.DIMENSION];

	/**
	 * All tuples in the database; Integer is the item's id
	 */
	public static HashMap<Integer, Point> pois;

	/**
	 * All crawled results
	 */
	public static Set<Integer> poisIDs = new HashSet<Integer>();

	/**
	 * point id: number crawled
	 */
	public static Map<Integer, Integer> poisCrawledTimes = new HashMap<Integer, Integer>();;

	public static KDTree kdtree;

	public Memory() {
		// this.dbNameSource = MainCrawlerDn.DB_NAME_SOURCE;

	}

	public void writeToExternalDB(int queryID, Point query, ResultSetForADim resultSets) {
		Strategy.dbExternal.writeToExternalDB(queryID, query, resultSets);
	}

	public void updataExternalDB() {
		// TODO Auto-generated method stub

	}

	public void index() {
		kdtree = new KDTree<Integer>(Main.DIMENSION);

		Iterator it = pois.entrySet().iterator();
		while(it.hasNext()){
			Entry entry = (Entry)it.next();
			Integer id = (Integer) entry.getKey();
			Point p = (Point) entry.getValue();
			double[] v = p.v;
			try {
				kdtree.insert(v, id);
			} catch (KeySizeException e) {
				e.printStackTrace();
			} catch (KeyDuplicateException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * compute the number of dimensions and the boundaries in the DSpace
	 * structure
	 * 
	 * @return
	 */
	public DSpace space() {
		DSpace dSpace = new DSpace(Main.DIMENSION, lowerBounds, upperBounds);
		return dSpace;
	}

	public List<Integer> query(Point queryPoint) {
		List<Integer> resultsID = searchNN(queryPoint, Main.TOP_K);
		for (int i = 0; i < resultsID.size(); i++) {
			int id = resultsID.get(i);
			int times = 0;
			if (poisCrawledTimes.containsKey(id)) {
				times = poisCrawledTimes.get(id);
			}
			times += 1;
			poisCrawledTimes.put(id, times);

		}
		poisIDs.addAll(resultsID);
		Strategy.countNumQueries++;
		queryPoint.id = Strategy.countNumQueries;

		if (logger.isDebugEnabled()) {
			logger.debug("query = " + Utils.ArrayToString(queryPoint.v));
			// if (Strategy.countNumQueries % 20 == 0) {
			logger.debug("countNumQueries = " + Strategy.countNumQueries);
			logger.debug("countCrawledPoints = " + poisCrawledTimes.size());
			logger.debug("crawled points + crawed times: " + poisCrawledTimes.toString());
			// }
		}
		return resultsID;
	}

	/**
	 * read point from external h2db, update the lowerbounds and the upper
	 * bounds
	 */
	public void readFromExtenalDB() {
		pois = Strategy.dbExternal.readFromExtenalDB(Main.DIMENSION, lowerBounds, upperBounds);

	}
	

	public List<Integer> searchNN(Point queryPoint, int topK) {
		List<Integer> list = null;
		try {
			list = (List<Integer>) kdtree.nearest(queryPoint.v, topK);
		} catch (KeySizeException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return list;
	}

}
