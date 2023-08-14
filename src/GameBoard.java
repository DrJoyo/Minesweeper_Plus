import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

public class GameBoard {
    public static final int TILE_SIZE = 20;
    public final Font tileFont = new Font("Dialog", Font.BOLD, 16);
    public final Color unrevealedColor = new Color(180, 180, 180, 255);
    public final Color revealedColor = new Color(150, 150, 150, 255);
    public final Color[] colorList = {Color.BLUE, new Color(0, 100, 0), Color.RED, new Color(0, 0, 80), new Color(80, 0, 0), new Color(0, 110, 110), Color.BLACK, Color.GRAY};
    private int width;
    private int height;
    private int minesFound;
    private int minesTotal;
    private Random random = new Random();
    Tile[][] board;
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
    public GameBoard(int w, int h, int mineCount) {
        this.width = w;
        this.height = h;
        this.minesFound = 0;
        this.minesTotal = mineCount;
        board = new Tile[width][height];
        populateBoard(mineCount);
    }
    public void runGame() {
        initialize();
        drawBoard();
        StdDraw.show();
        while (true) {
            if (StdDraw.isMousePressed()) {
                revealTile((int) StdDraw.mouseX(), (int) StdDraw.mouseY());
                StdDraw.show();
            }
        }
    }
    public void initialize() {
        StdDraw.setCanvasSize(width * TILE_SIZE, height * TILE_SIZE);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
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
        StdDraw.setFont(tileFont);
        if (t.visibility == 0) {
            StdDraw.setPenColor(unrevealedColor);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
        } else if (t.visibility == 1) {
            StdDraw.setPenColor(unrevealedColor);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.text(x + 0.5, x + 0.4, "F");
        } else {
            StdDraw.setPenColor(revealedColor);
            StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.45, 0.45);
            if (t.mine) {
                StdDraw.setPenColor(Color.BLACK);
                StdDraw.filledCircle(x + 0.5, y + 0.5, 0.35);
            } else {
                if (t.number > 0) {
                    StdDraw.setPenColor(colorList[t.number - 1]);
                    StdDraw.text(x + 0.5, y + 0.4, String.valueOf(t.number));
                }
            }
        }
    }
    public void revealTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        Tile t = board[x][y];
        if (t.visibility < 2) {
            t.visibility = 2;
            StdDraw.setPenColor(Color.GRAY);
            if (t.mine) {
                ;
            } else {
                int c = countMines(x, y);
                t.number = c;
                if (c == 0) {
                    for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j <= y + 1; j++) {
                            revealTile(i, j);
                        }
                    }
                }
            }
            drawTile(x, y);
        }
    }
    public int countMines(int x, int y) {
        int total = 0;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (mineAt(i, j)) {
                    total++;
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
}
