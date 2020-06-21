package application;

import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

/**
 * SVG generator.
 * @author ducda
 *
 */
public class SVGGenerator {
	/**
	 * Create up arrow.
	 * @return up arrow
	 */
	public static Shape upArrow() {
		SVGPath path1 = new SVGPath();
	    path1.setContent("M6.954,8.264 L21.26,8.264 L21.26,13.625 L6.954,13.625 Z");
	    path1.getTransforms().add(
	         Transform.affine(0.7071, 0.7071, -0.7071, 0.7071, 11.8708, -6.7699));
	    path1.setStroke(Color.BLACK);
	    SVGPath path2 = new SVGPath();
	    path2.setContent("M0.614,8.25 L14.921,8.25 L14.921,13.611 L0.615,13.611 Z");
	    path2.getTransforms().add(
		         Transform.affine(-0.7071, 0.7071, -0.7071, -0.7071, 20.9893, 13.1667));

	    path2.setStroke(Color.BLACK);
	    Shape up = Shape.union(path1, path2);
	    Glow glow = new Glow(0.0);
	    up.setEffect(glow);
	    up.getStyleClass().add("arrow");
		return up;
	}
	/**
	 * Create down arrow.
	 * @return down arrow
	 */
	public static Shape downArrow() {
		SVGPath path1 = new SVGPath();
	    path1.setContent("M6.954,8.264 L21.26,8.264 L21.26,13.625 L6.954,13.625 Z");
	    path1.getTransforms().add(
	         Transform.affine(0.7071, -0.7071, 0.7071, 0.7071, -3.6067, 13.1818));
	    path1.setStroke(Color.BLACK);
	    SVGPath path2 = new SVGPath();
	    path2.setContent("M0.615,8.25 L14.921,8.25 L14.921,13.611 L0.615,13.611 Z");
	    path2.getTransforms().add(
		         Transform.affine(-0.7071,-0.7071, 0.7071, -0.7071, 5.5309, 24.1523));
	    path2.setStroke(Color.BLACK);
	    Shape down = Shape.union(path1, path2);
	    Glow glow = new Glow(0.0);
	    down.setEffect(glow);
	    down.getStyleClass().add("arrow");
		return down;
	}
	/**
	 * Create left arrow.
	 * @return left arrow
	 */
	public static Shape leftArrow() {
		SVGPath path1 = new SVGPath();
	    path1.setContent("M3.791,5.087 L18.097,5.087 L18.097,10.448 L3.791,10.448 Z");
	    path1.getTransforms().add(
	         Transform.affine(0.7071, -0.7071, 0.7071, 0.7071, -2.287, 10.0139));
	    path1.setStroke(Color.BLACK);
	    SVGPath path2 = new SVGPath();
	    path2.setContent("M3.778,11.427 L18.084,11.427 L18.084,16.788 L3.778,16.788 Z");
	    path2.getTransforms().add(
		         Transform.affine(0.7071, 0.7071, -0.7071, 0.7071, 13.1761, -3.5973));
	    path2.setStroke(Color.BLACK);
	    Shape left = Shape.union(path1, path2);
	    left.getStyleClass().add("arrow");
	    Glow glow = new Glow(0.0);
	    left.setEffect(glow);
		return left;
	}
	/**
	 * Create right arrow.
	 * @return right arrow
	 */
	public static Shape rightArrow() {
		SVGPath path1 = new SVGPath();
	    path1.setContent("M3.791,5.087 L18.097,5.087 L18.097,10.448 L3.791,10.448 Z");
	    path1.getTransforms().add(
	         Transform.affine(-0.7071, -0.7071, 0.7071, -0.7071, 13.1915, 20.9994));
	    path1.setStroke(Color.BLACK);
	    SVGPath path2 = new SVGPath();
	    path2.setContent("M3.778,11.427 L18.084,11.427 L18.084,16.788 L3.778,16.788 Z");
	    path2.getTransforms().add(
		         Transform.affine(-0.7071, 0.7071, -0.7071, -0.7071, 28.6357, 16.3531));
	    path2.setStroke(Color.BLACK);
	    Shape right = Shape.union(path1, path2);
	    right.getStyleClass().add("arrow");
	    Glow glow = new Glow(0.0);
	    right.setEffect(glow);
		return right;
	}
	/**
	 * Create up right arrow.
	 * @return up right arrow
	 */
	public static Shape upRightArrow() {
		SVGPath path1 = new SVGPath();
	    path1.setContent("M3.791,5.087 L18.097,5.087 L18.097,10.448 L3.791,10.448 Z");
	    path1.getTransforms().add(
	         Transform.affine(-0.7071, -0.7071, 0.7071, -0.7071, 13.1915, 20.9994));
	    path1.setStroke(Color.BLACK);
	    SVGPath path2 = new SVGPath();
	    path2.setContent("M3.778,11.427 L18.084,11.427 L18.084,16.788 L3.778,16.788 Z");
	    path2.getTransforms().add(
		         Transform.affine(-0.7071, 0.7071, -0.7071, -0.7071, 28.6357, 16.3531));
	    path2.setStroke(Color.BLACK);
	    Shape upRight = Shape.union(path1, path2);
	    upRight.getStyleClass().add("arrow"); 
	    upRight.getTransforms().add(Transform.affine(0.7,-0.7,0.7,0.7,-4,9.4));
	    Glow glow = new Glow(0.0);
	    upRight.setEffect(glow);
		return upRight;
	}
	/**
	 * Create up left arrow.
	 * @return up left arrow
	 */
	public static Shape upLeftArrow() {
		SVGPath path1 = new SVGPath();
	    path1.setContent("M3.791,5.087 L18.097,5.087 L18.097,10.448 L3.791,10.448 Z");
	    path1.getTransforms().add(
	         Transform.affine(-0.7071, -0.7071, 0.7071, -0.7071, 13.1915, 20.9994));
	    path1.setStroke(Color.BLACK);
	    SVGPath path2 = new SVGPath();
	    path2.setContent("M3.778,11.427 L18.084,11.427 L18.084,16.788 L3.778,16.788 Z");
	    path2.getTransforms().add(
		         Transform.affine(-0.7071, 0.7071, -0.7071, -0.7071, 28.6357, 16.3531));
	    path2.setStroke(Color.BLACK);
	    Shape upLeft = Shape.union(path1, path2);
	    upLeft.getStyleClass().add("arrow"); 
	    upLeft.getTransforms().add(Transform.affine(-0.7,-0.7,0.7,-0.7,10.1,24.9));
	    Glow glow = new Glow(0.0);
	    upLeft.setEffect(glow);
		return upLeft;
	}
	/**
	 * Create down left arrow.
	 * @return down left arrow
	 */
	public static Shape downLeftArrow() {
		SVGPath path1 = new SVGPath();
	    path1.setContent("M6.954,8.264 L21.26,8.264 L21.26,13.625 L6.954,13.625 Z");
	    path1.getTransforms().add(
	         Transform.affine(0.7071, -0.7071, 0.7071, 0.7071, -3.6067, 13.1818));
	    path1.setStroke(Color.BLACK);
	    SVGPath path2 = new SVGPath();
	    path2.setContent("M0.615,8.25 L14.921,8.25 L14.921,13.611 L0.615,13.611 Z");
	    path2.getTransforms().add(
		         Transform.affine(-0.7071,-0.7071, 0.7071, -0.7071, 5.5309, 24.1523));
	    path2.setStroke(Color.BLACK);
	    Shape downLeft = Shape.union(path1, path2);
	    downLeft.getStyleClass().add("arrow"); 
	    downLeft.getTransforms().add(Transform.rotate(45,10.9,10.9));
	    Glow glow = new Glow(0.0);
	    downLeft.setEffect(glow);
		return downLeft;
	}
	/**
	 * Create down right arrow.
	 * @return down right arrow
	 */
	public static Shape downRightArrow() {
		SVGPath path1 = new SVGPath();
	    path1.setContent("M6.954,8.264 L21.26,8.264 L21.26,13.625 L6.954,13.625 Z");
	    path1.getTransforms().add(
	         Transform.affine(0.7071, -0.7071, 0.7071, 0.7071, -3.6067, 13.1818));
	    path1.setStroke(Color.BLACK);
	    SVGPath path2 = new SVGPath();
	    path2.setContent("M0.615,8.25 L14.921,8.25 L14.921,13.611 L0.615,13.611 Z");
	    path2.getTransforms().add(
		         Transform.affine(-0.7071,-0.7071, 0.7071, -0.7071, 5.5309, 24.1523));
	    path2.setStroke(Color.BLACK);
	    Shape downRight = Shape.union(path1, path2);
	    downRight.getStyleClass().add("arrow"); 
	    downRight.getTransforms().add(Transform.affine(0.7,-0.7,0.7,0.7,-4.3,10.9));
	    Glow glow = new Glow(0.0);
	    downRight.setEffect(glow);
		return downRight;
	}
}
