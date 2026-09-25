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
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ModernServer extends JFrame {

    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    private JTextArea logConsole;
    private JTextField portInput;
    private JButton toggleBtn;
    private JLabel statusPill;
    private DefaultListModel<String> userListModel;

    // Modern Koyu Arayüz Renkleri
    public static final Color COLOR_BG = new Color(24, 25, 28);
    public static final Color COLOR_SIDEBAR = new Color(32, 34, 37);
    public static final Color COLOR_CARD = new Color(47, 49, 54);
    public static final Color COLOR_TEXT = new Color(220, 221, 222);
    public static final Color COLOR_ACCENT = new Color(88, 101, 242);
    public static final Color COLOR_ONLINE = new Color(67, 181, 129);
    public static final Color COLOR_OFFLINE = new Color(240, 71, 71);

    public ModernServer() {
        setupUI();
    }

    private void setupUI() {
        setTitle("Modern Server Hub");
        setSize(780, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout(12, 12));

        // Üst Kontrol Paneli
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_SIDEBAR);
        topBar.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftControl.setOpaque(false);

        JLabel title = new JLabel("SERVER DASHBOARD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(COLOR_TEXT);
        portInput = createStyledTextField("1201", 6);

        toggleBtn = createStyledButton("Sunucuyu Başlat", COLOR_ACCENT);

        leftControl.add(title);
        leftControl.add(Box.createHorizontalStrut(15));
        leftControl.add(portLabel);
        leftControl.add(portInput);
        leftControl.add(toggleBtn);

        statusPill = new JLabel("● Çevrimdışı");
        statusPill.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusPill.setForeground(COLOR_OFFLINE);

        topBar.add(leftControl, BorderLayout.WEST);
        topBar.add(statusPill, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Sol Sidebar: Aktif Kullanıcı Listesi
        JPanel sidePanel = new JPanel(new BorderLayout(8, 8));
        sidePanel.setBackground(COLOR_SIDEBAR);
        sidePanel.setPreferredSize(new Dimension(210, 0));
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel userHeader = new JLabel("BAĞLI KULLANICILAR");
        userHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userHeader.setForeground(new Color(142, 146, 151));

        userListModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(userListModel);
        userList.setBackground(COLOR_CARD);
        userList.setForeground(COLOR_TEXT);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userList.setBorder(new EmptyBorder(6, 6, 6, 6));

        sidePanel.add(userHeader, BorderLayout.NORTH);
        sidePanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        add(sidePanel, BorderLayout.WEST);

        // Orta: Sistem Logları
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 0, 10, 10));

        logConsole = new JTextArea();
        logConsole.setEditable(false);
        logConsole.setBackground(COLOR_CARD);
        logConsole.setForeground(new Color(185, 187, 190));
        logConsole.setFont(new Font("Consolas", Font.PLAIN, 12));
        logConsole.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane logScroll = new JScrollPane(logConsole);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(logScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        toggleBtn.addActionListener(e -> {
            if (!isRunning) {
                startServer();
            } else {
                stopServer();
            }
        });
    }

    private void log(String msg) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SwingUtilities.invokeLater(() -> {
            logConsole.append(String.format("[%s] %s\n", time, msg));
            logConsole.setCaretPosition(logConsole.getDocument().getLength());
        });
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(portInput.getText().trim());
            serverSocket = new ServerSocket(port);
            isRunning = true;

            toggleBtn.setText("Sunucuyu Kapat");
            toggleBtn.setBackground(COLOR_OFFLINE);
            portInput.setEnabled(false);
            statusPill.setText("● Çevrimiçi (" + port + ")");
            statusPill.setForeground(COLOR_ONLINE);

            log("Sunucu başlatıldı. Port: " + port);

            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(socket);
                        clients.add(handler);
                        new Thread(handler).start();
                    } catch (IOException ex) {
                        break;
                    }
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
        }
    }

    private void stopServer() {
        isRunning = false;
        try {
            for (ClientHandler ch : new ArrayList<>(clients)) {
                ch.close();
            }
            clients.clear();
            userListModel.clear();
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        toggleBtn.setText("Sunucuyu Başlat");
        toggleBtn.setBackground(COLOR_ACCENT);
        portInput.setEnabled(true);
        statusPill.setText("● Çevrimdışı");
        statusPill.setForeground(COLOR_OFFLINE);
        log("Sunucu durduruldu.");
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.send(message);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private DataInputStream dis;
        private DataOutputStream dout;
        private String username;

        public ClientHandler(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            try {
                dis = new DataInputStream(socket.getInputStream());
                dout = new DataOutputStream(socket.getOutputStream());

                username = dis.readUTF();
                SwingUtilities.invokeLater(() -> userListModel.addElement(username));
                log(username + " (" + socket.getInetAddress().getHostAddress() + ") bağlandı.");
                broadcast("SYS:" + username + " odaya katıldı!");

                while (isRunning) {
                    String msg = dis.readUTF();
                    log(username + ": " + msg);
                    broadcast("MSG:" + username + ":" + msg);
                }
            } catch (IOException e) {
                // bağlantı kesildi
            } finally {
                close();
            }
        }

        public void send(String msg) {
            try {
                if (dout != null) {
                    dout.writeUTF(msg);
                    dout.flush();
                }
            } catch (IOException ignored) {}
        }

        public void close() {
            try {
                clients.remove(this);
                SwingUtilities.invokeLater(() -> userListModel.removeElement(username));
                broadcast("SYS:" + username + " ayrıldı.");
                log(username + " ayrıldı.");
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        }
    }

    public static JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField createStyledTextField(String text, int cols) {
        JTextField field = new JTextField(text, cols);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(47, 49, 54));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 63, 65)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return field;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModernServer().setVisible(true));
    }
}