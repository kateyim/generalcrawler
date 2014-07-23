package mo.umac.analyse;

import mo.umac.crawler.H2DB;

public class SampleGlass {

	public static void main(String[] args) {
		SampleGlass sg = new SampleGlass();
		// for sample: reduce the size of glass 
		String dbNameFull = "../data-experiment/glass-data/glass/glasses";
		String dbNameSample = "../data-experiment/glass-data/glass/glasses-4";
		int factor = 4;
		sg.sample(dbNameFull, dbNameSample, factor);

	}
	
	public void sample(String dbNameSource, String dbNameSample, int factor) {
		H2DB h2 = new H2DB(dbNameSource, dbNameSample);
		h2.sample(factor);

		int c1 = h2.count(dbNameSource, "ITEM");
		System.out.println("dbNameSource = " + c1);
		int c2 = h2.count(dbNameSample, "ITEM");
		System.out.println("dbNameSample = " + c2);
		h2.distroyConn();
	}

}
