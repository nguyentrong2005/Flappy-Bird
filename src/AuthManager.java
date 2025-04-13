import java.io.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

class AuthManager {
  private static final String USERS_FILE = "users.txt";
  private static final String SCORES_FILE = "scores.txt";
  private static final String AES_ALGORITHM = "AES";
  private static final byte[] SECRET_KEY = "MySecretKey12345".getBytes();

  public static String encryptPassword(String password) throws Exception {
    SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY, AES_ALGORITHM);
    Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec);
    return Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes()));
  }

  public static String decryptPassword(String encryptedPassword) throws Exception {
    SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY, AES_ALGORITHM);
    Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, keySpec);
    return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedPassword)));
  }

  public static boolean registerUser(String username, String password) throws Exception {
    Map<String, String> users = loadUsers();
    if (users.containsKey(username))
      return false;
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
      writer.write(username + "," + encryptPassword(password));
      writer.newLine();
    }
    return true;
  }

  public static boolean loginUser(String username, String password) throws Exception {
    Map<String, String> users = loadUsers();
    return users.containsKey(username) && decryptPassword(users.get(username)).equals(password);
  }

  private static Map<String, String> loadUsers() throws IOException {
    Map<String, String> users = new HashMap<>();
    File file = new File(USERS_FILE);
    if (!file.exists())
      return users;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 2)
          users.put(parts[0], parts[1]);
      }
    }
    return users;
  }

  public static void saveHighScore(String username, int score) throws IOException {
    Map<String, Integer> scores = loadScores();
    scores.put(username, Math.max(scores.getOrDefault(username, 0), score));
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORES_FILE))) {
      for (Map.Entry<String, Integer> entry : scores.entrySet()) {
        writer.write(entry.getKey() + "," + entry.getValue());
        writer.newLine();
      }
    }
  }

  private static Map<String, Integer> loadScores() throws IOException {
    Map<String, Integer> scores = new HashMap<>();
    File file = new File(SCORES_FILE);
    if (!file.exists())
      return scores;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 2)
          scores.put(parts[0], Integer.parseInt(parts[1]));
      }
    }
    return scores;
  }
}
