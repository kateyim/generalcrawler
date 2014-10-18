package mo.umac.crawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.infomatiq.jsi.Point;

/**
 * Operators of the database
 * 
 * @author Kate
 */
public class H2DB {

	protected static Logger logger = Logger.getLogger(H2DB.class.getName());

	private String dbNameSource;
	private String dbNameTarget;

	// table names
	private final String QUERY = "QUERY";
	public final static String ITEM = "ITEM";
	private final String CATEGORY = "CATEGORY";
	private final String RELATIONSHIP = "RELATIONSHIP";

	public H2DB() {
		this.dbNameSource = Main.DB_NAME_SOURCE;
		this.dbNameTarget = Main.DB_NAME_TARGET;
	}

	public H2DB(String dbNameSource, String dbNameTarget) {
		this.dbNameSource = dbNameSource;
		this.dbNameTarget = dbNameTarget;
	}

	/****************************** sqls for deleting table ******************************/
	private String sqlDeleteQueryTable = "DROP TABLE IF EXISTS QUERY";
	private String sqlDeleteItemTable = "DROP TABLE IF EXISTS ITEM";
	private String sqlDeleteRelationshipTable = "DROP TABLE IF EXISTS RELATIONSHIP";

	/****************************** sqls for creating table ******************************/
	/**
	 * level: the divided level radius: the radius of the circle want to covered
	 * PRIMARY KEY
	 */
	private String sqlCreateQueryTable = "CREATE TABLE IF NOT EXISTS QUERY " + "(QUERYID INT, QUERY VARCHAR(100))";

	private String sqlCreateItemTable = "CREATE TABLE IF NOT EXISTS ITEM " + "(POINTID INT, VALUE VARCHAR(100), NUMCRAWLED INT)";

	private String sqlCreateItemTableKey = "CREATE TABLE IF NOT EXISTS ITEM " + "(POINTID INT PRIMARY KEY, VALUE VARCHAR(100), NUMCRAWLED INT)";

	/**
	 * This table records that the item is returned by which query in which
	 * position.
	 */
	private String sqlCreateRelationshipTable = "CREATE TABLE IF NOT EXISTS RELATIONSHIP " + "(POINTID INT, QEURYID INT, POSITION INT)";

	/****************************** sqls preparation for insertion ******************************/
	private String sqlPrepInsertQuery = "INSERT INTO QUERY (QUERYID, QUERY) VALUES (?,?)";

	private String sqlPrepInsertItem = "INSERT INTO ITEM (POINTID, VALUE, NUMCRAWLED) VALUES (?,?,?)";

	private String sqlPrepInsertRelationship = "INSERT INTO RELATIONSHIP (POINTID, QEURYID, POSITION) VALUES(?,?,?)";

	/****************************** sqls preparation for update ******************************/
	private String sqlPrepUpdateItem = "update item set NUMCRAWLED = ? where POINTID = ?";

	/**
	 * sql for select all data from a table. Need concatenate the table's names.
	 */
	public static String sqlSelectStar = "SELECT * FROM ";

	private String sqlSelectCountStar = "SELECT COUNT(*) FROM ";

	/****************************** sqls revome duplicate ******************************/
	private String s01 = "DROP TABLE IF EXISTS holdkey";
	private String s02 = "DROP TABLE IF EXISTS holdups";
	// table 1
	private String s1Query = "create table holdkey as SELECT QUERYID from QUERY GROUP BY QUERYID";
	private String s2Query = "create table holdups as SELECT DISTINCT QUERY.*  FROM QUERY, holdkey WHERE QUERY.QUERYID = holdkey.QUERYID";
	private String s3Query = "DELETE FROM QUERY";
	private String s4Query = "INSERT into QUERY SELECT * FROM holdups";
	// table 2
	private String s1Item = "create table holdkey as SELECT POINTID from ITEM GROUP BY POINTID";
	private String s2Item = "create table holdups as SELECT DISTINCT ITEM.*  FROM ITEM, holdkey WHERE ITEM.POINTID = holdkey.POINTID";
	private String s3Item = "DELETE FROM ITEM";
	private String s4Item = "INSERT into ITEM SELECT * FROM holdups";
	// table 4
	private String s1Relationship = "create table holdkey as SELECT POINTID, QEURYID from Relationship GROUP BY POINTID, QEURYID";
	private String s2Relationship = "create table holdups as SELECT DISTINCT Relationship.*  FROM Relationship, holdkey WHERE Relationship.POINTID = holdkey.POINTID and Relationship.QEURYID = holdkey.QEURYID";
	private String s4Relationship = "INSERT into Relationship SELECT * FROM holdups";

	/**
	 * String represents the name of the database;
	 */
	public static HashMap<String, java.sql.Connection> connMap = new HashMap<String, java.sql.Connection>();

	public void writeToExternalDB(int queryID, Point query, ResultSetForADim resultSet) {
		String dbName = dbNameTarget;
		Connection con = getConnection(dbName);
		// prepared statement
		PreparedStatement prepQuery;
		PreparedStatement prepItem;
		PreparedStatement prepRelationship;
		try {
			con.setAutoCommit(false);

			prepItem = con.prepareStatement(sqlPrepInsertItem);
			prepQuery = con.prepareStatement(sqlPrepInsertQuery);
			prepRelationship = con.prepareStatement(sqlPrepInsertRelationship);

			Set<Integer> poiIDs = resultSet.getPoiIDs();

			Iterator it = poiIDs.iterator();
			int i = 0;
			while (it.hasNext()) {
				int id = (Integer) it.next();
				Point point = Memory.pois.get(id);
				// table 2
				setPrepItem(point, prepItem);
				prepItem.addBatch();
				// table 4
				setPrepRelationship(id, queryID, i + 1, prepRelationship);
				i++;
				prepRelationship.addBatch();
			}
			// table 1
			setPrepQuery(queryID, query.toString(), prepQuery);
			prepQuery.addBatch();

			prepItem.executeBatch();
			prepRelationship.executeBatch();
			prepQuery.executeBatch();

			con.commit();
			con.setAutoCommit(true);
			prepItem.close();
			prepQuery.close();
			prepRelationship.close();
		} catch (SQLException e) {
			// FIXME yanhui comment "the Unique index or primary key violation"
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public void updataExternalDB() {
		String dbName = dbNameTarget;
		Connection con = getConnection(dbName);

		PreparedStatement prepItem;

		try {
			con.setAutoCommit(false);

			prepItem = con.prepareStatement(sqlPrepUpdateItem);
			Iterator it = Strategy.dbInMemory.poisCrawledTimes.entrySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				int poiID = (Integer) entry.getKey();
				int times = (Integer) entry.getValue();
				prepItem.setInt(1, times);
				prepItem.setInt(2, poiID);
				prepItem.addBatch();
				i++;
				if (i % 1000 == 0) {
					logger.info("updating " + i);
					prepItem.executeBatch();
				}

			}
			prepItem.executeBatch();
			con.commit();
			con.setAutoCommit(true);
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public int count(String dbName, String tableName) {
		int count = 0;
		String sql = sqlSelectCountStar + tableName;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat.executeQuery(sql);
				while (rs.next()) {

					count = rs.getInt(1);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * Exam whether data has been successfully inserted to the database
	 */
	public void examData(String dbName) {
		// print
		// printQueryTable(dbName);
		// printItemTable(dbName);
		// printCategoryTable(dbName);
		// printRelationshipTable(dbName);

		// count
		int c1 = count(dbName, QUERY);
		System.out.println("count QUERY = " + c1);
		int c2 = count(dbName, ITEM);
		System.out.println("count ITEM = " + c2);
		int c4 = count(dbName, RELATIONSHIP);
		System.out.println("count RELATIONSHIP = " + c4);
	}

	/****************************** Printing tables ******************************/

	private void printQueryTable(String dbName) {
		String sqlSelectQuery = sqlSelectStar + QUERY;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat.executeQuery(sqlSelectQuery);
				while (rs.next()) {

					int queryID = rs.getInt(1);
					String query = rs.getString(2);

					// print query result to console
					System.out.println("queryID: " + queryID);
					System.out.println("query: " + query);
					System.out.println("--------------------------");
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void printItemTable(String dbName) {
		String sqlSelectItem = sqlSelectStar + ITEM;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat.executeQuery(sqlSelectItem);
				while (rs.next()) {

					int pointID = rs.getInt(1);
					String value = rs.getString(2);
					int numCrawler = rs.getInt(3);

					// print query result to console
					System.out.println("pointID: " + pointID);
					System.out.println("value: " + value);
					System.out.println("numCrawler: " + numCrawler);
					System.out.println("--------------------------");
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void printRelationshipTable(String dbName) {
		String sqlSelectRelationship = sqlSelectStar + RELATIONSHIP;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat.executeQuery(sqlSelectRelationship);
				while (rs.next()) {

					int pointID = rs.getInt(1);
					int queryID = rs.getInt(2);
					int position = rs.getInt(3);

					// print query result to console
					System.out.println("pointID: " + pointID);
					System.out.println("queryID: " + queryID);
					System.out.println("position: " + position);
					System.out.println("--------------------------");
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/****************************** Insert values ******************************/

	private PreparedStatement setPrepQuery(int queryID, String query, PreparedStatement prepQuery) {
		try {
			prepQuery.setInt(1, queryID);
			prepQuery.setString(2, query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prepQuery;
	}

	private PreparedStatement setPrepItem(Point point, PreparedStatement prepItem) {
		try {
			prepItem.setInt(1, point.id);
			prepItem.setString(2, point.toString());
			prepItem.setDouble(11, point.numCrawled);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prepItem;
	}

	private PreparedStatement setPrepRelationship(int resultID, int queryID, int position, PreparedStatement prepRelationship) {
		try {
			prepRelationship.setInt(1, resultID);
			prepRelationship.setInt(2, queryID);
			prepRelationship.setInt(3, position);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prepRelationship;
	}

	public void createTables(String dbName) {
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			// XXX delete tables before creating
			stat.execute(sqlDeleteQueryTable);
			stat.execute(sqlDeleteItemTable);
			stat.execute(sqlDeleteRelationshipTable);
			//
			stat.execute(sqlCreateQueryTable);
			stat.execute(sqlCreateItemTable);
			stat.execute(sqlCreateRelationshipTable);
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection(String dbname) {
		if (connMap.get(dbname) == null) {
			try {
				Class.forName("org.h2.Driver");
				java.sql.Connection conn = DriverManager.getConnection("jdbc:h2:file:" + dbname + ";MVCC=true;LOCK_TIMEOUT=3000000;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE", "sa", "");
				connMap.put(dbname, conn);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return (Connection) connMap.get(dbname);
	}

	/**
	 * Transfer the plain text dataset to the h2 dataset
	 * 
	 * @param folderPath
	 * @param h2Name
	 *            : not in use
	 */
	public void convertFileDBToH2DB(String folderPath, String h2Name) {
		HashMap<Integer, Point> pois = readFromFile(folderPath);
		convertPointFile(pois, h2Name);
	}

	/**
	 * Read from file: dbNameSource
	 */
	public static HashMap<Integer, Point> readFromFile(String fileName) {
		BufferedReader br;
		Memory.pois = new HashMap<Integer, Point>();
		try {
			br = new BufferedReader(new FileReader(fileName));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] split = sCurrentLine.split(";");
				int id = Integer.parseInt(split[0]);
				double[] v = new double[split.length - 1];
				for (int i = 1; i < split.length; i++) {
					v[i - 1] = Double.parseDouble(split[i]);
				}
				Point p = new Point(split.length - 1, v);
				// if (Memory.pois.get(id) != null ) {
				// System.out.println(id);
				// }
				Memory.pois.put(id, p);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Memory.pois;
	}

	private void convertPointFile(HashMap<Integer, Point> pois, String h2Name) {
		try {
			Connection conn = getConnection(h2Name);
			PreparedStatement prepQuery = conn.prepareStatement(sqlPrepInsertItem);
			Iterator it = pois.entrySet().iterator();
			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				int id = (Integer) entry.getKey();
				Point point = (Point) entry.getValue();
				setPrepQuery(id, point.toString(), prepQuery);
				prepQuery.addBatch();
			}

			prepQuery.executeBatch();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public HashMap<Integer, Point> readFromExtenalDB() {
		HashMap<Integer, Point> pois = new HashMap<Integer, Point>();
		try {
			Connection conn = getConnection(dbNameSource);
			Statement stat = conn.createStatement();

			// because the database has already been prunned before
			String sqlSelectItem = sqlSelectStar + ITEM;
			try {
				java.sql.ResultSet rs = stat.executeQuery(sqlSelectItem);
				while (rs.next()) {

					int pointID = rs.getInt(1);
					String values = rs.getString(2);
					int numCrawled = rs.getInt(3);
					//
					String[] split = values.split(";");
					int dimension = split.length;
					double[] value = new double[dimension];

					for (int i = 0; i < dimension; i++) {
						value[i] = Double.parseDouble(split[i]);
					}

					Point poi = new Point(pointID, dimension, value, numCrawled);
					pois.put(pointID, poi);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pois;
	}

	/**
	 * read point from external h2db, update the lower bounds and the upper
	 * bounds
	 * 
	 * @param dimension
	 * @param lowerBounds
	 * @param upperBounds
	 * @return
	 */
	public HashMap<Integer, Point> readFromExtenalDB(int dimension, double[] lowerBounds, double[] upperBounds) {
		HashMap<Integer, Point> pois = new HashMap<Integer, Point>();
		for (int i = 0; i < dimension; i++) {
			lowerBounds[i] = Double.MAX_VALUE;
			upperBounds[i] = -Double.MAX_VALUE;
		}
		try {
			Connection conn = getConnection(dbNameSource);
			Statement stat = conn.createStatement();

			// because the database has already been prunned before
			String sqlSelectItem = sqlSelectStar + ITEM;
			try {
				java.sql.ResultSet rs = stat.executeQuery(sqlSelectItem);
				while (rs.next()) {

					int pointID = rs.getInt(1);
					String values = rs.getString(2);
					int numCrawled = rs.getInt(3);
					//
					String[] split = values.split(";");
					double[] value = new double[dimension];

					for (int i = 0; i < dimension; i++) {
						value[i] = Double.parseDouble(split[i]);
						if (value[i] < lowerBounds[i]) {
							lowerBounds[i] = value[i];
						}
						if (value[i] > upperBounds[i]) {
							upperBounds[i] = value[i];
						}
					}
					Point poi = new Point(pointID, dimension, value, numCrawled);
					pois.put(pointID, poi);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pois;
	}

	public void prun() {
		String sql = "SELECT * FROM item";
		try {
			Connection conSrc = getConnection(dbNameSource);
			Statement statSrc = conSrc.createStatement();

			Connection conTarget = getConnection(dbNameTarget);
			conTarget.setAutoCommit(false);
			Statement statTarget = conTarget.createStatement();
			// create tables
			statTarget.execute(sqlCreateItemTableKey);

			PreparedStatement prepItem = conTarget.prepareStatement(sqlPrepInsertItem);

			java.sql.ResultSet rs = statSrc.executeQuery(sql);
			while (rs.next()) {

				int pointID = rs.getInt(1);
				String values = rs.getString(2);
				int numCrawled = rs.getInt(3);

				// write
				prepItem.setInt(1, pointID);
				prepItem.setString(2, values);
				prepItem.setDouble(3, numCrawled);
				prepItem.addBatch();

			}
			rs.close();
			statSrc.close();

			prepItem.executeBatch();
			conTarget.commit();
			conTarget.setAutoCommit(true);
			statTarget.close();
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void prunDuplicate(String dbName, String duplicateTableName, String targetTableName) {
		// FIXME
		String sql = "SELECT * FROM item where itemid in (select distinct POINTID from item)";
		try {
			Connection conSrc = getConnection(dbNameSource);
			Statement statSrc = conSrc.createStatement();

			Connection conTarget = getConnection(dbNameTarget);
			conTarget.setAutoCommit(false);
			Statement statTarget = conTarget.createStatement();
			// create tables
			statTarget.execute(sqlCreateItemTableKey);

			PreparedStatement prepItem = conTarget.prepareStatement(sqlPrepInsertItem);

			java.sql.ResultSet rs = statSrc.executeQuery(sql);
			while (rs.next()) {

				int pointID = rs.getInt(1);
				String values = rs.getString(2);
				int numCrawled = rs.getInt(3);

				// write
				prepItem.setInt(1, pointID);
				prepItem.setString(2, values);
				prepItem.setDouble(3, numCrawled);
				prepItem.addBatch();

			}
			rs.close();
			statSrc.close();

			prepItem.executeBatch();
			conTarget.commit();
			conTarget.setAutoCommit(true);
			statTarget.close();
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int numCrawlerPoints() {
		int c = count(dbNameTarget, ITEM);
		return c;
	}

	/**
	 * Convert H2DB to a files, only contain id, numCrawl, coordinates from the
	 * table
	 * 
	 * @param dbName
	 * @param tableName
	 * @param fileName
	 */
	// public void extractValuesFromItemTable(String dbName, String tableName,
	// String fileName) {
	// // delete the duplicate
	// Map idMap = new HashMap<Integer, Integer>();
	//
	// List<Integer> idList = new ArrayList<Integer>();
	// List<Double> latList = new ArrayList<Double>();
	// List<Double> longList = new ArrayList<Double>();
	// List<Integer> numCrawledList = new ArrayList<Integer>();
	//
	// try {
	// Connection conn = getConnection(dbNameSource);
	// Statement stat = conn.createStatement();
	// String sql = "SELECT POINTID, LATITUDE, LONGITUDE, NUMCRAWLED FROM item";
	// try {
	// java.sql.ResultSet rs = stat.executeQuery(sql);
	// while (rs.next()) {
	// int pointID = rs.getInt(1);
	// double latitude = rs.getDouble(2);
	// double longitude = rs.getDouble(3);
	// int numCrawled = rs.getInt(4);
	// //
	// if (!idMap.containsKey(pointID)) {
	// idMap.put(pointID, 0);
	//
	// idList.add(pointID);
	// latList.add(latitude);
	// longList.add(longitude);
	// numCrawledList.add(numCrawled);
	// }
	// }
	// rs.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// stat.close();
	// // conn.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// // writing to the file
	// File file = new File(fileName);
	// try {
	// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new
	// FileOutputStream(file, true)));
	// int n = idList.size();
	// for (int i = 0; i < n; i++) {
	// int id = idList.get(i);
	// double latitude = latList.get(i);
	// double longitude = longList.get(i);
	// int numCrawled = numCrawledList.get(i);
	// bw.write(Integer.toString(id));
	// bw.write(";");
	// bw.write(Integer.toString(numCrawled));
	// bw.write(";");
	// bw.write(Double.toString(longitude));
	// bw.write(";");
	// bw.write(Double.toString(latitude));
	// bw.newLine();
	// }
	// bw.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public void removeDuplicate() {

		try {
			Connection conTarget = getConnection(dbNameTarget);
			Statement statTarget = conTarget.createStatement();
			// table 1: query table
			statTarget.execute(s01);
			statTarget.execute(s02);
			statTarget.execute(s1Query);
			statTarget.execute(s2Query);
			statTarget.execute(s3Query);
			statTarget.execute(s4Query);
			// table 2: item table
			// create tables
			statTarget.execute(s01);
			statTarget.execute(s02);
			statTarget.execute(s1Item);
			statTarget.execute(s2Item);
			statTarget.execute(s3Item);
			statTarget.execute(s4Item);
			// table 4: relationship table
			statTarget.execute(s01);
			statTarget.execute(s02);
			statTarget.execute(s1Relationship);
			statTarget.execute(s2Relationship);
			statTarget.execute(s4Relationship);
			//
			statTarget.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
     * 
     */
	public static void distroyConn() {
		Iterator it = connMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			java.sql.Connection conn = (java.sql.Connection) entry.getValue();
			try {
				conn.commit();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Converting the existing h2 database to fit the format used in genernal
	 * crawler in d dimensional space.
	 * 
	 * @param oldH2
	 * @param newH2
	 */
	public void convertH2ForDn(String oldH2, String newH2) {
		HashMap<Integer, Point> pois = readFromExtenalDB(oldH2);
		convertPointFile(pois, newH2);

	}

	/**
	 * revised based on H2DB from project crawler
	 * 
	 * @param categoryQ
	 * @param stateQ
	 * @return
	 */
	private HashMap<Integer, Point> readFromExtenalDB(String dbNameSource) {
		HashMap<Integer, Point> map = new HashMap<Integer, Point>();
		try {
			Connection conn = getConnection(dbNameSource);
			Statement stat = conn.createStatement();
			String sql = "select * from item";
			try {
				java.sql.ResultSet rs = stat.executeQuery(sql);
				while (rs.next()) {
					int id = rs.getInt(1);
					double latitude = rs.getDouble(5);
					double longitude = rs.getDouble(6);
					int numCrawled = rs.getInt(11);
					//
					double[] v = { longitude, latitude };
					Point poi = new Point(id, 2, v, numCrawled);
					map.put(id, poi);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public void sample(int factor) {
		String sql = sqlSelectStar + ITEM;
		try {
			Connection conSrc = getConnection(dbNameSource);
			Statement statSrc = conSrc.createStatement();

			Connection conTarget = getConnection(dbNameTarget);
			conTarget.setAutoCommit(false);
			Statement statTarget = conTarget.createStatement();
			// create tables
			statTarget.execute(sqlCreateItemTableKey);

			PreparedStatement prepItem = conTarget
					.prepareStatement(sqlPrepInsertItem);

			java.sql.ResultSet rs = statSrc.executeQuery(sql);
			while (rs.next()) {

				int pointID = rs.getInt(1);
				String values = rs.getString(2);
				int numCrawled = rs.getInt(3);

				

				Random random = new Random(System.currentTimeMillis());
				int r = random.nextInt();
				if (r % factor <= factor - 2) {
					// write
					prepItem.setInt(1, pointID);
					prepItem.setString(2, values);
					prepItem.setDouble(3, numCrawled);
					prepItem.addBatch();
				}

			}
			rs.close();
			statSrc.close();

			prepItem.executeBatch();
			conTarget.commit();
			conTarget.setAutoCommit(true);
			statTarget.close();
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void sample(int divisor, int divident) {
		String sql = sqlSelectStar + ITEM;
//		int index = -1;
		try {
			Connection conSrc = getConnection(dbNameSource);
			Statement statSrc = conSrc.createStatement();

			Connection conTarget = getConnection(dbNameTarget);
			conTarget.setAutoCommit(false);
			Statement statTarget = conTarget.createStatement();
			// create tables
			statTarget.execute(sqlCreateItemTableKey);

			PreparedStatement prepItem = conTarget
					.prepareStatement(sqlPrepInsertItem);

			java.sql.ResultSet rs = statSrc.executeQuery(sql);
			Random random = new Random(System.currentTimeMillis());
			while (rs.next()) {

				int pointID = rs.getInt(1);
				String values = rs.getString(2);
				int numCrawled = rs.getInt(3);

				int r = Math.abs(random.nextInt());

//				index++;
				if (r % divisor < divident) {
					// write
					prepItem.setInt(1, pointID);
					prepItem.setString(2, values);
					prepItem.setDouble(3, numCrawled);
					prepItem.addBatch();
				}

			}
			rs.close();
			statSrc.close();

			prepItem.executeBatch();
			conTarget.commit();
			conTarget.setAutoCommit(true);
			statTarget.close();
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
