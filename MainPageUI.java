package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainPageUI extends JPanel {

    private static final Color COLOR_BG = new Color(200, 220, 225);
    private static final Color COLOR_LEFT = new Color(172, 198, 201);
    private static final Color COLOR_BUTTON = new Color(162, 193, 196);
    private static final Color COLOR_TEXT = new Color(60, 60, 60);
    private static final Color COLOR_PANEL = new Color(240, 240, 240);
    private static final Color COLOR_BORDER = new Color(130, 150, 150);

    // UI components
    private JLabel dateTimeLabel;
    private JLabel notificationLabel;
    private JButton attendButton;
    private JTable weekTable;
    private DefaultTableModel weekTableModel;
    private JButton checkStatusBtn;
    private JButton editScheduleBtn;

    public MainPageUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        buildUI();
    }

    private void buildUI() {
        // Left rail
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(300, 560));
        left.setBackground(COLOR_LEFT);
        left.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel logo = new JLabel("Logo", SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(200, 200));
        logo.setMaximumSize(new Dimension(200, 200));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        JLabel appName = new JLabel("Attendance Tracker");
        appName.setFont(new Font("SansSerif", Font.BOLD, 22));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        appName.setBorder(BorderFactory.createEmptyBorder(16, 0, 24, 0));

        checkStatusBtn = createPillButton("Check Status");
        editScheduleBtn = createPillButton("Edit Schedule");
        checkStatusBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        editScheduleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(logo);
        left.add(appName);
        left.add(Box.createVerticalStrut(8));
        left.add(checkStatusBtn);
        left.add(Box.createVerticalStrut(10));
        left.add(editScheduleBtn);
        left.add(Box.createVerticalGlue());

        // Right content
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);

        // Top bar with date/time
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        topBar.setOpaque(false);
        dateTimeLabel = new JLabel("Date + Time");
        dateTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        topBar.add(dateTimeLabel);

        // Center: canvas
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        center.add(createCanvasPanel(), BorderLayout.CENTER);

        // Bottom: notification pane
        JPanel notificationPane = createNotificationPane();

        right.add(topBar, BorderLayout.NORTH);
        right.add(center, BorderLayout.CENTER);
        right.add(notificationPane, BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
        startDateTimeUpdater();
    }

    private JComponent createCanvasPanel() {
        JPanel mainCanvasPanel = new JPanel(new BorderLayout());
        mainCanvasPanel.setPreferredSize(new Dimension(660, 360));
        mainCanvasPanel.setBackground(COLOR_PANEL);
        mainCanvasPanel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        String[] cols = {"", "Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        weekTableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        weekTable = new JTable(weekTableModel);
        weekTable.setRowHeight(72);
        weekTable.setIntercellSpacing(new Dimension(0, 10));
        weekTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        javax.swing.table.JTableHeader hdr = weekTable.getTableHeader();
        hdr.setBackground(COLOR_BUTTON);
        hdr.setForeground(COLOR_TEXT);
        mainCanvasPanel.add(new JScrollPane(weekTable), BorderLayout.CENTER);

        // placeholder
        weekTableModel.addRow(new Object[]{"<html><b>Sample Course</b><br/><i>Lecture</i></html>", "", "", "", "", "", ""});

        return mainCanvasPanel;
    }

    private JPanel createNotificationPane() {
        JPanel pane = new JPanel(new BorderLayout());
        pane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_PANEL);
        inner.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        inner.setPreferredSize(new Dimension(660, 80));
        notificationLabel = new JLabel("Notification Pane", SwingConstants.CENTER);
        attendButton = createPillButton("Mark Attended");
        attendButton.setEnabled(false);
        inner.add(notificationLabel, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(attendButton);
        inner.add(south, BorderLayout.SOUTH);
        pane.add(inner, BorderLayout.CENTER);
        return pane;
    }

    private static JButton createPillButton(String text) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BUTTON);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setForeground(COLOR_TEXT);
        return b;
    }

    private void startDateTimeUpdater() {
        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy   hh:mm:ss a");
            dateTimeLabel.setText(sdf.format(new Date()));
        });
        t.start();
    }

    // Getters for wiring
    public JButton getCheckStatusBtn() { return checkStatusBtn; }
    public JButton getEditScheduleBtn() { return editScheduleBtn; }
    public JButton getAttendButton() { return attendButton; }
    public JLabel getNotificationLabel() { return notificationLabel; }
    public JTable getWeekTable() { return weekTable; }
    public DefaultTableModel getWeekTableModel() { return weekTableModel; }
    public JLabel getDateTimeLabel() { return dateTimeLabel; }
}
