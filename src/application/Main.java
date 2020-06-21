package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Main application for pathfinding app.
 * @author ducda
 *
 */
public class Main extends Application {
	/**
	 * Number of rows.
	 */
	public static final int NUM_ROWS = 30;
	/**
	 * Number of columns.
	 */
	public static final int NUM_COLS = 40;
	/**
	 * Application Window.
	 */
	private Stage window;
	/**
	 * Application Scene.
	 */
	public static GridPane root;
	/**
	 * The grid.
	 */
	public static Graph graph;
	/**
	 * Diagonal check box to run A* with/without diagonal movement.
	 */
	public static CheckBox diagonal;
	/**
	 * Handle animation.
	 */
	public static Button button;
	/**
	 * Clear path and walls.
	 */
	public static Button clearButton;
	/**
	 * Whether to animate algorithms or not.
	 */
	public static CheckBox showSteps;
	/**
	 * Animation speed.
	 */
	public static ComboBox<String> speed;
	/**
	 * Run with Jump Point Search or not.
	 */
	public static CheckBox jps;
	/**
	 * Start image representing start location.
	 */
	public static ImageView startImage;
	/**
	 * End image representing end location.
	 */
	public static ImageView endImage;
	
	/**
	 * Start the application.
	 * @param primaryStage main window
	 */
	public void start(Stage primaryStage) {
		window = primaryStage;
		createContent();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		window.setResizable(false);
		window.sizeToScene();
		window.setScene(scene);
		window.show();

	}
	
	/**
	 * Put the grid and control board on the scene.
	 */
	private void createContent() {
		root = new GridPane();
		root.setPadding(new Insets(20, 20, 20, 20));
		root.setVgap(10);

		createGrid();

		createControlBoard();
	}
	/**
	 * Create grids based on number of rows and number of columns.
	 */
	private void createGrid() {
		graph = new Graph();
		graph.setPrefSize(1000, 750);
		graph.setId("grid");
		for (int i = 0; i < NUM_ROWS; i++) {
			for (int j = 0; j < NUM_COLS; j++) {
				Tile tile = new Tile(i, j);
				graph.grid[i][j] = tile;
				tile.setTranslateX(j * 25);
				tile.setTranslateY(i * 25);
				graph.getChildren().add(tile);
			}
		}
		root.add(graph, 0, 0, NUM_COLS, NUM_ROWS);

		graph.start = graph.grid[16][0];
		graph.end = graph.grid[16][39];
		startImage = new ImageView();
		startImage.setFitHeight(20);
		startImage.setFitWidth(20);
		startImage.setImage(new Image("/start.png"));

		endImage = new ImageView();
		endImage.setFitHeight(20);
		endImage.setFitWidth(20);
		endImage.setImage(new Image("/end.png"));
		graph.start.getChildren().add(startImage);
		graph.end.getChildren().add(endImage);
		graph.createAdjList();
	}
	
	/**
	 * Create control board to control the application.
	 */
	private void createControlBoard() {

		GridPane control = new GridPane();
		control.setId("control");
		control.setVgap(10);
		control.setHgap(10);
		
		showSteps = new CheckBox("Show Steps");
		showSteps.setSelected(true);
		control.add(showSteps, 0, 0, 1, 1);

		diagonal = new CheckBox("Diagonal");
		control.add(diagonal, 1, 0, 1, 1);

		jps = new CheckBox("JPS");
		jps.setOnAction(e -> {
			if(jps.isSelected()) {
				diagonal.setSelected(false);
				diagonal.setDisable(true);
			}
			else {
				diagonal.setDisable(false);
			}
		});
		control.add(jps, 2, 0, 1, 1);
		speed = new ComboBox<String>();
		speed.getItems().addAll("Slow", "Normal", "Fast");
		speed.setValue(speed.getItems().get(2));
		control.add(speed, 0, 1, 1, 1);

		button = new Button("Run");
		button.getStyleClass().add("button");
		button.setOnAction(e -> handleButton());

		control.add(button, 1, 1, 1,1);
		
		clearButton = new Button("Clear");
		clearButton.getStyleClass().add("button");
		clearButton.setOnAction(e -> this.clearWallAndPath());
		control.add(clearButton, 2, 1, 1, 1);
		
		root.add(control, 0, 31);
	}
	/**
	 * Main method to run the application.
	 * @param args command line arguments.
	 */
	public static void main(String[] args) {
		launch(args);
	}
	/**
	 * Handle the button.
	 */
	private void handleButton() {
		if (button.getText().equals("Run")) {
			if(jps.isSelected()) {
				if(showSteps.isSelected()) {
					graph.jps(mapSpeedJPS());
					speed.setDisable(true);
					clearButton.setDisable(true);
					graph.timeline.play();
					button.setText("Pause");
				}
				else {
					graph.jpsNoSteps();
				}
			} else {
				if(showSteps.isSelected()) {
					graph.AStarSearch(mapSpeed());
					speed.setDisable(true);
					clearButton.setDisable(true);
					graph.timeline.play();
					button.setText("Pause");
				}
				else {
					graph.aStarNoSteps();
				}
			}
		} else if (button.getText().equals("Pause")) {
			graph.timeline.pause();
			button.setText("Resume");
		} else if (button.getText().equals("Resume")) {
			graph.timeline.play();
			button.setText("Pause");
		}
	}
	/**
	 * Map speed for A*.
	 * @return speed
	 */
	private double mapSpeed() {
		if (speed.getValue().equals("Slow")) {
			return 50.0;
		} else if (speed.getValue().equals("Normal")) {
			return 25.0;
		} else {
			return 5.0;
		}
	}
	/**
	 * Map speed for Jump Point Search.
	 * @return speed
	 */
	private double mapSpeedJPS() {
		if (speed.getValue().equals("Slow")) {
			return 500.0;
		} else if (speed.getValue().equals("Normal")) {
			return 250.0;
		} else {
			return 50.0;
		}
	}
	/**
	 * Clear path and walls.
	 */
	private void clearWallAndPath() {
		for(int i = 0; i < NUM_ROWS; i++) {
			for(int j = 0; j < NUM_COLS; j++) {
				if(graph.grid[i][j].isWall) {
					graph.grid[i][j].border.setFill(Color.WHITE);
					graph.grid[i][j].isWall = false;
				}
			}
		}
		graph.cleanUp();
	}
}
