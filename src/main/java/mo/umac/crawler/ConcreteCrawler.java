/**
 * 
 */
package mo.umac.crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	public static Logger logger = Logger.getLogger(ConcreteCrawler.class.getName());

	public static double epsilon = 1e-6;

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
//		logger.info(d + "-dimension" + ", " + dSpaceToString(dSpace) + "---" + doubleArrayToString(fixedValues));
		ResultSetForADim results = new ResultSetForADim();
		double lower = dSpace.getLowerBoundOfADimension(d);
		double upper = dSpace.getUpperBoundOfADimension(d);
		//
		if (d == 0) {
			// one dimensional crawling problem
			results = crawl1(dSpace, lower, upper, fixedValues);
		} else if (d == 1) {
			// two dimensional crawling problem
			results = crawl2(dSpace, lower, upper, fixedValues);
		} else {
			double middle = (lower + upper) / 2;
			fixedValues[d] = middle;
			// specify a di-1 dimensional crawling problem
			ResultSetForADim resultsLowDim = crawlD(dSpace, d - 1, fixedValues);
			results.addPoiIDs(resultsLowDim.getPoiIDs());
			int nearestPointID = nearestPoint(resultsLowDim, d, middle);
			double nearestPointInterval = resultsLowDim.getCrawledPointsInterval().get(nearestPointID);
			nearestPointInterval = Math.abs(nearestPointInterval);

			if (nearestPointInterval > (upper - lower) / 2 || Math.abs(nearestPointInterval - (upper - lower) / 2) <= epsilon) {
				return results;
			}

			int numDimension = dSpace.getNumDimension();
			double[] lowerBounds = dSpace.getLowerBounds();
			double[] upperBounds = dSpace.getUpperBounds();
			DSpace lowerSpace;
			DSpace upperSpace;
			//
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
		}
		return results;
	}

	private List<Integer> pointsInsideNotCrawled(DSpace dSpace, double middle, double nearestPointInterval, int d, double[] fixedValues,
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
			logger.debug("covered region: " + Utils.ArrayToString(lowk) + " * " + Utils.ArrayToString(uppk));
		}
		try {
			List<Integer> list = (List<Integer>) Memory.kdtree.range(lowk, uppk);
			Set<Integer> crawledPoints = results.getPoiIDs();
			for (int i = 0; i < list.size(); i++) {
				int id = list.get(i);
				if (!crawledPoints.contains(id)) {
					Point p = Memory.pois.get(id);
					double vd = p.getValueOfADimension(d + 1);
					if (vd != lowk[d + 1] && vd != uppk[d + 1]) {
						pNotCrawled.add(id);
					} else {
						logger.debug("pointsInsideNotCrawled but on the edge: " + id + ": " + p.toString());
					}
				}
			}
			if (pNotCrawled.size() > 0) {
				logger.error("pointsInsideNotCrawled.size() = " + pNotCrawled.size());
				logger.info("pointsInsideNotCrawled.size() = " + pNotCrawled.size());
				logger.error("in covered region: " + Utils.ArrayToString(lowk) + " * " + Utils.ArrayToString(uppk));
				logger.info("in covered region: " + Utils.ArrayToString(lowk) + " * " + Utils.ArrayToString(uppk));
				for (int i = 0; i < pNotCrawled.size(); i++) {
					int id = pNotCrawled.get(i);
					Point p = Memory.pois.get(id);
					logger.error("pointsInsideNotCrawled: " + id + ": " + p.toString());
					logger.info("pointsInsideNotCrawled: " + id + ": " + p.toString());
				}
				//
				logger.error("crawledPoints are: ");
				logger.info("crawledPoints are: ");
				Iterator it = crawledPoints.iterator();
				while (it.hasNext()) {
					int id = (Integer) it.next();
					Point p = Memory.pois.get(id);
					logger.error("pointsInsideNotCrawled: " + id + ": " + p.toString());
					logger.info("pointsInsideNotCrawled: " + id + ": " + p.toString());
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
//		logger.info("crawl0 " + dSpaceToString(dSpace) + "---" + ", [" + lower + ", " + upper + "]" + doubleArrayToString(fixedValues));
		//
		ResultSetForADim resultSet = new ResultSetForADim();
		double middle = (lower + upper) / 2;
		fixedValues[0] = middle;
		//
		Point queryPoint = new Point(dSpace.getNumDimension(), fixedValues);
		List<Integer> answer = query(queryPoint);
		resultSet.addPoiIDs(answer);
		//
		if(dSpace.getNumDimension() != 1){
			double radiusD2 = radiusD2(answer, queryPoint, fixedValues);
			Circle aCircle = new Circle(queryPoint, radiusD2);
			resultSet.addACircle(aCircle);
		}
		//
		double radiusD1 = radiusD1(dSpace, 0, queryPoint, answer, fixedValues);
		//
		if (radiusD1 > (upper - lower) / 2 || Math.abs(radiusD1 - (upper - lower) / 2) <= epsilon) {
			return resultSet;
		}
		//
		ResultSetForADim lowerResultSet = crawl1(dSpace, lower, middle - radiusD1, fixedValues);
		resultSet.putAllCircles(lowerResultSet);
		resultSet.putAll(lowerResultSet);
		//
		ResultSetForADim upperResultSet = crawl1(dSpace, middle + radiusD1, upper, fixedValues);
		resultSet.putAllCircles(upperResultSet);
		resultSet.putAll(upperResultSet);

		return resultSet;
	}

	private ResultSetForADim crawl2(DSpace dSpace, double lower, double upper, double[] fixedValues) {
//		logger.info("crawl1 " + dSpaceToString(dSpace) + "---" + ", [" + lower + ", " + upper + "]" + doubleArrayToString(fixedValues));

		ResultSetForADim results = new ResultSetForADim();
		// for the 2-D space
		int d = 1;
		double middle = (lower + upper) / 2;

		fixedValues[d] = middle;
		// specify the 1-D crawling problem
		ResultSetForADim resultsLowDim = crawlD(dSpace, 0, fixedValues);
		results.addPoiIDs(resultsLowDim.getPoiIDs());
		sortingCircles(resultsLowDim, fixedValues);
		double intervalForD2 = distanceCovered(dSpace, resultsLowDim, fixedValues);

		if (intervalForD2 >= (upper - lower) / 2 || Math.abs(intervalForD2 - (upper - lower) / 2) <= epsilon) {
			return results;
		}

		int numDimension = dSpace.getNumDimension();
		double[] lowerBounds = dSpace.getLowerBounds();
		double[] upperBounds = dSpace.getUpperBounds();
		DSpace lowerSpace;
		DSpace upperSpace;
		// left
		double[] middleBounds = upperBounds.clone();
		middleBounds[d] = middle - intervalForD2;
		lowerSpace = new DSpace(numDimension, lowerBounds, middleBounds);
		ResultSetForADim lowerCrawledPoints = crawlD(lowerSpace, d, fixedValues);
		results.putAll(lowerCrawledPoints);

		// right
		double[] nearestPointBounds = lowerBounds.clone();
		nearestPointBounds[d] = middle + intervalForD2;
		upperSpace = new DSpace(numDimension, nearestPointBounds, upperBounds);
		ResultSetForADim upperCrawledPoints = crawlD(upperSpace, d, fixedValues);
		results.putAll(upperCrawledPoints);
		//
		return results;
	}

	private double radiusD2(List<Integer> answer, Point queryPoint, double[] fixedValues) {
		int d = 1;
		double radius = -1;
		for (int i = 0; i < answer.size(); i++) {
			Integer poiID = answer.get(i);
			Point point = Strategy.dbInMemory.pois.get(poiID);
			// compute the projection
			Point projectionOnD2 = projection(d, queryPoint, point, fixedValues);
			double distance = queryPoint.distance(projectionOnD2);
			// find the maximum distance
			if (radius < distance) {
				radius = distance;
			}
		}
		return radius;
	}

	/**
	 * sorting these circles according to the y-value
	 * 
	 * @param oneDimensionalResultSet
	 */
	private void sortingCircles(ResultSetForADim resultsLowDim, double[] fixedValues) {
		// if (logger.isDebugEnabled()) {
		// // print
		// logger.debug("before sorting the circles: ");
		// for (int i = 0; i < resultsLowDim.getCircles().size(); i++) {
		// Circle circle = resultsLowDim.getCircles().get(i);
		// logger.debug(circle.getCenter().toString());
		// }
		// }
		// sort all circles in the middle line
		Collections.sort(resultsLowDim.getCircles(), new CircleComparable());
		// if (logger.isDebugEnabled()) {
		// // print sorting results
		// logger.debug("After sorting the circles: ");
		// for (int i = 0; i < resultsLowDim.getCircles().size(); i++) {
		// Circle circle = resultsLowDim.getCircles().get(i);
		// logger.debug(circle.getCenter().toString());
		// }
		// }

	}

	public class CircleComparable implements Comparator<Circle> {
		@Override
		public int compare(Circle circle1, Circle circle2) {
			Point center1 = circle1.getCenter();
			Point center2 = circle2.getCenter();
			return center1.compareTo(center2);
		}
	}

	/**
	 * Compute the farthest distance (in the x-axis) of the covered region of
	 * the one dimensional results
	 * 
	 * @return
	 */
	private double distanceCovered(DSpace dSpace, ResultSetForADim resultsLowDim, double[] fixedValues) {
		List<Circle> circleList = resultsLowDim.getCircles();
		if (circleList == null) {
			logger.error(circleList == null);
		}
		double maxX = dSpace.getUpperBoundOfADimension(1);
		double minX = dSpace.getLowerBoundOfADimension(1);
		double maxY = dSpace.getUpperBoundOfADimension(0);
		double minY = dSpace.getLowerBoundOfADimension(0);
		// It doesn't matter if we set a bigger distance initially. Mainly for the left and the right line segment
		double minDistance = maxX - minX;
		// intersect with the boarder line of the envelope
		Circle c1 = circleList.get(0);
		double r1 = c1.getRadius();
		double y1 = c1.getCenter().getValueOfADimension(0);
		double distanceX;
		double y;
		// intersection of the circle and the boarder line
		distanceX = Math.sqrt((r1 * r1 - (minY - y1) * (minY - y1)));
		if (distanceX < minDistance) {
			minDistance = distanceX;
		}
		// intersection of circles
		for (int i = 1; i < circleList.size(); i++) {
			Circle c2 = circleList.get(i);
			double r2 = c2.getRadius();
			Point o2 = c2.getCenter();
			double y2 = o2.getValueOfADimension(0);
			;
			// the intersect points
			y = (r1 * r1 - r2 * r2 - (y1 * y1 - y2 * y2)) / (2 * (y2 - y1));
			distanceX = Math.sqrt(r1 * r1 - (y - y1) * (y - y1));
			if (distanceX < minDistance) {
				minDistance = distanceX;
			}
			c1 = c2;
			r1 = c1.getRadius();
			y1 = c1.getCenter().getValueOfADimension(0);
		}
		// intersection of the circle and the boarder line
		distanceX = Math.sqrt((r1 * r1 - (maxY - y1) * (maxY - y1)));
		if (distanceX < minDistance) {
			minDistance = distanceX;
		}

		return minDistance;
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
	private double radiusD1(DSpace dSpace, int d, Point queryPoint, List<Integer> answer, double[] fixedValues) {
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

	public static String doubleArrayToString(double[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			sb.append(",");
		}
		return sb.toString();
	}

	public static String dSpaceToString(DSpace dSpace) {
		StringBuffer sb = new StringBuffer();
		int d = dSpace.getNumDimension();
		for (int i = 0; i < d; i++) {
			sb.append("[");
			sb.append(dSpace.getLowerBoundOfADimension(i));
			sb.append(", ");
			sb.append(dSpace.getUpperBoundOfADimension(i));
			sb.append("]");
		}
		return sb.toString();
	}

}
