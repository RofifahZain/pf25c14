package Chapter5;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.util.Scanner;
/**
 * Tic-Tac-Toe: Two-player Graphic version with better OO design.
 * The Board and Cell classes are separated in their own classes.
 */
public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L; // to prevent serializable warning

    // Define named constants for the drawing graphics
    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG = Color.WHITE;
    public static final Color COLOR_BG_STATUS = new Color(255, 234, 234);
    public static final Color COLOR_CROSS = new Color(250, 148, 163);  // Red #FA94A3FF
    public static final Color COLOR_NOUGHT = new Color(147, 229, 255); // Blue #409AE1
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);

    // Define game objects
    private Chapter5.Board board;         // the game board
    private Chapter5.State currentState;  // the current state of the game
    private Chapter5.Seed currentPlayer;  // the current player
    private JLabel statusBar;    // for displaying status message
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;
    private String loggedInUser = "";
    private boolean gameEnded = false;
    private boolean userIsX = true;

    //timer
    private Timer turnTimer;
    private static final int TURN_TIME_LIMIT = 30; // dalam detik
    private int timeRemaining;
    private JLabel timerLabel; // label waktu

    //Constructor to setup the UI and game components
    public GameMain() {

        // This JPanel fires MouseEvent
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {  // mouse-clicked handler
                int mouseX = e.getX();
                int mouseY = e.getY();
                // Get the row and column clicked
                int row = mouseY / Chapter5.Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (currentState == Chapter5.State.PLAYING) {
                    if (row >= 0 && row < Chapter5.Board.ROWS && col >= 0 && col < Chapter5.Board.COLS
                            && board.cells[row][col].content == Chapter5.Seed.NO_SEED) {
                        // Update cells[][] and return the new game state after the move
                        currentState = board.stepGame(currentPlayer, row, col);
                        // Switch player
                        currentPlayer = (currentPlayer == Chapter5.Seed.CROSS) ? Chapter5.Seed.NOUGHT : Chapter5.Seed.CROSS;
                        startTurnTimer(); // RESET TIMER saat pemain berpindah
                    }
                } else {        // game over
                    newGame();  // restart the game
                }

                // Play appropriate sound clip
                if (currentState == State.PLAYING) {
                    SoundEffect.EXPLODE.play();
                } else if (currentState == State.DRAW) {
                    SoundEffect.EAT_FOOD.play();
                } else {
                    SoundEffect.DIE.play();
                }

                // Refresh the drawing canvas
                repaint();  // Callback paintComponent().
            }
        });

        // Setup the status bar (JLabel) to display status message
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));
        statusBar.setText("Game is starting...");

        timerLabel = new JLabel("Time: 30");
        timerLabel.setFont(FONT_STATUS);
        timerLabel.setBackground(COLOR_BG_STATUS);
        timerLabel.setOpaque(true);
        timerLabel.setHorizontalAlignment(JLabel.RIGHT);
        timerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

// Panel bawah untuk status dan timer
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(COLOR_BG_STATUS);
        bottomPanel.setPreferredSize(new Dimension(300, 30));
        bottomPanel.add(statusBar, BorderLayout.WEST);
        bottomPanel.add(timerLabel, BorderLayout.EAST);

// Atur layout panel utama
        super.setLayout(new BorderLayout());
        super.add(bottomPanel, BorderLayout.PAGE_END); // bagian bawah
        super.setPreferredSize(new Dimension(Chapter5.Board.CANVAS_WIDTH, Chapter5.Board.CANVAS_HEIGHT + 30));
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));
        // Set up Game
        initGame();
        newGame();
    }

    /** Initialize the game (run once) */
    public void initGame() {
        board = new Chapter5.Board();  // allocate the game-board
    }

    /** Reset the game-board contents and the current-state, ready for new game */
    public void newGame() {
        for (int row = 0; row < Chapter5.Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Chapter5.Seed.NO_SEED; // all cells empty
            }
        }
        currentPlayer = Chapter5.Seed.CROSS;    // cross plays first
        currentState = Chapter5.State.PLAYING;  // ready to play
        gameEnded = false;
        startTurnTimer(); // MULAI TIMER
        repaint();
    }

    /** Custom painting codes on this JPanel */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(COLOR_BG);
        board.paint(g);


        if (currentState == Chapter5.State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText((currentPlayer == Seed.CROSS) ? loggedInUser + "'s Turn" : "O's Turn");

        } else {
            if (!gameEnded) {
                // Tunda untuk memastikan gambar sudah tergambar
                gameEnded = true;
                Timer timer = new Timer(200, e -> checkAndHandleGameEnd());
                timer.setRepeats(false);
                timer.start();
            }

            if (currentState == Chapter5.State.DRAW) {
                statusBar.setForeground(new Color(110, 109, 109));
                statusBar.setText("It's a Draw!");
            } else if (currentState == Chapter5.State.CROSS_WON) {
                statusBar.setForeground(new Color(244, 75, 101));
                statusBar.setText(loggedInUser + " Won!");
            } else if (currentState == Chapter5.State.NOUGHT_WON) {
                statusBar.setForeground(new Color(100, 131, 250));
                statusBar.setText("O Won!");
            }
        }
    }

    private void startTurnTimer() {
        if (turnTimer != null && turnTimer.isRunning()) {
            turnTimer.stop();
        }

        timeRemaining = TURN_TIME_LIMIT;
        timerLabel.setText("Time: " + timeRemaining + "s");

        turnTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                timerLabel.setText("Time: " + timeRemaining + "s");

                if (timeRemaining <= 0) {
                    turnTimer.stop();
                    // Lewatkan giliran
                    currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    repaint();
                    startTurnTimer(); // mulai ulang timer untuk lawan
                }
            }
        });
        turnTimer.start();
    }

    private void stopTurnTimer() {
        if (turnTimer != null && turnTimer.isRunning()) {
            turnTimer.stop();
        }
    }

    private void checkAndHandleGameEnd() {
        stopTurnTimer(); // untuk stop timernya
        String message;
        String title = "Game Over";

        if (currentState == Chapter5.State.DRAW) {
            draws++;
            updateStats("draw", draws);
            message = "It's a Draw!";
        } else if (currentState == Chapter5.State.CROSS_WON) {
            wins++;
            updateStats("win", wins);
            message = loggedInUser + " Won!";
        } else {
            losses++;
            updateStats("loss", losses);
            message = "O Won!";
        }

        // Tampilkan 2 pilihan, Quit dan Play Again
        int choice = JOptionPane.showOptionDialog(
                this,
                message + "\nWhat would you like to do?",
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"Quit", "Play Again"},
                "Play Again"
        );

        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);  // keluar dari program
        } else if (choice == JOptionPane.NO_OPTION) {
            newGame();       // mulai ulang game
            JOptionPane.showMessageDialog(this,
                    "Updated Stats:\n" +
                            "Wins: " + wins + "\n" +
                            "Losses: " + losses + "\n" +
                            "Draws: " + draws,
                    "Leaderboard Info",
                    JOptionPane.INFORMATION_MESSAGE);
            repaint();       // perbarui tampilan
        }
    }

    private void updateStats(String column, int value) {
        String host = "mysql-2631b478-rofifahzain131-ttt.b.aivencloud.com";
        String port = "22587";
        String databaseName = "tictactoedb";
        String dbUser = "avnadmin";
        String dbPass = "AVNS_rAEVPQSOwl0Oo7KS89g";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", dbUser, dbPass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE game_user SET " + column + " = " + value + " WHERE user_name = '" + loggedInUser + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void loadUserStats(String userName) throws ClassNotFoundException {
        String host = "mysql-2631b478-rofifahzain131-ttt.b.aivencloud.com";
        String port = "22587";
        String databaseName = "tictactoedb";
        String dbUser = "avnadmin";
        String dbPass = "AVNS_rAEVPQSOwl0Oo7KS89g";

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT win, loss, draw FROM game_user WHERE user_name = '" + userName + "'")) {
            if (rs.next()) {
                wins = rs.getInt("win");
                losses = rs.getInt("loss");
                draws = rs.getInt("draw");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /** The entry "main" method */
    public static void main(String[] args) throws ClassNotFoundException {
        Scanner read = new Scanner(System.in);
        boolean passwordSalah = true;
        final String[] user = new String[1];
        do{
            String userName = JOptionPane.showInputDialog(null, "Enter your username:", "Login", JOptionPane.PLAIN_MESSAGE);
            String password = JOptionPane.showInputDialog(null, "Enter your password:"); // password terlihat
            String truePassword = getPassword(userName);
            if (password != null && password.equals(truePassword)) {
                passwordSalah = false;
                user[0] = userName;
            } else {
                JOptionPane.showMessageDialog(null, "Wrong password! please try again");
            }
        }while(passwordSalah);
        // Run GUI construction codes in Event-Dispatching thread for thread safety
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame(TITLE);
                try {
                    GameMain game = new GameMain();
                    game.loggedInUser = user[0];
                    game.loadUserStats(user[0]); // ambil statistik user
                    JOptionPane.showMessageDialog(frame,
                            "Welcome, " + user[0] + "!\n" +
                                    "Your current stats:\n" +
                                    "Wins: " + game.wins + "\n" +
                                    "Losses: " + game.losses + "\n" +
                                    "Draws: " + game.draws,
                            "Your Stats",
                            JOptionPane.INFORMATION_MESSAGE);

                        frame.setContentPane(game);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setLocationRelativeTo(null); // center the window
                    frame.setVisible(true);



            }
        });
    }

    static String getPassword(String uName) throws ClassNotFoundException{
        String host, port, databaseName, userName, password;
        host = port = databaseName = userName = password = null;
        host = "mysql-2631b478-rofifahzain131-ttt.b.aivencloud.com";
        userName = "avnadmin";
        password = "AVNS_rAEVPQSOwl0Oo7KS89g";
        databaseName = "tictactoedb";
        port = "22587";
        // JDBC allows to have nullable username and password
        if (host == null || port == null || databaseName == null) {
            System.out.println("Host, port, database information is required");

        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        String userPassword = "";
        try (final Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, password);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT password from game_user where user_name = '"+uName+"'")) {

            while (resultSet.next()) {
                userPassword = resultSet.getString("password");
            }
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }return userPassword;
    }
}
