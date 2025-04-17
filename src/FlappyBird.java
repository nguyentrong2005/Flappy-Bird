
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

  int boardWidth = 360;
  int boardHeight = 640;
  int moveSpeed = 2;

  // User
  String username;
  int highScore = 0;

  // Over
  private boolean showGameOverText = true;
  private Timer gameOverBlinkTimer;

  // Images
  Image backgroundImg;
  Image birdImg;
  Image topPipeImg;
  Image bottomPipeImg;

  // Bird
  int birdX = boardWidth / 8;
  int birdY = (boardHeight / 2);
  int birdWidth = 34;
  int birdHeight = 24;

  // game start
  boolean gameStarted = false;

  public class Bird {

    int x = birdX;
    int y = birdY;
    int width = birdWidth;
    int height = birdHeight;
    Image img;

    Bird(Image img) {
      this.img = img;
    }
  }

  // Pipes
  int pipeX = boardWidth;
  int pipeY = 0;
  int pipeWidth = 64; // scaled by 1/6
  int pipeHeight = 512;

  public class Pipe {

    int x = pipeX;
    int y = pipeY;
    int width = pipeWidth;
    int height = pipeHeight;
    Image img;
    boolean passed = false;
    boolean move = true;

    Pipe(Image img) {
      this.img = img;
    }
  }

  // game logic
  Bird bird;
  int velocityX = -4; // move pipes to the left speed (simulates bird moving to the right)
  int velocityY = 0;
  int gravity = 1;

  ArrayList<Pipe> pipes;
  Random random = new Random();

  Timer gameLoop;
  Timer placePipesTimer;
  boolean gameOver = false;
  double score = 0;

  FlappyBird(String username) {
    this.username = username;
    try {
      this.highScore = AuthManager.getHighScore(username);
    } catch (Exception e) {
      e.printStackTrace();
      this.highScore = 0;
    }

    setPreferredSize(new Dimension(boardWidth, boardHeight));
    setFocusable(true);
    addKeyListener(this);

    // Load images
    backgroundImg = new ImageIcon(getClass().getResource("images/flappyBirdBG.png")).getImage();
    birdImg = new ImageIcon(getClass().getResource("images/flappyBird.png")).getImage();
    topPipeImg = new ImageIcon(getClass().getResource("images/topPipe.png")).getImage();
    bottomPipeImg = new ImageIcon(getClass().getResource("images/bottomPipe.png")).getImage();

    // bird
    bird = new Bird(birdImg);
    pipes = new ArrayList<>();

    // place pipes timer
    placePipesTimer = new Timer(1500, e -> placePipes());

    // game timer
    gameLoop = new Timer(1000 / 60, this); // 1000ms/60fps = 16.6666666667ms

    if (gameStarted) {
      placePipesTimer.start();
      gameLoop.start();
    }
  }

  public void startGame() {
    // start playing background music
    SoundPlayer.play("./audio/start.wav", 1f);
    SoundPlayer.play("./audio/background.wav", 1f);
  }

  public void placePipes() {
    int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
    int openingSpace = (score >= 10) ? boardHeight / 5 : boardHeight / 4;

    Pipe topPipe = new Pipe(topPipeImg);
    topPipe.y = randomPipeY;
    pipes.add(topPipe);

    Pipe bottomPipe = new Pipe(bottomPipeImg);
    bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
    pipes.add(bottomPipe);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    draw(g);
  }

  public void draw(Graphics g) {
    // Draw background
    g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

    // draw bird
    g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

    // game start
    if (!gameStarted) {
      g.setFont(new Font("Arial", Font.BOLD, 24));
      g.setColor(Color.WHITE);
      g.drawString("Press SPACE to Start", boardWidth / 4, boardHeight / 2);
      return;
    }

    // draw pipes
    for (int i = 0; i < pipes.size(); i++) {
      Pipe pipe = pipes.get(i);
      g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
    }

    // draw score
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.PLAIN, 32));
    if (gameOver) {
      // Nền mờ chữ nhật
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(new Color(0, 0, 0, 170)); // màu đen mờ
      g2d.fillRoundRect(40, 180, 280, 220, 30, 30);

      // Thiết lập font Arial 16
      g.setFont(new Font("Arial", Font.BOLD, 18));
      g.setColor(Color.WHITE);

      // Canh giữa
      int centerX = boardWidth / 2;

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
    // bird
    velocityY += gravity;
    bird.y += velocityY;
    bird.y = Math.max(bird.y, 0);

    // pipes
    for (int i = 0; i < pipes.size(); i += 2) {
      // Pipe pipe = pipes.get(i);
      // pipe.x += velocityX;

      Pipe topPipe = pipes.get(i);
      Pipe bottomPipe = pipes.get(i + 1);

      topPipe.x += velocityX;
      bottomPipe.x += velocityX;

      // move pipes
      if (score >= 5) {
        int limitBottom = boardHeight - 120;

        if (bottomPipe.y > limitBottom) {
          topPipe.move = false;
        }

        if (topPipe.move) {
          topPipe.y += moveSpeed;
          bottomPipe.y += moveSpeed;
        } else {
          topPipe.y -= moveSpeed;
          bottomPipe.y -= moveSpeed;
        }
      }

      if (!topPipe.passed && bird.x > topPipe.x + topPipe.width) {
        topPipe.passed = true;
        bottomPipe.passed = true;
        SoundPlayer.play("./audio/point.wav", 0.8f);
        score += 1;
        // if (score % 5 == 0) {
        //   velocityX -= 1;
        //   int newDelay = Math.max(700, 1500 + velocityX * 50);
        //   placePipesTimer.setDelay(newDelay);
        // }
        // if (score % 20 == 0) {
        //   moveSpeed += 1;
        // }
      }

      // if (!pipe.passed && bird.x > pipe.x + pipe.width) {
      // pipe.passed = true;
      // SoundPlayer.play("./audio/point.wav", 0.9f);
      // score += 0.5; // 0.5 because there are 2 pipes! so 0.5 * 2 = 1, 1 for each
      // set of pipes
      // }
      // if (collision(bird, pipe)) {
      // //gameOver = true;
      // }
      if (collision(bird, topPipe) || collision(bird, bottomPipe)) {
        gameOver = true;
      }
    }

    if (bird.y > boardHeight - 100) {
      gameOver = true;
    }

    pipes.removeIf(pipe -> pipe.x + pipe.width < 0);
  }

  public boolean collision(Bird a, Pipe b) {
    return a.x < b.x + b.width
        && // a's top left corner doesn't reach b's top left corner
        a.x + a.width > b.x
        && // a's top right corner passes b's top right corner
        a.y < b.y + b.height
        && // a's top left corner doesn't reach b's bottom left corner
        a.y + a.height > b.y; // a's bottom left corner passes b's top left corner
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
    }
    if (gameOver && gameOverBlinkTimer == null) {
      gameOverBlinkTimer = new Timer(500, blinkEvent -> {
        showGameOverText = !showGameOverText;
        repaint();
      });
      gameOverBlinkTimer.start();
    }
  }

  @Override
  public void keyPressed(java.awt.event.KeyEvent e) {
    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
      // Game start
      if (!gameStarted) {
        gameStarted = true;
        gameLoop.start();
        placePipesTimer.start();
        return;
      }

      SoundPlayer.play("./audio/flap.wav", 0.8f);
      velocityY = -9;
      if (gameOver) {

        if (gameOverBlinkTimer != null) {
          gameOverBlinkTimer.stop();
          gameOverBlinkTimer = null;
          showGameOverText = true;
        }
        // restart the game by resetting the conditions
        bird.y = birdY;
        velocityY = 0;
        velocityX = -4;
        pipes.clear();
        score = 0;
        gameOver = false;
        gameLoop.start();
        placePipesTimer.start();

        // Am thanh replay ngau nhien
        String[] sounds = {
            "./audio/replay.wav",
            "./audio/replay1.wav",
            "./audio/replay2.wav",
            "./audio/replay3.wav"
        };
        // Random random = new Random();
        String randomSound = sounds[random.nextInt(sounds.length)];

        SoundPlayer.play(randomSound, 1f);
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
