package mo.umac.analyse;

import mo.umac.crawler.H2DB;
import mo.umac.crawler.Main;

public class FormatDataForDn {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String oldH2 = Main.DB_NAME_D2;
		String newH2 = Main.DB_NAME_SOURCE;
		H2DB h2 = new H2DB();
		h2.createTables(newH2);
		h2.convertH2ForDn(oldH2, newH2);
		// h2.printItemTable(newH2);
		h2.distroyConn();
	}

}
