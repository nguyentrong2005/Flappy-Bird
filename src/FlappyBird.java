import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

  // === Global Config ===
  public static final int BOARD_WIDTH = 360;
  public static final int BOARD_HEIGHT = 640;

  public static final int PIPE_SPACING = 1500; // ms giữa mỗi lần tạo cột

  public static final int BIRD_JUMP = -9; // độ nhảy lên của chim
  public static final int BIRD_GRAVITY = 1; // trọng lực kéo chim rơi

  public static final int GROUND_HEIGHT = 100; // vùng đất dưới (để phát hiện va chạm)

  // === User info ===
  String username;
  int highScore = 0;

  // === Game status ===
  boolean gameStarted = false;
  boolean gameOver = false;
  double score = 0;

  boolean showGameOverText = true;
  private Timer gameOverBlinkTimer;

  // === Graphics ===
  Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;

  // === Bird ===
  int birdX = BOARD_WIDTH / 8;
  int birdY = BOARD_HEIGHT / 2;
  int birdWidth = 34, birdHeight = 24;
  Bird bird;
  int velocityY = 0;

  // === Pipes ===
  ArrayList<Pipe> pipes;
  int pipeX = BOARD_WIDTH;
  int pipeY = 0;
  int pipeWidth = 64, pipeHeight = 512;

  // === Timers ===
  Timer gameLoop;
  Timer placePipesTimer;

  // === Others ===
  Random random = new Random();

  // === Classes ===
  public class Bird {
    int x = birdX, y = birdY, width = birdWidth, height = birdHeight;
    Image img;

    Bird(Image img) {
      this.img = img;
    }
  }

  public class Pipe {
    int x = pipeX, y = pipeY, width = pipeWidth, height = pipeHeight;
    Image img;
    boolean passed = false;
    boolean move = true;

    Pipe(Image img) {
      this.img = img;
    }
  }

  class DifficultyManager {
    private static final int[] scoreThresholds = { 0, 10, 30, 50, 70, 100 };

    public static int getLevel(double score) {
      for (int i = scoreThresholds.length - 1; i >= 0; i--) {
        if (score >= scoreThresholds[i])
          return i;
      }
      return 0;
    }

    public static int getPipeSpeed(int level) {
      return -3 - level;
    }

    public static int getPipeVerticalSpeed(int level) {
      return (level >= 1) ? 2 + level / 2 : 0;
    }

    public static int getPipeGap(int level) {
      return Math.max(90, FlappyBird.BOARD_HEIGHT / 4 - level * 15);
    }

    public static boolean shouldMovePipes(int level) {
      return level >= 1;
    }
  }

  // === Constructor ===
  FlappyBird(String username) {
    this.username = username;
    try {
      this.highScore = AuthManager.getHighScore(username);
    } catch (Exception e) {
      e.printStackTrace();
    }

    setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
    setFocusable(true);
    addKeyListener(this);

    // Load images
    backgroundImg = new ImageIcon(getClass().getResource("images/flappyBirdBG.png")).getImage();
    birdImg = new ImageIcon(getClass().getResource("images/flappyBird.png")).getImage();
    topPipeImg = new ImageIcon(getClass().getResource("images/topPipe.png")).getImage();
    bottomPipeImg = new ImageIcon(getClass().getResource("images/bottomPipe.png")).getImage();

    // Init bird and pipes
    bird = new Bird(birdImg);
    pipes = new ArrayList<>();

    placePipesTimer = new Timer(PIPE_SPACING, e -> placePipes());
    gameLoop = new Timer(1000 / 60, this);

    if (gameStarted) {
      placePipesTimer.start();
      gameLoop.start();
    }
  }

  public void startGame() {
    SoundPlayer.play("./audio/start.wav", 1f);
    SoundPlayer.play("./audio/background.wav", 1f);
  }

  public void placePipes() {
    int level = DifficultyManager.getLevel(score);
    int pipeGap = DifficultyManager.getPipeGap(level);
    int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));

    Pipe topPipe = new Pipe(topPipeImg);
    topPipe.y = randomPipeY;
    pipes.add(topPipe);

    Pipe bottomPipe = new Pipe(bottomPipeImg);
    bottomPipe.y = topPipe.y + pipeHeight + pipeGap;
    pipes.add(bottomPipe);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    draw(g);
  }

  public void draw(Graphics g) {
    g.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);
    g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

    if (!gameStarted) {
      g.setFont(new Font("Arial", Font.BOLD, 24));
      g.setColor(Color.WHITE);
      g.drawString("Press SPACE to Start", BOARD_WIDTH / 4, BOARD_HEIGHT / 2);
      return;
    }

    for (Pipe pipe : pipes) {
      g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
    }

    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.PLAIN, 32));

    if (gameOver) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(new Color(0, 0, 0, 170));
      g2d.fillRoundRect(40, 180, 280, 220, 30, 30);

      int centerX = BOARD_WIDTH / 2;
      g.setFont(new Font("Arial", Font.BOLD, 18));
      g.setColor(Color.WHITE);

      if (showGameOverText) {
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.RED);
        drawCenteredString(g, "GAME OVER", centerX, 210);
      }

      g.setFont(new Font("Arial", Font.BOLD, 18));
      drawCenteredString(g, "Username: " + username, centerX, 245);
      drawCenteredString(g, "Your Score: " + (int) score, centerX, 270);
      drawCenteredString(g, "High Score: " + highScore, centerX, 295);

      if ((int) score > highScore) {
        g.setColor(Color.YELLOW);
        drawCenteredString(g, "NEW RECORD!", centerX, 320);
      }

      g.setColor(Color.WHITE);
      drawCenteredString(g, "Press SPACE to play again", centerX, 360);
    } else {
      g.drawString(String.valueOf((int) score), 10, 35);
    }
  }

  public void drawCenteredString(Graphics g, String text, int xCenter, int y) {
    FontMetrics metrics = g.getFontMetrics(g.getFont());
    int x = xCenter - metrics.stringWidth(text) / 2;
    g.drawString(text, x, y);
  }

  public void move() {
    velocityY += BIRD_GRAVITY;
    bird.y += velocityY;
    bird.y = Math.max(bird.y, 0);

    int level = DifficultyManager.getLevel(score);
    int pipeSpeed = DifficultyManager.getPipeSpeed(level);
    int pipeVerticalSpeed = DifficultyManager.getPipeVerticalSpeed(level);
    boolean movePipes = DifficultyManager.shouldMovePipes(level);

    for (int i = 0; i < pipes.size(); i += 2) {
      Pipe topPipe = pipes.get(i);
      Pipe bottomPipe = pipes.get(i + 1);

      topPipe.x += pipeSpeed;
      bottomPipe.x += pipeSpeed;

      if (score >= 5) {
        int limitBottom = BOARD_HEIGHT - 120;

        if (bottomPipe.y > limitBottom) {
          topPipe.move = false;
        }

        if (topPipe.move) {
          topPipe.y += pipeVerticalSpeed;
          bottomPipe.y += pipeVerticalSpeed;
        } else {
          topPipe.y -= pipeVerticalSpeed;
          bottomPipe.y -= pipeVerticalSpeed;
        }
      }

      if (!topPipe.passed && bird.x > topPipe.x + topPipe.width) {
        topPipe.passed = true;
        bottomPipe.passed = true;
        SoundPlayer.play("./audio/point.wav", 0.8f);
        score += 1;
      }

      if (collision(bird, topPipe) || collision(bird, bottomPipe)) {
        gameOver = true;
      }
    }

    if (bird.y > BOARD_HEIGHT - GROUND_HEIGHT) {
      gameOver = true;
    }

    pipes.removeIf(pipe -> pipe.x + pipe.width < 0);
  }

  public boolean collision(Bird a, Pipe b) {
    return a.x < b.x + b.width &&
        a.x + a.width > b.x &&
        a.y < b.y + b.height &&
        a.y + a.height > b.y;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    move();
    repaint();

    if (gameOver) {
      if ((int) score > highScore) {
        highScore = (int) score;
        try {
          AuthManager.saveHighScore(username, highScore);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      SoundPlayer.play("./audio/hit.wav", 0.9f);
      placePipesTimer.stop();
      gameLoop.stop();

      if (gameOverBlinkTimer == null) {
        gameOverBlinkTimer = new Timer(500, blinkEvent -> {
          showGameOverText = !showGameOverText;
          repaint();
        });
        gameOverBlinkTimer.start();
      }
    }
  }

  @Override
  public void keyPressed(java.awt.event.KeyEvent e) {
    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
      if (!gameStarted) {
        gameStarted = true;
        gameLoop.start();
        placePipesTimer.start();
        return;
      }

      SoundPlayer.play("./audio/flap.wav", 0.8f);
      velocityY = BIRD_JUMP;

      if (gameOver) {
        if (gameOverBlinkTimer != null) {
          gameOverBlinkTimer.stop();
          gameOverBlinkTimer = null;
          showGameOverText = true;
        }

        // Reset game
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;

        gameLoop.start();
        placePipesTimer.start();

        // Play random restart sound
        String[] sounds = {
            "./audio/replay.wav", "./audio/replay1.wav",
            "./audio/replay2.wav", "./audio/replay3.wav"
        };
        SoundPlayer.play(sounds[random.nextInt(sounds.length)], 1f);
      }
    }
  }

  @Override
  public void keyTyped(java.awt.event.KeyEvent e) {
  }

  @Override
  public void keyReleased(java.awt.event.KeyEvent e) {
  }
}
