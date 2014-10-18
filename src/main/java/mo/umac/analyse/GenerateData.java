package mo.umac.analyse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import mo.umac.crawler.H2DB;

import org.apache.log4j.xml.DOMConfigurator;

import paint.PaintShapes;
import paint.WindowUtilities;

import com.infomatiq.jsi.Point;

import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;

public class GenerateData {

	/********************************* testing d dimensional space ************************************/
	public String pointFile = "../data-experiment/synthetic/3d-skewed/points-skewed-3d-2500.txt";
	public String testSource = "../data-experiment/synthetic/3d-skewed/skewed-3d-2500";
	public String testTarget = "";
	public int dimension = 3;
	public int n = 2500;
	public int k = 100;
	public static double[] scope = { 1000, 1000, 1000, 1000, 1000 };

	/*************************************************************************************************/
	// String pointFile = "../crawler-data/glass-data/test-2d/points.txt";
	// String testSource = "../crawler-data/glass-data/test-2d/source";
	// String testTarget = "";
	// int dimension = 2;
	// int n = 50;
	// int k = 5;
	// double[] v = { 10, 10 };

	// String pointFile = "../crawler-data/glass-data/test-3d/points.txt";
	// String testSource = "../crawler-data/glass-data/test-3d/source";
	// String testTarget = "";
	// int dimension = 3;
	// int n = 100;
	// int k = 5;
	// double[] v = { 52, 29.5, 16.5 };

	// String pointFile = "../crawler-data/glass-data/test-4d/points.txt";
	// String testSource = "../crawler-data/glass-data/test-4d/source";
	// String testTarget = "";
	// int dimension = 4;
	// int n = 100;
	// int k = 5;
	// double[] v = { 52, 29.5, 16.5, 135 };

	// String pointFile = Main.DB_NAME_FILE;
	// String testSource = Main.DB_NAME_SOURCE;
	// String testTarget = "";
	// int dimension = 4;
	// int k = 1; // not n

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("src/main/resources/log4j.xml");
//		PaintShapes.painting = true;
//		if (PaintShapes.painting) {
//			WindowUtilities.openInJFrame(PaintShapes.paint, 1000, 1000);
//		}
		GenerateData test = new GenerateData();
		test.init();

		// test 1
		// test.testKNNQuery();
		H2DB.distroyConn();
	}

	public void init() {
		// HashMap<Integer, Point> points = generatePointsUniform(dimension, n);
		HashMap<Integer, Point> points = generatePointsSkewed(dimension, n);
		exportDataToFile(pointFile, points);
		H2DB db = new H2DB(testSource, testTarget);
		db.createTables(testSource);
		db.convertFileDBToH2DB(pointFile, testSource);
		// db.printItemTable(testSource);
		int c = db.count(testSource, "ITEM");
		System.out.println("c = " + c);
	}

	private void testKNNQuery() {
		H2DB db = new H2DB(testSource, testSource);
		HashMap<Integer, Point> points = importPoints(testSource);
		System.out.println("after import");
		// db.printItemTable(testSource);
		System.out.println("size = " + points.size());

		KDTree kdTree = new KDTree(dimension);
		index(kdTree, points);
		// rtree.print();
		//
		Point searchPoint = new Point(dimension, scope);

		List<Integer> results = searchNN(kdTree, searchPoint, k);

		System.out.println("---------------After KNN---------------");
		System.out.println("number of points returned = " + results.size());
		for (int i = 0; i < results.size(); i++) {
			int id = results.get(i);
			Point p = points.get(id);
			System.out.println(id + ":" + p.toString());
		}

	}

	public void index(KDTree kdtree, HashMap<Integer, Point> pois) {

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
				e.printStackTrace();
			}
		}
	}

	public List<Integer> searchNN(KDTree kdtree, Point queryPoint, int topK) {
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

	private HashMap<Integer, Point> importPoints(String dbNameSource) {
		H2DB db = new H2DB(dbNameSource, "");
		return db.readFromExtenalDB();
	}

	public HashMap<Integer, Point> generatePointsUniform(int dimension, int numPoint) {
		double[] v = new double[dimension];
		HashMap<Integer, Point> map = new HashMap<Integer, Point>();
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < numPoint; i++) {
			for (int j = 0; j < dimension; j++) {
				v[j] = random.nextDouble() * scope[j];
			}

			Point Point = new Point(dimension, v);
			map.put(i, Point);
		}
		return map;
	}

	public HashMap<Integer, Point> generatePointsSkewed(int dimension, int numPoint) {
		HashMap<Integer, Point> map = new HashMap<Integer, Point>();
		List<double[]> list = new ArrayList<double[]>();
		//
		double mean = 0.1;
		double lamda = 1 / mean;
		double[] minX = new double[dimension];
		double[] maxX = new double[dimension];
		//
		for (int i = 0; i < dimension; i++) {
			minX[i] = Double.MAX_VALUE;
			maxX[i] = Double.MIN_VALUE;
		}
		//
		Random random = new Random(System.currentTimeMillis());
		Random r2 = new Random(System.currentTimeMillis());
		//
		for (int i = 0; i < numPoint; i++) {
			double[] v = new double[dimension];
			for (int j = 0; j < dimension; j++) {
				double u1 = random.nextDouble();
				boolean sign1 = r2.nextBoolean();
				v[j] = (Math.log(1 - u1) / (-lamda));
				if (v[j] < minX[j]) {
					minX[j] = v[j];
				}
				if (v[j] > maxX[j]) {
					maxX[j] = v[j];
				}

				if (!sign1) {
					v[j] *= -1;
				}
			}
			list.add(v);

		}
		//
		for (int i = 0; i < list.size(); i++) {
			double[] v = list.get(i);
			for (int j = 0; j < dimension; j++) {
				v[j] = (500 + v[j] / (maxX[j] - minX[j]) * 500) % 1000;
			}
			Point point = new Point(dimension, v);
			map.put(i, point);
			// PaintShapes.paint.addPoint(point);
		}
		// debug: paint:
		// PaintShapes.paint.myRepaint();
		return map;
	}

	public void exportDataToFile(String fileName, HashMap<Integer, Point> points) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					fileName)));

			Iterator it = points.entrySet().iterator();
			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				int id = (Integer) entry.getKey();
				Point p = (Point) entry.getValue();
				bw.write(Integer.toString(id));
				bw.write(";");
				bw.write(p.toString());
				bw.newLine();
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
