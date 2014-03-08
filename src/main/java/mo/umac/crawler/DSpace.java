/**
 * 
 */
package mo.umac.crawler;

import java.util.ArrayList;

/**
 * The d-dimensional space for describing the boundary of the space
 * 
 * @author kate
 * 
 */
public class DSpace {

	// Logger logger = Logger.getLogger(DSpace.class.getName());

	private int numDimension;

	/**
	 * number = numDimension
	 */
	private double[] lowerBounds;
	private double[] upperBounds;

	ArrayList<double[]> bounds;

	public DSpace(int numDimension, double[] lowerBounds, double[] upperBounds) {
		super();
		this.numDimension = numDimension;
		this.lowerBounds = lowerBounds;
		this.upperBounds = upperBounds;
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("Space: ");
		// sb.append("numDim = " + numDimension);
		// sb.append(", boundaries: ");
		for (int i = 0; i < numDimension; i++) {
			sb.append("[");
			sb.append(lowerBounds[i]);
			sb.append(", ");
			sb.append(upperBounds[i]);
			sb.append("];");
		}
		return sb.toString();
	}

	/**
	 * @param di
	 *            : begin from 0
	 * @return
	 */
	public double getLowerBoundOfADimension(int di) {
		return lowerBounds[di];
	}

	public double getUpperBoundOfADimension(int di) {
		return upperBounds[di];
	}

	public void setLowerBoundOfADimension(int di, double value) {
		lowerBounds[di] = value;
	}

	public void setUpperBoundOfADimension(int di, double value) {
		upperBounds[di] = value;
	}

	public int getNumDimension() {
		return numDimension;
	}

	public void setNumDimension(int d) {
		this.numDimension = d;
	}

	public double[] getLowerBounds() {
		return lowerBounds;
	}

	public void setLowerBounds(double[] lowerBounds) {
		this.lowerBounds = lowerBounds;
	}

	public double[] getUpperBounds() {
		return upperBounds;
	}

	public void setUpperBounds(double[] upperBounds) {
		this.upperBounds = upperBounds;
	}

}
