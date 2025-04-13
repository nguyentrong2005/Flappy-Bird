import java.awt.*;
import javax.swing.*;

public class LoginRegisterFrame extends JFrame {
  private JTextField usernameField;
  private JPasswordField passwordField;
  private boolean isLogin = true;

  public LoginRegisterFrame() {
    setTitle("Login / Register");
    setSize(360, 640);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
    usernameField = new JTextField();
    passwordField = new JPasswordField();
    formPanel.add(new JLabel("Username:"));
    formPanel.add(usernameField);
    formPanel.add(new JLabel("Password:"));
    formPanel.add(passwordField);
    add(formPanel, BorderLayout.CENTER);

    JButton submitBtn = new JButton("Login");
    JButton switchModeBtn = new JButton("Switch to Register");
    JPanel btnPanel = new JPanel();
    btnPanel.add(submitBtn);
    btnPanel.add(switchModeBtn);
    add(btnPanel, BorderLayout.SOUTH);

    submitBtn.addActionListener(e -> handleAuth());
    switchModeBtn.addActionListener(e -> {
      isLogin = !isLogin;
      submitBtn.setText(isLogin ? "Login" : "Register");
      switchModeBtn.setText(isLogin ? "Switch to Register" : "Switch to Login");
    });

    setVisible(true);
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
          dispose(); // Đóng cửa sổ đăng nhập
          new App(); // Gọi game Flappy Bird
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

  public static void main(String[] args) {
    new LoginRegisterFrame(); // Chạy giao diện đăng nhập trước
  }
}
