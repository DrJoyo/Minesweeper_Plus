import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class GameBoard implements Serializable {
    public static final int TILE_SIZE = 20;
    public static final Font TILEFONT = new Font("Dialog", Font.BOLD, 16);
    public static final Font SMALLTILEFONT = new Font("Dialog", Font.BOLD, 14);
    public static final Font MINEFONT = new Font("Dialog", Font.BOLD, 22);
    public static final Font GAMEOVERFONT = new Font("Dialog", Font.BOLD, 32);
    public static final Font MENUFONT = new Font("Dialog", Font.BOLD, 48);
    public static final Color UNREVEALEDCOLOR = new Color(180, 180, 180, 255);
    public static final Color REVEALEDCOLOR = new Color(150, 150, 150, 255);
    public static final Color SAVELOADCOLOR = new Color(40, 40, 40);
    public static final Color LOSECOLOR = new Color(255, 0, 0, 50);
    public static final Color[] COLORLIST = {Color.BLUE, new Color(0, 100, 0), Color.RED, new Color(0, 0, 80), new Color(80, 0, 0), new Color(0, 110, 110), Color.BLACK, Color.GRAY, new Color(64, 0, 128)};
    public static final String[] DIFFICULTYLIST = {"Easy", "Medium", "Hard", "Extreme", "Impossible"};
    public static final Color[] DIFFICULTYCOLORLIST = {new Color(40, 150, 255), Color.GREEN, Color.YELLOW, new Color(255, 128, 0), Color.RED};
    public static final File CWD = new File(System.getProperty("user.dir"));
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
    private boolean saved;
    private boolean autosave;
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
    public GameBoard(int w, int h, int mineCount, int difficulty, String mode) {
        this.width = w;
        this.height = h;
        this.minesMarked = 0;
        this.minesTotal = mineCount;
        this.difficulty = difficulty;
        this.mode = mode;
        this.autosave = true;
    }
    public boolean mouseInBox(double x, double y, double halfWidth, double halfHeight) {
        double mouseX = StdDraw.mouseX();
        double mouseY = StdDraw.mouseY();
        return (x - halfWidth <= mouseX && y - halfHeight <= mouseY && mouseX <= x + halfWidth && mouseY <= y + halfHeight);
    }
    public void mainMenu() {
        width = 30;
        height = 24;
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
                            m = 320;
                        }
                        if (mode.equals("Far")) {
                            m = m * 3 / 4;
                        }
                        runGame(w, h, m);
                    } else if (mouseInBox(width / 2, height / 2 + 2, 5, 1)) {
                        difficulty = (difficulty + 1) % 5;
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 1, 5, 1)) {
                        if (mode.equals("Normal")) {
                            mode = "Far";
                        } else if (mode.equals("Far")) {
                            mode = "Normal";
                        }
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 4, 5, 1)) {
                        this.autosave = !this.autosave;
                        renderMainMenu();
                    } else if (mouseInBox(width / 2, height / 2 - 7, 5, 1)) {
                        System.exit(0);
                    }
                }
                mPressed = false;
            }
            StdDraw.pause(5);
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
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.setFont(MENUFONT);
        StdDraw.text(width / 2, height / 2 + 9, "Minesweeper Plus");
        StdDraw.setFont(TILEFONT);
        StdDraw.text(width / 2, height / 2 + 5, "Start Game");
        StdDraw.text(width / 2, height / 2 + 2, "Difficulty: " + DIFFICULTYLIST[difficulty]);
        StdDraw.text(width / 2, height / 2 - 1, "View: " + mode);
        StdDraw.text(width / 2, height / 2 - 4, "Autosave: " + autosave);
        StdDraw.text(width / 2, height / 2 - 7, "Quit Game");
        StdDraw.setFont(MINEFONT);
        for (int i = 0; i < difficulty + 1; i++) {
            StdDraw.text(width / 2 + 6 + i, height / 2 + 1.88, "\u26EF");
            StdDraw.filledCircle(width / 2 + 6 + i, height / 2 + 2, 0.25);
        }
        StdDraw.show();
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
                    if (y < height) {
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
                        if (difficulty > 0) {
                            if (mouseInBox(width - 9.5, height + 0.5, 1.5, 0.5)) {
                                if (!autosave && !saved) {
                                    saveGame();
                                }
                            } else if (mouseInBox(width - 5.5, height + 0.5, 1.5, 0.5)) {
                                loadGame();
                            } else if (mouseInBox(width - 1.5, height + 0.5, 1.5, 0.5)) {
                                mainMenu();
                            }
                        } else {
                            if (mouseInBox(width - 6.5, height + 0.5, 1.125, 0.5)) {
                                if (!autosave && !saved) {
                                    saveGame();
                                }
                            } else if (mouseInBox(width - 3.75, height + 0.5, 1.125, 0.5)) {
                                loadGame();
                            } else if (mouseInBox(width - 1, height + 0.5, 1.125, 0.5)) {
                                mainMenu();
                            }
                        }
                    }
                    header();
                    StdDraw.show();
                }
                mPressed = false;
            }
            StdDraw.pause(5);
        }
        if (revealedTotal + minesTotal == width * height) {
            wonGame();
        } else {
            lostGame();
        }
        mainMenu();
    }
    public void header() {
        StdDraw.setPenColor(Color.DARK_GRAY);
        StdDraw.filledRectangle(width / 2, height + 0.5, width / 2, 0.5);
        StdDraw.setPenColor(SAVELOADCOLOR);
        if (difficulty > 0) {
            StdDraw.filledRectangle(width - 1.5, height + 0.5, 1.5, 0.5);
            StdDraw.filledRectangle(width - 5.5, height + 0.5, 1.5, 0.5);
            if (!autosave) {
                StdDraw.filledRectangle(width - 9.5, height + 0.5, 1.5, 0.5);
            }
        } else {
            StdDraw.filledRectangle(width - 1, height + 0.5, 1.125, 0.5);
            StdDraw.filledRectangle(width - 3.75, height + 0.5, 1.125, 0.5);
            if (!autosave) {
                StdDraw.filledRectangle(width - 6.5, height + 0.5, 1.125, 0.5);
            }
        }
        drawMine(0, height);
        StdDraw.setFont(TILEFONT);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textLeft(1, height + 0.4, "" + (minesTotal - minesMarked));
        if (difficulty > 0) {
            StdDraw.text(width - 1.5, height + 0.4, "Menu");
            StdDraw.text(width - 5.5, height + 0.4, "Load");
            if (!autosave) {
                if (saved) {
                    StdDraw.text(width - 9.5, height + 0.4, "Save");
                } else {
                    StdDraw.text(width - 9.5, height + 0.4, "Save*");
                }
            }
        } else {
            StdDraw.text(width - 1, height + 0.4, "Menu");
            StdDraw.text(width - 3.75, height + 0.4, "Load");
            if (!autosave) {
                if (saved) {
                    StdDraw.text(width - 6.5, height + 0.4, "Save");
                } else {
                    StdDraw.text(width - 6.5, height + 0.4, "Save*");
                }
            }
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
            GameBoard loaded = Utils.readObject(saveFile, GameBoard.class);
        } catch (Exception e) {
            return;
        }
        GameBoard loaded = Utils.readObject(saveFile, GameBoard.class);
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
            StdDraw.pause(5);
        }
    }
    public void showSolution() {
        StdDraw.clear(Color.BLACK);
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
            StdDraw.pause(5);
        }
    }
    public void wonGame() {
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
            StdDraw.pause(5);
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
        StdDraw.clear(Color.BLACK);
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
        } else if (t.visibility == 1) {
            StdDraw.setPenColor(UNREVEALEDCOLOR);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
            drawFlag(x, y);
        } else {
            StdDraw.setPenColor(REVEALEDCOLOR);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
            if (t.mine) {
                drawMine(x, y);
            } else {
                if (t.number > 0) {
                    if (t.number < 10) {
                        StdDraw.setPenColor(COLORLIST[t.number - 1]);
                        StdDraw.setFont(TILEFONT);
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
        if (t.visibility == 2) {
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
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        boolean changed = false;
        Tile t = board[x][y];
        if (t.visibility == 0) {
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
            if (t.number > 0 && countFlags(x, y) == t.number) {
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
        } else if (mode.equals("Far")) {
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
        } else if (mode.equals("Far")) {
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
        } else if (mode.equals("Far")) {
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
        ArrayList<Tile> tileArrayList = new ArrayList<>();
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
    }
    public boolean mineAt(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return board[x][y].mine;
        } else {
            return false;
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
