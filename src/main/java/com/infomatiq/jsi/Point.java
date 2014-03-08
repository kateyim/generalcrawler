//   Point.java
//   Java Spatial Index Library
//   Copyright (C) 2002-2005 Infomatiq Limited.
//  
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//  
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

package com.infomatiq.jsi;

/**
 * Currently hardcoded to 2 dimensions, but could be extended.
 */
public class Point {
	/**
	 * The (x, y) coordinates of the point.
	 */
	// public double x, y;
	public int dimension;
	public double[] v;

	public int id;
	public int numCrawled;

	/**
	 * Constructor.
	 * 
	 * @param x
	 *            The x coordinate of the point
	 * @param y
	 *            The y coordinate of the point
	 */
	public Point(int dimension, double[] x) {
		this.dimension = dimension;
		this.v = x.clone();
	}

	public Point(int id, int dimension, double[] x, int numCrawled) {
		this.id = id;
		this.dimension = dimension;
		this.v = x.clone();
		this.numCrawled = numCrawled;
	}

	/**
	 * Copy from another point into this one
	 */
	public void set(Point other) {
		this.dimension = other.dimension;
		this.v = other.v.clone();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < dimension; i++) {
			sb.append(v[i]);
			sb.append(";");
		}
		String s = sb.toString();
		s = s.substring(0, s.length() - 2);
		return s;
	}

	public int vInt(int i) {
		return (int) Math.round(v[i]);
	}

	public double getValueOfADimension(int i) {
		return v[i];
	}

	public double distance(Point another) {
		double distance = 0;
		for (int i = 0; i < dimension; i++) {
			distance += (v[i] - another.v[i]) * (v[i] - another.v[i]);
		}

		return (double) Math.sqrt(distance);
	}
}
