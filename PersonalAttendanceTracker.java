package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.*;
import java.util.*;

public class PersonalAttendanceTracker extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel dateTimeLabel;
    private JLabel notificationLabel;
    private JButton attendButton;
    private Subject notifiedSubject;
    private LocalDate notifiedDate;
    private JPanel mainSubjectListPanel;
    private JPanel mainCanvasPanel;
    private JTable weekTable;
    private DefaultTableModel weekTableModel;
    private DayOfWeek selectedDay = LocalDate.now().getDayOfWeek();
    private java.util.Map<DayOfWeek, JButton> dayButtons = new java.util.EnumMap<>(DayOfWeek.class);

    // Theme colors from mockup
    private static final Color COLOR_BG = new Color(200, 220, 225);        // page bg
    private static final Color COLOR_LEFT = new Color(172, 198, 201);      // left rail
    private static final Color COLOR_BUTTON = new Color(162, 193, 196);    // rounded buttons
    private static final Color COLOR_TEXT = new Color(60, 60, 60);
    private static final Color COLOR_PANEL = new Color(240, 240, 240);
    private static final Color COLOR_BORDER = new Color(130, 150, 150);
    private DefaultTableModel classTableModel;
    private JTable table;
    // Dialogs
    private ScheduleEditorDialog scheduleEditorDialog;
    // Subjects data model (attendance tracking per subject)
    private java.util.List<Subject> subjects = new java.util.ArrayList<>();

    public PersonalAttendanceTracker() {
        setTitle("Personal Attendance Tracker");
        setSize(980, 560);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createMainPage(), "Main");
        mainPanel.add(createClassesPage(), "Classes");
        mainPanel.add(createEditSchedulePage(), "EditSchedule");

        add(mainPanel);
        cardLayout.show(mainPanel, "Main");

        startDateTimeUpdater(); // real-time clock
    }

    // ===================== MAIN PAGE =====================
    private JPanel createMainPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);

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

        JLabel appName = new JLabel("Insert Name ng App");
        appName.setFont(new Font("SansSerif", Font.BOLD, 22));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        appName.setBorder(BorderFactory.createEmptyBorder(16, 0, 24, 0));

        JButton checkStatusBtn = createPillButton("Check Status");
        JButton editScheduleBtn = createPillButton("Edit Schedule");
        checkStatusBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        editScheduleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkStatusBtn.addActionListener(e -> new StatusDialog(this, subjects).setVisible(true));
        editScheduleBtn.addActionListener(e -> {
            if (scheduleEditorDialog == null) {
                scheduleEditorDialog = new ScheduleEditorDialog(this);
            }
            scheduleEditorDialog.setLocationRelativeTo(this);
            scheduleEditorDialog.setVisible(true);
        });

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

        // Center: canvas only (removed subject list panel)
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        // Removed floating subject list panel per request
        // center.add(createSubjectListPanel(), BorderLayout.WEST);
        center.add(createCanvasPanel(), BorderLayout.CENTER);

        // Bottom: notification pane
        JPanel notificationPane = createNotificationPane();

        right.add(topBar, BorderLayout.NORTH);
        right.add(center, BorderLayout.CENTER);
        right.add(notificationPane, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDayButtonsPanel() {
        JPanel days = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        days.setOpaque(false);
        DayOfWeek[] order = {DayOfWeek.MONDAY,DayOfWeek.TUESDAY,DayOfWeek.WEDNESDAY,DayOfWeek.THURSDAY,DayOfWeek.FRIDAY,DayOfWeek.SATURDAY};
        for (DayOfWeek d : order) {
            JButton b = createPillButton(capitalize(d.toString().toLowerCase()));
            b.addActionListener(e -> { selectedDay = d; refreshMainUI(); });
            dayButtons.put(d, b);
            days.add(b);
        }
        return days;
    }

    private JComponent createSubjectListPanel() {
        mainSubjectListPanel = new JPanel();
        mainSubjectListPanel.setLayout(new BoxLayout(mainSubjectListPanel, BoxLayout.Y_AXIS));
        mainSubjectListPanel.setOpaque(false);
        mainSubjectListPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        refreshSubjectButtons();
        return mainSubjectListPanel;
    }

    private JComponent createCanvasPanel() {
        mainCanvasPanel = new JPanel(new BorderLayout());
        mainCanvasPanel.setPreferredSize(new Dimension(660, 360));
        mainCanvasPanel.setBackground(COLOR_PANEL);
        mainCanvasPanel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        String[] cols = {"", "Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        weekTableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        weekTable = new JTable(weekTableModel);
        weekTable.setRowHeight(72);
        weekTable.setIntercellSpacing(new Dimension(0, 10));
        weekTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        // Header color styling to blue hue
        javax.swing.table.JTableHeader hdr = weekTable.getTableHeader();
        hdr.setBackground(COLOR_BUTTON);
        hdr.setForeground(COLOR_TEXT);
        mainCanvasPanel.add(new JScrollPane(weekTable), BorderLayout.CENTER);
        refreshWeekGrid();
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
        attendButton.addActionListener(e -> {
            if (notifiedSubject != null) {
                markAttendance(notifiedSubject, LocalDateTime.now());
                clearNotification();
                if (scheduleEditorDialog != null && scheduleEditorDialog.isVisible()) {
                    scheduleEditorDialog.refreshFromSubjects();
                }
            }
        });
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

    private void refreshSubjectButtons() {
        if (mainSubjectListPanel == null) return;
        mainSubjectListPanel.removeAll();
        java.util.List<Subject> todays = new java.util.ArrayList<>();
        for (Subject s : subjects) if (s.days.contains(selectedDay)) todays.add(s);
        if (todays.isEmpty()) {
            JLabel none = new JLabel("No subjects", SwingConstants.LEFT);
            mainSubjectListPanel.add(none);
        } else {
            for (Subject s : todays) {
                JButton btn = createPillButton(s.title);
                btn.setAlignmentX(Component.LEFT_ALIGNMENT);
                btn.setMaximumSize(new Dimension(160, 32));
                mainSubjectListPanel.add(btn);
                mainSubjectListPanel.add(Box.createVerticalStrut(8));
            }
        }
        mainSubjectListPanel.revalidate();
        mainSubjectListPanel.repaint();
    }

    private void refreshWeekGrid() {
        if (weekTableModel == null) return;
        weekTableModel.setRowCount(0);
        java.util.List<Subject> list = new java.util.ArrayList<>(subjects);
        for (Subject s : list) {
            String[] row = new String[7];
            // Show course title and type in left column
            row[0] = "<html><b>" + escape(s.title) + "</b><br/><i>" + escape(s.courseType) + "</i></html>";
            row[1] = s.days.contains(DayOfWeek.MONDAY)    ? subjectCellText(s) : "";
            row[2] = s.days.contains(DayOfWeek.TUESDAY)   ? subjectCellText(s) : "";
            row[3] = s.days.contains(DayOfWeek.WEDNESDAY) ? subjectCellText(s) : "";
            row[4] = s.days.contains(DayOfWeek.THURSDAY)  ? subjectCellText(s) : "";
            row[5] = s.days.contains(DayOfWeek.FRIDAY)    ? subjectCellText(s) : "";
            row[6] = s.days.contains(DayOfWeek.SATURDAY)  ? subjectCellText(s) : "";
            weekTableModel.addRow(row);
        }
        weekTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof String && ((String) value).contains("<html>")) {
                    setText((String) value);
                } else {
                    setText(value == null ? "" : value.toString());
                }
            }
        });
    }

    private String subjectCellText(Subject s) {
        String start = String.format("%02d:%02d", s.startTime.getHour(), s.startTime.getMinute());
        String end = String.format("%02d:%02d", s.endTime.getHour(), s.endTime.getMinute());
        String timeRange = start + " - " + end;
        // Only show time range, room, instructor
        return "<html><center>" + escape(timeRange) + "<br/>" + escape(s.room) + "<br/>" + escape(s.instructor) + "</center></html>";
    }

    private String escape(String v) { return v == null ? "" : v.replace("<","&lt;").replace(">","&gt;"); }

    private void refreshMainUI() {
        refreshSubjectButtons();
        refreshWeekGrid();
    }

    private static String capitalize(String s) { return s.substring(0,1).toUpperCase() + s.substring(1); }

    // ===================== CLASSES PAGE =====================
    private JPanel createClassesPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // Header with Back and Edit buttons
        JPanel header = new JPanel(new BorderLayout());

        // Left side: Back to Main
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton backButton = new JButton("Back to Main");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Main"));
        leftHeader.add(backButton);

        // Center: Title
        JLabel title = new JLabel("Insert Name ng App");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Right side: Edit Schedule
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton editSchedule = new JButton("Edit Schedule");
        editSchedule.addActionListener(e -> {
            if (scheduleEditorDialog == null) {
                scheduleEditorDialog = new ScheduleEditorDialog(this);
            }
            scheduleEditorDialog.setLocationRelativeTo(this);
            scheduleEditorDialog.setVisible(true);
        });
        rightHeader.add(editSchedule);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        header.add(rightHeader, BorderLayout.EAST);

        String[] cols = {"Date & Time ng Class Sched", "Status", "Log In", "Attendance"};
        classTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(classTableModel);
        table.setRowHeight(24);

        classTableModel.addRow(new Object[]{"10:00 a.m. August 26, 2025", "Done", "-", "Not Yet Logged"});
        classTableModel.addRow(new Object[]{"Upcoming", "Upcoming", "-", "Not Yet Logged"});
        classTableModel.addRow(new Object[]{"Ongoing", "Ongoing", "-", "Not Yet Logged"});

        JScrollPane scroll = new JScrollPane(table);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton addBtn = new JButton("Add");
        JButton logBtn = new JButton("Log");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton statsBtn = new JButton("Statistics");

        addBtn.addActionListener(e -> {
            String dt = JOptionPane.showInputDialog(this,
                    "Enter Date & Time (e.g., 10:00 a.m. August 30, 2025):",
                    "New Class Entry", JOptionPane.PLAIN_MESSAGE);
            if (dt != null && !dt.trim().isEmpty()) {
                classTableModel.addRow(new Object[]{dt.trim(), "Upcoming", "-", "Not Yet Logged"});
                checkAbsencePercent();
            }
        });

        logBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a row to log.");
                return;
            }
            String time = JOptionPane.showInputDialog(this,
                    "Enter Log In Time (e.g., 10:05 AM):",
                    classTableModel.getValueAt(row, 2));
            if (time == null || time.trim().isEmpty()) return;

            classTableModel.setValueAt(time.trim(), row, 2);
            String status = evaluateAttendance(time.trim());
            classTableModel.setValueAt(status, row, 3);
            convertThreeLatesIfNeeded();
            checkAbsencePercent();
        });

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a row to update.");
                return;
            }
            String newDate = JOptionPane.showInputDialog(this,
                    "Update Date & Time:",
                    classTableModel.getValueAt(row, 0));
            if (newDate != null && !newDate.trim().isEmpty()) {
                classTableModel.setValueAt(newDate.trim(), row, 0);
            }
            String newLog = JOptionPane.showInputDialog(this,
                    "Update Log In (hh:mm AM/PM), or leave blank to keep:",
                    classTableModel.getValueAt(row, 2));
            if (newLog != null && !newLog.trim().isEmpty()) {
                classTableModel.setValueAt(newLog.trim(), row, 2);
                String status = evaluateAttendance(newLog.trim());
                classTableModel.setValueAt(status, row, 3);
                convertThreeLatesIfNeeded();
                checkAbsencePercent();
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.");
                return;
            }
            classTableModel.removeRow(row);
            checkAbsencePercent();
        });

        statsBtn.addActionListener(e -> {
            int total = classTableModel.getRowCount();
            int absents = countAbsents();
            int lates = countLates(false);
            double rate = total == 0 ? 0 : (absents * 100.0 / total);
            JOptionPane.showMessageDialog(this,
                    String.format("Total Entries: %d%nLates: %d%nAbsents: %d%nAbsence Rate: %.2f%%",
                            total, lates, absents, rate),
                    "Statistics", JOptionPane.INFORMATION_MESSAGE);
        });

        buttons.add(addBtn);
        buttons.add(logBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(statsBtn);

        JPanel framed = new JPanel(new BorderLayout());
        framed.setBackground(Color.WHITE);
        framed.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel courseHeader = new JPanel(new BorderLayout());
        courseHeader.setOpaque(false);
        JLabel courseTitle = new JLabel("Course Title");
        courseTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton statusChip = new JButton("Overall Status (Reg/Dropped)");
        statusChip.setEnabled(false);
        courseHeader.add(courseTitle, BorderLayout.WEST);
        courseHeader.add(statusChip, BorderLayout.EAST);

        framed.add(courseHeader, BorderLayout.NORTH);
        framed.add(scroll, BorderLayout.CENTER);
        framed.add(buttons, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        panel.add(framed, BorderLayout.CENTER);
        return panel;
    }

    // ===================== EDIT SCHEDULE PAGE =====================
    private JPanel createEditSchedulePage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Main");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Main"));
        top.add(backButton);

        JButton showSchedule = new JButton("Show Schedule");
        showSchedule.addActionListener(e -> cardLayout.show(mainPanel, "Classes"));
        top.add(showSchedule);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField title = new JTextField("Object Oriented Programming");
        JTextField code  = new JTextField("COMSCI 2110");
        JTextField time1 = new JTextField("10:00 AM");
        JTextField room  = new JTextField("ComLab 214");
        JTextField instr = new JTextField("Ivan Salinas");

        gc.gridx = 0; gc.gridy = 0; form.add(new JLabel("Title"), gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1; form.add(title, gc);
        gc.gridx = 2; gc.gridy = 0; gc.weightx = 0; form.add(new JLabel("Time"), gc);
        gc.gridx = 3; gc.gridy = 0; gc.weightx = 1; form.add(time1, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; form.add(new JLabel("Code"), gc);
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1; form.add(code, gc);
        gc.gridx = 2; gc.gridy = 1; gc.weightx = 0; form.add(new JLabel("Repeat Weekly"), gc);
        JPanel days = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        String[] dayNames = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        JCheckBox[] dayChecks = new JCheckBox[dayNames.length];
        for (int i = 0; i < dayNames.length; i++) {
            dayChecks[i] = new JCheckBox(dayNames[i]);
            days.add(dayChecks[i]);
        }
        gc.gridx = 3; gc.gridy = 1; gc.weightx = 1; form.add(days, gc);

        gc.gridx = 0; gc.gridy = 2; form.add(new JLabel("Room"), gc);
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1; form.add(room, gc);
        gc.gridx = 2; gc.gridy = 2; gc.weightx = 0; form.add(new JLabel("Instructor"), gc);
        gc.gridx = 3; gc.gridy = 2; gc.weightx = 1; form.add(instr, gc);

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        formButtons.add(add); formButtons.add(update); formButtons.add(delete);

        add.addActionListener(e -> JOptionPane.showMessageDialog(this, "Added schedule: " + code.getText()));
        update.addActionListener(e -> JOptionPane.showMessageDialog(this, "Updated schedule: " + code.getText()));
        delete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Deleted schedule: " + code.getText()));

        JPanel framed = new JPanel(new BorderLayout());
        framed.setBackground(Color.WHITE);
        framed.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        framed.add(form, BorderLayout.CENTER);
        framed.add(formButtons, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(framed, BorderLayout.CENTER);
        return panel;
    }

    // ===================== ATTENDANCE LOGIC =====================
    private String evaluateAttendance(String logTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date classStart = sdf.parse("10:00 AM");
            Date log = sdf.parse(logTime);
            long diffMs = log.getTime() - classStart.getTime();
            if (diffMs <= 5 * 60 * 1000L) {
                return "On-Time";
            } else if (diffMs <= 15 * 60 * 1000L) {
                return "Late";
            } else {
                return "Absent";
            }
        } catch (Exception ex) {
            return "Invalid Time";
        }
    }

    private void convertThreeLatesIfNeeded() {
        java.util.List<Integer> lateIdx = new java.util.ArrayList<>();
        for (int r = 0; r < classTableModel.getRowCount(); r++) {
            Object v = classTableModel.getValueAt(r, 3);
            if (v != null && "Late".equals(v.toString())) {
                lateIdx.add(r);
            }
        }
        int conversions = 0;
        while (lateIdx.size() >= 3) {
            int i0 = lateIdx.remove(0);
            int i1 = lateIdx.remove(0);
            int i2 = lateIdx.remove(0);
            classTableModel.setValueAt("Absent (3 Lates)", i0, 3);
            classTableModel.setValueAt("Late (counted)", i1, 3);
            classTableModel.setValueAt("Late (counted)", i2, 3);
            conversions++;
        }
        if (conversions > 0) {
            JOptionPane.showMessageDialog(this,
                    conversions + " absent(s) created from groups of three lates.",
                    "Late Conversion", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private int countAbsents() {
        int c = 0;
        for (int r = 0; r < classTableModel.getRowCount(); r++) {
            Object v = classTableModel.getValueAt(r, 3);
            if (v != null && v.toString().startsWith("Absent")) c++;
        }
        return c;
    }

    private int countLates(boolean onlyPlainLate) {
        int c = 0;
        for (int r = 0; r < classTableModel.getRowCount(); r++) {
            Object v = classTableModel.getValueAt(r, 3);
            if (v == null) continue;
            String s = v.toString();
            if (onlyPlainLate) {
                if ("Late".equals(s)) c++;
            } else {
                if (s.startsWith("Late")) c++;
            }
        }
        return c;
    }

    private void checkAbsencePercent() {
        int total = classTableModel.getRowCount();
        if (total == 0) return;
        int absents = countAbsents();
        double rate = absents * 100.0 / total;
        if (rate >= 20.0) {
            JOptionPane.showMessageDialog(this,
                    String.format("Warning: Absences are now %.2f%% (â‰¥ 20%%). Student is unofficially dropped.",
                            rate),
                    "20% Rule Triggered", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ===================== REAL-TIME CLOCK =====================
    private void startDateTimeUpdater() {
        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy   hh:mm:ss a");
            dateTimeLabel.setText(sdf.format(new Date()));
            checkUpcomingNotifications();
            autoMarkAbsents();
        });
        t.start();
    }

    // ===================== MAIN =====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PersonalAttendanceTracker().setVisible(true));
    }

    // ===================== DIALOGS =====================
    static class ScheduleEditorDialog extends JDialog {
        private final PersonalAttendanceTracker parent;
        private JTable subjectTable;
        private DefaultTableModel subjectModel;

        ScheduleEditorDialog(PersonalAttendanceTracker owner) {
            super(owner, "Schedule Editor", true);
            this.parent = owner;
            setSize(900, 560);
            setLayout(new BorderLayout());

            JPanel header = new JPanel(new BorderLayout());
            JLabel title = new JLabel("Schedule Editor");
            title.setFont(new Font("SansSerif", Font.BOLD, 22));
            header.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
            header.add(title, BorderLayout.WEST);
            add(header, BorderLayout.NORTH);

            // Left action column
            JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            actionBar.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            JButton addBtn = PersonalAttendanceTracker.createPillButton("Add Subject");
            JButton deleteBtn = PersonalAttendanceTracker.createPillButton("Delete Subject");
            JButton editBtn = PersonalAttendanceTracker.createPillButton("Edit Subject");
            JButton newSchedBtn = PersonalAttendanceTracker.createPillButton("New Schedule");
            actionBar.add(addBtn);
            actionBar.add(deleteBtn);
            actionBar.add(editBtn);
            actionBar.add(newSchedBtn);

            // Center area will host the schedule table directly under the buttons
            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.setBorder(BorderFactory.createEmptyBorder(4, 16, 8, 16));

            add(actionBar, BorderLayout.NORTH);
            add(center, BorderLayout.CENTER);

            // Bottom table to show saved subjects (simple model)
            String[] cols = {"Course Title","Days","Start","End","Type","Instructor","Room"};
            subjectModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
            subjectTable = new JTable(subjectModel);
            subjectTable.setRowHeight(40);
            subjectTable.setIntercellSpacing(new Dimension(0, 8));
            subjectTable.setFillsViewportHeight(true);
            JScrollPane subjectScroll = new JScrollPane(subjectTable);
            subjectScroll.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            subjectScroll.setPreferredSize(new Dimension(600, 220));
            center.add(subjectScroll, BorderLayout.NORTH);

            // Add Subject dialog
            addBtn.addActionListener(e -> new AddSubjectDialog(this, null, -1).setVisible(true));
            deleteBtn.addActionListener(e -> {
                if (parent.subjects.isEmpty()) { JOptionPane.showMessageDialog(this, "No subjects to delete."); return; }
                String[] titles = parent.subjects.stream().map(s -> s.title).toArray(String[]::new);
                String chosen = (String) JOptionPane.showInputDialog(this, "Select subject to delete:", "Delete Subject",
                        JOptionPane.PLAIN_MESSAGE, null, titles, titles[0]);
                if (chosen == null) return;
                int confirm = JOptionPane.showConfirmDialog(this, "Delete '"+chosen+"'?", "Confirm Delete", JOptionPane.OK_CANCEL_OPTION);
                if (confirm != JOptionPane.OK_OPTION) return;
                parent.subjects.removeIf(s -> s.title.equals(chosen));
                // remove from table
                for (int r = 0; r < subjectModel.getRowCount(); r++) {
                    if (chosen.equals(subjectModel.getValueAt(r, 0))) { subjectModel.removeRow(r); break; }
                }
                parent.refreshMainUI();
            });
            editBtn.addActionListener(e -> {
                if (parent.subjects.isEmpty()) { JOptionPane.showMessageDialog(this, "No subjects to edit."); return; }
                String[] titles = parent.subjects.stream().map(s -> s.title).toArray(String[]::new);
                String chosen = (String) JOptionPane.showInputDialog(this, "Select subject to edit:", "Edit Subject",
                        JOptionPane.PLAIN_MESSAGE, null, titles, titles[0]);
                if (chosen == null) return;
                // find row and subject
                int rowIdx = -1; Subject subj = null;
                for (int r = 0; r < subjectModel.getRowCount(); r++) {
                    if (chosen.equals(subjectModel.getValueAt(r, 0))) { rowIdx = r; break; }
                }
                subj = parent.findSubjectByTitle(chosen);
                if (subj != null && rowIdx >= 0) new AddSubjectDialog(this, subj, rowIdx).setVisible(true);
            });
            newSchedBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete ALL subjects?", "New Schedule",
                        JOptionPane.OK_CANCEL_OPTION);
                if (confirm != JOptionPane.OK_OPTION) return;
                subjectModel.setRowCount(0);
                parent.subjects.clear();
                parent.refreshMainUI();
            });
        }

        void addSubjectRow(Subject s, Object[] displayRow) {
            subjectModel.addRow(displayRow);
            parent.refreshMainUI();
        }

        void refreshFromSubjects() {
            // no derived columns now; nothing to sync here
        }
    }

    static class AddSubjectDialog extends JDialog {
        private JTextField courseTitleField;
        private JCheckBox[] dayChecks;
        private JTextField startHour, startMin, endHour, endMin;
        private JComboBox<String> startAmPm, endAmPm;
        private JTextField courseTypeField, instructorField, roomField;

        AddSubjectDialog(ScheduleEditorDialog owner, Subject existing, int editingRow) {
            super(owner, "Add Subject", true);
            setSize(520, 520);
            setLayout(new BorderLayout());

            JPanel root = new JPanel();
            root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
            root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

            JLabel header = new JLabel("Add Subject");
            header.setFont(new Font("SansSerif", Font.BOLD, 22));
            header.setAlignmentX(Component.LEFT_ALIGNMENT);
            root.add(header);
            root.add(Box.createVerticalStrut(12));

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6,6,6,6);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1;

            courseTitleField = new JTextField();
            gc.gridx=0; gc.gridy=0; form.add(new JLabel("Course Title"), gc);
            gc.gridx=1; gc.gridy=0; form.add(courseTitleField, gc);

            // Schedule block
            JPanel schedPanel = new JPanel(new GridBagLayout());
            schedPanel.setBorder(BorderFactory.createTitledBorder("Schedule"));
            GridBagConstraints sgc = new GridBagConstraints();
            sgc.insets = new Insets(4,4,4,4);
            sgc.anchor = GridBagConstraints.WEST;

            String[] dn = {"Mon","Tue","Wed","Thu","Fri","Sat"};
            dayChecks = new JCheckBox[dn.length];
            JPanel dayRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            for (int i=0;i<dn.length;i++){ dayChecks[i] = new JCheckBox(dn[i]); dayRow.add(dayChecks[i]); }
            sgc.gridx=0; sgc.gridy=0; sgc.gridwidth=6; schedPanel.add(dayRow, sgc);

            startHour = new JTextField(2); startMin = new JTextField(2);
            endHour = new JTextField(2); endMin = new JTextField(2);
            startAmPm = new JComboBox<>(new String[]{"AM","PM"});
            endAmPm = new JComboBox<>(new String[]{"AM","PM"});

            JPanel timeRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            timeRow1.add(new JLabel("Starting Time"));
            timeRow1.add(startHour); timeRow1.add(new JLabel(":")); timeRow1.add(startMin); timeRow1.add(startAmPm);
            JPanel timeRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            timeRow2.add(new JLabel("End Time"));
            timeRow2.add(endHour); timeRow2.add(new JLabel(":")); timeRow2.add(endMin); timeRow2.add(endAmPm);
            sgc.gridy=1; sgc.gridwidth=6; schedPanel.add(timeRow1, sgc);
            sgc.gridy=2; schedPanel.add(timeRow2, sgc);

            courseTypeField = new JTextField();
            instructorField = new JTextField();
            roomField = new JTextField();
            JPanel meta = new JPanel(new GridBagLayout());
            GridBagConstraints mgc = new GridBagConstraints();
            mgc.insets = new Insets(4,4,4,4); mgc.fill = GridBagConstraints.HORIZONTAL; mgc.weightx=1;
            mgc.gridx=0; mgc.gridy=0; meta.add(new JLabel("Course Type"), mgc);
            mgc.gridx=1; mgc.gridy=0; meta.add(courseTypeField, mgc);
            mgc.gridx=0; mgc.gridy=1; meta.add(new JLabel("Instructor"), mgc);
            mgc.gridx=1; mgc.gridy=1; meta.add(instructorField, mgc);
            mgc.gridx=0; mgc.gridy=2; meta.add(new JLabel("Room"), mgc);
            mgc.gridx=1; mgc.gridy=2; meta.add(roomField, mgc);

            gc.gridx=0; gc.gridy=1; gc.gridwidth=2; form.add(schedPanel, gc);
            gc.gridx=0; gc.gridy=2; form.add(meta, gc);

            root.add(form);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton save = new JButton("Save");
            JButton cancel = new JButton("Cancel");
            actions.add(save); actions.add(cancel);
            root.add(Box.createVerticalStrut(12));
            root.add(actions);

            add(new JScrollPane(root), BorderLayout.CENTER);

            // Prefill when editing
            if (existing != null) {
                courseTitleField.setText(existing.title);
                for (JCheckBox cb : dayChecks) {
                    switch (cb.getText()) {
                        case "Mon": cb.setSelected(existing.days.contains(DayOfWeek.MONDAY)); break;
                        case "Tue": cb.setSelected(existing.days.contains(DayOfWeek.TUESDAY)); break;
                        case "Wed": cb.setSelected(existing.days.contains(DayOfWeek.WEDNESDAY)); break;
                        case "Thu": cb.setSelected(existing.days.contains(DayOfWeek.THURSDAY)); break;
                        case "Fri": cb.setSelected(existing.days.contains(DayOfWeek.FRIDAY)); break;
                        case "Sat": cb.setSelected(existing.days.contains(DayOfWeek.SATURDAY)); break;
                    }
                }
                int sh = existing.startTime.getHour();
                int sm = existing.startTime.getMinute();
                String ap = sh >= 12 ? "PM" : "AM";
                if (sh == 0) sh = 12; else if (sh > 12) sh -= 12;
                startHour.setText(String.valueOf(sh));
                startMin.setText(String.format("%02d", sm));
                startAmPm.setSelectedItem(ap);
                courseTypeField.setText(existing.courseType);
                instructorField.setText(existing.instructor);
                roomField.setText(existing.room);
            }

            save.addActionListener(e -> {
                StringBuilder days = new StringBuilder();
                for (JCheckBox c : dayChecks) if (c.isSelected()) { if (days.length()>0) days.append("/"); days.append(c.getText()); }
                String start = startHour.getText()+":"+startMin.getText()+" "+startAmPm.getSelectedItem();
                String end = endHour.getText()+":"+endMin.getText()+" "+endAmPm.getSelectedItem();
                Subject subj = existing != null ? existing : new Subject(courseTitleField.getText());
                subj.title = courseTitleField.getText();
                // Save start time and days to subject model
                int sh = parseIntSafe(startHour.getText());
                int sm = parseIntSafe(startMin.getText());
                if ("PM".equals(startAmPm.getSelectedItem()) && sh < 12) sh += 12;
                if ("AM".equals(startAmPm.getSelectedItem()) && sh == 12) sh = 0;
                subj.startTime = LocalTime.of(Math.max(0, Math.min(23, sh)), Math.max(0, Math.min(59, sm)));
                // Save end time
                int eh = parseIntSafe(endHour.getText());
                int em = parseIntSafe(endMin.getText());
                if ("PM".equals(endAmPm.getSelectedItem()) && eh < 12) eh += 12;
                if ("AM".equals(endAmPm.getSelectedItem()) && eh == 12) eh = 0;
                subj.endTime = LocalTime.of(Math.max(0, Math.min(23, eh)), Math.max(0, Math.min(59, em)));
                java.util.Set<DayOfWeek> daysSet = new java.util.HashSet<>();
                for (JCheckBox c : dayChecks) if (c.isSelected()) {
                    switch (c.getText()) {
                        case "Mon": daysSet.add(DayOfWeek.MONDAY); break;
                        case "Tue": daysSet.add(DayOfWeek.TUESDAY); break;
                        case "Wed": daysSet.add(DayOfWeek.WEDNESDAY); break;
                        case "Thu": daysSet.add(DayOfWeek.THURSDAY); break;
                        case "Fri": daysSet.add(DayOfWeek.FRIDAY); break;
                        case "Sat": daysSet.add(DayOfWeek.SATURDAY); break;
                    }
                }
                subj.days = daysSet;
                subj.courseType = courseTypeField.getText();
                subj.instructor = instructorField.getText();
                subj.room = roomField.getText();
                ScheduleEditorDialog dialog = (ScheduleEditorDialog)getOwner();
                if (existing == null) {
                    dialog.parent.addSubject(subj);
                    dialog.addSubjectRow(subj, new Object[]{
                            courseTitleField.getText(), days.toString(), start, end,
                            courseTypeField.getText(), instructorField.getText(), roomField.getText()
                    });
                } else {
                    // Update table row
                    int r = editingRow;
                    if (r >= 0) {
                        dialog.subjectModel.setValueAt(subj.title, r, 0);
                        dialog.subjectModel.setValueAt(days.toString(), r, 1);
                        dialog.subjectModel.setValueAt(start, r, 2);
                        dialog.subjectModel.setValueAt(end, r, 3);
                        dialog.subjectModel.setValueAt(subj.courseType, r, 4);
                        dialog.subjectModel.setValueAt(subj.instructor, r, 5);
                        dialog.subjectModel.setValueAt(subj.room, r, 6);
                        dialog.refreshFromSubjects();
                        dialog.parent.refreshMainUI();
                    }
                }
                dispose();
            });
            cancel.addActionListener(e -> dispose());
        }
    }

    // ===================== STATUS DIALOG =====================
    static class StatusDialog extends JDialog {
        StatusDialog(JFrame owner, java.util.List<Subject> subjects) {
            super(owner, "Subject Status", true);
            setSize(600, 400);
            setLayout(new BorderLayout());
            String[] cols = {"Subject","Lates","Absents","Dropped?"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
            for (Subject s : subjects) {
                model.addRow(new Object[]{s.title, s.lateCount, s.absentCount, s.dropped ? "Yes" : "No"});
            }
            add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
            JButton ok = new JButton("Close");
            ok.addActionListener(e -> dispose());
            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            south.add(ok);
            add(south, BorderLayout.SOUTH);
        }
    }

    // ===================== SUBJECT MODEL =====================
    static class Subject {
        String title;
        int lateCount;
        int absentCount;
        boolean dropped;
        java.util.Set<DayOfWeek> days = new java.util.HashSet<>();
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0); // <-- Add this line
        // Track last attendance date to avoid duplicate marks per day
        LocalDate lastAttendedDate;
        String courseType = "";
        String instructor = "";
        String room = "";

        Subject(String title) { this.title = title; }

        void addLate() { lateCount++; normalize(); }
        void addAbsent() { absentCount++; normalize(); }

        private void normalize() {
            while (lateCount >= 3) { lateCount -= 3; absentCount += 1; }
            if (absentCount >= 3) { dropped = true; }
        }
    }

    // Helpers for subjects management
    private void addSubject(Subject s) { subjects.add(s); }
    private Subject findSubjectByTitle(String title) {
        for (Subject s : subjects) if (s.title.equals(title)) return s;
        return null;
    }
    private static int parseIntSafe(String s) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; } }

    // ===================== NOTIFICATIONS AND AUTO-ATTENDANCE =====================
    private void checkUpcomingNotifications() {
        LocalDateTime now = LocalDateTime.now();
        for (Subject s : subjects) {
            if (s.dropped) continue;
            if (!s.days.contains(now.getDayOfWeek())) continue;
            LocalDateTime classStart = LocalDateTime.of(now.toLocalDate(), s.startTime);
            long minutesUntil = Duration.between(now, classStart).toMinutes();
            long minutesSinceStart = Duration.between(classStart, now).toMinutes();
            // Show notification from 10 minutes before until 15 minutes after start
            if (minutesUntil <= 10 && minutesUntil >= -15 &&
                (s.lastAttendedDate == null || !s.lastAttendedDate.equals(now.toLocalDate()))) {
                notifiedSubject = s;
                notifiedDate = now.toLocalDate();
                String statusText;
                if (minutesUntil >= 0) {
                    statusText = "in " + minutesUntil + " min";
                } else {
                    statusText = Math.abs(minutesUntil) + " min ago";
                }
                notificationLabel.setText("Upcoming: " + s.title + " at " + s.startTime + " (" + statusText + ")" +
                        (s.room.isEmpty() ? "" : "  • Room: " + s.room));
                attendButton.setEnabled(true);
                break;
            }
        }
    }

    private void markAttendance(Subject s, LocalDateTime logTime) {
        LocalDate today = logTime.toLocalDate();
        if (s.lastAttendedDate != null && s.lastAttendedDate.equals(today)) return;
        LocalDateTime classStart = LocalDateTime.of(today, s.startTime);
        long diffMinutes = Duration.between(classStart, logTime).toMinutes();
        if (diffMinutes <= 5) {
            // on-time, nothing to count
        } else if (diffMinutes <= 15) {
            s.addLate();
        } else {
            s.addAbsent();
        }
        s.lastAttendedDate = today;
    }

    private void clearNotification() {
        notificationLabel.setText("Notification Pane");
        attendButton.setEnabled(false);
        notifiedSubject = null;
        notifiedDate = null;
    }

    private void autoMarkAbsents() {
        LocalDateTime now = LocalDateTime.now();
        for (Subject s : subjects) {
            if (s.dropped) continue;
            if (!s.days.contains(now.getDayOfWeek())) continue;
            LocalDate today = now.toLocalDate();
            if (s.lastAttendedDate != null && s.lastAttendedDate.equals(today)) continue;
            LocalDateTime classStart = LocalDateTime.of(today, s.startTime);
            if (now.isAfter(classStart.plusMinutes(15))) {
                s.addAbsent();
                s.lastAttendedDate = today;
                if (notifiedSubject == s && Objects.equals(notifiedDate, today)) {
                    clearNotification();
                }
                if (scheduleEditorDialog != null && scheduleEditorDialog.isVisible()) {
                    scheduleEditorDialog.refreshFromSubjects();
                }
                refreshMainUI();
            }
        }
    }
}
