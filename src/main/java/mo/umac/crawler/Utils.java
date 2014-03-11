package mo.umac.crawler;

public class Utils {

	public static String ArrayToString(double[] array) { 
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			sb.append(";");
		}
		return sb.toString();
	}
	
	public static String ArrayToString(int[] array) { 
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			sb.append(";");
		}
		return sb.toString();
	}

}
