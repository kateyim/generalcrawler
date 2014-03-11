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
//		String fileName = "results/k=1";
//		String outputFile = "results/k=1.output";
		String fileName = "results/k=10";
		String outputFile = "results/k=10.output";
		a.readLog(fileName, outputFile);

	}

	public void readLog(String fileName, String outputFile) {
		ArrayList<String> queryList = new ArrayList<String>();
		ArrayList<String> pointList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String data = null;
			while ((data = br.readLine()) != null) {
				if (data.contains(NUM_QUERY)) {
					int index = data.indexOf("=");
					String query = data.substring(index + 2, data.length() - 1);
					queryList.add(query);
				} else if (data.contains(NUM_POINT)) {
					int index = data.indexOf("=");
					String point = data.substring(index + 2, data.length() - 1);
					pointList.add(point);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// print
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, false)));
			for (int i = 0; i < queryList.size(); i++) {
				bw.write(queryList.get(i));
				bw.newLine();
			}
			bw.write("-----------------pointList------------------");
			bw.newLine();
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
