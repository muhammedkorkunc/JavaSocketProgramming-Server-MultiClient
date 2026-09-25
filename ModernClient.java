package javasocketprogramming_java.multiclient.server;
/**
 * ============================================================================
 * PROJE BİLGİLERİ & YAZAR DETAYLARI
 * ============================================================================
 * @author   Muhammed Emin Korkunç
 * @email    muhammedeminkorkunc@gmail.com
 * @linkedin https://www.linkedin.com/in/muhammed-emin-korkunç-100ba2215
 * @github   https://github.com/muhammedkorkunc
 * ============================================================================
 */
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ModernClient extends JFrame {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dout;
    private boolean connected = false;

    private JTextField hostInput;
    private JTextField portInput;
    private JTextField usernameInput;
    private JButton connectBtn;

    private JEditorPane chatPane;
    private final StringBuilder chatHistory = new StringBuilder();
    private JTextField msgInput;
    private JButton sendBtn;

    private final Color COLOR_CHAT_BG = new Color(54, 57, 63);
    private final Color COLOR_TOPBAR = new Color(32, 34, 37);
    private final Color COLOR_ACCENT = new Color(88, 101, 242);
    private final Color COLOR_INPUT = new Color(64, 68, 75);

    public ModernClient() {
        setupUI();
    }

    private void setupUI() {
        setTitle("Modern Chat Client");
        setSize(650, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Üst Bağlantı Çubuğu
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topBar.setBackground(COLOR_TOPBAR);

        hostInput = ModernServer.createStyledTextField("127.0.0.1", 8);
        portInput = ModernServer.createStyledTextField("1201", 5);
        usernameInput = ModernServer.createStyledTextField("Kullanici", 8);
        connectBtn = ModernServer.createStyledButton("Bağlan", COLOR_ACCENT);

        topBar.add(new JLabel("<html><font color='#b9bbbe'>IP:</font></html>"));
        topBar.add(hostInput);
        topBar.add(new JLabel("<html><font color='#b9bbbe'>Port:</font></html>"));
        topBar.add(portInput);
        topBar.add(new JLabel("<html><font color='#b9bbbe'>Kullanıcı:</font></html>"));
        topBar.add(usernameInput);
        topBar.add(connectBtn);
        add(topBar, BorderLayout.NORTH);

        // Orta: HTML Destekli Chat Akışı
        chatPane = new JEditorPane();
        chatPane.setContentType("text/html");
        chatPane.setEditable(false);
        chatPane.setBackground(COLOR_CHAT_BG);
        chatPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Alt: Mesaj Giriş Alanı
        JPanel bottomBar = new JPanel(new BorderLayout(10, 10));
        bottomBar.setBackground(COLOR_TOPBAR);
        bottomBar.setBorder(new EmptyBorder(12, 14, 12, 14));

        msgInput = new JTextField();
        msgInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msgInput.setBackground(COLOR_INPUT);
        msgInput.setForeground(Color.WHITE);
        msgInput.setCaretColor(Color.WHITE);
        msgInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(32, 34, 37), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        msgInput.setEnabled(false);

        sendBtn = ModernServer.createStyledButton("Gönder", COLOR_ACCENT);
        sendBtn.setEnabled(false);

        bottomBar.add(msgInput, BorderLayout.CENTER);
        bottomBar.add(sendBtn, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        connectBtn.addActionListener(e -> {
            if (!connected) {
                connect();
            } else {
                disconnect();
            }
        });

        sendBtn.addActionListener(e -> sendMessage());
        msgInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        updateChatHTML();
    }

    private void updateChatHTML() {
        String baseHTML = "<html><head><style>"
                + "body { font-family: -apple-system, Segoe UI, Arial; background-color: #36393f; color: #dcddde; padding: 10px; }"
                + ".msg-row { margin-bottom: 12px; }"
                + ".author { font-weight: bold; color: #00b0f4; font-size: 13px; }"
                + ".time { color: #72767d; font-size: 10px; margin-left: 6px; }"
                + ".content { font-size: 13px; margin-top: 3px; color: #ffffff; }"
                + ".system { color: #f6c445; font-style: italic; font-size: 12px; }"
                + "</style></head><body>"
                + chatHistory.toString()
                + "</body></html>";

        chatPane.setText(baseHTML);
        chatPane.setCaretPosition(chatPane.getDocument().getLength());
    }

    private void addChatMessage(String user, String text) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        chatHistory.append("<div class='msg-row'>")
                .append("<span class='author'>").append(user).append("</span>")
                .append("<span class='time'>").append(time).append("</span>")
                .append("<div class='content'>").append(text).append("</div>")
                .append("</div>");
        updateChatHTML();
    }

    private void addSystemMessage(String text) {
        chatHistory.append("<div class='msg-row system'>• ").append(text).append("</div>");
        updateChatHTML();
    }

    private void connect() {
        String host = hostInput.getText().trim();
        String user = usernameInput.getText().trim();
        int port;

        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir kullanıcı adı girin!");
            return;
        }

        try {
            port = Integer.parseInt(portInput.getText().trim());
            socket = new Socket(host, port);
            dis = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            dout.writeUTF(user);
            dout.flush();

            connected = true;
            connectBtn.setText("Ayrıl");
            connectBtn.setBackground(new Color(240, 71, 71));
            setInputsLocked(true);
            addSystemMessage("Sohbete bağlandınız.");

            new Thread(() -> {
                while (connected) {
                    try {
                        String raw = dis.readUTF();
                        if (raw.startsWith("SYS:")) {
                            addSystemMessage(raw.substring(4));
                        } else if (raw.startsWith("MSG:")) {
                            String[] parts = raw.split(":", 3);
                            addChatMessage(parts[1], parts[2]);
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
                disconnect();
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Bağlanılamadı: " + ex.getMessage());
        }
    }

    private void sendMessage() {
        String text = msgInput.getText().trim();
        if (text.isEmpty() || !connected) return;

        try {
            dout.writeUTF(text);
            dout.flush();
            msgInput.setText("");
        } catch (IOException ex) {
            addSystemMessage("Mesaj iletilemedi.");
        }
    }

    private void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}

        SwingUtilities.invokeLater(() -> {
            connectBtn.setText("Bağlan");
            connectBtn.setBackground(COLOR_ACCENT);
            setInputsLocked(false);
            addSystemMessage("Bağlantı kesildi.");
        });
    }

    private void setInputsLocked(boolean locked) {
        hostInput.setEnabled(!locked);
        portInput.setEnabled(!locked);
        usernameInput.setEnabled(!locked);
        msgInput.setEnabled(locked);
        sendBtn.setEnabled(locked);
        if (locked) msgInput.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModernClient().setVisible(true));
    }
}