import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.concurrent.*;

class GameWithGUI extends JFrame {
    private static final int SIZE = 10;
    private static final char EMPTY_CELL = ' ';
    private static final char WOLF = 'W';
    private static final char HUNTER = 'H';
    private static final char SAFE_ZONE = 'S';

    private boolean safeZoneReached = false;
    private ImageIcon wolfIcon;
    private ImageIcon hunterIcon;
    private char[][] board;
    private int wolfX, wolfY, hunterX, hunterY, safeZoneX, safeZoneY;
    private int bullets = 2;

    private JButton[][] buttons;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public GameWithGUI() {
        initializeBoard();
        placeObjects();
        createUI();
        startGame();
    }

    private void startGame() {
        executor.submit(this::moveWolfTask);
        executor.submit(this::moveHunterTask);
    }

    private void moveWolfTask() {
        while (true) {
            moveWolf();
            updateUI();

            if (wolfX == hunterX && wolfY == hunterY) {
                JOptionPane.showMessageDialog(GameWithGUI.this, "Вовк впіймав мисливця! Гра закінчена.");
                break;
            }
            if (safeZoneReached) {
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveHunterTask() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP) {
                    moveHunter(Direction.UP);
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    moveHunter(Direction.DOWN);
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    moveHunter(Direction.LEFT);
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    moveHunter(Direction.RIGHT);
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    if (bullets > 0) {
                        shoot();
                        bullets--;
                        if (bullets == 0) {
                            JOptionPane.showMessageDialog(GameWithGUI.this, "Ви використали всі патрони!");
                        }
                    }
                }
                updateUI();
                if (hunterX == safeZoneX && hunterY == safeZoneY) {
                    JOptionPane.showMessageDialog(GameWithGUI.this, "Мисливець дійшов до безпечної зони! Ви виграли!");
                    safeZoneReached = true;
                    return;
                }
            }
        });
    }

    private void initializeBoard() {
        board = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = EMPTY_CELL;
            }
        }
    }

    private void placeObjects() {
        Random random = new Random();

        hunterX = random.nextInt(SIZE);
        hunterY = random.nextInt(SIZE);
        board[hunterX][hunterY] = HUNTER;

        do {
            wolfX = random.nextInt(SIZE);
            wolfY = random.nextInt(SIZE);
        } while (board[wolfX][wolfY] != EMPTY_CELL);
        board[wolfX][wolfY] = WOLF;

        do {
            safeZoneX = random.nextInt(SIZE);
            safeZoneY = random.nextInt(SIZE);
        } while (board[safeZoneX][safeZoneY] != EMPTY_CELL);
        board[safeZoneX][safeZoneY] = SAFE_ZONE;
    }

    private void createUI() {
        setTitle("Гра");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);

        setLayout(new GridLayout(SIZE, SIZE));
        buttons = new JButton[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                var button = new JButton(String.valueOf(EMPTY_CELL));
                button.setEnabled(false);
                buttons[i][j] = button;
                add(buttons[i][j]);
            }
        }

        wolfIcon = new ImageIcon("wolf.png");
        hunterIcon = new ImageIcon("hunter.png");
        ImageIcon safeZoneIcon = new ImageIcon("palatka.png");

        buttons[hunterX][hunterY].setIcon(hunterIcon);
        buttons[wolfX][wolfY].setIcon(wolfIcon);
        buttons[safeZoneX][safeZoneY].setIcon(safeZoneIcon);

        setFocusable(true);
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    buttons[i][j].setText(String.valueOf(board[i][j]));
                }
            }
        });
    }

    private void moveWolf() {
        int newWolfX = wolfX;
        int newWolfY = wolfY;

        if (hunterX > wolfX) {
            newWolfX++;
        } else if (hunterX < wolfX) {
            newWolfX--;
        } else if (hunterY > wolfY) {
            newWolfY++;
        } else if (hunterY < wolfY) {
            newWolfY--;
        }

        if (newWolfX >= 0 && newWolfX < SIZE && newWolfY >= 0 && newWolfY < SIZE) {
            buttons[wolfX][wolfY].setIcon(null);
            board[wolfX][wolfY] = EMPTY_CELL;

            wolfX = newWolfX;
            wolfY = newWolfY;

            board[wolfX][wolfY] = WOLF;
            buttons[wolfX][wolfY].setIcon(wolfIcon);
        }
    }

    private void moveHunter(Direction direction) {
        int newHunterX = hunterX;
        int newHunterY = hunterY;

        switch (direction) {
            case UP:
                newHunterX--;
                break;
            case DOWN:
                newHunterX++;
                break;
            case LEFT:
                newHunterY--;
                break;
            case RIGHT:
                newHunterY++;
                break;
        }

        if (isValidMove(newHunterX, newHunterY)) {
            buttons[hunterX][hunterY].setIcon(null);
            board[hunterX][hunterY] = EMPTY_CELL;

            hunterX = newHunterX;
            hunterY = newHunterY;

            board[hunterX][hunterY] = HUNTER;
            buttons[hunterX][hunterY].setIcon(hunterIcon);
        }
    }

    private void shoot() {
        Random random = new Random();
        int targetX = hunterX;
        int targetY = hunterY;

        // Логіка стрільби (випадковий успіх/невдача)
        for (int i = 0; i < 5; i++) {
            boolean hit = random.nextBoolean();

            if (hit) {
                targetX += random.nextInt(3) - 1; // -1, 0, або 1 для горизонтального руху
                targetY += random.nextInt(3) - 1; // -1, 0, або 1 для вертикального руху

                // Перевірка, чи стріляючий за межами поля
                if (targetX >= 0 && targetX < SIZE && targetY >= 0 && targetY < SIZE) {
                    buttons[targetX][targetY].setText("X");
                    if (targetX == wolfX && targetY == wolfY) {
                        JOptionPane.showMessageDialog(GameWithGUI.this, "Мисливець влучив в вовка! Ви виграли!");
                        safeZoneReached = true;
                    }
                }
            }
        }
        updateUI();
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE && board[x][y] != WOLF;
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWithGUI().setVisible(true));
    }
}





