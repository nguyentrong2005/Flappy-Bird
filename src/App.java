import javax.swing.*;

public class App {
  public static void main(String[] args) {
    new LoginRegisterFrame();
  }
  
  public static void launchGame(String username) {
    int boardWidth = 360;
    int boardHeight = 640;

    JFrame frame = new JFrame("Flappy Bird");
    frame.setSize(boardWidth, boardHeight);
    frame.setLocationRelativeTo(null);
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    FlappyBird flappyBird = new FlappyBird(username);
    flappyBird.startGame();
    frame.add(flappyBird);
    frame.pack();
    flappyBird.requestFocus();
    frame.setVisible(true);
  }
}
