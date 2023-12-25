import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class GameAxes implements Serializable {
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
    public static final Color[] COLORLIST = {Color.BLUE, new Color(0, 100, 0), Color.RED, new Color(0, 0, 80), new Color(80, 0, 0), new Color(0, 110, 110), Color.BLACK, new Color(100, 100, 100), new Color(64, 0, 128), new Color(255, 255, 0), new Color(0, 60, 0), new Color(100, 50, 0), new Color(255, 120, 150), new Color(200, 100, 0), new Color(128, 0, 255), new Color(240, 240, 240)};
    public static final String[] DIFFICULTYLIST = {"Easy", "Medium", "Hard", "Extreme", "Impossible"};
    public static final Color[] DIFFICULTYCOLORLIST = {new Color(40, 150, 255), Color.GREEN, Color.YELLOW, new Color(255, 128, 0), Color.RED};
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final int PAUSETIME = 30;
    private int width;
    private int height;
    private int minesMarked;
    private int minesTotal;
    private int revealedTotal;
    private Random random = new Random();
    Tile[][] board;
    private boolean gameOver;
    private int difficulty;
    private String mode;
    private boolean firstClick;
    private int testingNum = 0;
    private long startTime;
    private int totalSeconds;
    private int loadSeconds;
    private int mistakes;
    private boolean saved;
    private boolean autosave;
    private boolean highlight;
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
        GameAxes g = new GameAxes(30, 24, 100, 0, "Plus");
        g.startGame(); // runs game
    }
    public GameAxes(int w, int h, int mineCount, int difficulty, String mode) {
        this.width = w;
        this.height = h;
        this.minesMarked = 0;
        this.minesTotal = mineCount;
        this.difficulty = difficulty;
        this.mode = mode;
        this.autosave = true;
        this.highlight = false;
        this.firstClick = true;
        this.totalSeconds = 0;
        this.mistakes = 0;
    }
    public void readHighscores() {
        File saveFile = Utils.join(CWD, "Highscores.txt");
        try {
            highscores = Utils.readObject(saveFile, int[].class);
        } catch (Exception e) {
            highscores = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
            Utils.writeObject(saveFile, highscores);
        }
    }
    public void startGame() {
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
        if (highlight) {
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            StdDraw.clear(BACKGROUNDCOLOR);
            drawBoard();
            header();
            if (mouseX > 0 && mouseY > 0 && mouseX <= width && mouseY <= height && board[mouseX-1][mouseY-1].visibility == 2) {
                StdDraw.setPenColor(HIGHLIGHTCOLOR);
                if (mode.equals("Normal")) {
                    StdDraw.filledRectangle(mouseX + 0.5, mouseY + 0.5, 1.5, 1.5);
                } else {
                    StdDraw.filledRectangle(mouseX + 0.5, mouseY + 0.5, 2.5, 2.5);
                }
            }
            StdDraw.show();
        }
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
                        int w = 0, h = 0, m = 0;
                        if (mode.equals("Normal")) {
                            if (difficulty == 0) {
                                w = 10;
                                h = 10;
                                m = 10;
                            } else if (difficulty == 1) {
                                w = 16;
                                h = 16;
                                m = 40;
                            } else if (difficulty == 2) {
                                w = 30;
                                h = 16;
                                m = 99;
                            } else if (difficulty == 3) {
                                w = 30;
                                h = 24;
                                m = 160;
                            } else {
                                w = 40;
                                h = 30;
                                m = 280;
                            }
                        } else {
                            if (difficulty == 0) {
                                w = 10;
                                h = 10;
                                m = 7;
                            } else if (difficulty == 1) {
                                w = 16;
                                h = 16;
                                m = 30;
                            } else if (difficulty == 2) {
                                w = 30;
                                h = 16;
                                m = 75;
                            } else if (difficulty == 3) {
                                w = 30;
                                h = 24;
                                m = 120;
                            } else {
                                w = 40;
                                h = 30;
                                m = 240;
                            }
                        }
                        this.totalSeconds = 0;
                        this.mistakes = 0;
                        runGame(w, h, m);
                        break;
                    } else if (mouseInBox(width / 2, height / 2 + 2, 5, 1)) {
                        difficulty = (difficulty + 1) % 5;
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 1, 5, 1)) {
                        if (mode.equals("Normal")) {
                            mode = "Plus";
                        } else if (mode.equals("Plus")) {
                            mode = "Normal";
                        }
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 4, 5, 1)) {
                        this.autosave = !this.autosave;
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 7, 5, 1)) {
                        this.highlight = !this.highlight;
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 10, 5, 1)) {
                        highscoreScreen();
                        break;
                    }
                }
                mPressed = false;
            }
            StdDraw.pause(PAUSETIME);
        }
    }
    public void renderMainMenu() {
        StdDraw.clear(Color.LIGHT_GRAY);
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
        StdDraw.text(width / 2, height / 2 - 1, "Mode: " + mode);
        StdDraw.text(width / 2, height / 2 - 4, "Autosave: " + autosave);
        StdDraw.text(width / 2, height / 2 - 7, "Highlight: " + highlight);
        StdDraw.text(width / 2, height / 2 - 10, "High Scores");
//        StdDraw.text(width / 2, height / 2 - 10, "Quit Game");
        StdDraw.setFont(MINEFONT);
        for (int i = 0; i < difficulty + 1; i++) {
            StdDraw.text(width / 2 + 6 + i, height / 2 + 1.88, "\u26EF");
            StdDraw.filledCircle(width / 2 + 6 + i, height / 2 + 2, 0.25);
        }
        StdDraw.show();
    }
    public void highscoreScreen() {
        StdDraw.clear(Color.LIGHT_GRAY);
        StdDraw.setFont(MENUFONT);
        StdDraw.text(width / 2, height / 2 + 9, "High Scores");
        StdDraw.setFont(GAMEOVERFONT);
        StdDraw.text(width / 2 - 6, height / 2 + 4, "Normal");
        StdDraw.text(width / 2 + 6, height / 2 + 4, "Plus");
        StdDraw.setFont(TILEFONT);
        for (int i = 0; i < 5; i++) {
            String score;
            if (highscores[i] >= 0) {
                score = "" + highscores[i];
            } else {
                score = "N/A";
            }
//            StdDraw.setPenColor(DIFFICULTYCOLORLIST[i]);
            StdDraw.text(width / 2 - 6, height / 2 + 2 - 2 * i, DIFFICULTYLIST[i] + ": " + score);
        }
        for (int i = 0; i < 5; i++) {
            String score;
            if (highscores[i + 5] >= 0) {
                score = "" + highscores[i + 5];
            } else {
                score = "N/A";
            }
//            StdDraw.setPenColor(DIFFICULTYCOLORLIST[i]);
            StdDraw.text(width / 2 + 6, height / 2 + 2 - 2 * i, DIFFICULTYLIST[i] + ": " + score);
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
            testingNum++;
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    int x = (int) StdDraw.mouseX();
                    int y = (int) StdDraw.mouseY();
                    if (y <= height) {
                        if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                            if (invertTile(x, y)) {
                                if (autosave) {
                                    saveGame();
                                } else {
                                    this.saved = false;
                                }
                            }
                        } else if (StdDraw.isKeyPressed(KeyEvent.VK_H)) {
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
                            if (revealTile(x, y, true)) {
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
                        }
                    } else {
                        int width = this.width + 1;
                        int height = this.height + 1;
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
            if (highlight) {
                drawHighlight();
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
        int width = this.width + 1;
        int height = this.height + 1;
        StdDraw.setPenColor(Color.DARK_GRAY);
        StdDraw.filledRectangle(width / 2, height + 0.5, width / 2, 0.5);
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
//        StdDraw.textLeft(3, height + 0.4, "" + testingNum);
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
        String saveString = DIFFICULTYLIST[difficulty] + mode + "Save.txt";
        File saveFile = Utils.join(CWD, saveString);
        Utils.writeObject(saveFile, this);
        this.saved = true;
    }
    public void loadGame() {
        String saveString = DIFFICULTYLIST[difficulty] + mode + "Save.txt";
        File saveFile = Utils.join(CWD, saveString);
        try {
            GameAxes loaded = Utils.readObject(saveFile, GameAxes.class);
        } catch (Exception e) {
            return;
        }
        GameAxes loaded = Utils.readObject(saveFile, GameAxes.class);
        this.width = loaded.width;
        this.height = loaded.height;
        this.minesMarked = loaded.minesMarked;
        this.minesTotal = loaded.minesTotal;
        this.revealedTotal = loaded.revealedTotal;
        this.board = loaded.board;
        this.gameOver = loaded.gameOver;
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
        StdDraw.filledRectangle(width / 2, height + 1.5, width / 2 + 1, 0.5);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(TILEFONT);
        StdDraw.text(width / 2, height + 1.4, "Solution");
        for (int x = 1; x <= width; x++) {
            for (int y = 1; y <= height; y++) {
                Tile t = board[x-1][y-1];
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
        int gameIndex;
        if (mode.equals("Normal")) {
            gameIndex = difficulty;
        } else {
            gameIndex = 5 + difficulty;
        }
        if (mistakes == 0 && (highscores[gameIndex] < 0 || totalSeconds < highscores[gameIndex])) {
            highscores[gameIndex] = totalSeconds;
            File saveFile = Utils.join(CWD, "Highscores.txt");
            Utils.writeObject(saveFile, highscores);
        }
        StdDraw.setFont(TILEFONT);
        for (int x = 1; x <= width; x++) {
            for (int y = 1; y <= height; y++) {
                Tile t = board[x-1][y-1];
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
        StdDraw.setCanvasSize((width + 1) * TILE_SIZE, (height + 2) * TILE_SIZE);
        StdDraw.setXscale(0, width + 1);
        StdDraw.setYscale(0, height + 2);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear(BACKGROUNDCOLOR);
        StdDraw.setPenRadius(0.005);
    }
    public void drawBoard() {
        for (int x = 1; x <= width; x++) {
            for (int y = 1; y <= height; y++) {
                drawTile(x, y);
            }
        }
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(SMALLTILEFONT);
        for (int x = 5; x <= width; x += 5) {
            StdDraw.text(x + 0.5, 0.5, "" + x);
        }
        for (int y = 5; y <= height; y += 5) {
            StdDraw.text(0.5, y + 0.5, "" + y);
        }
    }
    public void drawTile(int x, int y) {
        if (x < 0 || x >= width + 1 || y < 0 || y >= height + 1) {
            return;
        }
        Tile t = board[x-1][y-1];
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
                    } else if (t.number < 17) {
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
        if (x <= 0 || x > width || y <= 0 || y > height) {
            return false;
        }
        boolean changed = false;
        Tile t = board[x-1][y-1];
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
        if (x <= 0 || x > width || y <= 0 || y > height) {
            return false;
        }
        boolean changed = false;
        Tile t = board[x-1][y-1];
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
    public boolean revealTile(int x, int y, boolean first) {
        if (x < 1 || x > width || y < 1 || y > height) {
            return false;
        }
        boolean changed = false;
        Tile t = board[x-1][y-1];
        if (t.visibility == 0) {
            if (t.mine) {
                mistakes++;
                saveGame();
            }
            changed = true;
            t.visibility = 2;
            revealedTotal++;
            StdDraw.setPenColor(Color.GRAY);
            if (t.mine) {
                gameOver = true;
            } else {
                int c = countMines(x, y);
                t.number = c;
                if (c == 0) {
                    revealAround(x, y);
                }
            }
            drawTile(x, y);
        } else if (first && t.visibility == 2) {
            if (t.number > 0 && countFlags(x, y) >= t.number) {
                if (revealAround(x, y)) {
                    changed = true;
                }
            }
        }
        return changed;
    }
    public boolean revealAround(int x, int y) {
        boolean changed = false;
        if (mode.equals("Normal")) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (revealTile(i, j, false)) {
                        changed = true;
                    }
                }
            }
        } else if (mode.equals("Plus")) {
            for (int i = x - 2; i <= x + 2; i++) {
                for (int j = y - 2; j <= y + 2; j++) {
                    if (revealTile(i, j, false)) {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
    public int countMines(int x, int y) {
        int total = 0;
        if (mode.equals("Normal")) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (mineAt(i, j)) {
                        total++;
                    }
                }
            }
        } else if (mode.equals("Plus")) {
            for (int i = x - 2; i <= x + 2; i++) {
                for (int j = y - 2; j <= y + 2; j++) {
                    if (mineAt(i, j)) {
                        total++;
                    }
                }
            }
        }
        return total;
    }
    public int countFlags(int x, int y) {
        int total = 0;
        if (mode.equals("Normal")) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (flagAt(i, j)) {
                        total++;
                    }
                }
            }
        } else if (mode.equals("Plus")) {
            for (int i = x - 2; i <= x + 2; i++) {
                for (int j = y - 2; j <= y + 2; j++) {
                    if (flagAt(i, j)) {
                        total++;
                    }
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
        }
    }
    public boolean checkImpossibleBoard() {
        if (mode.equals("Normal")) {
            return false;
        }
        for (int x = 1; x <= width - 1; x++) {
            for (int y = 1; y <= height - 1; y++) {
                if (checkImpossibleSpot(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean checkImpossibleSpot(int x, int y) {
        if ((mineAt(x, y) && mineAt(x + 1, y + 1) && !mineAt(x + 1, y) && !mineAt(x, y + 1))
                || (!mineAt(x, y) && !mineAt(x + 1, y + 1) && mineAt(x + 1, y) && mineAt(x, y + 1))) {
            return mineOrOut(x - 2, y - 2) && mineOrOut(x + 3, y - 2) && mineOrOut(x - 2, y + 3) && mineOrOut(x + 3, y + 3);
        }
        return false;
    }
    public boolean mineAt(int x, int y) {
        if (x > 0 && x <= width && y > 0 && y <= height) {
            return board[x-1][y-1].mine;
        } else {
            return false;
        }
    }
    public boolean mineOrOut(int x, int y) {
        if (x > 0 && x <= width && y > 0 && y <= height) {
            return board[x-1][y-1].mine;
        } else {
            return false;
        }
    }
    public boolean flagAt(int x, int y) {
        if (x > 0 && x <= width && y > 0 && y <= height) {
            return (board[x-1][y-1].visibility == 1);
        } else {
            return false;
        }
    }
}
