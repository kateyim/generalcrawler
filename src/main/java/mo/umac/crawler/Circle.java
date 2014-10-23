/**
 * 
 */
package mo.umac.crawler;

import org.apache.log4j.Logger;

import com.infomatiq.jsi.Point;



/**
 * This Circle represents a query which is also an area covered.
 * 
 * @author Kate Yim
 */
public class Circle {

	protected static Logger logger = Logger.getLogger(Circle.class.getName());

	private Point center = null;
	/* The unit of radius is 'm' in the map. */
	private double radius = 0.0;
	
	// TODO add dimension information

	public Circle(Point center2, double radius) {
		this.center = center2;
		this.radius = radius;
	}

	public Point getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("center: " + center.toString() + ", radius = " + radius);
		return sb.toString();
	}

}
