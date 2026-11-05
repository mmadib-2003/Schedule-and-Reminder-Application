// === COMPLETE, UPDATED IMPORT BLOCK ===
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.Timer; // Explicitly import the Swing Timer
import java.awt.*;
import java.awt.dnd.*; // For Drag and Drop
import java.awt.datatransfer.*; // For Drag and Drop
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map; //  util import
import java.util.HashMap; // Specific util import
import java.util.ArrayList; // Specific util import
import java.util.Date; // Specific util import
import java.util.Calendar; // Specific util import
import java.util.Comparator; // Specific util import
import java.util.Arrays; // Specific util import
import java.util.stream.Collectors;
import java.util.regex.Pattern; // For validation
import java.io.*;
import java.io.Serializable;

public class ScheduleReminderApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

// User Management System
class UserManager {
    private static final String USERS_FILE = "users.txt";
    private Map<String, User> users;
    
    public UserManager() {
        users = new HashMap<>();
        loadUsers();
    }
    
    public boolean register(String username, String password, String fullName, String email) {
        if (users.containsKey(username)) {
            return false;
        }
        User user = new User(username, password, fullName, email);
        users.put(username, user);
        saveUsers();
        return true;
    }
    
    public User login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    
    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                writer.write(user.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromFileString(line);
                if (user != null) {
                    users.put(user.getUsername(), user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// User Model
class User {
    private String username;
    private String password;
    private String fullName;
    private String email;
    
    public User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }
    
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    
    public String toFileString() {
        return String.join("|", username, password, fullName, email);
    }
    
    public static User fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 4) return null;
        return new User(parts[0], parts[1], parts[2], parts[3]);
    }
}

// Modern Color Scheme (Google Calendar inspired)
class ModernColors {
    // Light Mode
    public static final Color PRIMARY = new Color(66, 133, 244);      // Google Blue
    public static final Color PRIMARY_DARK = new Color(51, 103, 214);
    public static final Color BACKGROUND = new Color(255, 255, 255);
    public static final Color SURFACE = new Color(248, 249, 250);
    public static final Color CARD = new Color(255, 255, 255);
    public static final Color TEXT_PRIMARY = new Color(32, 33, 36);
    public static final Color TEXT_SECONDARY = new Color(95, 99, 104);
    public static final Color BORDER = new Color(218, 220, 224);
    public static final Color HOVER = new Color(241, 243, 244);
    
    // Dark Mode (Based on image_874f29.png)
    public static final Color DARK_BACKGROUND = new Color(32, 33, 36);
    public static final Color DARK_SURFACE = new Color(41, 42, 45); // Darker than card
    public static final Color DARK_CARD = new Color(48, 49, 52); // Dark grey
    public static final Color DARK_TEXT_PRIMARY = new Color(232, 234, 237);
    public static final Color DARK_TEXT_SECONDARY = new Color(154, 160, 166);
    public static final Color DARK_BORDER = new Color(60, 64, 67);
    public static final Color DARK_HOVER = new Color(55, 56, 59);
    
    // Status Colors
    public static final Color SUCCESS = new Color(52, 168, 83);
    public static final Color WARNING = new Color(251, 188, 5);
    public static final Color ERROR = new Color(234, 67, 53); // Red
    
    // Category Colors
    public static final Color WORK = new Color(66, 133, 244);
    public static final Color STUDY = new Color(52, 168, 83);
    public static final Color PERSONAL = new Color(251, 188, 5);
    public static final Color HEALTH = new Color(234, 67, 53);
    public static final Color FINANCE = new Color(156, 39, 176); // Purple
    public static final Color SOCIAL = new Color(255, 152, 0); // Orange
    public static final Color SHOPPING = new Color(0, 150, 136); // Teal
}

// === MODIFIED LoginFrame ===
class LoginFrame extends JFrame {
    private UserManager userManager;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginFrame() {
        userManager = new UserManager();
        
        setTitle("Schedule & Reminder - Login");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(ModernColors.BACKGROUND);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(ModernColors.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(450, 120));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("Schedule & Reminder");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Manage your time efficiently");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalGlue());
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(ModernColors.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        JLabel loginLabel = new JLabel("Login to your account");
        loginLabel.setFont(new Font("Arial", Font.BOLD, 18));
        loginLabel.setForeground(ModernColors.TEXT_PRIMARY);
        loginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        formPanel.add(loginLabel);
        formPanel.add(Box.createVerticalStrut(30));
        
        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(ModernColors.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel userWrapper = createStyledWrapper(usernameField, null);
        
        formPanel.add(userLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(userWrapper); 
        formPanel.add(Box.createVerticalStrut(20));
        
        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passLabel.setForeground(ModernColors.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        JToggleButton passToggle = createPasswordToggle(passwordField); 
        JPanel passWrapper = createStyledWrapper(passwordField, passToggle);
        
        formPanel.add(passLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(passWrapper); 
        formPanel.add(Box.createVerticalStrut(30));
        
        // Login Button
        JButton loginBtn = createModernButton("Login", ModernColors.PRIMARY);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT); 
        loginBtn.addActionListener(e -> handleLogin());
        formPanel.add(loginBtn);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Sign Up Link
        JPanel signupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        signupPanel.setBackground(ModernColors.BACKGROUND);
        JLabel signupLabel = new JLabel("Don't have an account?");
        signupLabel.setForeground(ModernColors.TEXT_SECONDARY);
        JButton signupBtn = new JButton("Sign Up");
        signupBtn.setForeground(ModernColors.PRIMARY);
        signupBtn.setBorderPainted(false);
        signupBtn.setContentAreaFilled(false);
        signupBtn.setFocusPainted(false);
        signupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupBtn.addActionListener(e -> showSignupDialog());
        
        signupPanel.add(signupLabel);
        signupPanel.add(signupBtn);
        formPanel.add(signupPanel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        passwordField.addActionListener(e -> handleLogin());
    }
    
    private JPanel createStyledWrapper(JComponent component, JComponent accessory) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.setBackground(Color.WHITE); 
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, ModernColors.BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12) 
        ));
        
        component.setBorder(null); 
        component.setBackground(Color.WHITE); 
        
        wrapper.add(component, BorderLayout.CENTER);
        if (accessory != null) {
            accessory.setOpaque(false);
            accessory.setBackground(Color.WHITE); 
            wrapper.add(accessory, BorderLayout.EAST);
        }
        return wrapper;
    }
    
    private JToggleButton createPasswordToggle(JPasswordField passField) {
        final char echoChar = passField.getEchoChar();
        
        Icon showIcon = new EyeIcon(true); // Open eye
        Icon hideIcon = new EyeIcon(false); // Slashed eye
        
        JToggleButton toggleBtn = new JToggleButton(hideIcon); 
        toggleBtn.setSelectedIcon(showIcon); 
        
        toggleBtn.setMargin(new Insets(0, 5, 0, 5));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setOpaque(false);
        toggleBtn.setContentAreaFilled(false); 
        toggleBtn.setBorderPainted(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setForeground(ModernColors.TEXT_SECONDARY); 

        toggleBtn.addActionListener(e -> {
            if (toggleBtn.isSelected()) {
                passField.setEchoChar((char) 0); 
            } else {
                passField.setEchoChar(echoChar); 
            }
        });
        return toggleBtn;
    }
    
    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(200, 45)); 
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true); 
        btn.setBorderPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        User user = userManager.login(username, password);
        if (user != null) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                MainAppFrame mainApp = new MainAppFrame(user);
                mainApp.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showSignupDialog() {
        SignupDialog dialog = new SignupDialog(this, userManager);
        dialog.setVisible(true);
    }
}

// === MODIFIED SignupDialog ===
class SignupDialog extends JDialog {
    private UserManager userManager;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    
    public SignupDialog(JFrame parent, UserManager userManager) {
        super(parent, "Create Account", true);
        this.userManager = userManager;
        
        setSize(450, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ModernColors.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JLabel titleLabel = new JLabel("Create your account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(ModernColors.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Full Name
        fullNameField = new JTextField();
        fullNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel fullWrapper = createStyledWrapper(fullNameField, null);
        addField(mainPanel, "Full Name", fullWrapper);
        
        // Email
        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel emailWrapper = createStyledWrapper(emailField, null);
        addField(mainPanel, "Email", emailWrapper);
        
        // Username
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel userWrapper = createStyledWrapper(usernameField, null);
        addField(mainPanel, "Username", userWrapper);
        
        // Password
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        JToggleButton passToggle = createPasswordToggle(passwordField); 
        JPanel passWrapper = createStyledWrapper(passwordField, passToggle);
        addField(mainPanel, "Password", passWrapper);
        
        // Confirm Password
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        JToggleButton confirmToggle = createPasswordToggle(confirmPasswordField); 
        JPanel confirmWrapper = createStyledWrapper(confirmPasswordField, confirmToggle);
        addField(mainPanel, "Confirm Password", confirmWrapper);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Buttons
        JButton signupBtn = createModernButton("Create Account", ModernColors.PRIMARY);
        signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT); 
        signupBtn.addActionListener(e -> handleSignup());
        mainPanel.add(signupBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        
        JButton cancelBtn = createModernButton("Cancel", ModernColors.TEXT_SECONDARY);
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT); 
        cancelBtn.addActionListener(e -> dispose());
        mainPanel.add(cancelBtn);
        
        add(new JScrollPane(mainPanel));
    }
    
    private void addField(JPanel panel, String label, JPanel fieldWrapper) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(ModernColors.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(5));
        panel.add(fieldWrapper); 
        panel.add(Box.createVerticalStrut(15));
    }
    
    private JPanel createStyledWrapper(JComponent component, JComponent accessory) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.setBackground(Color.WHITE); 
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, ModernColors.BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12) 
        ));
        
        component.setBorder(null); 
        component.setBackground(Color.WHITE); 
        
        wrapper.add(component, BorderLayout.CENTER);
        if (accessory != null) {
            accessory.setOpaque(false);
            accessory.setBackground(Color.WHITE); 
            wrapper.add(accessory, BorderLayout.EAST);
        }
        return wrapper;
    }
    
    private JToggleButton createPasswordToggle(JPasswordField passField) {
        final char echoChar = passField.getEchoChar();

        Icon showIcon = new EyeIcon(true); // Open eye
        Icon hideIcon = new EyeIcon(false); // Slashed eye
        
        JToggleButton toggleBtn = new JToggleButton(hideIcon); 
        toggleBtn.setSelectedIcon(showIcon); 
        
        toggleBtn.setMargin(new Insets(0, 5, 0, 5));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setOpaque(false);
        toggleBtn.setContentAreaFilled(false); 
        toggleBtn.setBorderPainted(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setForeground(ModernColors.TEXT_SECONDARY); 

        toggleBtn.addActionListener(e -> {
            if (toggleBtn.isSelected()) {
                passField.setEchoChar((char) 0); 
            } else {
                passField.setEchoChar(echoChar); 
            }
        });
        return toggleBtn;
    }
    
    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(300, 45)); 
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true); 
        btn.setBorderPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.matches(emailRegex, email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (!Pattern.matches(passwordRegex, password)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (userManager.register(username, password, fullName, email)) {
            JOptionPane.showMessageDialog(this, "Account created successfully! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// === MODIFIED MainAppFrame ===
class MainAppFrame extends JFrame {
    private User currentUser;
    private TaskManager taskManager;
    private CalendarPanel calendarPanel;
    private MainTaskListPanel mainTaskListPanel; 
    private boolean isDarkMode = false;
    private ReminderService reminderService; 
    
    private JPanel mainPanel;
    private JSplitPane splitPane;
    
    public MainAppFrame(User user) {
        this.currentUser = user;
        this.taskManager = new TaskManager(user.getUsername());
        
        this.reminderService = new ReminderService(this.taskManager, this);
        this.reminderService.start();
        
        setTitle("Schedule & Reminder - " + user.getFullName());
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(ModernColors.BACKGROUND);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(true); 
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(850);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        splitPane.setOpaque(true); 
        
        calendarPanel = new CalendarPanel(taskManager, this);
        calendarPanel.setOpaque(true); 
        
        mainTaskListPanel = new MainTaskListPanel(taskManager, this);
        mainTaskListPanel.setOpaque(true); 
        
        splitPane.setLeftComponent(calendarPanel);
        splitPane.setRightComponent(mainTaskListPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);
        
        setJMenuBar(createMenuBar()); 
        
        setupKeyboardShortcuts();
        applyTheme(); 
        
        showWelcomeMessage(user);
    }
    
    private String getRandomQuote() {
        String[] quotes = {
            "The way to get started is to quit talking and begin doing. - Walt Disney",
            "Your time is limited, don't waste it living someone else's life. - Steve Jobs",
            "The future belongs to those who believe in the beauty of their dreams. - Eleanor Roosevelt",
            "It does not matter how slowly you go as long as you do not stop. - Confucius",
            "The only way to do great work is to love what you do. - Steve Jobs",
            "Success is not final, failure is not fatal: it is the courage to continue that counts. - Winston Churchill",
            "What you get by achieving your goals is not as important as what you become by achieving your goals. - Zig Ziglar",
            "A goal without a plan is just a wish. - Antoine de Saint-Exupéry"
        };
        int randomIndex = (int) (Math.random() * quotes.length);
        return quotes[randomIndex];
    }
    
    private void showWelcomeMessage(User user) {
        JPanel welcomePanel = new JPanel(new BorderLayout(10, 0));
        welcomePanel.setBackground(isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        welcomePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        String title = "Welcome, " + user.getFullName() + "!";
        String quote = getRandomQuote();
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        
        JLabel quoteLabel = new JLabel("<html><i>" + quote + "</i></html>");
        quoteLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        quoteLabel.setForeground(isDarkMode ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY);
        
        welcomePanel.add(titleLabel, BorderLayout.NORTH);
        welcomePanel.add(quoteLabel, BorderLayout.CENTER);
        
        JButton dismissButton = new JButton("X");
        dismissButton.setFont(new Font("Arial", Font.BOLD, 12));
        dismissButton.setMargin(new Insets(2, 5, 2, 5));
        dismissButton.setFocusPainted(false);
        dismissButton.setForeground(isDarkMode ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY);
        dismissButton.setContentAreaFilled(false);
        dismissButton.setBorder(BorderFactory.createLineBorder(
            isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER, 1
        ));
        dismissButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        welcomePanel.add(dismissButton, BorderLayout.EAST);
        
        mainPanel.add(welcomePanel, BorderLayout.NORTH);
        mainPanel.revalidate();
        mainPanel.repaint();
        
        dismissButton.addActionListener(e -> {
            mainPanel.remove(welcomePanel);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        
        Timer welcomeTimer = new Timer(10000, e -> { 
            if (welcomePanel.isDisplayable()) {
                mainPanel.remove(welcomePanel);
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
        welcomeTimer.setRepeats(false); 
        welcomeTimer.start();
    }
    
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, 
            isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER));
        
        // File Menu
        JMenu fileMenu = createStyledMenu("File");
        addMenuItem(fileMenu, "New Task (Ctrl+N)", e -> showAddTaskDialog());
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Export Tasks", e -> exportTasks());
        addMenuItem(fileMenu, "Import Tasks", e -> importTasks());
        addMenuItem(fileMenu, "Export as HTML", e -> exportAsHtml()); 
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Logout", e -> logout());
        addMenuItem(fileMenu, "Exit", e -> System.exit(0));
        
        // View Menu
        JMenu viewMenu = createStyledMenu("View");
        addMenuItem(viewMenu, "Month View (Ctrl+1)", e -> calendarPanel.switchToMonthView());
        addMenuItem(viewMenu, "Week View (Ctrl+2)", e -> calendarPanel.switchToWeekView());
        addMenuItem(viewMenu, "Day View (Ctrl+3)", e -> calendarPanel.switchToDayView());
        viewMenu.addSeparator();
        addMenuItem(viewMenu, "Toggle Dark Mode", e -> toggleTheme());
        
        // Search Menu
        JMenu searchMenu = createStyledMenu("Search");
        addMenuItem(searchMenu, "Search Tasks (Ctrl+F)", e -> showSearchDialog());
        addMenuItem(searchMenu, "Go to Date...", e -> showGoToDateDialog()); 
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(searchMenu);
        
        return menuBar;
    }
    
    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setFont(new Font("Arial", Font.BOLD, 13));
        menu.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        menu.setOpaque(true);
        menu.setBackground(isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        return menu;
    }
    
    private void addMenuItem(JMenu menu, String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        menu.add(item);
    }
    
    private void setupKeyboardShortcuts() {
        getRootPane().registerKeyboardAction(e -> showAddTaskDialog(),
            KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> showSearchDialog(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> calendarPanel.switchToMonthView(),
            KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> calendarPanel.switchToWeekView(),
            KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> calendarPanel.switchToDayView(),
            KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    public void showAddTaskDialog() {
        TaskDialog dialog = new TaskDialog(this, taskManager, null, isDarkMode);
        dialog.setVisible(true);
        refreshViews();
    }
    
    public void showEditTaskDialog(Task task) {
        TaskDialog dialog = new TaskDialog(this, taskManager, task, isDarkMode);
        dialog.setVisible(true);
        refreshViews();
    }
    
    private void showSearchDialog() {
        SearchDialog dialog = new SearchDialog(this, taskManager, isDarkMode);
        dialog.setVisible(true);
    }

    private void showGoToDateDialog() {
        GoToDateDialog dialog = new GoToDateDialog(this, isDarkMode);
        dialog.setVisible(true);
        LocalDate selectedDate = dialog.getSelectedDate();
        if (selectedDate != null) {
            jumpToDate(selectedDate);
        }
    }
    
    public void jumpToDate(LocalDate date) {
        if (calendarPanel != null) {
            calendarPanel.jumpToDate(date);
        }
    }
    
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }
    
    private void applyTheme() {
        Container root = getContentPane();
        
        Color bg = isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND;
        Color surface = isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE;
        Color card = isDarkMode ? ModernColors.DARK_CARD : ModernColors.CARD;
        Color text = isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY;
        Color textSec = isDarkMode ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY;
        Color border = isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER;

        getContentPane().setBackground(bg);

        // JTabbedPane properties are now set in MainTaskListPanel.applyTheme()
        // and TaskDialog() constructor for more reliable updates.

        JMenuBar menuBar = getJMenuBar();
        if (menuBar != null) {
            menuBar.setBackground(surface);
            menuBar.setOpaque(true); 
            menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, border));
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                JMenu menu = menuBar.getMenu(i);
                menu.setBackground(surface);
                menu.setForeground(text);
                menu.setOpaque(true);
                for (Component menuItem : menu.getMenuComponents()) {
                    menuItem.setBackground(surface);
                    menuItem.setForeground(text);
                    if (menuItem instanceof JComponent) { 
                        ((JComponent)menuItem).setOpaque(true);
                    }
                }
            }
        }
        
        updateComponentTreeUI(root, bg, surface, card, text, textSec, border);
        
        calendarPanel.applyTheme(isDarkMode);
        mainTaskListPanel.applyTheme(isDarkMode); 
    }

    private void updateComponentTreeUI(Component comp, Color bg, Color surface, Color card, Color text, Color textSec, Color border) {
        if (comp instanceof JPanel) {
            comp.setBackground(surface); 
        } else if (comp instanceof JLabel) {
            comp.setForeground(text);
        } else if (comp instanceof JScrollPane) {
            comp.setBackground(surface);
            JViewport viewport = ((JScrollPane) comp).getViewport();
            if (viewport != null) viewport.setBackground(card);
            ((JScrollPane) comp).setBorder(BorderFactory.createLineBorder(border));
        } else if (comp instanceof JList) {
            comp.setBackground(card);
            comp.setForeground(text);
        } else if (comp instanceof JComboBox) {
            comp.setBackground(card);
            comp.setForeground(text);
        } else if (comp instanceof JButton) {
            comp.setForeground(text);
        } else if (comp instanceof JTabbedPane) {
            // This is now handled by MainTaskListPanel's applyTheme
            // and TaskDialog's constructor
            SwingUtilities.updateComponentTreeUI(comp);
        }

        if (comp.equals(calendarPanel)) {
            comp.setBackground(bg); 
        }
        if (comp.equals(mainTaskListPanel)) { 
            comp.setBackground(bg); 
        }
        if (comp instanceof JSplitPane) {
            comp.setBackground(bg); 
            ((JComponent) comp).setOpaque(true); 
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                updateComponentTreeUI(child, bg, surface, card, text, textSec, border);
            }
        }
    }
    
    public void refreshViews() {
        if (calendarPanel != null) calendarPanel.refresh();
        if (mainTaskListPanel != null) mainTaskListPanel.refresh(); 
    }
    
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    private void exportTasks() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Tasks");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            taskManager.exportToFile(chooser.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Tasks exported successfully!");
        }
    }
    
    private void exportAsHtml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Tasks as HTML");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML files", "html"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".html")) {
                file = new File(file.getParentFile(), file.getName() + ".html");
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                String htmlReport = taskManager.generateHtmlReport();
                writer.write(htmlReport);
                JOptionPane.showMessageDialog(this, 
                    "Tasks exported as HTML successfully!\nYou can open this file in a browser and print to PDF.",
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting HTML: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void importTasks() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Tasks");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            taskManager.importFromFile(chooser.getSelectedFile());
            refreshViews();
            JOptionPane.showMessageDialog(this, "Tasks imported successfully!");
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", 
            "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            reminderService.stop(); 
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
}

// === MODIFIED Task Model (with Duration) ===
class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private int durationMinutes; 
    private Category category;
    private Priority priority;
    private boolean completed;
    private RecurrenceType recurrence;
    private List<String> attachmentPaths; 

    public Task(String title, String description, LocalDateTime dateTime,
                Category category, Priority priority, int durationMinutes) { 
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.category = category;
        this.priority = priority;
        this.durationMinutes = durationMinutes; 
        this.completed = false;
        this.recurrence = RecurrenceType.NONE;
        this.attachmentPaths = new ArrayList<>(); 
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public int getDurationMinutes() { return durationMinutes; } 
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; } 
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public RecurrenceType getRecurrence() { return recurrence; }
    public void setRecurrence(RecurrenceType recurrence) { this.recurrence = recurrence; }
    
    public List<String> getAttachmentPaths() { return new ArrayList<>(attachmentPaths); }
    public void addAttachmentPath(String path) { this.attachmentPaths.add(path); }
    public void setAttachmentPaths(List<String> paths) { this.attachmentPaths = new ArrayList<>(paths); }

    public String toFileString() {
        String attachments = String.join(";", attachmentPaths);
        
        return String.join("|",
            id, title, description,
            dateTime != null ? dateTime.toString() : "",
            category.name(), priority.name(),
            String.valueOf(completed), recurrence.name(),
            attachments, String.valueOf(durationMinutes) 
        );
    }

    public static Task fromFileString(String line) {
        String[] parts = line.split("\\|", -1); 
        if (parts.length < 9) return null; 

        Task task = new Task(parts[1], parts[2],
            parts[3].isEmpty() ? null : LocalDateTime.parse(parts[3]),
            Category.valueOf(parts[4]), Priority.valueOf(parts[5]),
            60 // Default duration
        );
        task.setId(parts[0]);
        task.setCompleted(Boolean.parseBoolean(parts[6]));
        task.setRecurrence(RecurrenceType.valueOf(parts[7]));

        if (parts.length > 8 && !parts[8].isEmpty()) {
            String[] paths = parts[8].split(";");
            task.setAttachmentPaths(Arrays.asList(paths));
        }
        
        if (parts.length > 9 && !parts[9].isEmpty()) {
            try {
                task.setDurationMinutes(Integer.parseInt(parts[9]));
            } catch (NumberFormatException e) {
                task.setDurationMinutes(60); // Default on error
            }
        }
        
        return task;
    }
}

// === MODIFIED Category Enum ===
enum Category {
    WORK("Work", ModernColors.WORK),
    STUDY("Study", ModernColors.STUDY),
    PERSONAL("Personal", ModernColors.PERSONAL),
    HEALTH("Health", ModernColors.HEALTH),
    FINANCE("Finance", ModernColors.FINANCE),
    SOCIAL("Social", ModernColors.SOCIAL),
    SHOPPING("Shopping", ModernColors.SHOPPING);
    
    private String displayName;
    private Color color;
    
    Category(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() { return displayName; }
    public Color getColor() { return color; }
}

enum Priority {
    HIGH("High"), MEDIUM("Medium"), LOW("Low");
    
    private String displayName;
    
    Priority(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    public Color getColor() {
        return switch(this) {
            case HIGH -> ModernColors.ERROR;
            case MEDIUM -> ModernColors.WARNING;
            case LOW -> ModernColors.SUCCESS;
        };
    }
}

enum RecurrenceType {
    NONE("None"), DAILY("Daily"), WEEKLY("Weekly"), MONTHLY("Monthly");
    
    private String displayName;
    
    RecurrenceType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
}

// Task Manager
class TaskManager {
    private List<Task> tasks;
    private String username;
    
    public TaskManager(String username) {
        this.username = username;
        this.tasks = new ArrayList<>();
        loadTasks();
    }
    
    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }
    
    public void updateTask(Task task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(task.getId())) {
                tasks.set(i, task);
                saveTasks();
                break;
            }
        }
    }
    
    public void deleteTask(Task task) {
        tasks.removeIf(t -> t.getId().equals(task.getId()));
        saveTasks();
    }
    
    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }
    
    public Task getTaskById(String id) {
        return tasks.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<Task> getTasksForDate(LocalDate date) {
        return tasks.stream()
            .filter(t -> t.getDateTime() != null && t.getDateTime().toLocalDate().equals(date))
            .sorted(Comparator.comparing(Task::getDateTime))
            .collect(Collectors.toList()); 
    }
    
    public List<Task> searchTasks(String keyword) {
        return tasks.stream()
            .filter(t -> t.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        t.getDescription().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList()); 
    }
    
    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(username + "_tasks.txt"))) {
            for (Task task : tasks) {
                writer.write(task.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadTasks() {
        File file = new File(username + "_tasks.txt");
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Task task = Task.fromFileString(line);
                if (task != null) {
                    tasks.add(task);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void exportToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Task task : tasks) {
                writer.write(task.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void importFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Task task = Task.fromFileString(line);
                if (task != null) {
                    if (tasks.stream().noneMatch(t -> t.getId().equals(task.getId()))) {
                         tasks.add(task);
                    }
                }
            }
            saveTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String generateHtmlReport() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Task Report</title><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background: #f9f9f9; }");
        html.append("h1 { color: #333; }");
        html.append("table { width: 100%; border-collapse: collapse; }");
        html.append("th, td { padding: 12px; border: 1px solid #ddd; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append(".completed { text-decoration: line-through; color: #888; }");
        html.append("</style></head><body>");
        html.append("<h1>Task Report for ").append(username).append("</h1>");
        
        List<Task> upcoming = tasks.stream()
            .filter(t -> !t.isCompleted() && t.getDateTime() != null && t.getDateTime().isAfter(LocalDateTime.now()))
            .sorted(Comparator.comparing(Task::getDateTime)).collect(Collectors.toList());
            
        List<Task> previous = tasks.stream()
            .filter(t -> t.getDateTime() != null && t.getDateTime().isBefore(LocalDateTime.now()))
            .sorted(Comparator.comparing(Task::getDateTime).reversed()).collect(Collectors.toList());

        html.append("<h2>Upcoming Tasks</h2>");
        html.append("<table><tr><th>Title</th><th>Date & Time</th><th>Category</th><th>Priority</th></tr>");
        if (upcoming.isEmpty()) {
            html.append("<tr><td colspan='4'>No upcoming tasks.</td></tr>");
        }
        for (Task task : upcoming) {
            html.append("<tr>");
            html.append("<td>").append(task.getTitle()).append("</td>");
            html.append("<td>").append(task.getDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</td>");
            html.append("<td>").append(task.getCategory().getDisplayName()).append("</td>");
            html.append("<td>").append(task.getPriority().getDisplayName()).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        
        html.append("<h2>Previous & Completed Tasks</h2>");
        html.append("<table><tr><th>Title</th><th>Date & Time</th><th>Category</th><th>Status</th></tr>");
        if (previous.isEmpty()) {
            html.append("<tr><td colspan='4'>No previous tasks.</td></tr>");
        }
        for (Task task : previous) {
            String titleClass = task.isCompleted() ? "class='completed'" : "";
            String status = task.isCompleted() ? "Completed" : "Missed";
            html.append("<tr>");
            html.append("<td ").append(titleClass).append(">").append(task.getTitle()).append("</td>");
            html.append("<td>").append(task.getDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</td>");
            html.append("<td>").append(task.getCategory().getDisplayName()).append("</td>");
            html.append("<td>").append(status).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        
        html.append("</body></html>");
        return html.toString();
    }
}

// === MODIFIED Calendar Panel ===
class CalendarPanel extends JPanel {
    private TaskManager taskManager;
    private MainAppFrame mainApp;
    private LocalDate currentDate;
    private String viewMode = "MONTH";
    private JPanel centerPanel; 
    private JPanel calendarGrid;
    private DayViewPanel dayViewPanel; 
    private JLabel monthLabel;
    private JPanel monthLabelWrapper; 

    public CalendarPanel(TaskManager taskManager, MainAppFrame mainApp) {
        this.taskManager = taskManager;
        this.mainApp = mainApp;
        this.currentDate = LocalDate.now();
        
        setLayout(new BorderLayout());
        setOpaque(true); 
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        add(createHeader(), BorderLayout.NORTH);
        
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        add(centerPanel, BorderLayout.CENTER);
        
        calendarGrid = new JPanel();
        calendarGrid.setOpaque(true); 
        
        dayViewPanel = new DayViewPanel(taskManager, mainApp); 
        
        switchToMonthView();
    }
    
    public void applyTheme(boolean isDarkMode) {
        Color bg = isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND;
        Color text = isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY;
        Color surface = isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE;
        Color border = isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER;

        setBackground(bg);
        calendarGrid.setBackground(bg);
        dayViewPanel.applyTheme(isDarkMode);
        
        if (monthLabelWrapper != null) {
            monthLabelWrapper.setBackground(surface);
            monthLabelWrapper.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, border), 
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
            ));
        }
        if (monthLabel != null) {
            monthLabel.setForeground(text);
        }
        
        if (getComponentCount() > 0 && getComponent(0) instanceof Container) {
            for(Component c : ((Container) getComponent(0)).getComponents()) { 
                 c.setBackground(bg); 
                 if (c instanceof JPanel) {
                     for(Component c2 : ((JPanel)c).getComponents()) { 
                         if (!(c2 instanceof JButton) && !(c2 == monthLabelWrapper)) { 
                             c2.setBackground(bg);
                             if (c2 instanceof JPanel) { 
                                 for(Component c3 : ((JPanel)c2).getComponents()) {
                                     c3.setBackground(bg);
                                 }
                             }
                         }
                     }
                 }
            }
        }
        
        refresh(); 
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        monthLabel = new JLabel("", SwingConstants.LEFT);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        monthLabelWrapper = new JPanel(new BorderLayout());
        monthLabelWrapper.setBackground(mainApp.isDarkMode() ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        monthLabelWrapper.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, mainApp.isDarkMode() ? ModernColors.DARK_BORDER : ModernColors.BORDER), 
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        monthLabelWrapper.add(monthLabel, BorderLayout.CENTER);
        
        JPanel navPanel = new JPanel(new BorderLayout(15, 0));
        navPanel.setOpaque(false);
        
        JButton prevBtn = createNavButton("<");
        prevBtn.addActionListener(e -> navigate(-1));
        
        JButton nextBtn = createNavButton(">");
        nextBtn.addActionListener(e -> navigate(1));
        
        JPanel arrowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        arrowPanel.setOpaque(false);
        arrowPanel.add(prevBtn);
        arrowPanel.add(nextBtn);
        
        navPanel.add(monthLabelWrapper, BorderLayout.WEST); 
        navPanel.add(arrowPanel, BorderLayout.EAST);
        
        JButton todayBtn = createNavButton("Today", ModernColors.ERROR); 
        todayBtn.addActionListener(e -> {
            currentDate = LocalDate.now();
            refresh();
        });
        
        header.add(navPanel, BorderLayout.CENTER);
        header.add(todayBtn, BorderLayout.EAST);
        
        return header;
    }
    
    private JButton createNavButton(String text) {
        return createNavButton(text, ModernColors.PRIMARY); 
    }
    
    private JButton createNavButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true); 
        btn.setBorderPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(text.equals("Today") ? 80 : 45, 35));
        return btn;
    }
    
    public void jumpToDate(LocalDate date) {
        this.currentDate = date;
        switchToDayView(); 
    }
    
    public void switchToMonthView() {
        viewMode = "MONTH";
        centerPanel.removeAll();
        centerPanel.add(calendarGrid, BorderLayout.CENTER);
        refresh();
    }
    
    public void switchToWeekView() {
        viewMode = "WEEK";
        centerPanel.removeAll();
        centerPanel.add(calendarGrid, BorderLayout.CENTER); 
        refresh();
    }
    
    public void switchToDayView() {
        viewMode = "DAY";
        centerPanel.removeAll();
        JScrollPane scroll = new JScrollPane(dayViewPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(scroll, BorderLayout.CENTER);
        refresh();
    }
    
    private void navigate(int direction) {
        if (viewMode.equals("MONTH")) {
            currentDate = currentDate.plusMonths(direction);
        } else if (viewMode.equals("WEEK")) {
            currentDate = currentDate.plusWeeks(direction);
        } else {
            currentDate = currentDate.plusDays(direction);
        }
        refresh();
    }
    
    public void refresh() {
        if (viewMode.equals("MONTH")) {
            monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        } else if (viewMode.equals("WEEK")) {
             LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            monthLabel.setText(startOfWeek.format(DateTimeFormatter.ofPattern("MMM dd")) + 
                              " - " + startOfWeek.plusDays(6).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        } else {
            monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        }

        if (viewMode.equals("MONTH")) {
            calendarGrid.removeAll();
            renderMonthView();
        } else if (viewMode.equals("WEEK")) {
            calendarGrid.removeAll();
            renderWeekView();
        } else {
            dayViewPanel.setDate(currentDate);
        }
        
        centerPanel.revalidate();
        centerPanel.repaint();
    }
    
    private void renderMonthView() {
        calendarGrid.setLayout(new GridLayout(0, 7, 8, 8));
        calendarGrid.setOpaque(true); 
        calendarGrid.setBackground(mainApp.isDarkMode() ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND);
        
        boolean isDarkMode = mainApp.isDarkMode();
        Color headerBg = isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE;
        Color headerFg = isDarkMode ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY;
        Color headerBorder = isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER;
        
        String[] days = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        for (String day : days) {
            JPanel dayHeaderPanel = new JPanel(new BorderLayout());
            dayHeaderPanel.setBackground(headerBg);
            dayHeaderPanel.setBorder(new RoundedBorder(8, headerBorder));
            
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 13));
            label.setForeground(headerFg);
            label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); 
            
            dayHeaderPanel.add(label, BorderLayout.CENTER);
            calendarGrid.add(dayHeaderPanel);
        }
        
        LocalDate firstDay = currentDate.withDayOfMonth(1);
        int startDay = firstDay.getDayOfWeek().getValue() - 1; 
        int daysInMonth = currentDate.lengthOfMonth();
        
        for (int i = 0; i < startDay; i++) {
            calendarGrid.add(new JLabel(""));
        }
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentDate.withDayOfMonth(day);
            calendarGrid.add(createDayPanel(date));
        }
    }
    
    private JPanel createDayPanel(LocalDate date) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        Color defaultBgColor = mainApp.isDarkMode() ? ModernColors.DARK_CARD : ModernColors.CARD;
        Color defaultFgColor = mainApp.isDarkMode() ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY;
        Color borderColor = mainApp.isDarkMode() ? ModernColors.DARK_BORDER : ModernColors.BORDER;
        
        panel.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        panel.setBackground(defaultBgColor); 
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        topPanel.setOpaque(false);
        
        JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        dayLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dayLabel.setPreferredSize(new Dimension(30, 30)); 
        
        if (date.equals(LocalDate.now())) {
            JPanel roundBg = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ModernColors.ERROR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); 
                    g2.dispose();
                }
            };
            roundBg.setOpaque(false);
            roundBg.setPreferredSize(new Dimension(30, 30));
            dayLabel.setForeground(Color.WHITE); 
            roundBg.add(dayLabel, BorderLayout.CENTER);
            topPanel.add(roundBg);
            
        } else {
            dayLabel.setForeground(defaultFgColor); 
            topPanel.add(dayLabel);
        }
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        List<Task> tasksForDay = taskManager.getTasksForDate(date);
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setOpaque(false); 
        taskPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        
        int displayLimit = 3;
        for (Task task : tasksForDay.stream().limit(displayLimit).collect(Collectors.toList())) {
            JPanel taskItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 1));
            taskItem.setOpaque(false); 
            
            JLabel dot = new JLabel("●");
            dot.setForeground(task.getCategory().getColor());
            dot.setFont(new Font("Arial", Font.PLAIN, 10));
            
            JLabel taskLabel = new JLabel(task.getTitle());
            taskLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            taskLabel.setForeground(defaultFgColor); 
            
            taskItem.add(dot);
            taskItem.add(taskLabel);
            taskPanel.add(taskItem);
        }
        
        if (tasksForDay.size() > displayLimit) {
            JLabel moreLabel = new JLabel("+" + (tasksForDay.size() - displayLimit) + " more", SwingConstants.CENTER);
            moreLabel.setFont(new Font("Arial", Font.ITALIC, 9));
            moreLabel.setForeground(ModernColors.PRIMARY); 
            taskPanel.add(moreLabel);
        }
        
        panel.add(taskPanel, BorderLayout.CENTER);
        
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.setTransferHandler(new DayPanelDropHandler(date, taskManager, mainApp, null));
        
        final Color hoverColor = mainApp.isDarkMode() ? ModernColors.DARK_HOVER : ModernColors.HOVER;
        final Color originalBg = panel.getBackground();
        
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showTasksForDate(date);
            }
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                panel.setBackground(originalBg); 
            }
        });
        
        return panel;
    }
    
    private void renderWeekView() {
        calendarGrid.setLayout(new GridLayout(1, 7, 10, 0));
        calendarGrid.setOpaque(true);
        calendarGrid.setBackground(mainApp.isDarkMode() ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND);
        
        LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        for (int i = 0; i < 7; i++) {
            calendarGrid.add(createDetailedDayPanel(startOfWeek.plusDays(i)));
        }
    }
    
    private JPanel createDetailedDayPanel(LocalDate date) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(
            mainApp.isDarkMode() ? ModernColors.DARK_BORDER : ModernColors.BORDER, 2));
        panel.setBackground(mainApp.isDarkMode() ? ModernColors.DARK_CARD : ModernColors.CARD);
        
        JLabel dateLabel = new JLabel(date.format(DateTimeFormatter.ofPattern("EEE, MMM dd")), SwingConstants.CENTER);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 15));
        dateLabel.setForeground(mainApp.isDarkMode() ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        dateLabel.setBackground(mainApp.isDarkMode() ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        dateLabel.setOpaque(true);
        
        if (date.equals(LocalDate.now())) {
            dateLabel.setBackground(ModernColors.ERROR);
            dateLabel.setForeground(Color.WHITE);
        }
        
        panel.add(dateLabel, BorderLayout.NORTH);
        
        List<Task> tasks = taskManager.getTasksForDate(date);
        JPanel taskList = new JPanel();
        taskList.setLayout(new BoxLayout(taskList, BoxLayout.Y_AXIS));
        taskList.setBackground(panel.getBackground());
        taskList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        for (Task task : tasks) {
            taskList.add(createTaskCard(task));
            taskList.add(Box.createVerticalStrut(8));
        }
        
        if (tasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("No tasks scheduled", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 13));
            emptyLabel.setForeground(mainApp.isDarkMode() ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY);
            taskList.add(emptyLabel);
        }
        
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(panel.getBackground());
        scrollPane.getViewport().setBackground(panel.getBackground());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        panel.setTransferHandler(new DayPanelDropHandler(date, taskManager, mainApp, null));
        
        return panel;
    }
    
    private JPanel createTaskCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(mainApp.isDarkMode() ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, task.getCategory().getColor()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setOpaque(false);
        
        JLabel priorityDot = new JLabel("●");
        priorityDot.setForeground(task.getPriority().getColor());
        priorityDot.setFont(new Font("Arial", Font.BOLD, 20));
        
        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(mainApp.isDarkMode() ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        
        leftPanel.add(priorityDot);
        leftPanel.add(titleLabel);
        
        JLabel timeLabel = new JLabel(task.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setForeground(mainApp.isDarkMode() ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY);
        
        card.add(leftPanel, BorderLayout.CENTER);
        card.add(timeLabel, BorderLayout.EAST);
        
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                mainApp.showEditTaskDialog(task);
            }
        });
        
        return card;
    }
    
    private void showTasksForDate(LocalDate date) {
        List<Task> tasks = taskManager.getTasksForDate(date);
        
        JDialog dialog = new JDialog(mainApp, 
            "Tasks for " + date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), true);
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(mainApp.isDarkMode() ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        DefaultListModel<Task> listModel = new DefaultListModel<>();
        tasks.forEach(listModel::addElement);
        
        JList<Task> taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskListCellRenderer(mainApp.isDarkMode()));
        taskList.setBackground(mainApp.isDarkMode() ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && taskList.getSelectedValue() != null) {
                    mainApp.showEditTaskDialog(taskList.getSelectedValue());
                    dialog.dispose();
                }
            }
        });
        
        panel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        
        JButton addBtn = new JButton("Add New Task");
        addBtn.setBackground(ModernColors.PRIMARY);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setOpaque(true);
        addBtn.setBorderPainted(false);
        addBtn.addActionListener(e -> {
            mainApp.showAddTaskDialog();
            dialog.dispose();
        });
        panel.add(addBtn, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
}


// === MODIFIED: Main Task List Panel (FIXED) ===
class MainTaskListPanel extends JPanel {
    private TaskManager taskManager;
    private MainAppFrame mainApp;
    private JTabbedPane tabbedPane;
    private TaskListView upcomingTasksPanel;
    private TaskListView pastTasksPanel;

    public MainTaskListPanel(TaskManager taskManager, MainAppFrame mainApp) {
        this.taskManager = taskManager;
        this.mainApp = mainApp;
        
        setLayout(new BorderLayout(0, 15));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Tasks");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        upcomingTasksPanel = new TaskListView(taskManager, mainApp, "upcoming");
        pastTasksPanel = new TaskListView(taskManager, mainApp, "past");
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Upcoming", upcomingTasksPanel);
        tabbedPane.addTab("Past & Completed", pastTasksPanel);
        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton addBtn = createModernButton("Add Task", ModernColors.PRIMARY);
        addBtn.addActionListener(e -> mainApp.showAddTaskDialog());
        
        JButton deleteBtn = createModernButton("Delete", ModernColors.ERROR);
        deleteBtn.addActionListener(e -> deleteSelectedTask());
        
        JButton completeBtn = createModernButton("Complete", ModernColors.SUCCESS);
        completeBtn.addActionListener(e -> toggleTaskCompletion());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(completeBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private TaskListView getActiveListView() {
        if (tabbedPane.getSelectedComponent() == upcomingTasksPanel) {
            return upcomingTasksPanel;
        } else {
            return pastTasksPanel;
        }
    }

    private void deleteSelectedTask() {
        Task selected = getActiveListView().getSelectedTask();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this task?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                taskManager.deleteTask(selected);
                mainApp.refreshViews();
            }
        }
    }

    private void toggleTaskCompletion() {
        Task selected = getActiveListView().getSelectedTask();
        if (selected != null) {
            selected.setCompleted(!selected.isCompleted());
            taskManager.updateTask(selected);
            mainApp.refreshViews();
        }
    }

    public void refresh() {
        upcomingTasksPanel.refresh();
        pastTasksPanel.refresh();
    }
    
    // === MODIFIED: applyTheme (FIXED) ===
    public void applyTheme(boolean isDarkMode) {
        Color bg = isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND;
        Color text = isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY;
        Color surface = isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE;
        Color card = isDarkMode ? ModernColors.DARK_CARD : ModernColors.CARD;
        Color border = isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER;

        setBackground(bg);
        
        Component titleComp = getComponent(0);
        if (titleComp instanceof JLabel) {
             ((JLabel)titleComp).setForeground(text);
        }
        
        // === THIS IS THE FIX ===
        // We set the UIManager properties *just before* updating the component
        // This is more reliable for JTabbedPane
        if (isDarkMode) {
            Color lightText = ModernColors.DARK_TEXT_PRIMARY; 
            UIManager.put("TabbedPane.background", surface); 
            UIManager.put("TabbedPane.foreground", lightText); 
            UIManager.put("TabbedPane.selectedForeground", lightText);
            UIManager.put("TabbedPane.contentAreaColor", card); 
            UIManager.put("TabbedPane.selected", card); // Dark Card
            UIManager.put("TabbedPane.unselectedBackground", surface); // Dark Surface
            UIManager.put("TabbedPane.tabAreaBackground", surface); 
            UIManager.put("TabbedPane.borderHighlightColor", border);
            UIManager.put("TabbedPane.darkShadow", border);
            UIManager.put("TabbedPane.focus", surface.brighter()); 
        } else {
            Color darkText = ModernColors.TEXT_PRIMARY; 
            UIManager.put("TabbedPane.background", ModernColors.SURFACE);
            UIManager.put("TabbedPane.foreground", darkText);
            UIManager.put("TabbedPane.selectedForeground", darkText);
            UIManager.put("TabbedPane.contentAreaColor", ModernColors.CARD);
            UIManager.put("TabbedPane.selected", ModernColors.CARD); // White Card
            UIManager.put("TabbedPane.unselectedBackground", ModernColors.SURFACE); // Light Surface
            UIManager.put("TabbedPane.tabAreaBackground", ModernColors.SURFACE); 
            UIManager.put("TabbedPane.borderHighlightColor", ModernColors.BORDER);
            UIManager.put("TabbedPane.darkShadow", ModernColors.BORDER);
            UIManager.put("TabbedPane.focus", ModernColors.SURFACE.darker());
        }
        
        // This applies the UIManager properties
        SwingUtilities.updateComponentTreeUI(tabbedPane);
        // === END OF FIX ===
        
        upcomingTasksPanel.applyTheme(isDarkMode);
        pastTasksPanel.applyTheme(isDarkMode);

        if (getComponentCount() > 2 && getComponent(2) != null) {
             Component buttonPanelComp = getComponent(2);
             if (buttonPanelComp instanceof JPanel) {
                 buttonPanelComp.setBackground(bg);
             }
        }
    }
    
    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setOpaque(true); 
        btn.setBorderPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 35));
        return btn;
    }
}

// === NEW: Panel for a single Task List ===
class TaskListView extends JPanel {
    private TaskManager taskManager;
    private MainAppFrame mainApp;
    private String filterMode; 
    private JList<Task> taskList;
    private DefaultListModel<Task> listModel;
    
    public TaskListView(TaskManager taskManager, MainAppFrame mainApp, String filterMode) {
        this.taskManager = taskManager;
        this.mainApp = mainApp;
        this.filterMode = filterMode;
        
        setLayout(new BorderLayout());
        setOpaque(true);
        
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskListCellRenderer(mainApp.isDarkMode()));
        taskList.setOpaque(true);
        
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && taskList.getSelectedValue() != null) {
                    mainApp.showEditTaskDialog(taskList.getSelectedValue());
                }
            }
        });
        
        taskList.setDragEnabled(true);
        taskList.setTransferHandler(new TaskDragHandler());
        
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setOpaque(true);
        add(scrollPane, BorderLayout.CENTER);
        
        refresh();
    }
    
    public Task getSelectedTask() {
        return taskList.getSelectedValue();
    }
    
    public void applyTheme(boolean isDarkMode) {
        Color card = isDarkMode ? ModernColors.DARK_CARD : ModernColors.CARD;
        Color border = isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER;
        
        setBackground(card); 
        taskList.setBackground(card);
        taskList.setCellRenderer(new TaskListCellRenderer(isDarkMode)); 
        
        JScrollPane scrollPane = (JScrollPane) getComponent(0);
        scrollPane.setBackground(card);
        scrollPane.getViewport().setBackground(card);
        scrollPane.setBorder(BorderFactory.createLineBorder(border));
        
        refresh();
    }
    
    public void refresh() {
        listModel.clear();
        
        List<Task> tasks = getFilteredTasks();
        tasks.stream()
            .sorted(Comparator.comparing(Task::getDateTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .forEach(listModel::addElement);
    }
    
    private List<Task> getFilteredTasks() {
        List<Task> allTasks = taskManager.getTasks();
        LocalDate today = LocalDate.now();
        
        if (filterMode.equals("upcoming")) {
            return allTasks.stream()
                .filter(t -> !t.isCompleted() && t.getDateTime() != null && !t.getDateTime().toLocalDate().isBefore(today))
                .collect(Collectors.toList());
        } else { // "past"
            return allTasks.stream()
                .filter(t -> t.isCompleted() || (t.getDateTime() != null && t.getDateTime().toLocalDate().isBefore(today)))
                .collect(Collectors.toList());
        }
    }
}


// === MODIFIED Task List Cell Renderer (FIXED) ===
class TaskListCellRenderer extends JPanel implements ListCellRenderer<Task> {
    private JLabel titleLabel;
    private JLabel detailsLabel;
    private JCheckBox completeBox;
    private JLabel priorityLabel;
    private boolean isDarkMode;
    
    public TaskListCellRenderer(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        setLayout(new BorderLayout(10, 5));
        
        setBackground(isDarkMode ? ModernColors.DARK_CARD : ModernColors.CARD);
        
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        completeBox = new JCheckBox();
        completeBox.setEnabled(false);
        completeBox.setOpaque(false);
        
        priorityLabel = new JLabel("●");
        priorityLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        detailsLabel = new JLabel();
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        detailsLabel.setForeground(isDarkMode ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(detailsLabel);
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(completeBox);
        leftPanel.add(priorityLabel);
        
        add(leftPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }
    
    private Color mixColor(Color c1, Color c2, double ratio) {
        int r = (int) (c1.getRed() * ratio + c2.getRed() * (1.0 - ratio));
        int g = (int) (c1.getGreen() * ratio + c2.getGreen() * (1.0 - ratio));
        int b = (int) (c1.getBlue() * ratio + c2.getBlue() * (1.0 - ratio));
        return new Color(r, g, b);
    }
    
    @Override
    public Component getListCellRendererComponent(JList<? extends Task> list, Task task,
                                                 int index, boolean isSelected, boolean cellHasFocus) {
        completeBox.setSelected(task.isCompleted());
        priorityLabel.setForeground(task.getPriority().getColor());
        
        String title = task.getTitle();
        if (task.isCompleted()) {
            title = "<html><strike>" + title + "</strike></html>";
        }
        titleLabel.setText(title);
        titleLabel.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        
        String details = task.getCategory().getDisplayName();
        if (task.getDateTime() != null) {
            details += " • " + task.getDateTime().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        }
        detailsLabel.setText(details);
        
        Color cardBg = isDarkMode ? ModernColors.DARK_CARD : ModernColors.CARD;
        Color categoryColor = task.getCategory().getColor();
        Color taskBg = mixColor(categoryColor, cardBg, isDarkMode ? 0.2 : 0.15); 
        Color selectedBg = isDarkMode ? ModernColors.DARK_HOVER : ModernColors.HOVER;
        
        setBackground(isSelected ? selectedBg : taskBg);
        
        // Make text panel background match so selection looks seamless
        for(Component c : getComponents()) {
            if (c instanceof JComponent) { 
                ((JComponent) c).setOpaque(false); 
            }
        }
        
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 1, 0, task.getCategory().getColor()),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        return this;
    }
}

// === MODIFIED Task Dialog (REVERTED TO SCROLLING) ===
class TaskDialog extends JDialog {
    private TaskManager taskManager;
    private MainAppFrame mainApp; 
    private Task editingTask;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private JSpinner durationSpinner; 
    private JComboBox<Category> categoryCombo;
    private JComboBox<Priority> priorityCombo;
    private JComboBox<RecurrenceType> recurrenceCombo;
    private boolean isDarkMode;
    
    private JList<String> attachmentList;
    private DefaultListModel<String> attachmentListModel;

    public TaskDialog(MainAppFrame owner, TaskManager taskManager, Task task, boolean isDarkMode) {
        super(owner, task == null ? "Add Task" : "Edit Task", true);
        this.mainApp = owner; 
        this.taskManager = taskManager;
        this.editingTask = task;
        this.isDarkMode = isDarkMode;

        setSize(550, 800); // Reverted to original scrolling height
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel titleLabel = new JLabel(task == null ? "Create New Task" : "Edit Task");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Re-center title
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(25));

        // Title
        addFieldLabel(mainPanel, "Title *");
        titleField = new JTextField();
        titleField.setFont(new Font("Arial", Font.PLAIN, 13));
        if (task != null) titleField.setText(task.getTitle());
        JPanel titleWrapper = createStyledWrapper(titleField, null, isDarkMode);
        mainPanel.add(titleWrapper);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Description
        addFieldLabel(mainPanel, "Description");
        descriptionArea = new JTextArea(task != null ? task.getDescription() : "", 4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 13));
        descriptionArea.setBackground(isDarkMode ? ModernColors.DARK_CARD : Color.WHITE);
        descriptionArea.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER), 
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        descScroll.setBorder(null); 
        mainPanel.add(descScroll);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Date
        addFieldLabel(mainPanel, "Date *");
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MMM dd, yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (task != null && task.getDateTime() != null) {
            dateSpinner.setValue(java.sql.Timestamp.valueOf(task.getDateTime()));
        }
        mainPanel.add(dateSpinner);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Time
        addFieldLabel(mainPanel, "Time *");
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (task != null && task.getDateTime() != null) {
            timeSpinner.setValue(java.sql.Timestamp.valueOf(task.getDateTime()));
        }
        mainPanel.add(timeSpinner);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Duration
        addFieldLabel(mainPanel, "Duration (minutes) *");
        SpinnerNumberModel durationModel = new SpinnerNumberModel(60, 1, 1440, 15); 
        durationSpinner = new JSpinner(durationModel);
        durationSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (task != null) {
            durationSpinner.setValue(task.getDurationMinutes());
        }
        mainPanel.add(durationSpinner);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Category
        addFieldLabel(mainPanel, "Category *");
        categoryCombo = new JComboBox<>(Category.values());
        categoryCombo.setRenderer(new CategoryComboRenderer());
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (task != null) categoryCombo.setSelectedItem(task.getCategory());
        mainPanel.add(categoryCombo);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Priority
        addFieldLabel(mainPanel, "Priority *");
        priorityCombo = new JComboBox<>(Priority.values());
        priorityCombo.setRenderer(new PriorityComboRenderer());
        priorityCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (task != null) priorityCombo.setSelectedItem(task.getPriority());
        mainPanel.add(priorityCombo);
        mainPanel.add(Box.createVerticalStrut(15));

        // Recurrence
        addFieldLabel(mainPanel, "Recurrence");
        recurrenceCombo = new JComboBox<>(RecurrenceType.values());
        recurrenceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (task != null) recurrenceCombo.setSelectedItem(task.getRecurrence());
        mainPanel.add(recurrenceCombo);
        mainPanel.add(Box.createVerticalStrut(15));

        // Attachments Section
        addFieldLabel(mainPanel, "Attachments (Double-click to open)");
        attachmentListModel = new DefaultListModel<>();
        attachmentList = new JList<>(attachmentListModel);
        attachmentList.setBackground(isDarkMode ? ModernColors.DARK_CARD : Color.WHITE);
        attachmentList.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);

        if (task != null) {
            task.getAttachmentPaths().forEach(attachmentListModel::addElement);
        }
        
        attachmentList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedAttachment();
                }
            }
        });

        JScrollPane attScroll = new JScrollPane(attachmentList);
        attScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        attScroll.setBorder(BorderFactory.createLineBorder(isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER));
        mainPanel.add(attScroll);
        mainPanel.add(Box.createVerticalStrut(8));
        
        JButton addAttachmentBtn = createStyledButton("Add Attachment", isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        addAttachmentBtn.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY); 
        addAttachmentBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addAttachmentBtn.addActionListener(e -> addAttachment());
        mainPanel.add(addAttachmentBtn);
        mainPanel.add(Box.createVerticalStrut(25));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton cancelButton = createStyledButton("Cancel", isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        cancelButton.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY); 
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = createStyledButton("Save Task", ModernColors.PRIMARY);
        saveButton.setForeground(Color.WHITE); 
        saveButton.addActionListener(e -> saveTask());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        mainPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }
    
    private void openSelectedAttachment() {
        String selectedPath = attachmentList.getSelectedValue();
        if (selectedPath == null) return;
        
        try {
            File file = new File(selectedPath);
            if (Desktop.isDesktopSupported() && file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot open file: " + selectedPath, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addAttachment() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select file to attach");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            attachmentListModel.addElement(file.getAbsolutePath());
        }
    }
    
    private void addFieldLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(isDarkMode ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // Reverted
        
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
    }

    private JPanel createStyledWrapper(JComponent component, JComponent accessory, boolean isDarkMode) {
        Color bg = isDarkMode ? ModernColors.DARK_CARD : Color.WHITE;
        Color border = isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER;
        
        JPanel wrapper = new JPanel(new BorderLayout(5, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.setBackground(bg);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, border),
            BorderFactory.createEmptyBorder(8, 12, 8, 12) 
        ));
        
        component.setBorder(null); 
        component.setBackground(bg);
        
        wrapper.add(component, BorderLayout.CENTER);
        if (accessory != null) {
            accessory.setOpaque(false);
            accessory.setBackground(bg); 
            wrapper.add(accessory, BorderLayout.EAST);
        }
        return wrapper;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setOpaque(true); 
        btn.setBorderPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40)); 
        return btn;
    }

    private void saveTask() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Date date = (Date) dateSpinner.getValue();
            Date time = (Date) timeSpinner.getValue();

            java.util.Calendar dateCal = java.util.Calendar.getInstance();
            dateCal.setTime(date);
            java.util.Calendar timeCal = java.util.Calendar.getInstance();
            timeCal.setTime(time);

            dateCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
            dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
            dateCal.set(java.util.Calendar.SECOND, 0);

            LocalDateTime dateTime = LocalDateTime.ofInstant(dateCal.toInstant(), ZoneId.systemDefault());
            
            int duration = (Integer) durationSpinner.getValue();

            List<String> attachmentPaths = new ArrayList<>();
            for (int i = 0; i < attachmentListModel.getSize(); i++) {
                attachmentPaths.add(attachmentListModel.getElementAt(i));
            }

            if (editingTask == null) {
                Task newTask = new Task(
                    titleField.getText().trim(),
                    descriptionArea.getText().trim(),
                    dateTime,
                    (Category) categoryCombo.getSelectedItem(),
                    (Priority) priorityCombo.getSelectedItem(),
                    duration 
                );
                newTask.setRecurrence((RecurrenceType) recurrenceCombo.getSelectedItem());
                newTask.setAttachmentPaths(attachmentPaths); 
                taskManager.addTask(newTask);
            } else {
                editingTask.setTitle(titleField.getText().trim());
                editingTask.setDescription(descriptionArea.getText().trim());
                editingTask.setDateTime(dateTime);
                editingTask.setDurationMinutes(duration); 
                editingTask.setCategory((Category) categoryCombo.getSelectedItem());
                editingTask.setPriority((Priority) priorityCombo.getSelectedItem());
                editingTask.setRecurrence((RecurrenceType) recurrenceCombo.getSelectedItem());
                editingTask.setAttachmentPaths(attachmentPaths); 
                taskManager.updateTask(editingTask);
            }

            dispose();
            mainApp.refreshViews(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving task: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}

// Category Combo Renderer
class CategoryComboRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                 int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Category) {
            Category cat = (Category) value;
            label.setText("  " + cat.getDisplayName());
            if (!isSelected) {
                label.setBackground(cat.getColor());
                int brightness = (int) (0.299 * cat.getColor().getRed() + 
                                        0.587 * cat.getColor().getGreen() + 
                                        0.114 * cat.getColor().getBlue());
                label.setForeground(brightness > 186 ? Color.BLACK : Color.WHITE);
            }
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }
        return label;
    }
}

// Priority Combo Renderer
class PriorityComboRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                 int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Priority) {
            Priority priority = (Priority) value;
            label.setText("● " + priority.getDisplayName());
            label.setForeground(priority.getColor());
            label.setFont(new Font("Arial", Font.BOLD, 13));
            label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }
        return label;
    }
}

// === MODIFIED Search Dialog ===
class SearchDialog extends JDialog {
    private TaskManager taskManager;
    private JTextField searchField;
    private JList<Task> resultList;
    private DefaultListModel<Task> listModel;
    private boolean isDarkMode;
    
    public SearchDialog(Frame owner, TaskManager taskManager, boolean isDarkMode) {
        super(owner, "Search Tasks", true);
        this.taskManager = taskManager;
        this.isDarkMode = isDarkMode;
        
        setSize(650, 550);
        setLocationRelativeTo(owner);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel titleLabel = new JLabel("Search Tasks");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBackground(isDarkMode ? ModernColors.DARK_CARD : Color.WHITE);
        searchField.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        searchField.setCaretColor(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        searchField.addActionListener(e -> performSearch());
        JPanel searchWrapper = createStyledWrapper(searchField, null, isDarkMode);
        
        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Arial", Font.BOLD, 13));
        searchBtn.setBackground(ModernColors.PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setOpaque(true);
        searchBtn.setBorderPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.setPreferredSize(new Dimension(100, 40));
        searchBtn.addActionListener(e -> performSearch());
        
        searchPanel.add(searchWrapper, BorderLayout.CENTER); 
        searchPanel.add(searchBtn, BorderLayout.EAST);
        
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setOpaque(false);
        searchContainer.add(searchPanel, BorderLayout.NORTH);
        searchContainer.add(Box.createVerticalStrut(15), BorderLayout.SOUTH);
        mainPanel.add(searchContainer, BorderLayout.NORTH);
        
        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setCellRenderer(new TaskListCellRenderer(isDarkMode));
        resultList.setBackground(isDarkMode ? ModernColors.DARK_CARD : ModernColors.CARD);
        resultList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && resultList.getSelectedValue() != null) {
                    ((MainAppFrame) owner).showEditTaskDialog(resultList.getSelectedValue());
                    dispose();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(BorderFactory.createLineBorder(
            isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 13));
        closeBtn.setBackground(isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE);
        closeBtn.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        closeBtn.setFocusPainted(false);
        closeBtn.setOpaque(true);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dispose());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        searchField.requestFocusInWindow();
    }
    
    private JPanel createStyledWrapper(JComponent component, JComponent accessory, boolean isDarkMode) {
        Color bg = isDarkMode ? ModernColors.DARK_CARD : Color.WHITE;
        Color border = isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER;
        
        JPanel wrapper = new JPanel(new BorderLayout(5, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.setBackground(bg);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, border),
            BorderFactory.createEmptyBorder(8, 12, 8, 12) 
        ));
        
        component.setBorder(null); 
        component.setBackground(bg);
        
        wrapper.add(component, BorderLayout.CENTER);
        if (accessory != null) {
            accessory.setOpaque(false);
            accessory.setBackground(bg); 
            wrapper.add(accessory, BorderLayout.EAST);
        }
        return wrapper;
    }
    
    private void performSearch() {
        listModel.clear();
        String keyword = searchField.getText().trim();
        
        if (!keyword.isEmpty()) {
            List<Task> results = taskManager.searchTasks(keyword);
            results.forEach(listModel::addElement);
            
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No tasks found matching '" + keyword + "'",
                    "No Results", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}

// === NEW CLASS ===
/**
 * A custom Swing border that paints rounded corners.
 */
class RoundedBorder implements Border {
    private int radius;
    private Color color;

    public RoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius/2, this.radius, this.radius/2, this.radius);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.color);
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g2.dispose();
    }
}

// === NEW CLASS: Draws the eye icon ===
/**
 * A custom Icon that draws an "eye" or "slashed eye"
 * to be used on a JToggleButton.
 */
class EyeIcon implements Icon {
    private boolean shown; 
    private int width = 16;
    private int height = 16;

    public EyeIcon(boolean shown) {
        this.shown = shown;
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground()); 
        g2.setStroke(new BasicStroke(1.5f));

        int eyeX = x + 2;
        int eyeY = y + 5;
        int eyeWidth = 12;
        
        g2.drawArc(eyeX, eyeY - 2, eyeWidth, 8, 30, 120);
        g2.drawArc(eyeX, eyeY - 2, eyeWidth, 8, -30, -120);
        
        g2.drawOval(eyeX + 4, eyeY, 4, 4);

        if (!shown) { // Draw slash
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 3, y + 13, x + 13, y + 3);
        }
        g2.dispose();
    }
}

// === MODIFIED ReminderService (FIXED) ===
/**
 * Runs a background timer to check for upcoming tasks
 * and triggers a system beep and a custom notification dialog.
 */
class ReminderService {
    private TaskManager taskManager;
    private MainAppFrame mainApp; 
    private Timer timer; 
    private Set<String> upcomingRemindedIds; // MODIFIED
    private Set<String> dueRemindedIds;     // MODIFIED

    public ReminderService(TaskManager taskManager, MainAppFrame mainApp) {
        this.taskManager = taskManager;
        this.mainApp = mainApp;
        this.upcomingRemindedIds = new HashSet<>(); // MODIFIED
        this.dueRemindedIds = new HashSet<>();     // MODIFIED
        this.timer = new Timer(30000, e -> checkReminders());
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    // === MODIFIED: Logic for two-stage reminders ===
    private void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        
        for (Task task : taskManager.getTasks()) {
            if (!task.isCompleted() && task.getDateTime() != null) {
                long minutesUntilDue = ChronoUnit.MINUTES.between(now, task.getDateTime());
                
                // 1. 10-Minute Warning
                // Remind if 9 or 10 minutes away.
                if (minutesUntilDue >= 9 && minutesUntilDue <= 10) {
                    if (!upcomingRemindedIds.contains(task.getId())) {
                        triggerReminder(task, " (in 10 minutes)");
                        upcomingRemindedIds.add(task.getId());
                    }
                }

                // 2. On-Time (Alarm)
                // Remind if 0 minutes away, or 1 minute past (to catch the 30-sec timer window)
                if (minutesUntilDue >= -1 && minutesUntilDue <= 0) {
                    if (!dueRemindedIds.contains(task.getId())) {
                        triggerReminder(task, " (DUE NOW)");
                        dueRemindedIds.add(task.getId());
                    }
                }
            }
        }
    }

    // === MODIFIED: Added dueMessage parameter ===
    private void triggerReminder(Task task, String dueMessage) {
        Toolkit.getDefaultToolkit().beep();
        
        SwingUtilities.invokeLater(() -> {
            ReminderDialog dialog = new ReminderDialog(task, mainApp.isDarkMode(), dueMessage);
            dialog.setVisible(true);
        });
    }
}

// === MODIFIED: ReminderDialog (FIXED) ===
/**
 * A custom JDialog to show a reminder with clickable attachments.
 */
class ReminderDialog extends JDialog {
    // === MODIFIED: Added dueMessage parameter ===
    public ReminderDialog(Task task, boolean isDarkMode, String dueMessage) {
        setTitle("Task Reminder!");
        setSize(450, 300); 
        setLocationRelativeTo(null); 
        setAlwaysOnTop(true);
        
        Color bg = isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND;
        Color surface = isDarkMode ? ModernColors.DARK_SURFACE : ModernColors.SURFACE;
        Color text = isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY;
        Color border = isDarkMode ? ModernColors.DARK_BORDER : ModernColors.BORDER; 
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel title = new JLabel(task.getTitle());
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(task.getPriority().getColor());
        panel.add(title, BorderLayout.NORTH);
        
        // === MODIFIED: Added dueMessage to time label ===
        String timeText = "Due at: " + task.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        if (dueMessage != null) {
            timeText += dueMessage;
        }
        JLabel time = new JLabel(timeText);
        time.setFont(new Font("Arial", Font.PLAIN, 14));
        time.setForeground(text);
        panel.add(time, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout(10, 10)); 
        southPanel.setOpaque(false);
        southPanel.setBackground(bg);

        if (!task.getAttachmentPaths().isEmpty()) {
            JPanel attachmentPanel = new JPanel();
            attachmentPanel.setLayout(new BoxLayout(attachmentPanel, BoxLayout.Y_AXIS));
            attachmentPanel.setBackground(surface);
            
            // === THIS IS THE FIX ===
            Border lineBorder = BorderFactory.createLineBorder(border);
            TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, "Attachments");
            titledBorder.setTitleColor(text); // Set title color
            attachmentPanel.setBorder(titledBorder);
            // === END OF FIX ===
            
            for (String path : task.getAttachmentPaths()) {
                File file = new File(path);
                JButton attButton = new JButton(file.getName()); 
                attButton.setHorizontalAlignment(SwingConstants.LEFT);
                attButton.setBorderPainted(false);
                attButton.setContentAreaFilled(false);
                attButton.setOpaque(false);
                attButton.setForeground(ModernColors.PRIMARY);
                attButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                attButton.addActionListener(e -> {
                    try {
                        if (Desktop.isDesktopSupported() && file.exists()) {
                            Desktop.getDesktop().open(file);
                        }
                    } catch (IOException ioex) {
                        JOptionPane.showMessageDialog(this, "Error opening file: " + ioex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                attachmentPanel.add(attButton);
            }
            JScrollPane attScroll = new JScrollPane(attachmentPanel);
            attScroll.setPreferredSize(new Dimension(400, 100));
            southPanel.add(attScroll, BorderLayout.CENTER);
        }

        JButton closeButton = new JButton("Dismiss");
        closeButton.setBackground(ModernColors.PRIMARY);
        closeButton.setForeground(Color.WHITE);
        closeButton.setOpaque(true);
        closeButton.setBorderPainted(false);
        closeButton.addActionListener(e -> dispose());
        
        southPanel.add(closeButton, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);

        add(panel);
    }
}

// === NEW CLASS ===
/**
 * A custom JDialog to search for a specific date.
 */
class GoToDateDialog extends JDialog {
    private JSpinner dateSpinner;
    private LocalDate selectedDate = null;
    
    public GoToDateDialog(Frame owner, boolean isDarkMode) {
        super(owner, "Go to Date", true);
        setSize(350, 200);
        setLocationRelativeTo(owner);
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel label = new JLabel("Select a date to jump to:");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(isDarkMode ? ModernColors.DARK_TEXT_PRIMARY : ModernColors.TEXT_PRIMARY);
        panel.add(label, BorderLayout.NORTH);
        
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MMM dd, yyyy");
        dateSpinner.setEditor(dateEditor);
        panel.add(dateSpinner, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        
        JButton findBtn = new JButton("Find");
        findBtn.setBackground(ModernColors.PRIMARY);
        findBtn.setForeground(Color.WHITE);
        findBtn.setOpaque(true);
        findBtn.setBorderPainted(false);
        findBtn.addActionListener(e -> {
            Date date = (Date) dateSpinner.getValue();
            selectedDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            dispose();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(findBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(panel);
    }
    
    public LocalDate getSelectedDate() {
        return selectedDate;
    }
}

// === NEW: Drag and Drop TransferHandler for JList ===
class TaskDragHandler extends TransferHandler {
    
    public static final DataFlavor TASK_ID_FLAVOR = DataFlavor.stringFlavor;

    public TaskDragHandler() {
        super();
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JList<Task> taskList = (JList<Task>) c;
        Task task = taskList.getSelectedValue();
        
        if (task != null) {
            return new StringSelection(task.getId()); 
        }
        return null;
    }
}

// === NEW: Drag and Drop TransferHandler for Day Panels ===
class DayPanelDropHandler extends TransferHandler {
    private LocalDate date;
    private TaskManager taskManager;
    private MainAppFrame mainApp;
    private DayViewPanel dayViewPanel; 

    public DayPanelDropHandler(LocalDate date, TaskManager taskManager, MainAppFrame mainApp, DayViewPanel dayViewPanel) {
        this.date = date;
        this.taskManager = taskManager;
        this.mainApp = mainApp;
        this.dayViewPanel = dayViewPanel;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            String taskID = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            Task task = taskManager.getTaskById(taskID);
            if (task == null) {
                return false;
            }
            
            Point dropPoint = support.getDropLocation().getDropPoint();
            
            LocalTime time;
            if (dayViewPanel != null) {
                time = dayViewPanel.getTimeFromY(dropPoint.y);
            } else {
                time = task.getDateTime().toLocalTime();
            }

            task.setDateTime(this.date.atTime(time));
            taskManager.updateTask(task);
            mainApp.refreshViews();
            
            return true;

        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

// === NEW CLASS: Day View Panel ===
class DayViewPanel extends JPanel {
    private static final int HOUR_HEIGHT = 60;
    private static final int LEFT_MARGIN = 60;
    
    private TaskManager taskManager;
    private MainAppFrame mainApp;
    private LocalDate date;
    private List<Task> tasks;
    
    public DayViewPanel(TaskManager taskManager, MainAppFrame mainApp) {
        this.taskManager = taskManager;
        this.mainApp = mainApp;
        this.date = LocalDate.now();
        this.tasks = new ArrayList<>();
        
        setLayout(null); 
        setPreferredSize(new Dimension(600, HOUR_HEIGHT * 24));
        
        setTransferHandler(new DayPanelDropHandler(date, taskManager, mainApp, this));
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    LocalTime clickedTime = getTimeFromY(e.getY());
                    mainApp.showAddTaskDialog();
                }
            }
        });
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
        this.tasks = taskManager.getTasksForDate(date);
        
        setTransferHandler(new DayPanelDropHandler(date, taskManager, mainApp, this));
        
        removeAll();
        addTasksToView();
        
        revalidate();
        repaint();
    }
    
    public void applyTheme(boolean isDarkMode) {
        setBackground(isDarkMode ? ModernColors.DARK_BACKGROUND : ModernColors.BACKGROUND);
        setForeground(isDarkMode ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY);
        revalidate();
        repaint();
    }
    
    public LocalTime getTimeFromY(int y) {
        int totalMinutes = (y * 24 * 60) / (HOUR_HEIGHT * 24);
        int hour = totalMinutes / 60;
        int minute = totalMinutes % 60;
        if (hour > 23) hour = 23;
        if (hour < 0) hour = 0;
        return LocalTime.of(hour, minute);
    }
    
    private int getYFromTime(LocalTime time) {
        int minutes = time.getHour() * 60 + time.getMinute();
        return (minutes * (HOUR_HEIGHT * 24)) / (24 * 60);
    }
    
    private void addTasksToView() {
        for (Task task : tasks) {
            JPanel taskPanel = new JPanel(new BorderLayout(5, 2));
            taskPanel.setBackground(task.getCategory().getColor().brighter());
            taskPanel.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, task.getCategory().getColor()));
            
            int y = getYFromTime(task.getDateTime().toLocalTime());
            int height = (int) (task.getDurationMinutes() / 60.0 * HOUR_HEIGHT);
            if (height < 20) height = 20; 
            
            taskPanel.setBounds(LEFT_MARGIN + 5, y, getWidth() - LEFT_MARGIN - 10, height);
            
            JLabel title = new JLabel(task.getTitle());
            title.setFont(new Font("Arial", Font.BOLD, 12));
            title.setForeground(Color.BLACK); 
            taskPanel.add(title, BorderLayout.NORTH);
            
            JLabel time = new JLabel(task.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            time.setFont(new Font("Arial", Font.PLAIN, 10));
            time.setForeground(Color.BLACK);
            taskPanel.add(time, BorderLayout.SOUTH);
            
            taskPanel.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        mainApp.showEditTaskDialog(task);
                        e.consume(); 
                    }
                }
            });
            
            add(taskPanel);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        int width = getWidth();
        
        for (int i = 0; i < 24; i++) {
            int y = i * HOUR_HEIGHT;
            g2.setColor(mainApp.isDarkMode() ? ModernColors.DARK_BORDER : ModernColors.BORDER);
            g2.drawLine(LEFT_MARGIN, y, width, y);
            
            g2.setColor(mainApp.isDarkMode() ? ModernColors.DARK_TEXT_SECONDARY : ModernColors.TEXT_SECONDARY);
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            String time = LocalTime.of(i, 0).format(DateTimeFormatter.ofPattern("h a"));
            g2.drawString(time, 10, y + 5); 
        }
        
        if (date.equals(LocalDate.now())) {
            int y = getYFromTime(LocalTime.now());
            g2.setColor(ModernColors.ERROR);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(LEFT_MARGIN, y, width, y);
            g2.fillOval(LEFT_MARGIN - 5, y - 5, 10, 10);
        }
    }
}