package mo.umac.crawler;

public class Coordinate {
	/**
	 * The x-coordinate.
	 */
	public double x;
	/**
	 * The y-coordinate.
	 */
	public double y;

	/**
	 * Constructs a <code>Coordinate</code> at (x,y,z).
	 * 
	 * @param x
	 *            the x-value
	 * @param y
	 *            the y-value
	 * @param z
	 *            the z-value
	 */
	public Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double distance(Coordinate p) {
		double dx = x - p.x;
		double dy = y - p.y;

		return Math.sqrt(dx * dx + dy * dy);
	}
	
	  /**
	   *  Compares this {@link Coordinate} with the specified {@link Coordinate} for order.
	   *  This method ignores the z value when making the comparison.
	   *  Returns:
	   *  <UL>
	   *    <LI> -1 : this.x < other.x || ((this.x == other.x) && (this.y <
	   *    other.y))
	   *    <LI> 0 : this.x == other.x && this.y = other.y
	   *    <LI> 1 : this.x > other.x || ((this.x == other.x) && (this.y > other.y))
	   *
	   *  </UL>
	   *  Note: This method assumes that ordinate values
	   * are valid numbers.  NaN values are not handled correctly.
	   *
	   *@param  o  the <code>Coordinate</code> with which this <code>Coordinate</code>
	   *      is being compared
	   *@return    -1, zero, or 1 as this <code>Coordinate</code>
	   *      is less than, equal to, or greater than the specified <code>Coordinate</code>
	   */
	  public int compareTo(Object o) {
	    Coordinate other = (Coordinate) o;

	    if (x < other.x) return -1;
	    if (x > other.x) return 1;
	    if (y < other.y) return -1;
	    if (y > other.y) return 1;
	    return 0;
	  }
}
