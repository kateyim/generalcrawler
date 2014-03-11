package edu.wlu.cs.levy.CG;

import java.util.List;

public class MyTest2 {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[] k0 = { 49, 1 };
		double[] k1 = { 51.1, 0.01 };
		// double[] k3 = {68.36, 35.9};
		// double[] k6 = {41.64, 45.09} ;

		// make a KD-tree and add some nodes
		KDTree<Integer> kd = new KDTree<Integer>(2);
		try {
			kd.insert(k0, new Integer(0));
			kd.insert(k1, new Integer(1));
			// kd.insert(k3, new Integer(3));
			// kd.insert(k6, new Integer(6));

		} catch (Exception e) {
			System.err.println(e);
		}

		//
		double[] k = { 50, 0 };
		List<Integer> results;
		try {
			results = kd.nearest(k, 1);
			for (int i = 0; i < results.size(); i++) {
				System.out.println(results.get(i));
			}

		} catch (KeySizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
