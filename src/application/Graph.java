package application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
/**
 * Graph.
 * @author ducda
 *
 */
public class Graph extends Pane {
	/**
	 * Number of vertices in the graph.
	 */
	protected int numVertices = Main.NUM_COLS * Main.NUM_ROWS;
	/**
	 * Adjacency matrix.
	 */
	protected double[][] adjList;
	/**
	 * Actual Representation of the Grid.
	 */
	protected Tile[][] grid;
	/**
	 * Timeline object used to animate.
	 */
	protected Timeline timeline;
	/**
	 * Start location.
	 */
	protected Tile start;
	/**
	 * End location.
	 */
	protected Tile end;
	/**
	 * Whether or not the animation is done.
	 */
	private boolean isDone = false;
	/**
	 * Mouse Dragged Event.
	 */
	private EventHandler<? super MouseEvent> mouseDragged;
	/**
	 * Comparator used in algorithms to sort tiles based on their F score.
	 */
	private Comparator<Tile> cmp;
	/**
	 * Distance from start to other nodes.
	 */
	protected double cost[];
	/**
	 * Priority Queue.
	 */
	private PriorityQueue<Tile> pq;
	/**
	 * Used to traverse from end to start.
	 */
	private ArrayList<Tile> path;
	/**
	 * Change listener for Timeline used in A*.
	 */
	private ChangeListener<Status> cl;
	/**
	 * Change listener for Timeline used in JPS.
	 */
	private ChangeListener<Status> clJPS;
	/**
	 * Frame used for searching.
	 */
	private KeyFrame search;
	/**
	 * Frame used for showing path.
	 */
	private KeyFrame showPath;
	/**
	 * Glow effect.
	 */
	private ParallelTransition glow;
	/**
	 * Arrows used in JPS to show path.
	 */
	private ArrayList<Arrow> arrows;
	/**
	 * Stack used to traverse from end to start.
	 */
	private Stack<Tile> stackJPS;
	
	/**
	 * Constructor.
	 */
	public Graph() {
		numVertices = Main.NUM_COLS * Main.NUM_ROWS;
		grid = new Tile[Main.NUM_ROWS][Main.NUM_COLS];
		adjList = new double[numVertices][numVertices];

		cmp = new Comparator<Tile>() {
			public int compare(Tile o1, Tile o2) {
				if (o1.estimated > o2.estimated) {
					return 1;
				} else if (o1.estimated < o2.estimated) {
					return -1;
				} else {
					return 0;
				}
			}
		};

		this.setOnMouseDragged(e -> {
			if (e.getPickResult().getIntersectedNode() instanceof ImageView) {
				moveImageView(e);
			}
			if (e.getPickResult().getIntersectedNode() instanceof Rectangle) {
				Rectangle border = (Rectangle) e.getPickResult().getIntersectedNode();
				changeTile(border, e.getButton());
			}
		});
		mouseDragged = this.getOnMouseDragged();
		cl = new ChangeListener<Status>() {
			public void changed(ObservableValue<? extends Status> observable, Status oldValue, Status newValue) {
				if (oldValue == Status.RUNNING && newValue == Status.STOPPED) {
					timeline.getKeyFrames().clear();
					if (end.parent != null) {
						Tile current = end;
						while (current != null) {
							path.add(current);
							current = current.parent;
						}
						timeline.getKeyFrames().add(showPath);
						timeline.play();
					} else {
						finished();
					}
				}
			}
		};
		clJPS = new ChangeListener<Status>() {
			public void changed(ObservableValue<? extends Status> observable, Status oldValue, Status newValue) {
				if (oldValue == Status.RUNNING && newValue == Status.STOPPED) {
					timeline.getKeyFrames().clear();
					if (end.parent != null) {
						stackJPS = new Stack<>();
						Tile current = end;
						while (current != null) {
							stackJPS.push(current);
							current = current.parent;
						}
						timeline.getKeyFrames().add(showPath);
						timeline.play();
					} else {
						finished();
					}
				}
			}
		};

	}

	// Graph Operations
	/**
	 * Create adjacency matrix.
	 */
	public void createAdjList() {
		for (int i = 0; i < Main.NUM_ROWS; i++) {
			for (int j = 0; j < Main.NUM_COLS; j++) {
				ArrayList<Tile> neighbors = getNeighbors(grid[i][j], true);
				int currentPos = grid[i][j].row * Main.NUM_COLS + grid[i][j].col;
				adjList[currentPos][currentPos] = 0;
				for (Tile neighbor : neighbors) {
					// each tile is numbered from left to right, from row 0 to the end.
					int neighborPos = neighbor.row * Main.NUM_COLS + neighbor.col;
					// diagonal
					int diffRow = Math.abs(neighbor.row - grid[i][j].row);
					int diffCol = Math.abs(neighbor.col - grid[i][j].col);
					if (diffRow != 0 && diffCol != 0) {
						adjList[currentPos][neighborPos] = Math.sqrt(2);
					} else {
						adjList[currentPos][neighborPos] = 1;
					}
				}
			}
		}
	}
	
	/**
	 * Get cost moving from v1 to v2.
	 * @param v1 tile 1
	 * @param v2 tile 2
	 * @return movement cost
	 */
	private double getCost(Tile v1, Tile v2) {
		int pos1 = v1.row * Main.NUM_COLS + v1.col;
		int pos2 = v2.row * Main.NUM_COLS + v2.col;
		return adjList[pos1][pos2];
	}
	/**
	 * Get neighbors of current tile.
	 * @param vertex current tile
	 * @param diagonal whether to account for diagonal neighbors
	 * @return list of neighbors
	 */
	private ArrayList<Tile> getNeighbors(Tile vertex, boolean diagonal) {
		ArrayList<Tile> neighbors = new ArrayList<>();
		int nextRow;
		int nextCol;
		boolean n = true;
		boolean s = true;
		boolean e = true;
		boolean w = true;
		int[] points = new int[] { -1, 0, 1, 0, 0, 1, 0, -1 };
		for (int i = 0; i < points.length; i++) {
			int row = vertex.row + points[i];
			int col = vertex.col + points[++i];
			if (row < 0 || col < 0 || row >= Main.NUM_ROWS || col >= Main.NUM_COLS) {
				continue;
			}
			if (!grid[row][col].isWall) {
				neighbors.add(grid[row][col]);
			} else {
				if (i == 1) {
					n = false;
				} else if (i == 3) {
					s = false;
				} else if (i == 5) {
					e = false;
				} else {
					w = false;
				}
			}
		}
		if (diagonal) {
			if (n || e) {
				nextRow = vertex.row - 1;
				nextCol = vertex.col + 1;
				if (nextRow >= 0 && nextCol >= 0 && nextRow < Main.NUM_ROWS
						&& nextCol < Main.NUM_COLS) {
					if (!grid[nextRow][nextCol].isWall) {
						neighbors.add(grid[nextRow][nextCol]);
					}
				}
			}
			if (e || s) {
				nextRow = vertex.row + 1;
				nextCol = vertex.col + 1;
				if (nextRow >= 0 && nextCol >= 0 && nextRow < Main.NUM_ROWS
						&& nextCol < Main.NUM_COLS) {
					if (!grid[nextRow][nextCol].isWall) {
						neighbors.add(grid[nextRow][nextCol]);
					}
				}
			}
			if (s || w) {
				nextRow = vertex.row + 1;
				nextCol = vertex.col - 1;
				if (nextRow >= 0 && nextCol >= 0 && nextRow < Main.NUM_ROWS
						&& nextCol < Main.NUM_COLS) {
					if (!grid[nextRow][nextCol].isWall) {
						neighbors.add(grid[nextRow][nextCol]);
					}
				}
			}
			if (w || n) {
				nextRow = vertex.row - 1;
				nextCol = vertex.col - 1;
				if (nextRow >= 0 && nextCol >= 0 && nextRow < Main.NUM_ROWS
						&& nextCol < Main.NUM_COLS) {
					if (!grid[nextRow][nextCol].isWall) {
						neighbors.add(grid[nextRow][nextCol]);
					}
				}
			}
		}
		return neighbors;
	}
	
	// User Interaction With Graph
	/**
	 * Move end and start locations around the grind. If the algorithms are done, whenever end or start locations are moved, run the current algorithm again.
	 * @param e mouse event
	 */
	private void moveImageView(MouseEvent e) {
		ImageView iv = (ImageView) e.getPickResult().getIntersectedNode();
		iv.setOnDragDetected(e1 -> {
			iv.startFullDrag();
		});
		for (int i = 0; i < Main.NUM_ROWS; i++) {
			for (int j = 0; j < Main.NUM_COLS; j++) {
				Tile current = grid[i][j];
				if (current.getChildren().contains(Main.startImage) || current.getChildren().contains(Main.endImage)) {
					continue;
				}
				current.setOnMouseDragEntered(e1 -> {
					Tile parent = (Tile) iv.getParent();
					parent.getChildren().remove(iv);
					current.getChildren().add(iv);
					if (current.isWall) {
						changeCostNeighbors(current);
					}
					if (iv.equals(Main.startImage)) {
						this.start = current;
					} else {
						this.end = current;
					}
					if (isDone) {
						if (!Main.jps.isSelected()) {
							this.aStarNoSteps();
						} else {
							this.jpsNoSteps();
						}
					}
				});
			}
		}

	}
	/**
	 * Change current tile into wall left mouse is pressed, into normal tile if right mouse is pressed.
	 * @param border current tile
	 * @param mb mouse button
	 */
	private void changeTile(Rectangle border, MouseButton mb) {
		Tile parent = (Tile) border.getParent();
		if (!parent.equals(start) && !parent.equals(end)) {
			if (mb.equals(MouseButton.PRIMARY) && !parent.isWall) {
				if (parent.getChildren().size() > 1) {
					parent.getChildren().remove(parent.getChildren().size() - 1);
				}
				changeCostNeighbors(parent);
			} else if (mb.equals(MouseButton.SECONDARY) && parent.isWall) {
				changeCostNeighbors(parent);
			}
		}
	}
	
	/**
	 * Change color.
	 * @param vertex current tile
	 */
	public void changeCostNeighbors(Tile vertex) {
		if (!vertex.isWall) {
			vertex.border.setFill(Color.DARKBLUE);
			vertex.isWall = true;
		} else {
			vertex.border.setFill(Color.WHITE);
			vertex.isWall = false;
		}
	}
	// calculate heuristic cost (no diagonal movement allowed)
	/**
	 * Manhattan distance from current tile to end location.
	 * @param current current tile
	 * @return Manhattan distance
	 */
	private double heuristicCost(Tile current) {
		double dx = Math.abs(current.col - end.col);
		double dy = Math.abs(current.row - end.row);
		return (dx + dy);
	}



	// set up before running
	/**
	 * Initialize objects used by algorithms.
	 */
	private void setUp() {
		path = new ArrayList<>();
		timeline = new Timeline();
		cost = new double[numVertices];
		pq = new PriorityQueue<>(cmp);

		setUpCost(cost);
		glow = new ParallelTransition();
		this.setOnMouseDragged(null);
		Main.startImage.setOnDragDetected(null);
		Main.endImage.setOnDragDetected(null);
		arrows = new ArrayList<>();
	}

	// clean up before running again.
	/**
	 * Clean up before running again.
	 */
	public void cleanUp() {
		for (int i = 0; i < Main.NUM_ROWS; i++) {
			for (int j = 0; j < Main.NUM_COLS; j++) {
				Tile current = grid[i][j];
				if (!current.isWall) {
					current.border.setFill(Color.WHITE);
				}
				current.estimated = 0.0;
				current.isVisited = false;
				current.parent = null;
			}
		}
		if(isDone) {
			for (Tile current : path) {
				// if the current node is not start or end, and it has not been turned into a
				// wall
				if (!current.equals(start) && !current.equals(end) && current.getChildren().size() > 1) {
					current.getChildren().remove(current.getChildren().size() - 1);
				}
			}
		}
		if(arrows != null) {
			this.getChildren().removeAll(arrows);
		}
		isDone = false;
		if(glow != null) {
			glow.stop();
		}
	}
	private void finished() {
		Main.button.setText("Run");
		isDone = true;
		Main.speed.setDisable(false);
		Main.clearButton.setDisable(false);
		this.setOnMouseDragged(mouseDragged);
		System.out.println(cost[end.row * Main.NUM_COLS + end.col]);
	}
	
	/**
	 * Set up cost array.
	 * @param cost cost array
	 */
	private void setUpCost(double[] cost) {
		for (int i = 0; i < Main.NUM_ROWS; i++) {
			for (int j = 0; j < Main.NUM_COLS; j++) {
				int pos = grid[i][j].row * Main.NUM_COLS + grid[i][j].col;
				if (grid[i][j].equals(start)) {
					cost[pos] = 0;
				} else {
					cost[pos] = Double.POSITIVE_INFINITY;
				}
			}
		}
	}
	/**
	 * Change color of current tile.
	 * @param current current tile
	 * @param isPath whether this tile is on the path or not
	 */
	private void changeColor(Tile current, boolean isPath) {
		if (!isPath) {
			if (current.isVisited) {
				current.border.setFill(Color.DARKSEAGREEN);
			} else {
				current.border.setFill(Color.ORANGERED);
			}
		} else {
			current.border.setFill(Color.BLACK);
		}

	}
	/**
	 * Direction of next tile from current tile
	 * @param current current tile
	 * @param next next tile
	 */
	private void direction(Tile current, Tile next) {
		if (current.equals(start)) {
			return;
		}
		Shape svg;
		int diffRow = next.row - current.row;
		int diffCol = next.col - current.col;
		if (diffRow == -1 && diffCol == 1) {
			// up right
			svg = SVGGenerator.upRightArrow();
			current.getChildren().add(svg);
		} else if (diffRow == -1 && diffCol == -1) {
			// up left
			svg = SVGGenerator.upLeftArrow();
			current.getChildren().add(svg);
		} else if (diffRow == 1 && diffCol == 1) {
			// down right
			svg = SVGGenerator.downRightArrow();
			current.getChildren().add(svg);
		} else if (diffRow == 1 && diffCol == -1) {
			// down left
			svg = SVGGenerator.downLeftArrow();
			current.getChildren().add(svg);
		} else if (diffRow == -1 && diffCol == 0) {
			// up
			svg = SVGGenerator.upArrow();
			current.getChildren().add(svg);
		} else if (diffRow == 1 && diffCol == 0) {
			// down
			svg = SVGGenerator.downArrow();
			current.getChildren().add(svg);
		} else if (diffRow == 0 && diffCol == 1) {
			// right
			svg = SVGGenerator.rightArrow();
			current.getChildren().add(svg);
		} else {
			// left
			svg = SVGGenerator.leftArrow();
			current.getChildren().add(svg);
		}
		
	}
	/**
	 * Create glow effect for SVG arrows.
	 * @param current current tile
	 */
	private void glowEffect(Tile current) {
		Glow g = (Glow) current.getChildren().get(current.getChildren().size() - 1).getEffect();
		if (g != null) {
			KeyFrame init = new KeyFrame(Duration.ZERO, new KeyValue(g.levelProperty(), g.getLevel()));
			KeyFrame end = new KeyFrame(Duration.millis(1000), new KeyValue(g.levelProperty(), 0.9));
			Timeline tl = new Timeline(init, end);
			tl.setAutoReverse(true);
			tl.setCycleCount(Timeline.INDEFINITE);
			glow.getChildren().add(tl);
		}
	}
	// a* with showing steps
	/**
	 * A* pathfinding with animation.
	 * @param speed animation speed
	 */
	public void AStarSearch(double speed) {
		if (isDone) {
			cleanUp();
		}
		setUp();
		pq.add(start);

		start.isVisited = true;
		search = new KeyFrame(Duration.millis(speed), t -> {
			if (!pq.isEmpty()) {
				Tile current = pq.poll();
				changeColor(current, false);
				if (current.equals(end)) {

					timeline.stop();
					return;
				}
				ArrayList<Tile> neighbors = getNeighbors(current, Main.diagonal.isSelected());
				int currentPos = current.row * Main.NUM_COLS + current.col;
				for (Tile neighbor : neighbors) {
					double newCost = cost[currentPos] + getCost(current, neighbor);
					int neighborPos = neighbor.row * Main.NUM_COLS + neighbor.col;
					if (pq.contains(neighbor) && newCost < cost[neighborPos]) {
						neighbor.isVisited = false;
						pq.remove(neighbor);
					}
					if (!neighbor.isVisited && newCost < cost[neighborPos]) {
						changeColor(neighbor, false);

						cost[neighborPos] = newCost;
						neighbor.estimated = newCost + heuristicCost(neighbor);
						pq.add(neighbor);
						neighbor.isVisited = true;
						neighbor.parent = current;
					}
				}
			} else {
				timeline.stop();
			}
		});
		showPath = new KeyFrame(Duration.millis(10), t -> {
			if (!path.isEmpty()) {
				Tile current = path.remove(path.size() - 1);
				if (!current.equals(start)) {
					direction(current.parent, current);
					glowEffect(current.parent);
				}

				changeColor(current, true);
				if (current.equals(end)) {
					timeline.stop();
					timeline.getKeyFrames().clear();
					finished();
					glow.play();
				}
			}

		});
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().addAll(search);
		timeline.statusProperty().addListener(cl);
	}
	/**
	 * A* no steps.
	 */
	public void aStarNoSteps() {
		if (isDone) {
			cleanUp();
		}
		setUp();
		pq.add(start);

		start.isVisited = true;
		while (!pq.isEmpty()) {
			Tile current = pq.poll();

			changeColor(current, false);
			if (current.equals(end)) {
				break;
			}
			ArrayList<Tile> neighbors = getNeighbors(current, Main.diagonal.isSelected());
			int currentPos = current.row * Main.NUM_COLS + current.col;
			for (Tile neighbor : neighbors) {
				double newCost = cost[currentPos] + getCost(current, neighbor);
				int neighborPos = neighbor.row * Main.NUM_COLS + neighbor.col;
				if (pq.contains(neighbor) && newCost < cost[neighborPos]) {
					neighbor.isVisited = false;
					pq.remove(neighbor);
				}
				if (!neighbor.isVisited && newCost < cost[neighborPos]) {
					changeColor(neighbor, false);

					cost[neighborPos] = newCost;
					neighbor.estimated = newCost + heuristicCost(neighbor);
					pq.add(neighbor);
					neighbor.isVisited = true;
					neighbor.parent = current;
				}
			}
		}

		if (end.parent != null) {
			Tile current = end;
			while (current != null) {
				path.add(current);
				current = current.parent;
			}
			for (int i = path.size() - 1; i >= 0; i--) {
				current = path.get(i);
				if (!current.equals(start)) {
					direction(current.parent, current);
					glowEffect(current.parent);
				}
				changeColor(current, true);
			}
			glow.play();
		}
		finished();
	}





	// Jump Point Search Section
	/**
	 * Check if moving from the current tile in a direction is possible
	 * @param current current tile
	 * @param dCol x-direction
	 * @param dRow y-direction
	 * @return true if it is possible, false otherwise
	 */
	private boolean isWalkable(Tile current, int dCol, int dRow) {
		int col = current.col;
		int row = current.row;

		int nextCol = col + dCol;
		int nextRow = row + dRow;

		if (nextCol < 0 || nextCol >= Main.NUM_COLS) {
			return false;
		}

		if (nextRow < 0 || nextRow >= Main.NUM_ROWS) {
			return false;
		}

		if (dCol != 0 && dRow != 0) {
			if (grid[nextRow][col].isWall && grid[row][nextCol].isWall) {
				return false;
			}
			if (grid[nextRow][nextCol].isWall) {
				return false;
			}
		} else if (dCol != 0) {
			if (grid[row][nextCol].isWall) {
				return false;
			}
		} else {
			if (grid[nextRow][col].isWall) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Get neighbors of current tile based on the direction from its parent to it.
	 * @param current current tile
	 * @return list of neighbors
	 */
	private ArrayList<Tile> prunedNeighbors(Tile current) {
		if (current.equals(start)) {
			return getNeighbors(current, true);
		}
		ArrayList<Tile> prunedNeighbors = new ArrayList<>();
		int dCol = current.col - current.parent.col;
		int dRow = current.row - current.parent.row;

		if (dCol != 0 && dRow != 0) {
			dCol = (int) (dCol * (Math.abs(1 / (double) dCol)));
			dRow = (int) (dRow * (Math.abs(1 / (double) dRow)));
			if (isWalkable(current, dCol, 0)) {
				prunedNeighbors.add(grid[current.row][current.col + dCol]);
			}
			if (isWalkable(current, 0, dRow)) {
				prunedNeighbors.add(grid[current.row + dRow][current.col]);
			}
			if (isWalkable(current, dCol, dRow)) {
				prunedNeighbors.add(grid[current.row + dRow][current.col + dCol]);
			}
			if (!isWalkable(current, -dCol, 0) && isWalkable(current, 0, dRow)) {
				prunedNeighbors.add(grid[current.row + dRow][current.col - dCol]);
			}
			if (!isWalkable(current, 0, -dRow) && isWalkable(current, dCol, 0)) {
				prunedNeighbors.add(grid[current.row - dRow][current.col + dCol]);
			}
		} else {
			if (dCol != 0) {
				dCol = (int) (dCol * (Math.abs(1 / (double) dCol)));
				if (isWalkable(current, dCol, 0)) {
					if (isWalkable(current, dCol, 0)) {
						prunedNeighbors.add(grid[current.row][current.col + dCol]);
					}
					if (!isWalkable(current, 0, 1) && isWalkable(current, dCol, 1)) {
						prunedNeighbors.add(grid[current.row + 1][current.col + dCol]);
					}
					if (!isWalkable(current, 0, -1) && isWalkable(current, dCol, -1)) {
						prunedNeighbors.add(grid[current.row - 1][current.col + dCol]);
					}
				}
			} else {
				dRow = (int) (dRow * (Math.abs(1 / (double) dRow)));
				if (isWalkable(current, 0, dRow)) {
					if (isWalkable(current, 0, dRow)) {
						prunedNeighbors.add(grid[current.row + dRow][current.col]);
					}
					if (!isWalkable(current, 1, 0) && isWalkable(current, 1, dRow)) {
						prunedNeighbors.add(grid[current.row + dRow][current.col + 1]);
					}
					if (!isWalkable(current, -1, 0) && isWalkable(current, -1, dRow)) {
						prunedNeighbors.add(grid[current.row + dRow][current.col - 1]);
					}
				}
			}

		}
		return prunedNeighbors;
	}
	/**
	 * Jump to a next tile in a direction from current tile.
	 * @param current current tile
	 * @param dCol x-direction
	 * @param dRow y-direction
	 * @return
	 */
	private Tile jump(Tile current, int dCol, int dRow) {
		int nextCol = current.col + dCol;
		int nextRow = current.row + dRow;

		if (!isWalkable(current, dCol, dRow)) {
			return null;
		}

		if (grid[nextRow][nextCol].equals(end)) {
			return grid[nextRow][nextCol];
		}
		// jump diagonally
		if (dCol != 0 && dRow != 0) {
			/*
			 * o      
			 * x c or   c 
			 * p      p x o
			 */
			if ((!isWalkable(grid[nextRow][nextCol], -dCol, 0) && isWalkable(grid[nextRow][nextCol], -dCol, dRow))
					|| (!isWalkable(grid[nextRow][nextCol], 0, -dRow)
							&& isWalkable(grid[nextRow][nextCol], dCol, -dRow))) {
				return grid[nextRow][nextCol];
			}
			Tile horSearch = jump(grid[nextRow][nextCol], dCol, 0);
			Tile verSearch = jump(grid[nextRow][nextCol], 0, dRow);
			if (horSearch != null || verSearch != null) {
				return grid[nextRow][nextCol];
			}
		} else {
			if (dCol != 0) {
				/*
				 * o x o
				 * p c 
				 * o x o
				 */
				if ((!isWalkable(grid[nextRow][nextCol], 0, 1) && isWalkable(grid[nextRow][nextCol], dCol, 1))
						|| (!isWalkable(grid[nextRow][nextCol], 0, -1)
								&& isWalkable(grid[nextRow][nextCol], dCol, -1))) {
					return grid[nextRow][nextCol];
				}

			} else {
				/*
				 * o   o
				 * x c x
				 * o p o
				 * 
				 */
				if ((!isWalkable(grid[nextRow][nextCol], 1, 0) && isWalkable(grid[nextRow][nextCol], 1, dRow))
						|| (!isWalkable(grid[nextRow][nextCol], -1, 0)
								&& isWalkable(grid[nextRow][nextCol], -1, dRow))) {
					return grid[nextRow][nextCol];
				}
			}
		}
		return jump(grid[nextRow][nextCol], dCol, dRow);
	}
	/**
	 * Indentify jump point successors of current tile.
	 * @param current current tile
	 * @return list of successors
	 */
	private ArrayList<Tile> identifySuccessors(Tile current) {
		ArrayList<Tile> successors = new ArrayList<>();
		ArrayList<Tile> prunedNeighbors = this.prunedNeighbors(current);
		for (Tile pruned : prunedNeighbors) {
			int dCol = pruned.col - current.col;
			int dRow = pruned.row - current.row;
			Tile successor = jump(current, dCol, dRow);
			if (successor != null) {
				successors.add(successor);
			}
		}
		return successors;
	}
	/**
	 * Get distance from current tile to its successor.
	 * @param current current tile
	 * @param successor successor of current tile
	 * @return distance between current tile and its successor
	 */
	private double getDistance(Tile current, Tile successor) {
		int dCol = current.col - successor.col;
		int dRow = current.row - successor.row;

		if (dCol != 0 && dRow != 0) {
			return Math.abs(dCol) * Math.sqrt(2);
		} else {
			if (dCol != 0) {
				return Math.abs(dCol);
			} else {
				return Math.abs(dRow);
			}
		}
	}

	// showing steps
	/**
	 * Jump point search with no steps.
	 */
	public void jpsNoSteps() {
		if (isDone) {
			cleanUp();
		}
		setUp();
		start.estimated = 0 + heuristicCost(start);
		pq.add(start);
		while (!pq.isEmpty()) {
			Tile current = pq.poll();

			if (current.equals(end)) {
				break;
			}

			current.isVisited = true;
			changeColor(current, false);
			for (Tile successor : this.identifySuccessors(current)) {
				if (successor.isVisited) {
					continue;
				}
				double newCost = cost[current.row * Main.NUM_COLS + current.col]
						+ getDistance(current, successor);
				if (newCost < cost[successor.row * Main.NUM_COLS + successor.col] || !pq.contains(successor)) {
					successor.parent = current;
					cost[successor.row * Main.NUM_COLS + successor.col] = newCost;
					successor.estimated = newCost + heuristicCost(successor);
					pq.add(successor);
					drawPath(current, successor);
					changeColor(successor, false);
				}
			}
		}
		if (end.parent != null) {
			stackJPS = new Stack<>();
			Tile current = end;
			while (current != null) {
				stackJPS.push(current);
				current = current.parent;
			}
			while (!stackJPS.isEmpty()) {
				current = stackJPS.pop();
				if (current.parent != null) {
					Arrow arrow = getArrow(current);
					arrow.getStyleClass().remove(0);
					arrow.setStrokeWidth(2);
					arrow.setFill(Color.GOLDENROD);
				}
				changeColor(current, true);
			}
		}
		finished();
	}
	/**
	 * Jump point search with steps.
	 * @param speed
	 */
	public void jps(double speed) {
		if (isDone) {
			cleanUp();
		}
		setUp();
		start.estimated = 0 + heuristicCost(start);
		pq.add(start);
		search = new KeyFrame(Duration.millis(speed), t -> {
			if (!pq.isEmpty()) {
				Tile current = pq.poll();
				if (current.equals(end)) {
					timeline.stop();
				}
				current.isVisited = true;
				changeColor(current, false);
				for (Tile successor : this.identifySuccessors(current)) {
					if (successor.isVisited) {
						continue;
					}
					double newCost = cost[current.row * Main.NUM_COLS + current.col]
							+ getDistance(current, successor);
					if (newCost < cost[successor.row * Main.NUM_COLS + successor.col]
							|| !pq.contains(successor)) {
						successor.parent = current;
						cost[successor.row * Main.NUM_COLS + successor.col] = newCost;
						successor.estimated = newCost + heuristicCost(successor);
						pq.add(successor);
						drawPath(current, successor);
						changeColor(successor, false);
					}
				}
			} else {
				timeline.stop();
			}
		});
		showPath = new KeyFrame(Duration.millis(500), t -> {
			if (!stackJPS.isEmpty()) {
				Tile current = stackJPS.pop();
				if (current.parent != null) {
					Arrow arrow = getArrow(current);
					arrow.setStrokeWidth(2.0);
					arrow.setFill(Color.DARKGOLDENROD);
				}
				changeColor(current, true);
				if (current.equals(end)) {
					timeline.stop();
					timeline.getKeyFrames().clear();
					finished();
				}
			}
		});
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().addAll(search);
		timeline.statusProperty().addListener(clJPS);
	}
	/**
	 * Get arrow between this tile and its parent.
	 * @param current current tile
	 * @return arrow object
	 */
	private Arrow getArrow(Tile current) {
		double x1 = current.parent.col * 25 + 12.5;
		double y1 = current.parent.row * 25 + 12.5;

		double x2 = current.col * 25 + 12.5;
		double y2 = current.row * 25 + 12.5;
		for (Arrow arrow : arrows) {
			if (arrow.startX == x1 && arrow.startY == y1 && arrow.endX == x2 && arrow.endY == y2) {
				return arrow;
			}
		}
		return null;
	}
	/**
	 * Set an arrow between current tile and next tile.
	 * @param current current tile 
	 * @param next tile
	 */
	private void drawPath(Tile current, Tile next) {
		double x1 = current.col * 25 + 12.5;
		double y1 = current.row * 25 + 12.5;
		double x2 = next.col * 25 + 12.5;
		double y2 = next.row * 25 + 12.5;
		Arrow arrow = new Arrow(x1, y1, x2, y2, 10);
		arrows.add(arrow);
		arrow.getStyleClass().add("successor");
		this.getChildren().add(arrow);
	}
}
