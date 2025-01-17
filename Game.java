import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;

public class Game extends JFrame implements MouseListener {
    private final int mapRow = 9; // Number of rows
    private final int mapCol = 9; // Number of columns
    private final JButton[][] buttons = new JButton[mapRow][mapCol]; // Button grid
    private int minesCount = 10; // Number of mines
    private final JLabel minesLabel = new JLabel("The remaining mines are: " + minesCount);
    private int score = 0; // Player score
    private final JLabel scoreLabel = new JLabel("Score: " + score);
    private int elapsedTime = 0; // Timer
    private final JLabel timerLabel = new JLabel("Time: " + elapsedTime);
    private Timer timer; // Timer for tracking elapsed time
    private final JTextField playerNameField = new JTextField(10); // Player name input
    private final int[][] map = new int[mapRow][mapCol]; // Game map with mines
    private final boolean[][] buttonPressed = new boolean[mapRow][mapCol]; // Track pressed buttons
    private final int[][] bombsAroundCount = new int[mapRow][mapCol]; // Count of bombs around each cell
    private final int[][] directions = {
            {0, 0}, {0, 1}, {0, -1}, {1, 0}, {-1, 0},
            {1, 1}, {-1, -1}, {-1, 1}, {1, -1}
    }; // Directions for adjacent cells
    private final ArrayList<PlayerStats> playerStatsList; // List to hold player statistics

    // Images for game states
    private final ImageIcon[] numberIcons = new ImageIcon[9]; // Icons for numbers 1-8
    private final ImageIcon mineIcon = new ImageIcon("C:\\Users\\limha\\IdeaProjects\\minesweeper(image)\\src\\mines.jpg");
    private final ImageIcon markedIcon = new ImageIcon("C:\\Users\\limha\\IdeaProjects\\minesweeper(image)\\src\\Mark.jpg");
    private final ImageIcon winIcon = new ImageIcon("C:\\Users\\limha\\IdeaProjects\\minesweeper(image)\\src\\win.jpg");
    private final ImageIcon loseIcon = new ImageIcon("C:\\Users\\limha\\IdeaProjects\\minesweeper(image)\\src\\/lose.jpg");

    Game() {
        // Load number icons
        for (int i = 1; i <= 8; i++) {
            numberIcons[i] = new ImageIcon("C:\\Users\\limha\\IdeaProjects\\minesweeper(image)\\src\\" + i + ".jpg"); // Ensure the paths are correct
        }

        // Window setup
        setSize(850, 850);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Minesweeper");
        setLocationRelativeTo(null);

        // Top panel setup
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Player Name:"));
        topPanel.add(playerNameField);
        topPanel.add(minesLabel);
        topPanel.add(scoreLabel);

        JButton restartButton = new JButton("New Game");
        restartButton.setActionCommand("restart");
        restartButton.addMouseListener(this);
        topPanel.add(restartButton);

        topPanel.add(timerLabel);
        setupTimer();

        // Center button panel setup
        JPanel centerButtonPanel = new JPanel();
        centerButtonPanel.setLayout(new GridLayout(mapRow, mapCol));
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCol; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(80, 80));
                buttons[i][j].setBackground(Color.white);
                buttons[i][j].setActionCommand(i + " " + j);
                buttons[i][j].addMouseListener(this);
                centerButtonPanel.add(buttons[i][j]);
            }
        }

        add(topPanel, BorderLayout.NORTH);
        add(centerButtonPanel, BorderLayout.CENTER);
        playerStatsList = new ArrayList<>();
        resetGame(); // Initialize the game
        setVisible(true);
    }

    private void setMap() {
        // Randomly placing mines on the map
        int count = 0;
        while (count < minesCount) {
            int x = (int) (Math.random() * mapRow);
            int y = (int) (Math.random() * mapCol);
            if (map[x][y] == 0) {
                map[x][y] = 1; // Place a mine
                count++;
            }
        }
    }

    private void setupTimer() {
        timer = new Timer(1000, e -> {
            elapsedTime++;
            timerLabel.setText("Time: " + elapsedTime);
        });
    }

    private void calculateBombsAround() {
        // Calculate bombs around each cell
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCol; j++) {
                if (map[i][j] == 1) {
                    bombsAroundCount[i][j] = -1; // Mark mine location
                } else {
                    int bombCount = 0;
                    for (int[] dir : directions) {
                        int row = i + dir[0];
                        int col = j + dir[1];
                        // Check bounds and increment bomb count
                        if (row >= 0 && row < mapRow && col >= 0 && col < mapCol && map[row][col] == 1) {
                            bombCount++;
                        }
                    }
                    bombsAroundCount[i][j] = bombCount; // Set count
                }
            }
        }
    }

    private void resetGame() {
        // Reset game state
        Arrays.stream(map).forEach(row -> Arrays.fill(row, 0));
        Arrays.stream(buttonPressed).forEach(row -> Arrays.fill(row, false));
        Arrays.stream(bombsAroundCount).forEach(row -> Arrays.fill(row, 0));

        minesCount = 10; // Reset mines count
        minesLabel.setText("The remaining mines are: " + minesCount);
        score = 0; // Reset score
        scoreLabel.setText("Score: " + score);
        elapsedTime = 0; // Reset timer
        timerLabel.setText("Time: " + elapsedTime);
        timer.restart();

        playerNameField.setText(""); // Clear player name
        setMap(); // Place mines on the map
        calculateBombsAround(); // Calculate bombs around each cell

        // Reset buttons in GUI
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCol; j++) {
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].setText("");
                buttons[i][j].setIcon(null);
            }
        }
    }

    private void revealCell(int row, int col) {
        if (row < 0 || row >= mapRow || col < 0 || col >= mapCol || buttonPressed[row][col]) {
            return;
        }

        // Mark the cell as pressed
        buttonPressed[row][col] = true;
        buttons[row][col].setBackground(Color.GRAY); // Grey out revealed cells

        // Update score and display bomb count if any
        if (bombsAroundCount[row][col] > 0) {
            // Set corresponding number icon instead of text
            buttons[row][col].setIcon(numberIcons[bombsAroundCount[row][col]]);
            score += 5; // Increment score
        } else {
            score += 10; // Increment score for an empty cell
            // Reveal surrounding cells
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) revealCell(row + i, col + j);
                }
            }
        }

        scoreLabel.setText("Score: " + score);

        // Check for win condition after revealing cell
        if (checkWinCondition()) {
            String playerName = playerNameField.getText().isEmpty() ? "Unknown Player" : playerNameField.getText();
            storePlayerStats();
            JOptionPane.showMessageDialog(null,
                    "You win ! ! ! (^o^)",
                    "You Win!", JOptionPane.INFORMATION_MESSAGE, winIcon);
            displayAllPlayerStats();
            resetGame();
        }
    }

    private void handleLeftClick(int row, int col) {
        if (buttonPressed[row][col]) return; // Already pressed

        if (map[row][col] == 1) {
            // Hit a mine!
            buttons[row][col].setIcon(mineIcon); // Show mine icon
            revealAllMines(); // Reveal all mines when losing
            showGameOverDialog(); // Show game over dialog
            return;
        }

        revealCell(row, col);
    }

    private void handleRightClick(int row, int col) {
        // Only allow marking if the cell has not been revealed
        if (!buttonPressed[row][col]) {
            if (buttons[row][col].getIcon() == markedIcon) {
                buttons[row][col].setIcon(null); // Unmark
                minesCount++; // Unmarking increases mines count
            } else {
                buttons[row][col].setIcon(markedIcon); // Mark
                minesCount--; // Marking decreases mines count
            }
            minesLabel.setText("The remaining mines are: " + minesCount);
        }
    }

    private void revealAllMines() {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                if (map[r][c] == 1) {
                    buttons[r][c].setIcon(mineIcon); // Change color to indicate a mine
                }
            }
        }
    }

    private void showGameOverDialog() {
        String playerName = playerNameField.getText().isEmpty() ? "Unknown Player" : playerNameField.getText();
        storePlayerStats();
        JOptionPane.showMessageDialog(null,
                "Hahaha, goodbye Boom!",
                "You Lose!", JOptionPane.INFORMATION_MESSAGE, loseIcon);
        revealAllMines(); // Reveal all mines on loss
        displayAllPlayerStats();
        resetGame();
    }

    private boolean checkWinCondition() {
        // Check if all non-mine cells are revealed
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCol; j++) {
                if (map[i][j] == 0 && !buttonPressed[i][j]) {
                    return false; // Found a non-mine cell that is not revealed
                }
            }
        }
        return true; // All non-mine cells have been revealed
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        JButton source = (JButton) e.getSource();

        // Check if the New Game button was clicked
        if (source.getActionCommand().equals("restart")) {
            resetGame();
            return; // Exit the method to avoid processing further
        }

        // Get row and column from the button's action command for normal game buttons
        String[] command = source.getActionCommand().split(" ");
        int row = Integer.parseInt(command[0]);
        int col = Integer.parseInt(command[1]);

        if (e.getButton() == MouseEvent.BUTTON1) {
            handleLeftClick(row, col);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            handleRightClick(row, col);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    private void storePlayerStats() {
        String playerName = playerNameField.getText().isEmpty() ? "Unknown Player" : playerNameField.getText();
        playerStatsList.add(new PlayerStats(playerName, score, elapsedTime));
    }

    private void displayAllPlayerStats() {
        if (playerStatsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No player stats available.", "Player Stats", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder statsBuilder = new StringBuilder("Player Stats:\n");
        for (PlayerStats stats : playerStatsList) {
            statsBuilder.append(String.format("Player: %s; Score: %d; Time: %d seconds\n",
                    stats.name(),
                    stats.score(),
                    stats.time()));
        }

        JOptionPane.showMessageDialog(this, statsBuilder.toString(), "Player Stats", JOptionPane.INFORMATION_MESSAGE);
    }
}

// Record class to hold player statistics
record PlayerStats(String name, int score, int time) {}

// Main class to run the Minesweeper game
class Minesweeper {
    public static void main(String[] args) {
        new Game();
    }
}