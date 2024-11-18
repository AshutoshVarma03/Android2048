package com.example.android_2048;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    private int[][] board = new int[4][4];
    private TextView[][] cellViews = new TextView[4][4];
    private TextView scoreText, gameStatus;
    private MaterialButton undoButton, resetButton;
    private MaterialButton mode2048, mode4096, mode8192;
    private GridLayout gameBoard;
    private int currentScore = 0;
    private int gameMode = 2048;
    private Stack<GameState> undoStack = new Stack<>();
    private GestureDetectorCompat gestureDetector;
    private boolean isGameOver = false;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        initializeGame();
    }

    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        gameStatus = findViewById(R.id.gameStatus);
        undoButton = findViewById(R.id.undoButton);
        resetButton = findViewById(R.id.resetButton);
        mode2048 = findViewById(R.id.mode2048);
        mode4096 = findViewById(R.id.mode4096);
        mode8192 = findViewById(R.id.mode8192);
        gameBoard = findViewById(R.id.gameBoard);
        gestureDetector = new GestureDetectorCompat(this, this);

        setupButtons();
        setupGameBoard();
    }

    private void setupButtons() {
        mode2048.setOnClickListener(v -> setGameMode(2048));
        mode4096.setOnClickListener(v -> setGameMode(4096));
        mode8192.setOnClickListener(v -> setGameMode(8192));
        undoButton.setOnClickListener(v -> undo());
        resetButton.setOnClickListener(v -> resetGame());
    }

    private void setupGameBoard() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextView cell = new TextView(this);
                cell.setWidth(getResources().getDimensionPixelSize(R.dimen.cell_size));
                cell.setHeight(getResources().getDimensionPixelSize(R.dimen.cell_size));
                cell.setTextSize(24);
                cell.setGravity(android.view.Gravity.CENTER);
                cell.setBackgroundResource(R.drawable.tile_background);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(8, 8, 8, 8);
                cell.setLayoutParams(params);
                gameBoard.addView(cell);
                cellViews[i][j] = cell;
            }
        }
    }

    private void initializeGame() {
        clearBoard();
        addNewTile();
        addNewTile();
        updateUI();
    }

    private void clearBoard() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                board[i][j] = 0;
            }
        }
        currentScore = 0;
        isGameOver = false;
        undoStack.clear();
        updateUI();
    }

    private void addNewTile() {
        ArrayList<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            board[cell[0]][cell[1]] = random.nextInt(10) < 9 ? 2 : 4;
        }
    }

    private void updateUI() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextView cell = cellViews[i][j];
                int value = board[i][j];
                cell.setText(value > 0 ? String.valueOf(value) : "");
                cell.setBackgroundResource(getTileBackground(value));
                cell.setTextColor(getTileTextColor(value));
            }
        }
        scoreText.setText(String.valueOf(currentScore));

        if (isGameOver) {
            gameStatus.setVisibility(View.VISIBLE);
            gameStatus.setText("Game Over :(");
        } else if (hasWon()) {
            gameStatus.setVisibility(View.VISIBLE);
            gameStatus.setText("You Won!");
        } else {
            gameStatus.setVisibility(View.GONE);
        }
    }

    // Implement gesture detection methods here...
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                if (diffX > 0) {
                    moveRight();
                } else {
                    moveLeft();
                }
            }
        } else {
            if (Math.abs(diffY) > 100 && Math.abs(velocityY) > 100) {
                if (diffY > 0) {
                    moveDown();
                } else {
                    moveUp();
                }
            }
        }
        return true;
    }

    // Additional required gesture methods...
    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    // Add these classes and methods after the previous code

    private static class GameState {
        int[][] board;
        int score;

        GameState(int[][] board, int score) {
            this.board = new int[4][4];
            for (int i = 0; i < 4; i++) {
                System.arraycopy(board[i], 0, this.board[i], 0, 4);
            }
            this.score = score;
        }
    }

    private void saveState() {
        undoStack.push(new GameState(board, currentScore));
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            GameState previousState = undoStack.pop();
            board = previousState.board;
            currentScore = previousState.score;
            isGameOver = false;
            updateUI();
        }
    }

    private void setGameMode(int mode) {
        gameMode = mode;
        resetGame();
    }

    private void resetGame() {
        clearBoard();
        addNewTile();
        addNewTile();
        updateUI();
    }

    private void moveLeft() {
        saveState();
        boolean moved = false;
        for (int i = 0; i < 4; i++) {
            int[] row = new int[4];
            int index = 0;

            // Collect non-zero numbers
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != 0) {
                    row[index++] = board[i][j];
                }
            }

            // Merge
            for (int j = 0; j < index - 1; j++) {
                if (row[j] == row[j + 1]) {
                    row[j] *= 2;
                    currentScore += row[j];
                    row[j + 1] = 0;
                    moved = true;
                }
            }

            // Compact again
            int[] newRow = new int[4];
            index = 0;
            for (int j = 0; j < 4; j++) {
                if (row[j] != 0) {
                    newRow[index++] = row[j];
                }
            }

            // Check if movement occurred
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != newRow[j]) {
                    moved = true;
                }
                board[i][j] = newRow[j];
            }
        }

        if (moved) {
            addNewTile();
            checkGameStatus();
            updateUI();
        }
    }

    private void moveRight() {
        saveState();
        boolean moved = false;
        for (int i = 0; i < 4; i++) {
            int[] row = new int[4];
            int index = 3;

            // Collect non-zero numbers from right
            for (int j = 3; j >= 0; j--) {
                if (board[i][j] != 0) {
                    row[index--] = board[i][j];
                }
            }

            // Merge from right
            for (int j = 3; j > 0; j--) {
                if (row[j] == row[j - 1] && row[j] != 0) {
                    row[j] *= 2;
                    currentScore += row[j];
                    row[j - 1] = 0;
                    moved = true;
                }
            }

            // Compact again
            int[] newRow = new int[4];
            index = 3;
            for (int j = 3; j >= 0; j--) {
                if (row[j] != 0) {
                    newRow[index--] = row[j];
                }
            }

            // Check if movement occurred
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != newRow[j]) {
                    moved = true;
                }
                board[i][j] = newRow[j];
            }
        }

        if (moved) {
            addNewTile();
            checkGameStatus();
            updateUI();
        }
    }

    private void moveUp() {
        saveState();
        boolean moved = false;
        for (int j = 0; j < 4; j++) {
            int[] column = new int[4];
            int index = 0;

            // Collect non-zero numbers
            for (int i = 0; i < 4; i++) {
                if (board[i][j] != 0) {
                    column[index++] = board[i][j];
                }
            }

            // Merge
            for (int i = 0; i < index - 1; i++) {
                if (column[i] == column[i + 1]) {
                    column[i] *= 2;
                    currentScore += column[i];
                    column[i + 1] = 0;
                    moved = true;
                }
            }

            // Compact again
            int[] newColumn = new int[4];
            index = 0;
            for (int i = 0; i < 4; i++) {
                if (column[i] != 0) {
                    newColumn[index++] = column[i];
                }
            }

            // Check if movement occurred
            for (int i = 0; i < 4; i++) {
                if (board[i][j] != newColumn[i]) {
                    moved = true;
                }
                board[i][j] = newColumn[i];
            }
        }

        if (moved) {
            addNewTile();
            checkGameStatus();
            updateUI();
        }
    }

    private void moveDown() {
        saveState();
        boolean moved = false;
        for (int j = 0; j < 4; j++) {
            int[] column = new int[4];
            int index = 3;

            // Collect non-zero numbers from bottom
            for (int i = 3; i >= 0; i--) {
                if (board[i][j] != 0) {
                    column[index--] = board[i][j];
                }
            }

            // Merge from bottom
            for (int i = 3; i > 0; i--) {
                if (column[i] == column[i - 1] && column[i] != 0) {
                    column[i] *= 2;
                    currentScore += column[i];
                    column[i - 1] = 0;
                    moved = true;
                }
            }

            // Compact again
            int[] newColumn = new int[4];
            index = 3;
            for (int i = 3; i >= 0; i--) {
                if (column[i] != 0) {
                    newColumn[index--] = column[i];
                }
            }

            // Check if movement occurred
            for (int i = 0; i < 4; i++) {
                if (board[i][j] != newColumn[i]) {
                    moved = true;
                }
                board[i][j] = newColumn[i];
            }
        }

        if (moved) {
            addNewTile();
            checkGameStatus();
            updateUI();
        }
    }

    private boolean hasWon() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == gameMode) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkGameStatus() {
        if (!hasMovesAvailable()) {
            isGameOver = true;
        }
    }

    private boolean hasMovesAvailable() {
        // Check for empty cells
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) {
                    return true;
                }
            }
        }

        // Check for possible merges
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == board[i][j + 1]) {
                    return true;
                }
            }
        }

        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 3; i++) {
                if (board[i][j] == board[i + 1][j]) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getTileBackground(int value) {
        switch (value) {
            case 2: return R.color.tile_2;
            case 4: return R.color.tile_4;
            case 8: return R.color.tile_8;
            case 16: return R.color.tile_16;
            case 32: return R.color.tile_32;
            case 64: return R.color.tile_64;
            case 128: return R.color.tile_128;
            case 256: return R.color.tile_256;
            case 512: return R.color.tile_512;
            case 1024: return R.color.tile_1024;
            case 2048: return R.color.tile_2048;
            case 4096: return R.color.tile_4096;
            case 8192: return R.color.tile_8192;
            default: return R.color.board_background;
        }
    }

    private int getTileTextColor(int value) {
        return (value > 4) ? R.color.white : R.color.text_color;
    }
}
