import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class GameBoard {
    public static final int TILE_SIZE = 20;
    public static final Font tileFont = new Font("Dialog", Font.BOLD, 16);
    public static final Font smallTileFont = new Font("Dialog", Font.BOLD, 14);
    public static final Font mineFont = new Font("Dialog", Font.BOLD, 22);
    public static final Color unrevealedColor = new Color(180, 180, 180, 255);
    public static final Color revealedColor = new Color(150, 150, 150, 255);
    public static final Color[] colorList = {Color.BLUE, new Color(0, 100, 0), Color.RED, new Color(0, 0, 80), new Color(80, 0, 0), new Color(0, 110, 110), Color.BLACK, Color.GRAY, new Color(64, 0, 128)};
    private int width;
    private int height;
    private int minesMarked;
    private int minesTotal;
    private int revealedTotal;
    private Random random = new Random();
    Tile[][] board;
    private boolean gameOver;
    private int difficulty;
    private String[] difficultyList = {"Easy", "Medium", "Hard", "Extreme"};
    private String mode;
    private boolean firstClick;
    private int worldsGenerated = 0;
    public class Tile {
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
        this.mode = mode;
        board = new Tile[width][height];
    }
    public boolean mouseInBox(double x, double y, double halfWidth, double halfHeight) {
        double mouseX = StdDraw.mouseX();
        double mouseY = StdDraw.mouseY();
        return (x - halfWidth <= mouseX && y - halfHeight <= mouseY && mouseX <= x + halfWidth && mouseY <= y + halfHeight);
    }
    public void mainMenu() {
        StdDraw.setCanvasSize(60 * TILE_SIZE, 31 * TILE_SIZE);
        StdDraw.setXscale(0, 60);
        StdDraw.setYscale(0, 31);
        StdDraw.enableDoubleBuffering();
        renderMainMenu();
        boolean mPressed = false;
        while (true) {
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    if (mouseInBox(30, 18, 5, 1)) {
                        difficulty = (difficulty + 1) % 4;
                        renderMainMenu();
                    }
                    else if (mouseInBox(30, 15, 5, 1)) {
                        if (mode == "Normal") {
                            mode = "Far";
                        } else if (mode == "Far") {
                            mode = "Normal";
                        }
                        renderMainMenu();
                    } else if (mouseInBox(30, 12, 5, 1)) {
                        int w = 0, h = 0, m = 0;
                        if (difficulty == 0) {
                            w = 10;
                            h = 10;
                            m = 10;
                        } else if (difficulty == 1) {
                            w = 30;
                            h = 20;
                            m = 80;
                        } else if (difficulty == 2) {
                            w = 50;
                            h = 25;
                            m = 200;
                        } else {
                            w = 60;
                            h = 30;
                            m = 300;
                        }
                        m = m * 3 / 4;
                        runGame(w, h, m);
                    }
                }
                mPressed = false;
            }
        }
    }
    public void renderMainMenu() {
        StdDraw.clear(Color.LIGHT_GRAY);
        StdDraw.setPenColor(Color.GRAY);
        StdDraw.filledRectangle(30, 18, 5, 1);
        StdDraw.filledRectangle(30, 15, 5, 1);
        StdDraw.filledRectangle(30, 12, 5, 1);
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.text(30, 18, "Difficulty: " + difficultyList[difficulty]);
        StdDraw.text(30, 15, "View: " + mode);
        StdDraw.text(30, 12, "Start Game");
        StdDraw.show();
    }
    public void runGame(int w, int h, int m) {
        initialize(w, h, m);
        populateBoard(minesTotal);
        boolean mPressed = false;
        gameOver = false;
        firstClick = true;
        worldsGenerated = 0;
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
                    if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                        invertTile(x, y);
                    } else {
                        if (firstClick) {
                            safeFirstClick(x, y);
                        }
                        revealTile(x, y, true);
                        if (revealedTotal + minesTotal == width * height) {
                            gameOver = true;
                        }
                    }
                    header();
                    StdDraw.show();
                }
                mPressed = false;
            }
        }
        if (revealedTotal + minesTotal == width * height) {
            wonGame();
        } else {
            showSolution();
        }
        StdDraw.show();
        while (true) {
            if (StdDraw.isMousePressed()) {
                mPressed = true;
            } else {
                if (mPressed) {
                    mainMenu();
                } else {
                    mPressed = false;
                }
            }
        }
    }
    public void header() {
        StdDraw.setPenColor(Color.DARK_GRAY);
        StdDraw.filledRectangle(width / 2, height + 0.5, width / 2, 0.5);
        StdDraw.setFont(tileFont);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textLeft(1, height + 0.4, "Mines left: " + (minesTotal - minesMarked));
        //StdDraw.textLeft(8, height + 0.4, "" + revealedTotal);
        StdDraw.show();
    }
    public void showSolution() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile t = board[x][y];
                if (t.visibility == 0) {
                    t.visibility = 2;
                    drawTile(x, y);
                } else if (t.visibility == 1 && !t.mine) {
                    StdDraw.setPenColor(Color.RED);
                    StdDraw.setPenRadius(0.005);
                    StdDraw.line(x + 0.2, y + 0.2, x + 0.8, y + 0.8);
                    StdDraw.line(x + 0.8, y + 0.2, x + 0.2, y + 0.8);
                }
            }
        }
    }
    public void wonGame() {
        StdDraw.setFont(tileFont);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile t = board[x][y];
                if (t.mine) {
                    StdDraw.setPenColor(unrevealedColor);
                    StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
                    StdDraw.setPenColor(Color.YELLOW);
                    StdDraw.text(x + 0.5, y + 0.4, "☺");
                }
            }
        }
    }
    public void initialize(int w, int h, int m) {
        this.width = w;
        this.height = h;
        this.minesTotal = m;
        StdDraw.setCanvasSize(width * TILE_SIZE, (height + 1) * TILE_SIZE);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height + 1);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear(Color.BLACK);
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
            StdDraw.setPenColor(unrevealedColor);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
        } else if (t.visibility == 1) {
            StdDraw.setPenColor(unrevealedColor);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
            drawFlag(x, y);
        } else {
            StdDraw.setPenColor(revealedColor);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
            if (t.mine) {
                drawMine(x, y);
            } else {
                if (t.number > 0) {
                    if (t.number < 10) {
                        StdDraw.setPenColor(colorList[t.number - 1]);
                        StdDraw.setFont(tileFont);
                    } else {
                        StdDraw.setPenColor(Color.BLACK);
                        StdDraw.setFont(smallTileFont);
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
        StdDraw.setFont(mineFont);
        StdDraw.text(x + 0.5, y + 0.38, "⛯");
        StdDraw.filledCircle(x + 0.5, y + 0.5, 0.25);
    }
    public void invertTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        Tile t = board[x][y];
        if (t.visibility == 0) {
            t.visibility = 1;
            minesMarked++;
        } else if (t.visibility == 1) {
            t.visibility = 0;
            minesMarked--;
        }
        drawTile(x, y);
    }
    public void safeFirstClick(int x, int y) {
        boolean success = false;
        while (!success) {
            populateBoard(minesTotal);
            if (countMines(x, y) == 0) {
                success = true;
            }
            worldsGenerated++;
        }
        firstClick = false;
    }
    public void revealTile(int x, int y, boolean first) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        Tile t = board[x][y];
        if (t.visibility == 0) {
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
                revealAround(x, y);
            }
        }
    }
    public void revealAround(int x, int y) {
        if (mode.equals("Normal")) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    revealTile(i, j, false);
                }
            }
        } else if (mode.equals("Far")) {
            for (int i = x - 2; i <= x + 2; i++) {
                for (int j = y - 2; j <= y + 2; j++) {
                    revealTile(i, j, false);
                }
            }
        }
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
