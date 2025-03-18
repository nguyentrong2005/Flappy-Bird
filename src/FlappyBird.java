
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    int boardWidth = 360;
    int boardHeight = 640;
    int moveSpeed = 2;

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

    class Bird {

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

    class Pipe {

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

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.BLUE);
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("images/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("images/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("images/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("images/bottompipe.png")).getImage();

        // bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // start playing background music
        playSound("./audio/start.wav", 1f);
        playSound("./audio/background.wav", 1f);

        // place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        // game timer
        gameLoop = new Timer(1000 / 60, this); // 1000ms/60fps = 16.6666666667ms

        if (gameStarted) {
            placePipesTimer.start();
            gameLoop.start();
        }
    }

    public void placePipes() {
        // (0-1) * pipeHeight/2 -> (0-256)
        // 128
        // 0 - 128 - (0-256) --> pipeHeight/4 -> 3/4 pipeHeight

        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // draw bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        //game start
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
            if ((System.currentTimeMillis() / 500) % 2 == 0) {
                g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
            }
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
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
                // int limitTop = 50;
                int limitBottom = boardHeight - 120;

                // if (topPipe.y >= limitTop || bottomPipe.y <= limitBottom) {
                //   topPipe.move = !topPipe.move;
                // }
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
                playSound("./audio/point.wav", 0.8f);
                score += 1;
                if (score % 5 == 0) {
                    velocityX -= 1;
                }
                if (score % 20 == 0){
                  moveSpeed += 1;
                }
            }

            // if (!pipe.passed && bird.x > pipe.x + pipe.width) {
            //     pipe.passed = true;
            //     playSound("./audio/point.wav", 0.9f);
            //     score += 0.5; // 0.5 because there are 2 pipes! so 0.5 * 2 = 1, 1 for each set of pipes
            // }
            // if (collision(bird, pipe)) {
            //     //gameOver = true;
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

    // Nhac nen
    private static Clip background;

    // Sound game
    public void playSound(String soundFile, float volume) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // volume
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float dB = (max - min) * volume + min;
            gainControl.setValue(dB);

            if (soundFile.equals("./audio/background.wav")) {
                if (background != null && background.isRunning()) {
                    return;
                }
                background = clip;
                background.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }

            audioStream.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            playSound("./audio/hit.wav", 0.9f);
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
            //Game start
            if (!gameStarted) {
                gameStarted = true;
                gameLoop.start();
                placePipesTimer.start();
                return;
            }

            playSound("./audio/flap.wav", 0.8f);
            velocityY = -9;
            if (gameOver) {
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
                Random random = new Random();
                String randomSound = sounds[random.nextInt(sounds.length)];

                playSound(randomSound, 1f);
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
