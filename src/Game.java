import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Game implements Serializable {
    public static final int TILE_SIZE = 22;
    public static final Font TILEFONT = new Font("Dialog", Font.BOLD, 16);
    public static final Font SMALLTILEFONT = new Font("Dialog", Font.BOLD, 14);
    public static final Font MINEFONT = new Font("Dialog", Font.BOLD, 22);
    public static final Font GAMEOVERFONT = new Font("Dialog", Font.BOLD, 32);
    public static final Font MENUFONT = new Font("Dialog", Font.BOLD, 48);
    public static final Color BACKGROUNDCOLOR = Color.BLACK;
    public static final Color UNREVEALEDCOLOR = new Color(190, 190, 190, 255);
    public static final Color REVEALEDCOLOR = new Color(155, 155, 155, 255);
    public static final Color SAVELOADCOLOR = new Color(40, 40, 40);
    public static final Color LOSECOLOR = new Color(255, 0, 0, 50);
    public static final Color HIGHLIGHTCOLOR = new Color(255, 255, 0, 50);
    public static final Color[] COLORLIST = {
            Color.BLUE, new Color(0, 100, 0), Color.RED, new Color(0, 0, 80), new Color(80, 0, 0),
            new Color(0, 110, 110), Color.BLACK, new Color(100, 100, 100), new Color(64, 0, 128), new Color(255, 255, 0),
            new Color(0, 60, 0), new Color(150, 75, 0), new Color(255, 150, 220), new Color(255, 155, 0), new Color(128, 0, 255),
            new Color(0, 255, 255), new Color(150, 255, 0), new Color(255, 198, 153), new Color(255, 0, 150), new Color(255, 255, 255)
    };
    public static final String[] DIFFICULTYLIST = {"Easy", "Medium", "Hard", "Extreme", "Impossible"};
    public static final Color[] DIFFICULTYCOLORLIST = {new Color(40, 150, 255), Color.GREEN, Color.YELLOW, new Color(255, 128, 0), Color.RED};
    public static final int[][] PARAMETERS = new int[][]{
            new int[]{10, 10, 10, 16, 16, 40, 30, 16, 99, 30, 24, 155, 40, 30, 270},
            new int[]{10, 10, 7, 16, 16, 30, 30, 16, 75, 30, 24, 125, 40, 30, 240},
            new int[]{13, 13, 10, 18, 18, 24, 30, 18, 55, 30, 26, 100, 40, 30, 180}
    };
    public static final String[] MODELIST = {"Normal", "Plus", "Double-Plus"};
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File SAVEDATA = Utils.join(CWD, "Savedata");
    public static final int PAUSETIME = 30;
    private int width;
    private int height;
    private int minesMarked;
    private int minesTotal;
    private int revealedTotal;
    private Random random = new Random();
    Tile[][] board;
    private transient boolean gameOver;
    private int difficulty;
    private int mode;
    private boolean firstClick;
    private int testingNum = 0;
    private long startTime;
    private int totalSeconds;
    private int loadSeconds;
    private int mistakes;
    private boolean saved;
    private boolean autosave;
    private boolean highlighted;
    private transient int[] highscores;
    public class Tile implements Serializable {
        private int x;
        private int y;
        private boolean mine;
        /* 0: empty, 1: flagged, 2: clicked */
        private int visibility;
        private int number = -1;
        public Tile(int x, int y, boolean mine) {
            this.x = x;
            this.y = y;
            this.mine = mine;
            this.visibility = 0;
        }
    }
    public static void main(String[] args) {
        Game g = new Game(30, 24, 100, 0, 1);
        g.startGame();
    }
    public Game(int w, int h, int mineCount, int difficulty, int mode) {
        this.width = w;
        this.height = h;
        this.minesMarked = 0;
        this.minesTotal = mineCount;
        this.difficulty = difficulty;
        this.mode = mode;
        this.autosave = true;
        this.highlighted = false;
        this.firstClick = true;
        this.totalSeconds = 0;
        this.mistakes = 0;
    }
    public void readHighscores() {
        File saveFile = Utils.join(SAVEDATA, "Highscores.txt");
        try {
            highscores = Utils.readObject(saveFile, int[].class);
            if (highscores.length == 10) {
                int[] temp = new int[15];
                for (int i = 0; i < 10; i++) {
                    temp[i] = highscores[i];
                }
                for (int i = 10; i < 15; i++) {
                    temp[i] = -1;
                }
                highscores = temp;
                saveHighscore();
            }
        } catch (Exception e) {
            highscores = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
            Utils.writeObject(saveFile, highscores);
        }
    }
    public void startGame() {
        if (!SAVEDATA.exists()) {
            SAVEDATA.mkdir();
        }
        readHighscores();
        while (true) {
            mainMenu();
        }
    }
    public boolean mouseInBox(double x, double y, double halfWidth, double halfHeight) {
        double mouseX = StdDraw.mouseX();
        double mouseY = StdDraw.mouseY();
        return (x - halfWidth <= mouseX && y - halfHeight <= mouseY && mouseX <= x + halfWidth && mouseY <= y + halfHeight);
    }
    public void drawHighlight() {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        StdDraw.clear(BACKGROUNDCOLOR);
        drawBoard();
        header();
        if (mouseX < width && mouseY < height && board[mouseX][mouseY].visibility == 2) {
            StdDraw.setPenColor(HIGHLIGHTCOLOR);
            StdDraw.filledRectangle(mouseX + 0.5, mouseY + 0.5, 1.5 + mode, 1.5 + mode);
        }
        StdDraw.show();
    }
    public void mainMenu() {
        width = 30;
        height = 27;
        StdDraw.setCanvasSize(width * TILE_SIZE, (height + 1) * TILE_SIZE);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height + 1);
        StdDraw.enableDoubleBuffering();
        renderMainMenu();
        boolean mPressed = false;
        while (true) {
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    if (mouseInBox(width / 2, height / 2 + 5, 5, 1)) {
                        int[] modeParam = PARAMETERS[mode];
                        int w = modeParam[3 * difficulty], h = modeParam[3 * difficulty + 1], m = modeParam[3 * difficulty + 2];
                        this.totalSeconds = 0;
                        this.mistakes = 0;
                        runGame(w, h, m);
                        break;
                    } else if (mouseInBox(width / 2, height / 2 + 2, 5, 1)) {
                        difficulty = (difficulty + 1) % 5;
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 1, 5, 1)) {
                        mode = (mode + 1) % 3;
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 4, 5, 1)) {
                        this.autosave = !this.autosave;
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 7, 5, 1)) {
                        highscoreScreen();
                        break;
                    } else if (mouseInBox(width / 2, height / 2 - 10, 5, 1)) {
                        System.exit(0);
                    }
                }
                mPressed = false;
            }
            StdDraw.pause(PAUSETIME);
        }
    }
    public void renderMainMenu() {
        StdDraw.clear(Color.LIGHT_GRAY);
//        testNumberColors();
        StdDraw.setPenColor(DIFFICULTYCOLORLIST[difficulty]);
        StdDraw.filledRectangle(width / 2, height / 2 + 2, 5, 1);
        StdDraw.setPenColor(Color.GRAY);
        StdDraw.filledRectangle(width / 2, height / 2 + 5, 5, 1);
        StdDraw.filledRectangle(width / 2, height / 2 - 1, 5, 1);
        StdDraw.filledRectangle(width / 2, height / 2 - 4, 5, 1);
        StdDraw.filledRectangle(width / 2, height / 2 - 7, 5, 1);
        StdDraw.filledRectangle(width / 2, height / 2 - 10, 5, 1);
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.setFont(MENUFONT);
        StdDraw.text(width / 2, height / 2 + 9, "Minesweeper Plus");
        StdDraw.setFont(TILEFONT);
        StdDraw.text(width / 2, height / 2 + 5, "Start Game");
        StdDraw.text(width / 2, height / 2 + 2, "Difficulty: " + DIFFICULTYLIST[difficulty]);
        StdDraw.text(width / 2, height / 2 - 1, "Mode: " + MODELIST[mode]);
        StdDraw.text(width / 2, height / 2 - 4, "Autosave: " + autosave);
        StdDraw.text(width / 2, height / 2 - 7, "High Scores");
        StdDraw.text(width / 2, height / 2 - 10, "Quit Game");
        StdDraw.setFont(MINEFONT);
        for (int i = 0; i < difficulty + 1; i++) {
            StdDraw.text(width / 2 + 6 + i, height / 2 + 1.88, "\u26EF");
            StdDraw.filledCircle(width / 2 + 6 + i, height / 2 + 2, 0.25);
        }
        int[] modeParam = PARAMETERS[mode];
        StdDraw.setFont(TILEFONT);
        StdDraw.textLeft(width / 2 + 6, height / 2 + 6.5, "Width: " + modeParam[3 * difficulty]);
        StdDraw.textLeft(width / 2 + 6, height / 2 + 5.5, "Height: " + modeParam[3 * difficulty + 1]);
        StdDraw.textLeft(width / 2 + 6, height / 2 + 4.5, "Mines: " + modeParam[3 * difficulty + 2]);
        StdDraw.textLeft(width / 2 + 6, height / 2 + 3.5, "Mine Density: " +
                String.format("%.3f", 1.0 * modeParam[3 * difficulty + 2] / (modeParam[3 * difficulty] * modeParam[3 * difficulty + 1])));
        StdDraw.show();
    }
    public void testNumberColors() {
        StdDraw.setPenColor(BACKGROUNDCOLOR);
        StdDraw.filledRectangle(COLORLIST.length / 2, height + 0.5, COLORLIST.length / 2 + 1, 0.5);
        for (int i = 0; i < COLORLIST.length; i++) {
            StdDraw.setPenColor(REVEALEDCOLOR);
            StdDraw.filledRectangle(0.5 + i, height + 0.5, 0.45, 0.45);
            StdDraw.setPenColor(COLORLIST[i]);
            if (i < 9) {
                StdDraw.setFont(TILEFONT);
            } else {
                StdDraw.setFont(SMALLTILEFONT);
            }
            StdDraw.text(0.5 + i, height + 0.4, "" + (i + 1));
        }
//        StdDraw.show();
    }
    public void highscoreScreen() {
        StdDraw.clear(Color.LIGHT_GRAY);
        StdDraw.setFont(MENUFONT);
        StdDraw.text(width / 2, height / 2 + 9, "High Scores");
        StdDraw.setFont(GAMEOVERFONT);
        StdDraw.text(width / 2 - 9, height / 2 + 4, MODELIST[0]);
        StdDraw.text(width / 2, height / 2 + 4, MODELIST[1]);
        StdDraw.text(width / 2 + 9, height / 2 + 4, MODELIST[2]);
        StdDraw.setFont(TILEFONT);
        for (int i = 0; i < 5; i++) {
            String score;
            if (highscores[i] >= 0) {
                score = "" + highscores[i];
            } else {
                score = "N/A";
            }
//            StdDraw.setPenColor(DIFFICULTYCOLORLIST[i]);
            StdDraw.text(width / 2 - 9, height / 2 + 2 - 2 * i, DIFFICULTYLIST[i] + ": " + score);
        }
        for (int i = 0; i < 5; i++) {
            String score;
            if (highscores[i + 5] >= 0) {
                score = "" + highscores[i + 5];
            } else {
                score = "N/A";
            }
//            StdDraw.setPenColor(DIFFICULTYCOLORLIST[i]);
            StdDraw.text(width / 2, height / 2 + 2 - 2 * i, DIFFICULTYLIST[i] + ": " + score);
        }
        for (int i = 0; i < 5; i++) {
            String score;
            if (highscores[i + 10] >= 0) {
                score = "" + highscores[i + 10];
            } else {
                score = "N/A";
            }
//            StdDraw.setPenColor(DIFFICULTYCOLORLIST[i]);
            StdDraw.text(width / 2 + 9, height / 2 + 2 - 2 * i, DIFFICULTYLIST[i] + ": " + score);
        }
        StdDraw.show();
        boolean mPressed = false;
        while (true) {
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    return;
                }
                mPressed = false;
            }
            StdDraw.pause(PAUSETIME);
        }
    }
    public void runGame(int w, int h, int m) {
        initialize(w, h, m);
        board = new Tile[width][height];
        populateBoard(minesTotal);
        boolean mPressed = false;
        gameOver = false;
        firstClick = true;
        testingNum = 0;
        revealedTotal = 0;
        minesMarked = 0;
        drawBoard();
        header();
        StdDraw.show();
        while (!gameOver) {
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    int x = (int) StdDraw.mouseX();
                    int y = (int) StdDraw.mouseY();
                    if (y < height) {
                        if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                            if (invertTile(x, y)) {
                                if (autosave) {
                                    saveGame();
                                } else {
                                    this.saved = false;
                                }
                            }
                        } else if (StdDraw.isKeyPressed(KeyEvent.VK_P)) {
                            if (hideTile(x, y)) {
                                if (autosave) {
                                    saveGame();
                                } else {
                                    this.saved = false;
                                }
                            }
                        } else {
                            if (firstClick) {
                                safeFirstClick(x, y);
                                this.startTime = System.currentTimeMillis();
                                this.totalSeconds = 0;
                                this.loadSeconds = 0;
                            }
                            Tile t = board[x][y];
                            if (t.visibility == 0) {
                                if (t.mine) {
                                    mistakes++;
                                    saveGame();
                                    revealTile(t, true);
                                    gameOver = true;
                                } else {
                                    revealTile(t, true);
                                    if (revealedTotal + minesTotal == width * height) {
                                        minesMarked = minesTotal;
                                        gameOver = true;
                                    }
                                    if (autosave) {
                                        if (!gameOver) {
                                            saveGame();
                                        }
                                    } else {
                                        this.saved = false;
                                    }
                                }
                            } else if (t.visibility == 2) {
                                if (t.number > 0 && countFlags(x, y) >= t.number) {
                                    ArrayList<Tile> toChange = revealAround(x, y);
                                    if (toChange.size() > 0) {
                                        for (Tile tile : toChange) {
                                            if (tile.mine) {
                                                gameOver = true;
                                            }
                                        }
                                        if (!gameOver) {
                                            for (Tile tile : toChange) {
                                                revealTile(tile, true);
                                            }
                                            if (revealedTotal + minesTotal == width * height) {
                                                minesMarked = minesTotal;
                                                gameOver = true;
                                            }
                                            if (autosave) {
                                                if (!gameOver) {
                                                    saveGame();
                                                }
                                            } else {
                                                this.saved = false;
                                            }
                                        } else if (autosave) {
                                            mistakes++;
                                            saveGame();
                                        }
                                    }

                                }
                            }
//                            if (revealTile(x, y, true)) {
//                                if (revealedTotal + minesTotal == width * height) {
//                                    minesMarked = minesTotal;
//                                    gameOver = true;
//                                }
//                                if (autosave) {
//                                    if (!gameOver) {
//                                        saveGame();
//                                    } else {
//                                        loadGame();
//                                        mistakes++;
//                                        saveGame();
//                                    }
//                                } else {
//                                    this.saved = false;
//                                }
//                            }
                        }
                    } else {
                        if (difficulty > 1) {
//                            if (mouseInBox(width - 9.5, height + 0.5, 1.5, 0.5)) {
//                                if (!autosave && !saved) {
//                                    saveGame();
//                                }
//                            } else
                            if (mouseInBox(width - 5.5, height + 0.5, 1.5, 0.5)) {
                                loadGame();
                            } else if (mouseInBox(width - 1.5, height + 0.5, 1.5, 0.5)) {
                                return;
                            }
                        } else if (difficulty == 1) {
//                            if (mouseInBox(width - 6.5, height + 0.5, 1.125, 0.5)) {
//                                if (!autosave && !saved) {
//                                    saveGame();
//                                }
//                            } else
                            if (mouseInBox(width - 3.375, height + 0.5, 1, 0.5)) {
                                loadGame();
                            } else if (mouseInBox(width - 1, height + 0.5, 1, 0.5)) {
                                return;
                            }
                        } else {
                            if (mouseInBox(width - 2.375, height + 0.5, 1, 0.5)) {
                                loadGame();
                            } else if (mouseInBox(width - 0.5, height + 0.5, 0.5, 0.5)) {
                                return;
                            }
                        }
                    }
                    header();
                    StdDraw.show();
                }
                mPressed = false;
            }
            if (StdDraw.isKeyPressed(KeyEvent.VK_H)) {
                drawHighlight();
                highlighted = true;
            } else {
                if (highlighted) {
                    highlighted = false;
                    StdDraw.clear(BACKGROUNDCOLOR);
                    drawBoard();
                    header();
                    StdDraw.show();
                }
            }
            if (!firstClick) {
                int timeDiff = (int) ((System.currentTimeMillis() - startTime) / 1000);
                if (timeDiff > loadSeconds) {
                    totalSeconds += timeDiff - loadSeconds;
                    loadSeconds = timeDiff;
                    header();
                    StdDraw.show();
                }
            }
            StdDraw.pause(PAUSETIME);
        }
        if (revealedTotal + minesTotal == width * height) {
            wonGame();
        } else {
            lostGame();
        }
        return;
    }
    public void header() {
        StdDraw.setPenColor(Color.DARK_GRAY);
        StdDraw.filledRectangle(width / 2, height + 0.5, width / 2 + 1, 0.5);
        StdDraw.setPenColor(SAVELOADCOLOR);
        if (difficulty > 1) {
            StdDraw.filledRectangle(width - 1.5, height + 0.5, 1.5, 0.5);
            StdDraw.filledRectangle(width - 5, height + 0.5, 1.5, 0.5);
//            if (!autosave) {
//                StdDraw.filledRectangle(width - 9.5, height + 0.5, 1.5, 0.5);
//            }
        } else if (difficulty == 1) {
            StdDraw.filledRectangle(width - 1, height + 0.5, 1, 0.5);
            StdDraw.filledRectangle(width - 3.375, height + 0.5, 1, 0.5);
//            if (!autosave) {
//                StdDraw.filledRectangle(width - 6.5, height + 0.5, 1.125, 0.5);
//            }
        } else {
            StdDraw.filledRectangle(width - 0.5, height + 0.5, 0.5, 0.5);
            StdDraw.filledRectangle(width - 2.375, height + 0.5, 1, 0.5);
        }
        drawMine(0, height);
        StdDraw.setPenColor(Color.WHITE);
        if (difficulty > 1) {
            StdDraw.setFont(TILEFONT);
            StdDraw.textLeft(3.25, height + 0.4, "Time: " + totalSeconds);
            StdDraw.textLeft(9, height + 0.4, "Deaths: " + mistakes);
        } else if (difficulty == 1) {
            StdDraw.setFont(SMALLTILEFONT);
            StdDraw.textLeft(2.75, height + 0.4, "Time: " + totalSeconds);
            StdDraw.textLeft(7.25, height + 0.4, "Deaths: " + mistakes);
        } else {
            StdDraw.setFont(SMALLTILEFONT);
            StdDraw.textLeft(2.125, height + 0.4, "T: " + totalSeconds);
            StdDraw.textLeft(4.5, height + 0.4, "D: " + mistakes);
        }
        StdDraw.textLeft(1, height + 0.4, "" + (minesTotal - minesMarked));
//        StdDraw.textLeft(width / 2, height + 0.4, "" + testingNum);
        if (difficulty > 1) {
            StdDraw.text(width - 1.5, height + 0.4, "Menu");
            StdDraw.text(width - 5, height + 0.4, "Load");
//            if (!autosave) {
//                if (saved) {
//                    StdDraw.text(width - 9.5, height + 0.4, "Save");
//                } else {
//                    StdDraw.text(width - 9.5, height + 0.4, "Save*");
//                }
//            }
        } else if (difficulty == 1) {
            StdDraw.text(width - 1, height + 0.4, "Menu");
            StdDraw.text(width - 3.375, height + 0.4, "Load");
//            if (!autosave) {
//                if (saved) {
//                    StdDraw.text(width - 6.5, height + 0.4, "Save");
//                } else {
//                    StdDraw.text(width - 6.5, height + 0.4, "Save*");
//                }
//            }
        } else {
            StdDraw.text(width - 0.5, height + 0.4, "M");
            StdDraw.text(width - 2.375, height + 0.4, "Load");
        }
        //StdDraw.textLeft(8, height + 0.4, "" + testingNum);
    }
    public void saveGame() {
        String saveString = DIFFICULTYLIST[difficulty] + MODELIST[mode] + "Save.txt";
        File saveFile = Utils.join(SAVEDATA, saveString);
        Utils.writeObject(saveFile, this);
        this.saved = true;
    }
    public void saveHighscore() {
        File saveFile = Utils.join(SAVEDATA, "Highscores.txt");
        Utils.writeObject(saveFile, highscores);
    }
    public void loadGame() {
        String saveString = DIFFICULTYLIST[difficulty] + MODELIST[mode] + "Save.txt";
        File saveFile = Utils.join(SAVEDATA, saveString);
        try {
            Game loaded = Utils.readObject(saveFile, Game.class);
        } catch (Exception e) {
            return;
        }
        Game loaded = Utils.readObject(saveFile, Game.class);
        this.width = loaded.width;
        this.height = loaded.height;
        this.minesMarked = loaded.minesMarked;
        this.minesTotal = loaded.minesTotal;
        this.revealedTotal = loaded.revealedTotal;
        this.board = loaded.board;
        this.difficulty = loaded.difficulty;
        this.mode = loaded.mode;
        this.firstClick = loaded.firstClick;
        this.testingNum = loaded.testingNum;
        this.totalSeconds = loaded.totalSeconds;
        this.loadSeconds = 0;
        this.mistakes = loaded.mistakes;
        this.startTime = System.currentTimeMillis();
        initialize(width, height, minesTotal);
        drawBoard();
        header();
        StdDraw.show();
    }
    public void lostGame() {
        StdDraw.clear(LOSECOLOR);
        StdDraw.setPenColor(Color.DARK_GRAY);
        StdDraw.filledRectangle(width / 2, height / 2 + 1, 4, 0.5);
        StdDraw.filledRectangle(width / 2, height / 2 - 1, 4, 0.5);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(TILEFONT);
        StdDraw.text(width / 2, height / 2 + 0.9, "Main Menu");
        StdDraw.text(width / 2, height / 2 - 1.1, "Solution");
        StdDraw.setFont(GAMEOVERFONT);
        StdDraw.text(width / 2, height / 2 + 3.4, "Game Over");
        StdDraw.show();
        boolean mPressed = false;
        while (true) {
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    if (mouseInBox(width / 2, height / 2 + 1, 4, 0.5)) {
                        return;
                    } else if (mouseInBox(width / 2, height / 2 - 1, 4, 0.5)) {
                        showSolution();
                        return;
                    }
                }
                mPressed = false;
            }
            StdDraw.pause(PAUSETIME);
        }
    }
    public void showSolution() {
        StdDraw.clear(BACKGROUNDCOLOR);
        StdDraw.setPenColor(Color.DARK_GRAY);
        StdDraw.filledRectangle(width / 2, height + 0.5, width / 2, 0.5);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(TILEFONT);
        StdDraw.text(width / 2, height + 0.4, "Solution");
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile t = board[x][y];
                if (t.visibility == 1) {
                    StdDraw.setPenColor(UNREVEALEDCOLOR);
                    StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
                    if (t.mine) {
                        drawFlag(x, y);
                    } else {
                        drawMine(x, y);
                        StdDraw.setPenColor(Color.RED);
                        StdDraw.line(x + 0.2, y + 0.2, x + 0.8, y + 0.8);
                        StdDraw.line(x + 0.8, y + 0.2, x + 0.2, y + 0.8);
                    }
                } else {
                    if (t.mine) {
                        t.visibility = 2;
                    }
                    drawTile(x, y);
                }
            }
        }
        StdDraw.show();
        boolean mPressed = false;
        while (true) {
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    return;
                } else {
                    mPressed = false;
                }
            }
            StdDraw.pause(PAUSETIME);
        }
    }
    public void wonGame() {
        int gameIndex = 5 * mode + difficulty;
        if (mistakes == 0 && (highscores[gameIndex] < 0 || totalSeconds < highscores[gameIndex])) {
            highscores[gameIndex] = totalSeconds;
            saveHighscore();
        }
        StdDraw.setFont(TILEFONT);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile t = board[x][y];
                if (t.mine) {
                    StdDraw.setPenColor(UNREVEALEDCOLOR);
                    StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
                    StdDraw.setPenColor(Color.YELLOW);
                    StdDraw.text(x + 0.5, y + 0.4, "\u263A");
                }
            }
        }
        StdDraw.show();
        boolean mPressed = false;
        while (true) {
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    return;
                } else {
                    mPressed = false;
                }
            }
            StdDraw.pause(PAUSETIME);
        }
    }
    public void initialize(int w, int h, int m) {
        this.width = w;
        this.height = h;
        this.minesTotal = m;
        this.saved = true;
        StdDraw.setCanvasSize(width * TILE_SIZE, (height + 1) * TILE_SIZE);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height + 1);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear(BACKGROUNDCOLOR);
        StdDraw.setPenRadius(0.005);
    }
    public void drawBoard() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                drawTile(x, y);
            }
        }
    }
    public void drawTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        Tile t = board[x][y];
        if (t.visibility == 0) {
            StdDraw.setPenColor(UNREVEALEDCOLOR);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
//            StdDraw.setPenColor(Color.WHITE);
//            StdDraw.line(x+0.1, y+0.9, x+0.9, y+0.9);
//            StdDraw.line(x+0.1, y+0.1, x+0.1, y+0.9);
//            StdDraw.setPenColor(Color.GRAY);
//            StdDraw.line(x+0.1, y+0.1, x+0.9, y+0.1);
//            StdDraw.line(x+0.9, y+0.1, x+0.9, y+0.9);
        } else if (t.visibility == 1) {
            StdDraw.setPenColor(UNREVEALEDCOLOR);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
//            StdDraw.setPenColor(Color.WHITE);
//            StdDraw.line(x+0.05, y+0.9, x+0.9, y+0.9);
//            StdDraw.line(x+0.05, y+0.05, x+0.05, y+0.9);
//            StdDraw.setPenColor(Color.GRAY);
//            StdDraw.line(x+0.05, y+0.05, x+0.9, y+0.05);
//            StdDraw.line(x+0.9, y+0.05, x+0.9, y+0.9);
            drawFlag(x, y);
        } else {
            StdDraw.setPenColor(BACKGROUNDCOLOR);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.5, 0.5);
            StdDraw.setPenColor(REVEALEDCOLOR);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
            if (t.mine) {
                drawMine(x, y);
            } else {
                if (t.number > 0) {
                    if (t.number < 10) {
                        StdDraw.setPenColor(COLORLIST[t.number - 1]);
                        StdDraw.setFont(TILEFONT);
                    } else if (t.number <= COLORLIST.length) {
                        StdDraw.setPenColor(COLORLIST[t.number - 1]);
                        StdDraw.setFont(SMALLTILEFONT);
                    } else {
                        StdDraw.setPenColor(Color.BLACK);
                        StdDraw.setFont(SMALLTILEFONT);
                    }
                    StdDraw.text(x + 0.5, y + 0.4, String.valueOf(t.number));
                }
            }
        }
    }
    public void drawFlag(int x, int y) {
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(x + 0.5, y + 0.15, 0.25, 0.05);
        StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.05, 0.3);
        StdDraw.setPenColor(Color.RED);
        StdDraw.filledPolygon(new double[]{x + 0.5, x + 0.5, x + 0.2}, new double[]{y + 0.5, y + 0.8, y + 0.65});
    }
    public void drawMine(int x, int y) {
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.setFont(MINEFONT);
        StdDraw.text(x + 0.5, y + 0.38, "\u26EF");
        StdDraw.filledCircle(x + 0.5, y + 0.5, 0.25);
    }
    public boolean invertTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        boolean changed = false;
        Tile t = board[x][y];
        if (t.visibility == 0) {
            changed = true;
            t.visibility = 1;
            minesMarked++;
        } else if (t.visibility == 1) {
            changed = true;
            t.visibility = 0;
            minesMarked--;
        }
        drawTile(x, y);
        return changed;
    }
    public boolean hideTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        boolean changed = false;
        Tile t = board[x][y];
        if (t.visibility == 2 & !t.mine) {
            changed = true;
            t.visibility = 0;
            revealedTotal--;
        }
        drawTile(x, y);
        return changed;
    }
    public void safeFirstClick(int x, int y) {
        boolean success = false;
        while (!success) {
            populateBoard(minesTotal);
            if (countMines(x, y) == 0) {
                success = true;
            }
        }
        firstClick = false;
    }
    public boolean revealTile(Tile t, boolean first) {
//        if (x < 0 || x >= width || y < 0 || y >= height) {
//            return false;
//        }
        boolean changed = false;
//        Tile t = board[x][y];
        if (t.visibility == 0) {
//            if (t.mine) {
//                mistakes++;
//                saveGame();
//            }
            changed = true;
            t.visibility = 2;
            StdDraw.setPenColor(Color.GRAY);
            if (t.mine) {
                gameOver = true;
            } else {
                revealedTotal++;
                int c = countMines(t.x, t.y);
                t.number = c;
                if (c == 0) {
                    for (Tile tile : revealAround(t.x, t.y)) {
                        revealTile(tile, true);
                    }
                }
            }
            drawTile(t.x, t.y);
        }
//        else if (first && t.visibility == 2) {
//            if (t.number > 0 && countFlags(x, y) >= t.number) {
//                if (revealAround(x, y)) {
//                    changed = true;
//                }
//            }
//        }
        return changed;
    }
    public ArrayList<Tile> revealAround(int x, int y) {
        ArrayList<Tile> toChange = new ArrayList<>();
        int xUpper = Math.min(width - 1, x + 1 + mode);
        int yUpper = Math.min(height - 1, y + 1 + mode);
        for (int i = Math.max(x - 1 - mode, 0); i <= xUpper; i++) {
            for (int j = Math.max(y - 1 - mode, 0); j <= yUpper; j++) {
                if (board[i][j].visibility == 0) {
                    toChange.add(board[i][j]);
                }
            }
        }
        return toChange;
    }
    public int countMines(int x, int y) {
        int total = 0;
        for (int i = x - 1 - mode; i <= x + 1 + mode; i++) {
            for (int j = y - 1 - mode; j <= y + 1 + mode; j++) {
                if (mineAt(i, j)) {
                    total++;
                }
            }
        }
        return total;
    }
    public int countFlags(int x, int y) {
        int total = 0;
        for (int i = x - 1 - mode; i <= x + 1 + mode; i++) {
            for (int j = y - 1 - mode; j <= y + 1 + mode; j++) {
                if (flagAt(i, j)) {
                    total++;
                }
            }
        }
        return total;
    }
    public void populateBoard(int mineCount) {
        ArrayList<Tile> tileArrayList;
        boolean impossible = true;
        while (impossible) {
            tileArrayList = new ArrayList<>();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Tile newTile = new Tile(x, y, false);
                    board[x][y] = newTile;
                    tileArrayList.add(newTile);
                }
            }
            for (int i = 0; i < mineCount; i++) {
                int index = random.nextInt(tileArrayList.size());
                Tile toPlant = tileArrayList.remove(index);
                toPlant.mine = true;
            }
            if (!checkImpossibleBoard()) {
                impossible = false;
            }
//            testingNum++;
        }
    }
    public boolean checkImpossibleBoard() {
        if (mode < 2) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (checkImpossible2x2(x, y) || checkImpossible1x2(x, y) ||checkImpossible2x1(x, y)) {
                        return true;
                    }
                }
            }
        } else {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (checkImpossible2x2(x, y)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean checkImpossible2x2(int x, int y) {
        if (x >= width - 1 || y >= height - 1) {
            return false;
        }
        if ((mineAt(x, y) && mineAt(x + 1, y + 1) && !mineAt(x + 1, y) && !mineAt(x, y + 1))
                || (!mineAt(x, y) && !mineAt(x + 1, y + 1) && mineAt(x + 1, y) && mineAt(x, y + 1))) {
            return mineOrOut(x - 1 - mode, y - 1 - mode) && mineOrOut(x + 2 + mode, y - 1 - mode)
                    && mineOrOut(x - 1 - mode, y + 2 + mode) && mineOrOut(x + 2 + mode, y + 2 + mode);
        }
        return false;
    }
    public boolean checkImpossible1x2(int x, int y) {
        if (x >= width - 1 || y >= height) {
            return false;
        }
        if ((mineAt(x, y) && !mineAt(x + 1, y)) || (!mineAt(x, y) && mineAt(x + 1, y))) {
            boolean imposs = true;
            for (int i = y - 1 - mode; i <= y + 1 + mode; i++) {
                if (!mineOrOut(x - 1 - mode, i)) {
                    imposs = false;
                }
            }
            for (int i = y - 1 - mode; i <= y + 1 + mode; i++) {
                if (!mineOrOut(x + 2 + mode, i)) {
                    imposs = false;
                }
            }
            return imposs;
        }
        return false;
    }
    public boolean checkImpossible2x1(int x, int y) {
        if (x >= width || y >= height - 1) {
            return false;
        }
        if ((mineAt(x, y) && !mineAt(x, y + 1)) || (!mineAt(x, y) && mineAt(x, y + 1))) {
            boolean imposs = true;
            for (int i = x - 1 - mode; i <= x + 1 + mode; i++) {
                if (!mineOrOut(i, y - 1 - mode)) {
                    imposs = false;
                }
            }
            for (int i = x - 1 - mode; i <= x + 1 + mode; i++) {
                if (!mineOrOut(i, y + 2 + mode)) {
                    imposs = false;
                }
            }
            return imposs;
        }
        return false;
    }

    public boolean mineAt(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return board[x][y].mine;
        } else {
            return false;
        }
    }
    public boolean mineOrOut(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return board[x][y].mine;
        } else {
            return true;
        }
    }
    public boolean flagAt(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return (board[x][y].visibility == 1);
        } else {
            return false;
        }
    }
}
