/**
 * 
 */
package mo.umac.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.infomatiq.jsi.Point;

/**
 * Implement the d-dimensional upper bound proof for n-dimensional space
 * 
 * @author kate
 * 
 */
public class ConcreteCrawler extends Strategy {

	public static Logger logger = Logger.getLogger(ConcreteCrawler.class.getName());

	public ConcreteCrawler() {
		super();
		logger.info("------------ConcreteCrawler------------");
	}

	@Override
	public void crawl(DSpace dSpace) {
		if (logger.isDebugEnabled()) {
			logger.info("------------crawling---------");
			logger.info(dSpace.toString());
		}

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
			if (logger.isDebugEnabled()) {
				logger.debug("crawling: " + dSpace.toString() + "; " + d + "; " + Utils.ArrayToString(fixedValues));
			}
			// specify a di-1 dimensional crawling problem
			ResultSetForADim resultsLowDim = crawlD(dSpace, d - 1, fixedValues);
			// deals with the result points
			results.addPoiIDs(resultsLowDim.getPoiIDs());
			int nearestPointID = nearestPoint(resultsLowDim, d, middle);
			double nearestPointInterval = resultsLowDim.getCrawledPointsInterval().get(nearestPointID);
			if (logger.isDebugEnabled()) {
				logger.debug("nearestPointID = " + nearestPointID);
				logger.debug("nearestPointInterval = " + nearestPointInterval);
			}

			int numDimension = dSpace.getNumDimension();
			double[] lowerBounds = dSpace.getLowerBounds();
			double[] upperBounds = dSpace.getUpperBounds();
			DSpace lowerSpace;
			DSpace upperSpace;
			//
			if (nearestPointInterval >= 0 && middle + nearestPointInterval < upper) {
				if (logger.isDebugEnabled()) {
					if (nearestPointInterval == 0) {
						logger.debug("nearestPointInterval = " + nearestPointInterval);
						logger.debug("nearestPointID = " + nearestPointID);
					}
					if (middle + nearestPointInterval == upper) {
						logger.debug("middle + nearestPointInterval == upper : " + upper);
					}
				}

				// the nearest point located at the right side of the middle
				double[] middleBounds = upperBounds.clone();
				middleBounds[d] = middle - nearestPointInterval;
				lowerSpace = new DSpace(numDimension, lowerBounds, middleBounds);
				ResultSetForADim lowerCrawledPoints = crawlD(lowerSpace, d, fixedValues);
				results.putAll(lowerCrawledPoints);
				//
				double[] nearestPointBounds = lowerBounds.clone();
				nearestPointBounds[d] = middle + nearestPointInterval;
				upperSpace = new DSpace(numDimension, nearestPointBounds, upperBounds);
				ResultSetForADim upperCrawledPoints = crawlD(upperSpace, d, fixedValues);
				results.putAll(upperCrawledPoints);
			} else if (nearestPointInterval < 0 && middle + nearestPointInterval > lower) {
				if (logger.isDebugEnabled()) {
					if (middle + nearestPointInterval == upper) {
						logger.debug("middle + nearestPointInterval == lower : " + lower);
					}
				}

				double[] nearestPointBounds = upperBounds.clone();
				nearestPointBounds[d] = middle + nearestPointInterval;
				lowerSpace = new DSpace(numDimension, lowerBounds, nearestPointBounds);
				ResultSetForADim lowerCrawledPoints = crawlD(lowerSpace, d, fixedValues);
				results.putAll(lowerCrawledPoints);
				//
				double[] middleBounds = lowerBounds.clone();
				middleBounds[d] = middle - nearestPointInterval;
				upperSpace = new DSpace(numDimension, middleBounds, upperBounds);
				ResultSetForADim upperCrawledPoints = crawlD(upperSpace, d, fixedValues);
				results.putAll(upperCrawledPoints);
			} else {
				if (nearestPointInterval == 0) {
					logger.debug("The nearest point is on the middle line!");
				}
			}
		}
		return results;
	}

	/**
	 * @param resultsLowDim
	 * @param di
	 * @param middle
	 * @return
	 */
	private int nearestPoint(ResultSetForADim resultsLowDim, int di, double middle) {
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
	private ResultSetForADim crawl1(DSpace dSpace, double lower, double upper, double[] fixedValues) {
		//
		ResultSetForADim resultSet = new ResultSetForADim();
		double middle = (lower + upper) / 2;
		fixedValues[0] = middle;

		if (logger.isDebugEnabled()) {
			logger.debug("crawling 0: [" + lower + ", " + upper + "]; " + Utils.ArrayToString(fixedValues));
		}

		Point queryPoint = new Point(dSpace.getNumDimension(), fixedValues);
		List<Integer> answer = query(queryPoint);
		// if (logger.isDebugEnabled()) {
		// logger.debug("answer: " + answer.toString());
		// }
		resultSet.addPoiIDs(answer);
		//
		double coveredRadius = CoveredRadius(dSpace, 0, queryPoint, answer, fixedValues);

		if (lower < middle - coveredRadius) {
			ResultSetForADim lowerResultSet = crawl1(dSpace, lower, middle - coveredRadius, fixedValues);
			resultSet.putAll(lowerResultSet);
		}
		if (middle + coveredRadius < upper) {
			ResultSetForADim upperResultSet = crawl1(dSpace, middle + coveredRadius, upper, fixedValues);
			resultSet.putAll(upperResultSet);
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
	private double CoveredRadius(DSpace dSpace, int d, Point queryPoint, List<Integer> answer, double[] fixedValues) {
		double radius = 0;
		int nearestPointId = 0;
		for (int i = 0; i < answer.size(); i++) {
			Integer poiID = answer.get(i);
			Point point = Strategy.dbInMemory.pois.get(poiID);
			// because the crawling algorithm on dimension 1 comes from the 2
			// dimensional space.
			if (d < Main.DIMENSION) {
				// compute the projection
				Point projection = projection(d, queryPoint, point, fixedValues);
				double distance = queryPoint.distance(projection);
				if (radius < distance) {
					nearestPointId = poiID;
					radius = distance;
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("point & coveredRadius: " + nearestPointId + ", " + radius);
		}
		return radius;
	}

	private Point projection(int d, Point query, Point result, double[] fixedValues) {
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
