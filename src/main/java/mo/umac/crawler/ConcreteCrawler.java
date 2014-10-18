/**
 * 
 */
package mo.umac.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.infomatiq.jsi.Point;

import edu.wlu.cs.levy.CG.KeySizeException;

/**
 * Implement the d-dimensional upper bound proof for n-dimensional space
 * 
 * @author kate
 */
public class ConcreteCrawler extends Strategy {

	public static Logger logger = Logger.getLogger(ConcreteCrawler.class
			.getName());

	public ConcreteCrawler() {
		super();
		logger.info("------------ConcreteCrawler------------");
	}

	@Override
	public void crawl(DSpace dSpace) {
		int numDimensions = dSpace.getNumDimension();
		double[] fixedValues = new double[numDimensions];
		crawlD(dSpace, numDimensions - 1, fixedValues);
	}

	/**
	 * @param dSpace
	 * @param d
	 * @param fixedValues
	 * @return every point's id & the distance to the d-1 subspace
	 */
	private ResultSetForADim crawlD(DSpace dSpace, int d, double[] fixedValues) {

		ResultSetForADim results = new ResultSetForADim();
		double lower = dSpace.getLowerBoundOfADimension(d);
		double upper = dSpace.getUpperBoundOfADimension(d);
		//
		if (d == 0) {
			// one dimensional crawling problem
			results = crawl1(dSpace, lower, upper, fixedValues);
		} else {
			double middle = (lower + upper) / 2;
			fixedValues[d] = middle;
			// specify a di-1 dimensional crawling problem
			ResultSetForADim resultsLowDim = crawlD(dSpace, d - 1, fixedValues);
			results.addPoiIDs(resultsLowDim.getPoiIDs());
			int nearestPointID = nearestPoint(resultsLowDim, d, middle);
			double nearestPointInterval = resultsLowDim
					.getCrawledPointsInterval().get(nearestPointID);
			int numDimension = dSpace.getNumDimension();
			double[] lowerBounds = dSpace.getLowerBounds();
			double[] upperBounds = dSpace.getUpperBounds();
			DSpace lowerSpace;
			DSpace upperSpace;
			//
			nearestPointInterval = Math.abs(nearestPointInterval);
			if (nearestPointInterval != 0) {
				if (middle - nearestPointInterval >= lower) {
					double[] middleBounds = upperBounds.clone();
					middleBounds[d] = middle - nearestPointInterval;
					lowerSpace = new DSpace(numDimension, lowerBounds,
							middleBounds);
					ResultSetForADim lowerCrawledPoints = crawlD(lowerSpace, d,
							fixedValues);
					results.putAll(lowerCrawledPoints);

				}

				if (middle + Math.abs(nearestPointInterval) <= upper) {
					double[] nearestPointBounds = lowerBounds.clone();
					nearestPointBounds[d] = middle + nearestPointInterval;
					upperSpace = new DSpace(numDimension, nearestPointBounds,
							upperBounds);
					ResultSetForADim upperCrawledPoints = crawlD(upperSpace, d,
							fixedValues);
					results.putAll(upperCrawledPoints);
				}

			} else {
				if (middle - Memory.EPSILON >= lower) {

					double[] middleBounds = upperBounds.clone();
					middleBounds[d] = middle - Memory.EPSILON;
					if (Math.ceil(middleBounds[d]) != Math.floor(middle)) {
						logger.error("nearestPointInterval == 0, "
								+ middleBounds[d] + ", " + middle);
					}
					lowerSpace = new DSpace(numDimension, lowerBounds,
							middleBounds);
					ResultSetForADim lowerCrawledPoints = crawlD(lowerSpace, d,
							fixedValues);
					results.putAll(lowerCrawledPoints);
				}
				//
				if (middle + Memory.EPSILON <= upper) {
					double[] nearestPointBounds = lowerBounds.clone();
					nearestPointBounds[d] = middle + Memory.EPSILON;
					if (Math.floor(nearestPointBounds[d]) != Math.ceil(middle)) {
						logger.error("nearestPointInterval == 0, "
								+ nearestPointBounds[d] + ", " + middle);
					}
					upperSpace = new DSpace(numDimension, nearestPointBounds,
							upperBounds);
					ResultSetForADim upperCrawledPoints = crawlD(upperSpace, d,
							fixedValues);
					results.putAll(upperCrawledPoints);
				}

			}
		}
		return results;
	}

	// private ResultSetForADim crawlD(DSpace dSpace, int d, double[]
	// fixedValues) {
	//
	// ResultSetForADim results = new ResultSetForADim();
	// double lower = dSpace.getLowerBoundOfADimension(d);
	// double upper = dSpace.getUpperBoundOfADimension(d);
	// //
	// if (d == 0) {
	// // one dimensional crawling problem
	// results = crawl1(dSpace, lower, upper, fixedValues);
	// } else {
	// double middle = (lower + upper) / 2;
	// fixedValues[d] = middle;
	// if (logger.isDebugEnabled()) {
	// logger.debug("crawling: " + dSpace.toString() + "; " + d + "; " +
	// Utils.ArrayToString(fixedValues));
	// }
	// // specify a di-1 dimensional crawling problem
	// ResultSetForADim resultsLowDim = crawlD(dSpace, d - 1, fixedValues);
	// // deals with the result points
	// // results.putAll(resultsLowDim);
	// results.addPoiIDs(resultsLowDim.getPoiIDs());
	// int nearestPointID = nearestPoint(resultsLowDim, d, middle);
	// double nearestPointInterval =
	// resultsLowDim.getCrawledPointsInterval().get(nearestPointID);
	// if (logger.isDebugEnabled()) {
	// logger.debug("nearestPointID = " + nearestPointID);
	// logger.debug("nearestPointInterval = " + nearestPointInterval);
	// }
	// // debug
	// if (logger.isDebugEnabled()) {
	// // print results
	// Set<Integer> alreadyCrawledResults = resultsLowDim.getPoiIDs();
	// logger.debug("alreadyCrawledResults' size = " +
	// alreadyCrawledResults.size());
	// logger.debug("alreadyCrawledResults: " +
	// alreadyCrawledResults.toString());
	// Iterator it = alreadyCrawledResults.iterator();
	// while (it.hasNext()) {
	// int id = (Integer) it.next();
	// Point p = Memory.pois.get(id);
	// logger.debug(id + ": " + Utils.ArrayToString(p.v));
	// }
	// pointsInsideNotCrawled(dSpace, middle, nearestPointInterval, d - 1,
	// fixedValues, results);
	// // end debug
	// }
	// int numDimension = dSpace.getNumDimension();
	// double[] lowerBounds = dSpace.getLowerBounds();
	// double[] upperBounds = dSpace.getUpperBounds();
	// DSpace lowerSpace;
	// DSpace upperSpace;
	// //
	// if (nearestPointInterval > 0 && middle + nearestPointInterval <= upper) {
	// if (logger.isDebugEnabled()) {
	// if (middle + nearestPointInterval == upper) {
	// logger.debug("middle + nearestPointInterval == upper : " + upper);
	// }
	// }
	//
	// // the nearest point located at the right side of the middle
	// double[] middleBounds = upperBounds.clone();
	// middleBounds[d] = middle - nearestPointInterval;
	// lowerSpace = new DSpace(numDimension, lowerBounds, middleBounds);
	// ResultSetForADim lowerCrawledPoints = crawlD(lowerSpace, d, fixedValues);
	// results.putAll(lowerCrawledPoints);
	// //
	// double[] nearestPointBounds = lowerBounds.clone();
	// nearestPointBounds[d] = middle + nearestPointInterval;
	// upperSpace = new DSpace(numDimension, nearestPointBounds, upperBounds);
	// ResultSetForADim upperCrawledPoints = crawlD(upperSpace, d, fixedValues);
	// results.putAll(upperCrawledPoints);
	// }
	// else if (nearestPointInterval < 0 && middle + nearestPointInterval >=
	// lower) {
	// if (logger.isDebugEnabled()) {
	// if (middle + nearestPointInterval == lower) {
	// logger.debug("middle + nearestPointInterval == lower : " + lower);
	// }
	// }
	//
	// double[] nearestPointBounds = upperBounds.clone();
	// nearestPointBounds[d] = middle + nearestPointInterval;
	// lowerSpace = new DSpace(numDimension, lowerBounds, nearestPointBounds);
	// ResultSetForADim lowerCrawledPoints = crawlD(lowerSpace, d, fixedValues);
	// results.putAll(lowerCrawledPoints);
	// //
	// double[] middleBounds = lowerBounds.clone();
	// middleBounds[d] = middle - nearestPointInterval;
	// upperSpace = new DSpace(numDimension, middleBounds, upperBounds);
	// ResultSetForADim upperCrawledPoints = crawlD(upperSpace, d, fixedValues);
	// results.putAll(upperCrawledPoints);
	// } else {
	// if (nearestPointInterval == 0) {
	// logger.debug("nearestPointInterval == 0");
	// // TODO
	// double[] middleBounds = upperBounds.clone();
	// middleBounds[d] = middle - Memory.EPSILON;
	// lowerSpace = new DSpace(numDimension, lowerBounds, middleBounds);
	// ResultSetForADim lowerCrawledPoints = crawlD(lowerSpace, d, fixedValues);
	// results.putAll(lowerCrawledPoints);
	// //
	// double[] nearestPointBounds = lowerBounds.clone();
	// nearestPointBounds[d] = middle + Memory.EPSILON;
	// upperSpace = new DSpace(numDimension, nearestPointBounds, upperBounds);
	// ResultSetForADim upperCrawledPoints = crawlD(upperSpace, d, fixedValues);
	// results.putAll(upperCrawledPoints);
	//
	// }
	// }
	// }
	// return results;
	// }

	private List<Integer> pointsInsideNotCrawled(DSpace dSpace, double middle,
			double nearestPointInterval, int d, double[] fixedValues,
			ResultSetForADim results) {
		List<Integer> pNotCrawled = new ArrayList<Integer>();
		double[] lowk = new double[Main.DIMENSION + 1];
		double[] uppk = new double[Main.DIMENSION + 1];
		for (int i = 0; i <= d; i++) {
			lowk[i] = dSpace.getLowerBoundOfADimension(i);
			uppk[i] = dSpace.getUpperBoundOfADimension(i);
		}
		// for the d+1 dimension: interval
		if (nearestPointInterval != 0) {
			lowk[d + 1] = middle - Math.abs(nearestPointInterval);
			uppk[d + 1] = middle + Math.abs(nearestPointInterval);
		} else {
			lowk[d + 1] = middle - Memory.EPSILON;
			uppk[d + 1] = middle + Memory.EPSILON;
		}

		// for the other dimension: fixedValue
		for (int i = d + 2; i < Main.DIMENSION; i++) {
			lowk[i] = fixedValues[i];
			uppk[i] = fixedValues[i];
		}
		// for the last dimension.
		lowk[Main.DIMENSION] = 0;
		uppk[Main.DIMENSION] = 1;
		// logger.info("covered region: " + Utils.ArrayToString(lowk) + " * " +
		// Utils.ArrayToString(uppk));
		if (logger.isDebugEnabled()) {
			logger.debug("covered region: " + Utils.ArrayToString(lowk) + " * "
					+ Utils.ArrayToString(uppk));
		}
		try {
			List<Integer> list = (List<Integer>) Memory.kdtree
					.range(lowk, uppk);
			Set<Integer> crawledPoints = results.getPoiIDs();
			for (int i = 0; i < list.size(); i++) {
				int id = list.get(i);
				if (!crawledPoints.contains(id)) {
					Point p = Memory.pois.get(id);
					double vd = p.getValueOfADimension(d + 1);
					if (vd != lowk[d + 1] && vd != uppk[d + 1]) {
						pNotCrawled.add(id);
					} else {
						logger.debug("pointsInsideNotCrawled but on the edge: "
								+ id + ": " + p.toString());
					}
				}
			}
			if (pNotCrawled.size() > 0) {
				logger.error("pointsInsideNotCrawled.size() = "
						+ pNotCrawled.size());
				logger.info("pointsInsideNotCrawled.size() = "
						+ pNotCrawled.size());
				logger.error("in covered region: " + Utils.ArrayToString(lowk)
						+ " * " + Utils.ArrayToString(uppk));
				logger.info("in covered region: " + Utils.ArrayToString(lowk)
						+ " * " + Utils.ArrayToString(uppk));
				for (int i = 0; i < pNotCrawled.size(); i++) {
					int id = pNotCrawled.get(i);
					Point p = Memory.pois.get(id);
					logger.error("pointsInsideNotCrawled: " + id + ": "
							+ p.toString());
					logger.info("pointsInsideNotCrawled: " + id + ": "
							+ p.toString());
				}
				//
				logger.error("crawledPoints are: ");
				logger.info("crawledPoints are: ");
				Iterator it = crawledPoints.iterator();
				while (it.hasNext()) {
					int id = (Integer) it.next();
					Point p = Memory.pois.get(id);
					logger.error("pointsInsideNotCrawled: " + id + ": "
							+ p.toString());
					logger.info("pointsInsideNotCrawled: " + id + ": "
							+ p.toString());
				}
			}

		} catch (KeySizeException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return pNotCrawled;
	}

	/**
	 * @param resultsLowDim
	 * @param di
	 * @param middle
	 * @return
	 */
	private int nearestPoint(ResultSetForADim resultsLowDim, int di,
			double middle) {
		Iterator<Integer> it = resultsLowDim.getPoiIDs().iterator();
		HashMap<Integer, Double> crawledPointsInterval = new HashMap<Integer, Double>();
		double minDistance = Double.MAX_VALUE;
		int minId = 0;
		while (it.hasNext()) {
			int id = (Integer) it.next();
			double vD = Memory.pois.get(id).getValueOfADimension(di);
			double distance = vD - middle;
			crawledPointsInterval.put(id, distance);
			if (Math.abs(distance) < minDistance) {
				minDistance = Math.abs(distance);
				minId = id;
			}
		}
		resultsLowDim.addCrawledPointsInterval(crawledPointsInterval);
		return minId;
	}

	/**
	 * Crawl 1 dimension
	 * 
	 * @param dSpace
	 * @param lower
	 * @param upper
	 * @param fixedValues
	 * @return the covered boundary of the next dimension
	 */
	private ResultSetForADim crawl1(DSpace dSpace, double lower, double upper,
			double[] fixedValues) {
		//
		ResultSetForADim resultSet = new ResultSetForADim();
		double middle = (lower + upper) / 2;
		fixedValues[0] = middle;

		if (logger.isDebugEnabled()) {
			logger.debug("crawling 0: [" + lower + ", " + upper + "]; "
					+ Utils.ArrayToString(fixedValues));
		}

		Point queryPoint = new Point(dSpace.getNumDimension(), fixedValues);
		List<Integer> answer = query(queryPoint);
		// if (logger.isDebugEnabled()) {
		// logger.debug("answer: " + answer.toString());
		// }
		resultSet.addPoiIDs(answer);
		//
		double coveredRadius = CoveredRadius(dSpace, 0, queryPoint, answer,
				fixedValues);
		//
		if (coveredRadius == 0) {
			if (middle - Memory.EPSILON >= lower) {
				ResultSetForADim lowerResultSet = crawl1(dSpace, lower, middle
						- Memory.EPSILON, fixedValues);
				resultSet.putAll(lowerResultSet);
				if (Math.ceil(middle - Memory.EPSILON) != Math.floor(middle)) {
					logger.error("nearestPointInterval == 0, "
							+ (middle - Memory.EPSILON) + ", " + middle);
				}
			}
			if (middle + Memory.EPSILON <= upper) {
				ResultSetForADim upperResultSet = crawl1(dSpace, middle
						+ Memory.EPSILON, upper, fixedValues);
				resultSet.putAll(upperResultSet);
				if (Math.floor(middle + Memory.EPSILON) != Math.ceil(middle)) {
					logger.error("nearestPointInterval == 0, "
							+ (middle + Memory.EPSILON) + ", " + middle);
				}
			}
		} else {

			if (lower <= middle - coveredRadius) {
				ResultSetForADim lowerResultSet = crawl1(dSpace, lower, middle
						- coveredRadius, fixedValues);
				resultSet.putAll(lowerResultSet);
			}
			if (middle + coveredRadius <= upper) {
				ResultSetForADim upperResultSet = crawl1(dSpace, middle
						+ coveredRadius, upper, fixedValues);
				resultSet.putAll(upperResultSet);
			}
		}

		return resultSet;
	}

	/**
	 * @param dSpace
	 * @param d
	 *            TODO
	 * @param queryPoint
	 *            TODO
	 * @param fixedValues
	 * @param resultSet
	 * @return the projection from d dimension to 1 dimension
	 */
	private double CoveredRadius(DSpace dSpace, int d, Point queryPoint,
			List<Integer> answer, double[] fixedValues) {
		double radius = -1;
		int nearestPointId = -1;
		for (int i = 0; i < answer.size(); i++) {
			Integer poiID = answer.get(i);
			Point point = Strategy.dbInMemory.pois.get(poiID);
			// because the crawling algorithm on dimension 1 comes from the 2
			// dimensional space.
			if (d < Main.DIMENSION) {
				// compute the projection
				Point projection = projection(d, queryPoint, point, fixedValues);
				double distance = queryPoint.distance(projection);
				// find the maximum distance
				if (radius < distance) {
					nearestPointId = poiID;
					radius = distance;
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("point & coveredRadius: " + nearestPointId + ", "
					+ radius);
		}
		return radius;
	}

	private Point projection(int d, Point query, Point result,
			double[] fixedValues) {
		int dimension = query.dimension;
		double[] v = new double[dimension];
		for (int i = 0; i <= d; i++) {
			v[i] = result.v[i];
		}
		for (int i = d + 1; i < dimension; i++) {
			v[i] = query.v[i];
		}
		Point projection = new Point(dimension, v);

		return projection;
	}

	private List<Integer> query(Point queryPoint) {
		return Strategy.dbInMemory.query(queryPoint);
	}

}
