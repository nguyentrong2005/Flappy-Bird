import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class LoginRegisterFrame extends JFrame {
  private JTextField usernameField;
  private JPasswordField passwordField;
  private JButton submitBtn, switchModeBtn;
  private boolean isLogin = true;

  public LoginRegisterFrame() {
    // Sound login
    SoundPlayer.play("./audio/login.wav", 1f);

    setTitle("Login / Register");
    setSize(360, 640);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    AnimatedBackgroundPanel bgPanel = new AnimatedBackgroundPanel();
    setContentPane(bgPanel);
    bgPanel.setLayout(null);

    // Title label
    JLabel titleLabel = new JLabel("Welcome!");
    titleLabel.setBounds(0, 100, 360, 40);
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
    titleLabel.setForeground(Color.WHITE);
    bgPanel.add(titleLabel);

    // Form
    JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    formPanel.setOpaque(false);
    formPanel.setBounds(60, 180, 240, 60);

    JLabel userLabel = new JLabel("Username:");
    userLabel.setForeground(Color.WHITE);
    userLabel.setFont(new Font("Arial", Font.PLAIN, 16));

    JLabel passLabel = new JLabel("Password:");
    passLabel.setForeground(Color.WHITE);
    passLabel.setFont(new Font("Arial", Font.PLAIN, 16));

    usernameField = new JTextField();
    usernameField.setFont(new Font("Arial", Font.PLAIN, 16));

    passwordField = new JPasswordField();
    passwordField.setFont(new Font("Arial", Font.PLAIN, 16));

    usernameField.setBorder(new LineBorder(Color.GRAY));
    passwordField.setBorder(new LineBorder(Color.GRAY));

    formPanel.add(userLabel);
    formPanel.add(usernameField);
    formPanel.add(passLabel);
    formPanel.add(passwordField);
    bgPanel.add(formPanel);

    // Buttons
    submitBtn = new JButton("Login");
    submitBtn.setFocusPainted(false);
    submitBtn.setBackground(new Color(46, 204, 113)); // Green
    submitBtn.setForeground(Color.WHITE);
    submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
    submitBtn.setBounds(110, 270, 140, 35);
    submitBtn.setBorder(new LineBorder(Color.WHITE, 2, true));

    switchModeBtn = new JButton("Switch to Register");
    switchModeBtn.setFocusPainted(false);
    switchModeBtn.setBackground(new Color(241, 196, 15)); // Orange
    switchModeBtn.setForeground(Color.BLACK);
    switchModeBtn.setFont(new Font("Arial", Font.PLAIN, 12));
    switchModeBtn.setBounds(100, 320, 160, 28);
    switchModeBtn.setBorder(new LineBorder(Color.WHITE, 1, true));

    bgPanel.add(submitBtn);
    bgPanel.add(switchModeBtn);

    submitBtn.addActionListener(e -> handleAuth());
    switchModeBtn.addActionListener(e -> toggleMode());

    setVisible(true);
  }

  private void toggleMode() {
    isLogin = !isLogin;
    submitBtn.setText(isLogin ? "Login" : "Register");

    if (isLogin) {
      submitBtn.setBackground(new Color(46, 204, 113)); // xanh
      switchModeBtn.setText("Switch to Register");
    } else {
      submitBtn.setBackground(new Color(230, 126, 34)); // cam
      switchModeBtn.setText("Switch to Login");
    }
  }

  private void handleAuth() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword()).trim();

    if (username.isEmpty() || password.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
      return;
    }

    try {
      if (isLogin) {
        if (AuthManager.loginUser(username, password)) {
          JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
          SoundPlayer.stopCurrent();
          dispose(); // Đóng cửa sổ đăng nhập
          App.launchGame(username); // Gọi game
        } else {
          JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
        }
      } else {
        if (AuthManager.registerUser(username, password)) {
          JOptionPane.showMessageDialog(this, "Đăng ký thành công! Bây giờ bạn có thể đăng nhập.");
          isLogin = true;
        } else {
          JOptionPane.showMessageDialog(this, "Tên tài khoản đã tồn tại!");
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(this, "Lỗi xử lý: " + ex.getMessage());
    }
  }
}