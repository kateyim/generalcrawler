package mo.umac.crawler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	public static final double EPSILON = 0.00000001;

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
		logger.info("indexing");
		kdtree = new KDTree<Integer>(Main.DIMENSION + 1);
		Iterator it = pois.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			Integer id = (Integer) entry.getKey();
			Point p = (Point) entry.getValue();
			// double[] v = p.v;
			double[] v = new double[Main.DIMENSION + 1];
			for (int i = 0; i < Main.DIMENSION; i++) {
				v[i] = p.getValueOfADimension(i);
			}
			v[Main.DIMENSION] = id * EPSILON;
			try {
				kdtree.insert(v, id);
			} catch (KeySizeException e) {
				e.printStackTrace();
			} catch (KeyDuplicateException e) {
				logger.error(e.toString());
				// e.printStackTrace();
			}
		}
	}

	public void indexWithoutDuplicateKey() {
		kdtree = new KDTree<Integer>(Main.DIMENSION);

		Iterator it = pois.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			Integer id = (Integer) entry.getKey();
			Point p = (Point) entry.getValue();
			double[] v = p.v;
			try {
				kdtree.insert(v, id);
			} catch (KeySizeException e) {
				e.printStackTrace();
			} catch (KeyDuplicateException e) {
				logger.error(e.toString());
				// e.printStackTrace();
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
			logger.debug("answers = ");
			for (int i = 0; i < resultsID.size(); i++) {
				int id = resultsID.get(i);
				if (logger.isDebugEnabled()) {
					logger.debug(id + ":" + pois.get(id).toString());
				}
			}

		}
		if (Strategy.countNumQueries % 10 == 0) {
			logger.info("countNumQueries = " + Strategy.countNumQueries);
			logger.info("countCrawledPoints = " + poisCrawledTimes.size());
			// logger.info("crawled points + crawled times: " +
			// poisCrawledTimes.toString());
		}
		return resultsID;
	}

	/**
	 * read point from external h2db, update the lowerbounds and the upper
	 * bounds
	 * 
	 * @param dimension
	 */
	public void readFromExtenalDB(int dimension) {
		pois = Strategy.dbExternal.readFromExtenalDB(dimension, lowerBounds, upperBounds);
	}

	public List<Integer> searchNNWithoutDuplicate(Point queryPoint, int topK) {
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

	public List<Integer> searchNN(Point queryPoint, int topK) {
		List<Integer> list = null;
		try {
			double[] key = new double[Main.DIMENSION + 1];
			for (int i = 0; i < Main.DIMENSION; i++) {
				key[i] = queryPoint.getValueOfADimension(i);
			}
			key[Main.DIMENSION] = 0;
			list = (List<Integer>) kdtree.nearest(key, topK);
		} catch (KeySizeException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * pruning pois, and remove the >k points at the same location
	 * 
	 * @param overlapNum
	 */
	public void pruning(int overlapNum) {
		logger.info("pruning");
		// hashcode - top-k smallest ids
		HashMap<Integer, int[]> map = new HashMap<Integer, int[]>();
		Iterator it = pois.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			int id = (Integer) entry.getKey();
			Point p = (Point) entry.getValue();
			// if (logger.isDebugEnabled()) {
			// logger.debug("For point " + id + ": " + p.toString());
			// }
			int hash = p.hashCode();
			int[] gotValues = map.get(hash);
			if (gotValues == null) {
				int[] ids = new int[overlapNum];
				ids[0] = id;
				for (int i = 1; i < overlapNum; i++) {
					ids[i] = Integer.MAX_VALUE;
				}
				map.put(hash, ids);
				// if (logger.isDebugEnabled()) {
				// logger.debug("map put " + hash + " + " +
				// Utils.ArrayToString(ids));
				// }
			} else {
				// update top-k smallest ids
				for (int i = 0; i < overlapNum; i++) {
					int existId = gotValues[i];
					if (id < existId) {
						gotValues[i] = id;
						id = existId;
					}
				}
				map.remove(hash);
				map.put(hash, gotValues);
				// if (logger.isDebugEnabled()) {
				// logger.debug("map collision");
				// logger.debug("map put " + hash + " + "
				// + Utils.ArrayToString(gotValues));
				// }

			}
		}
		//
		HashMap<Integer, Point> newPois = new HashMap<Integer, Point>();
		Iterator it2 = map.entrySet().iterator();
		while (it2.hasNext()) {
			Entry entry = (Entry) it2.next();
			int[] ids = (int[]) entry.getValue();
			for (int i = 0; i < overlapNum; i++) {
				int id = ids[i];
				if (id < Integer.MAX_VALUE) {
					newPois.put(id, pois.get(id));
				}
			}
		}
		pois = newPois;
	}

}
