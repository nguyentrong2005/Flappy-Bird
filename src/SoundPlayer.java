import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.sound.sampled.*;

public class SoundPlayer {

  private static Clip backgroundClip;
  private static final HashMap<String, Clip> clipCache = new HashMap<>();
  private static Clip currentClip; // Thêm clip hiện tại (dùng cho các file khác)

  public static void play(String filePath, float volume) {
    try {
      // Nếu là background thì dùng riêng
      if (filePath.equals("./audio/background.wav")) {
        if (backgroundClip != null && backgroundClip.isRunning())
          return;

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath));
        backgroundClip = AudioSystem.getClip();
        backgroundClip.open(audioStream);
        setVolume(backgroundClip, volume);
        backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        return;
      }

      // Các âm thanh khác
      AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath));
      Clip clip = AudioSystem.getClip();
      clip.open(audioStream);
      setVolume(clip, volume);
      clip.start();
      currentClip = clip;

    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      System.err.println("Lỗi phát âm thanh: " + e.getMessage());
    }
  }

  private static void setVolume(Clip clip, float volume) {
    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    float min = gainControl.getMinimum();
    float max = gainControl.getMaximum();
    float dB = (max - min) * volume + min;
    gainControl.setValue(dB);
  }

  public static void stopBackground() {
    if (backgroundClip != null) {
      backgroundClip.stop();
      backgroundClip.close();
      backgroundClip = null;
    }
  }

  public static void stopCurrent() {
    if (currentClip != null && currentClip.isRunning()) {
      currentClip.stop();
      currentClip.close();
      currentClip = null;
    }
  }
}
