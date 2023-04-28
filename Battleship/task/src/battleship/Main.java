package battleship;

import java.util.Scanner;

public class Main {

    static Scanner scanner;
    static BattleField bf1;
    static Ship[] ships1 = new Ship[5];
    static BattleField bf2;
    static Ship[] ships2 = new Ship[5];
    static int playerMove = 1;

    public static void main(String[] args) {
        bf1 = new BattleField();
        bf2 = new BattleField();

        scanner = new Scanner(System.in);

        System.out.println("Player 1, place your ships on the game field");
        placeShips(bf1, ships1);
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("Player 2, place your ships to the game field");
        placeShips(bf2, ships2);
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();
        System.out.print("\033[H\033[2J");
        System.out.flush();

        startGame();

        scanner.close();
    }

    private static void startGame() {

        BattleField myBF;
        BattleField enemyBF;
        Ship[] enemyShips;

        while (true) {

            if (playerMove == 1) {
                myBF = bf1;
                enemyBF = bf2;
                enemyShips = ships2;
            } else {
                myBF = bf2;
                enemyBF = bf1;
                enemyShips = ships1;
            }

            enemyBF.print(false);
            System.out.println("---------------------");
            myBF.print(true);

            System.out.println("Player " + playerMove + ", it's your turn:");

            if (makeHit(enemyBF, enemyShips)) break;

            playerMove = 3 - playerMove;

            System.out.println("Press Enter and pass the move to another player");
            scanner.nextLine();
        }


    }

    // make a single hit. Returns true if the player won.
    private static boolean makeHit(BattleField bf, Ship[] ships) {
        Coordinate hit;

        // get hit coordinates
        while (true) {
            String move = scanner.nextLine();
            hit = getCoord(move);
            if (hit == null)
                System.out.println("\nError! You entered the wrong coordinates! Try again:\n");
            else break;
        }

        bf.board[hit.r][hit.c].isHit = true;

        if (bf.board[hit.r][hit.c].occupy != null) {
            if (bf.board[hit.r][hit.c].occupy.hit(hit)) {
                if (isFinished(ships)) {
                    System.out.println("\nYou sank the last ship. You won. Congratulations!\n");
                    return true;
                } else {
                    System.out.println("\nYou sank a ship! Specify a new target:\n");
                }
            } else {
                System.out.println("\nYou hit a ship! Try again:\n");
            }
        } else {
            System.out.println("\nYou missed. Try again:\n");
        }

        return false;
    }

    private static boolean isFinished(Ship[] ships) {
        for (Ship ship : ships)
            if (!ship.isSunk) return false;
        return true;
    }

    private static void placeShips(BattleField bf, Ship[] ships) {
        bf.print(true);

        ships[0] = new Ship("Aircraft Carrier", 5);
        place(ships[0], bf);
        bf.print(true);

        ships[1] = new Ship("Battleship", 4);
        place(ships[1], bf);
        bf.print(true);

        ships[2] = new Ship("Submarine", 3);
        place(ships[2], bf);
        bf.print(true);

        ships[3] = new Ship("Cruiser", 3);
        place(ships[3], bf);
        bf.print(true);

        ships[4] = new Ship("Destroyer", 2);
        place(ships[4], bf);
        bf.print(true);
    }

    private static void place(Ship ship, BattleField bf) {
        if (ship == null || bf == null) return;

        System.out.println("\nEnter the coordinates of the " +
                ship.name + " (" + ship.size + " cells):\n");


        while (true) {
            String[] inp = scanner.nextLine().split(" ");
            if (inp.length < 2) {
                System.out.println("\nError! Wrong coordinates! Try again:\n");
                continue;
            }

            String start = inp[0];
            String finish = inp[1];

            Coordinate from = getCoord(start);
            Coordinate to = getCoord(finish);
            if (from == null || to == null) {
                System.out.println("\nError! Wrong coordinates! Try again:\n");
                continue;
            }

            if (from.r != to.r && from.c != to.c) {
                System.out.println("\nError! Wrong ship location! Try again:\n");
                continue;
            }

            if (from.r > to.r) {
                int temp = from.r;
                from.r = to.r;
                to.r = temp;
            }

            if (from.c > to.c) {
                int temp = from.c;
                from.c = to.c;
                to.c = temp;
            }

            int size = to.r - from.r + to.c - from.c + 1;
            if (size != ship.size) {
                System.out.println("\nError! Wrong length of the " +
                        ship.name + "! Try again:\n");
                continue;
            }

            int rDir = getSign(to.r - from.r);
            int cDir = getSign(to.c - from.c);

            int r = from.r;
            int c = from.c;
            boolean isEmpty = true;
            while (r != (to.r + rDir) || c != (to.c + cDir)) {
                if (!bf.board[r][c].isFreeToPlace) {
                    isEmpty = false;
                    break;
                }
                r += rDir;
                c += cDir;
            }
            if (!isEmpty) {
                System.out.println("\nError! You placed it too close to another one. Try again:\n");
                continue;
            }

            // placing
            r = from.r;
            c = from.c;
            while (r != (to.r + rDir) || c != (to.c + cDir)) {
                bf.board[r][c].occupy = ship;
                r += rDir;
                c += cDir;
            }
            ship.place(from, to);

            // marking cells around
            for (r = Math.max(0, from.r - 1);
                 r < Math.min(to.r + 2, bf.rows); r++) {
                for (c = Math.max(0, from.c - 1);
                     c < Math.min(to.c + 2, bf.columns); c++) {
                    bf.board[r][c].isFreeToPlace = false;
                }
            }

            break;
        }
    }

    public static int getSign(int i) {
        return Integer.compare(i, 0);
    }

    // check input coordinates for correctness
    private static Coordinate getCoord(String coord) {
        if (coord == null) return null;
        if (coord.length() != 2 && coord.length() != 3) return null;

        int r = Character.toUpperCase(coord.charAt(0)) - 'A';
        int c;
        try {
            c = Integer.parseInt(coord.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return null;
        }

        if (r < 0 || r >= bf1.rows) return null;
        if (c < 0 || c >= bf1.columns) return null;
        return new Coordinate(r, c);
    }


}

class BattleField {
    int rows;
    int columns;
    Cell[][] board;

    public BattleField() {
        this(10, 10);
    }

    public BattleField(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        board = new Cell[rows][columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++)
                this.board[r][c] = new Cell();
        }
    }

    // printing the board with ships.
    // if toShow is true, shows ships, else hides them
    void print(boolean toShow) {

        System.out.print(" ");
        for (int c = 0; c < columns; c++) {
            System.out.print(" " + (c + 1));
        }
        System.out.println();

        for (int r = 0; r < rows; r++) {
            System.out.printf("%c", 'A' + r);
            for (int c = 0; c < columns; c++) {
                if (this.board[r][c].occupy == null) {
                    if (this.board[r][c].isHit) System.out.print(" M");
                    else /*if (this.board[r][c].isFreeToPlace)*/ System.out.print(" ~");
//                    else System.out.print("  ");
                } else if (this.board[r][c].isHit) System.out.print(" X");
                else if (toShow) System.out.print(" O");
                else System.out.print(" ~");
            }
            System.out.println();
        }
    }
}

// Represents a cell of tha battlefield
class Cell {
    boolean isFreeToPlace = true;
    boolean isHit = false;
    Ship occupy = null;

}

class Coordinate {
    public int r;
    public int c;
    boolean isHit = false;

    public Coordinate(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public boolean equals(Coordinate other) {
        return this.r == other.r && this.c == other.c;
    }
}

class Ship {
    int size;
    String name;
    boolean isSunk = false;
    Coordinate[] place;

    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public void place(Coordinate start, Coordinate end) {

        place = new Coordinate[size];

        int dR = Integer.compare(end.r, start.r);
        int dC = Integer.compare(end.c, start.c);

        Coordinate c = new Coordinate(start.r, start.c);

        for (int i = 0; i < size; i++) {
            place[i] = new Coordinate(c.r, c.c);
            c.r += dR;
            c.c += dC;
        }
    }

    // returns true if the ship is sunk
    public boolean hit(Coordinate hit) {

        isSunk = true;

        for (int i = 0; i < size; i++) {
            if (this.place[i].equals(hit)) this.place[i].isHit = true;
            else if (!this.place[i].isHit) {
                isSunk = false;
            }
        }

        return isSunk;
    }
}