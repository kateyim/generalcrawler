package mo.umac.analyse;

import mo.umac.crawler.H2DB;

public class SampleGlass {

	public static void main(String[] args) {
		SampleGlass sg = new SampleGlass();
		// for sample: reduce the size of glass
		String dbNameFull = "../data-experiment/glass-data/glass/glasses";
		String dbNameSample = "../data-experiment/glass-data/glass/glasses-0.8";
		int divident = 4;
		int divisor = 5;
		sg.sample(dbNameFull, dbNameSample, divident, divisor);

	}

	public void sample(String dbNameSource, String dbNameSample, int divident, int divisor) {
		H2DB h2 = new H2DB(dbNameSource, dbNameSample);
		h2.sample(divisor, divident);

		int c1 = h2.count(dbNameSource, "ITEM");
		System.out.println("dbNameSource = " + c1);
		int c2 = h2.count(dbNameSample, "ITEM");
		System.out.println("dbNameSample = " + c2);
		h2.distroyConn();
	}

}
