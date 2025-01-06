import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Draw extends Application {
    private int screenWidth;
    private int screenHeight;
    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline timeline;

    // runtime settings
    private Thread thread;
    private int delayTime = 50;

    // game graphics
    public static final int TILE_WIDTH = 20;
    public static final int TILE_HEIGHT = 20;
    private Image[] img = new Image[10];
    private final int IMGID_MINE = 0;
    private final int IMGID_FLAG = 1;
    private final int IMGID_FALSEFLAG = 2;
    private final int IMGID_HIDDENTILE = 3;
    private final int IMGID_REVEALEDTILE = 4;
    private final int IMGID_NEWGAME = 5;
    private final int IMGID_WIN = 6;
    private final int IMGID_LOSE = 7;
    private final int IMGID_TIMER = 8;
    private int numMilliseconds = 0;

    // game settings
    private MinesweeperBoard board;
    private final int NUM_ROWS = 10;
    private final int NUM_COLS = 10;
    private final int NUM_MINES = 10;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        board = new MinesweeperBoard(NUM_ROWS, NUM_COLS, NUM_MINES);

        screenWidth = board.getNumCols() * TILE_WIDTH;
        screenHeight = (board.getNumRows() + 1) * TILE_HEIGHT;

        canvas = new Canvas(screenWidth, screenHeight);
        canvas.setOnMouseClicked(event -> mouseClicked(event));
        gc = canvas.getGraphicsContext2D();
        loadImages();

        Scene scene = new Scene(new StackPane(canvas));
        primaryStage.setScene(scene);
        primaryStage.setTitle("Minesweeper");
        primaryStage.show();

        timeline = new Timeline(new KeyFrame(Duration.millis(delayTime), event -> {
            draw(gc);
            if (!board.isLose() && !board.isWin()) {
                numMilliseconds += delayTime;
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setOnCloseRequest(event -> timeline.stop());
    }

    private void loadImages() {
        try {
            img[IMGID_MINE] = new Image(getClass().getResourceAsStream("/img/mine.png"));
            img[IMGID_FLAG] = new Image(getClass().getResourceAsStream("/img/flag.jpg"));
            img[IMGID_HIDDENTILE] = new Image(getClass().getResourceAsStream("/img/hidden_tile.jpg"));
            img[IMGID_REVEALEDTILE] = new Image(getClass().getResourceAsStream("/img/revealed_tile.jpg"));
            img[IMGID_NEWGAME] = new Image(getClass().getResourceAsStream("/img/new_game.jpg"));
            img[IMGID_FALSEFLAG] = new Image(getClass().getResourceAsStream("/img/false_flag.jpg"));
            img[IMGID_WIN] = new Image(getClass().getResourceAsStream("/img/win.jpg"));
            img[IMGID_LOSE] = new Image(getClass().getResourceAsStream("/img/lose.jpg"));
            img[IMGID_TIMER] = new Image(getClass().getResourceAsStream("/img/timer.png"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void draw(GraphicsContext gc) {
        // clear canvas
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillRect(0, 0, screenWidth, screenHeight);

        gc.drawImage(img[IMGID_NEWGAME], (board.getNumCols() / 2) * TILE_WIDTH, board.getNumRows() * TILE_HEIGHT,
                TILE_WIDTH, TILE_HEIGHT);
        gc.drawImage(img[IMGID_MINE], (board.getNumCols() / 4) * TILE_WIDTH, board.getNumRows() * TILE_HEIGHT,
                TILE_WIDTH, TILE_HEIGHT);
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("" + board.getNumMinesLeft(), (board.getNumCols() / 4 + 1) * TILE_WIDTH,
                (board.getNumRows() + 1) * TILE_HEIGHT);
        gc.drawImage(img[IMGID_TIMER], (3 * board.getNumCols() / 4) * TILE_WIDTH, board.getNumRows() * TILE_HEIGHT,
                TILE_WIDTH, TILE_HEIGHT);
        gc.fillText("" + numMilliseconds / 1000, (3 * board.getNumCols() / 4 + 1) * TILE_WIDTH,
                (board.getNumRows() + 1) * TILE_HEIGHT);

        drawBoard(gc, board);
        checkBoardState(gc, board);
    }

    public void drawBoard(GraphicsContext gc, MinesweeperBoard b) {
        for (int r = 0; r < b.getNumRows(); r++) {
            for (int c = 0; c < b.getNumCols(); c++) {
                // draw initial square as hidden
                gc.drawImage(img[IMGID_HIDDENTILE], c * TILE_WIDTH, r * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);

                if (b.getIsRevealed(r, c)) {
                    drawTile(b, r, c);
                }

                if (b.getIsFlagged(r, c)) {
                    gc.drawImage(img[IMGID_FLAG], c * TILE_WIDTH, r * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
                }

                if (b.isLose()) {
                    if (b.getIsFlagged(r, c)) {
                        if (b.getTile(r, c) != MinesweeperBoard.ID_MINE) {
                            gc.drawImage(img[IMGID_FALSEFLAG], c * TILE_WIDTH, r * TILE_HEIGHT, TILE_WIDTH,
                                    TILE_HEIGHT);
                        }
                    }
                }

                // draw outline of square
                gc.setStroke(javafx.scene.paint.Color.BLACK);
                gc.strokeRect(c * TILE_WIDTH, r * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
            }
        }
    }

    public void checkBoardState(GraphicsContext gc, MinesweeperBoard b) {
        // check current state of board
        if (b.isLose()) {
            gc.drawImage(img[IMGID_LOSE], (board.getNumCols() / 2) * TILE_WIDTH, board.getNumRows() * TILE_HEIGHT,
                    TILE_WIDTH, TILE_HEIGHT);
        }

        if (b.isWin()) {
            b.flagAllMines();
            gc.drawImage(img[IMGID_WIN], (board.getNumCols() / 2) * TILE_WIDTH, board.getNumRows() * TILE_HEIGHT,
                    TILE_WIDTH, TILE_HEIGHT);
        }
    }

    public void drawTile(MinesweeperBoard b, int r, int c) {
        gc.drawImage(img[IMGID_REVEALEDTILE], c * TILE_WIDTH, r * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);

        if (b.getTile(r, c) == MinesweeperBoard.ID_MINE) {
            gc.drawImage(img[IMGID_MINE], c * TILE_WIDTH, r * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
        } else {
            if (b.getTile(r, c) > MinesweeperBoard.ID_EMPTY) {
                Color fillColor = getFillColor(b.getTile(r, c));
                gc.setFill(fillColor);
                gc.fillText("" + b.getTile(r, c), c * TILE_WIDTH + TILE_WIDTH / 2, r * TILE_HEIGHT + TILE_HEIGHT / 2);
            }
        }
    }

    private Color getFillColor(int tileValue) {
        Color fillColor;
        switch (tileValue) {
            case 1:
                fillColor = javafx.scene.paint.Color.LIGHTBLUE;
                break;
            case 2:
                fillColor = javafx.scene.paint.Color.LIGHTGREEN;
                break;
            case 3:
                fillColor = javafx.scene.paint.Color.LIGHTCORAL;
                break;
            case 4:
                fillColor = javafx.scene.paint.Color.BLUE;
                break;
            case 5:
                fillColor = javafx.scene.paint.Color.RED;
                break;
            case 6:
                fillColor = javafx.scene.paint.Color.CYAN;
                break;
            case 7:
                fillColor = javafx.scene.paint.Color.BLACK;
                break;
            case 8:
                fillColor = javafx.scene.paint.Color.GRAY;
                break;
            default:
                fillColor = javafx.scene.paint.Color.WHITE;
                break;
        }
        return fillColor;
    }

    private void mouseClicked(MouseEvent e) {
        int r = (int) (e.getY() / TILE_HEIGHT);
        int c = (int) (e.getX() / TILE_WIDTH);

        if (c == board.getNumCols() / 2 && r == board.getNumRows()
                && e.getButton() == MouseButton.PRIMARY) {
            board = new MinesweeperBoard(NUM_ROWS, NUM_COLS, NUM_MINES);
            numMilliseconds = 0;
            return;
        }

        if (board.isLose() || board.isWin()) {
            return;
        }

        if (!board.isValid(r, c)) {
            return;
        }

        if (e.getButton() == MouseButton.PRIMARY) {
            if (!board.getIsFlagged(r, c)) {
                if (board.getTile(r, c) == MinesweeperBoard.ID_MINE) {
                    revealAllMines(board);
                } else {
                    if (board.getTile(r, c) == MinesweeperBoard.ID_EMPTY) {
                        board.floodFillEmptyTiles(r, c);
                    } else {
                        board.setIsRevealed(true, r, c);
                    }
                }
            }
        }

        if (e.getButton() == MouseButton.SECONDARY) {
            if (board.getIsFlagged(r, c)) {
                board.setNumMinesLeft(board.getNumMinesLeft() + 1);
            } else {
                board.setNumMinesLeft(board.getNumMinesLeft() - 1);
            }
            board.setIsFlagged(!board.getIsFlagged(r, c), r, c);
        }
    }

    public void revealAllMines(MinesweeperBoard b) {
        for (int r = 0; r < b.getNumRows(); r++) {
            for (int c = 0; c < b.getNumCols(); c++) {
                if (b.getTile(r, c) == MinesweeperBoard.ID_MINE) {
                    board.setIsRevealed(true, r, c);
                    if (!b.getIsFlagged(r, c)) {
                        gc.drawImage(img[IMGID_REVEALEDTILE], c * TILE_WIDTH, r * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
                    }
                }
            }
        }
    }
}