package application;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
/**
 * Tile object.
 * @author ducda
 *
 */
public class Tile extends StackPane {
	/**
	 * Border of the tile.
	 */
	protected Rectangle border;
	/**
	 * its row pos.
	 */
	protected int row;
	/**
	 * its col pos.
	 */
	protected int col;
	/**
	 * is the tile wall or not.
	 */
	protected boolean isWall = false;
	/**
	 * is the tile visited.
	 */
	protected boolean isVisited = false;
	/**
	 * its parent.
	 */
	protected Tile parent = null;
	/**
	 * its G Score.
	 */
	protected double estimated;
	/**
	 * Constructor.
	 * @param row its row pos
	 * @param col its col pos
	 */
	public Tile(int row, int col) {
		this.row = row;
		this.col = col;
		estimated = 0;
		border = new Rectangle(25,25);
		border.getStyleClass().add("tile");
		
		this.getChildren().add(border);
	}
	@Override
	/**
	 * Compare this tile with another tile.
	 * @param obj other tile
	 * @return true if two tile are identical, false otherwise
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
	/**
	 * Create string representation of current tile.
	 * @return string representation of current tile
	 */
	public String toString() {
		return row + " " + col + " ";
	}
}
