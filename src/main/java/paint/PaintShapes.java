package paint;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class PaintShapes extends JPanel {

	public static boolean painting = false;

	public static List<Shape> arrDraw = new ArrayList<Shape>();
	public static List<Shape> arrFill = new ArrayList<Shape>();
	// for polygon
	public static List<Shape> arrFillConstraintPoly = new ArrayList<Shape>();
	public static List<Shape> arrDrawTempTriangles = new ArrayList<Shape>();

	public static PaintShapes paint = new PaintShapes();

	public Color redTranslucence = new Color(255, 0, 0, 50);
	public Color greenTranslucence = new Color(0, 255, 0, 50);
	public Color blueTranslucence = new Color(0, 0, 255, 150);
	public Color blackTranslucence = Color.BLACK;

	public static Color color;

	public void paintComponent(Graphics g) {
		// XXX clear() only used testing AlgoDCDT
//		clear(g);
		g.setColor(color);
		for (Shape i : arrDraw) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.draw(i);
		}
		for (Shape i : arrFill) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.fill(i);
		}
		// PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
		for (Shape i : arrDrawTempTriangles) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.draw(i);
		}
		// PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
		for (Shape i : arrFillConstraintPoly) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.fill(i);
		}
		arrDraw.clear();
		arrFill.clear();
		arrDrawTempTriangles.clear();
		arrFillConstraintPoly.clear();
	}

	/**
	 * super.paintComponent clears offscreen pixmap, since we're using double
	 * buffering by default.
	 * 
	 * @param g
	 */
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}

	public static void myRepaint() {
		paint.repaint();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



	public static Rectangle2D.Double getPoint(com.infomatiq.jsi.Point point) {
		double x = point.getValueOfADimension(0);
		double y = point.getValueOfADimension(1);
		Rectangle2D.Double shape = new Rectangle2D.Double(x - 2.5, y - 2.5, 5, 5);
		return shape;
	}

	public static void addPoint(com.infomatiq.jsi.Point point) {
		arrFill.add(getPoint(point));
	}

}
