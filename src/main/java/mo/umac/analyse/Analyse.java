package mo.umac.analyse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Analyse {

	public static final String NUM_QUERY = "countNumQueries = ";
	public static final String NUM_POINT = "countCrawledPoints = ";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Analyse a = new Analyse();
		// String fileName = "results/info.log";
		// String queryFile = "results/k=270.query";
		// String pointFile = "results/k=270.point";

		String fileName = "../data-experiment/info.log";
		String queryFile = "../data-experiment/info.query";
		String pointFile = "../data-experiment/info.point";
//		String queryFile2 = "../data-experiment/info2.query";
//		String pointFile2 = "../data-experiment/info2.point";
		a.readLog(fileName, queryFile, pointFile);
		// a.readLog2(fileName, queryFile2, pointFile2);

	}

	public void readLog(String fileName, String queryFile, String pointFile) {
		ArrayList<String> queryList = new ArrayList<String>();
		ArrayList<String> pointList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String data = null;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				if (data.contains(NUM_QUERY)) {
					int index = data.indexOf("=");
					String query = data.substring(index + 2, data.length());
					queryList.add(query);
				} else if (data.contains(NUM_POINT)) {
					int index = data.indexOf("=");
					String point = data.substring(index + 2, data.length());
					pointList.add(point);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// print query
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryFile, true)));
			for (int i = 0; i < queryList.size(); i++) {
				bw.write(queryList.get(i));
				bw.newLine();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// print point
		bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pointFile, true)));
			for (int i = 0; i < pointList.size(); i++) {
				bw.write(pointList.get(i));
				bw.newLine();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
