// Hamming distance metric class

package edu.wlu.cs.levy.CG;

class EuclideanDistance extends DistanceMetric {

	protected double distance(double[] a, double[] b) {

		return Math.sqrt(sqrdist(a, b));

	}

	// protected static double sqrdist(double [] a, double [] b) {
	//
	// double dist = 0;
	//
	// for (int i=0; i<a.length; ++i) {
	// double diff = (a[i] - b[i]);
	// dist += diff*diff;
	// }
	//
	// return dist;
	// }

	/**
	 * revised by kate 2014-3-11
	 * 
	 * @author kate
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected static double sqrdist(double[] a, double[] b) {

		double dist = 0;

		for (int i = 0; i < a.length - 1; ++i) {
			double diff = (a[i] - b[i]);
			dist += diff * diff;
		}

		return dist;
	}
}
