import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class AnimatedBackgroundPanel extends JPanel {
  Image bgImg, topPipeImg, bottomPipeImg;
  ArrayList<Rectangle> pipes;
  Timer timer;
  int velocityX = -4;
  int pipeWidth = 64;
  int pipeHeight = 512;
  int gap = 150;
  Random random = new Random();

  public AnimatedBackgroundPanel() {
    setLayout(null);
    bgImg = new ImageIcon(getClass().getResource("images/flappyBirdBG.png")).getImage();
    topPipeImg = new ImageIcon(getClass().getResource("images/topPipe.png")).getImage();
    bottomPipeImg = new ImageIcon(getClass().getResource("images/bottomPipe.png")).getImage();

    pipes = new ArrayList<>();
    addPipe();

    timer = new Timer(16, e -> {
      movePipes();
      repaint();
    });
    timer.start();
  }

  private void addPipe() {
    int x = 400;
    int topY = -random.nextInt(pipeHeight / 2);
    pipes.add(new Rectangle(x, topY, pipeWidth, pipeHeight));
    pipes.add(new Rectangle(x, topY + pipeHeight + gap, pipeWidth, pipeHeight));
  }

  void movePipes() {
    for (Rectangle r : pipes) {
      r.x += velocityX;
    }
    if (pipes.get(0).x + pipeWidth < 0) {
      pipes.clear();
      addPipe();
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), null);
    for (int i = 0; i < pipes.size(); i++) {
      Rectangle r = pipes.get(i);
      if (i % 2 == 0) {
        g.drawImage(topPipeImg, r.x, r.y, r.width, r.height, null);
      } else {
        g.drawImage(bottomPipeImg, r.x, r.y, r.width, r.height, null);
      }
    }
  }
}
