package src;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;


public class AddSchedUI extends JDialog {
    private static final Color COLOR_BG = new Color(245, 245, 245);
    private static final Color COLOR_TEXT = new Color(60, 60, 60);
    private static final Color COLOR_BUTTON = new Color(162, 193, 196);

    private JTextField courseTitleField;
    private JCheckBox[] dayChecks;
    private JTextField startHour, startMin, endHour, endMin;
    private JComboBox<String> startAmPm, endAmPm;
    private JTextField courseTypeField, instructorField, roomField;
    private JButton saveBtn, cancelBtn;

    private SubjectData result;

    public AddSchedUI(Window owner, SubjectData existing) {
        super(owner, "Add Subject", ModalityType.APPLICATION_MODAL);
        setSize(520, 520);
        setLayout(new BorderLayout());
        buildUI(existing);
    }

    private void buildUI(SubjectData existing) {


        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(COLOR_BG);

    // Top: Course Title
    JPanel inputPanel = new JPanel(new GridBagLayout());
    inputPanel.setBackground(COLOR_BG);
    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
    JLabel titleLabel = new JLabel("Course Title:");
    titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    titleLabel.setForeground(COLOR_TEXT);
    inputPanel.add(titleLabel, gbc);

    gbc.gridx = 1; gbc.weightx = 1;
    courseTitleField = new JTextField();
    courseTitleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    courseTitleField.setPreferredSize(new Dimension(260, 28));
    inputPanel.add(courseTitleField, gbc);

    root.add(inputPanel);

    // Schedule days (slightly above the time fields)
    JPanel schedulePanel = new JPanel(new GridBagLayout());
    schedulePanel.setBackground(COLOR_BG);
    schedulePanel.setBorder(BorderFactory.createTitledBorder("Schedule"));
    GridBagConstraints sgc = new GridBagConstraints();
    sgc.insets = new Insets(6,6,6,6); sgc.fill = GridBagConstraints.HORIZONTAL; sgc.gridx = 0; sgc.gridy = 0; sgc.weightx = 1;
    String[] dn = {"Mon","Tue","Wed","Thu","Fri","Sat"};
    dayChecks = new JCheckBox[dn.length];
    JPanel dayRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    dayRow.setBackground(COLOR_BG);
    for (int i=0;i<dn.length;i++) { dayChecks[i] = new JCheckBox(dn[i]); dayRow.add(dayChecks[i]); }
    schedulePanel.add(dayRow, sgc);
    root.add(schedulePanel);

    // Time fields (placed slightly higher)
    JPanel timePanel = new JPanel(new GridBagLayout());
    timePanel.setBackground(COLOR_BG);
    GridBagConstraints tgc = new GridBagConstraints();
    tgc.insets = new Insets(4, 4, 4, 4);
        tgc.fill = GridBagConstraints.HORIZONTAL;
        tgc.gridx = 0;
        tgc.gridy = 0;
        tgc.weightx = 0;
        JLabel startLabel = new JLabel("Start Time:");
        startLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        startLabel.setForeground(COLOR_TEXT);
        timePanel.add(startLabel, tgc);

        tgc.gridx = 1;
        tgc.weightx = 1;
    startHour = new JTextField(2);
    startHour.setPreferredSize(new Dimension(40, 24));
    startMin = new JTextField(2);
    startMin.setPreferredSize(new Dimension(40, 24));
        startAmPm = new JComboBox<>(new String[]{"AM", "PM"});
        JPanel startTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        startTimePanel.setBackground(COLOR_BG);
        startTimePanel.add(startHour);
        startTimePanel.add(new JLabel(":"));
        startTimePanel.add(startMin);
        startTimePanel.add(startAmPm);
        timePanel.add(startTimePanel, tgc);

        tgc.gridx = 0;
        tgc.gridy = 1;
        tgc.weightx = 0;
        JLabel endLabel = new JLabel("End Time:");
        endLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        endLabel.setForeground(COLOR_TEXT);
        timePanel.add(endLabel, tgc);

    tgc.gridx = 1;
    tgc.weightx = 1;
    endHour = new JTextField(2);
    endHour.setPreferredSize(new Dimension(40, 24));
    endMin = new JTextField(2);
    endMin.setPreferredSize(new Dimension(40, 24));
        endAmPm = new JComboBox<>(new String[]{"AM", "PM"});
        JPanel endTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        endTimePanel.setBackground(COLOR_BG);
        endTimePanel.add(endHour);
        endTimePanel.add(new JLabel(":"));
        endTimePanel.add(endMin);
        endTimePanel.add(endAmPm);
        timePanel.add(endTimePanel, tgc);

        root.add(timePanel);

        // Course Type, Instructor, Room fields
        JPanel miscPanel = new JPanel(new GridBagLayout());
        miscPanel.setBackground(COLOR_BG);
        GridBagConstraints mgc = new GridBagConstraints();
        mgc.insets = new Insets(5, 5, 5, 5);
        mgc.fill = GridBagConstraints.HORIZONTAL;
        mgc.gridx = 0;
        mgc.gridy = 0;
        mgc.weightx = 0;
        JLabel typeLabel = new JLabel("Course Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeLabel.setForeground(COLOR_TEXT);
        miscPanel.add(typeLabel, mgc);
        mgc.gridx = 1;
        mgc.weightx = 1;
        courseTypeField = new JTextField();
        courseTypeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        miscPanel.add(courseTypeField, mgc);

        mgc.gridx = 0;
        mgc.gridy = 1;
        mgc.weightx = 0;
        JLabel instrLabel = new JLabel("Instructor:");
        instrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instrLabel.setForeground(COLOR_TEXT);
        miscPanel.add(instrLabel, mgc);
        mgc.gridx = 1;
        mgc.weightx = 1;
        instructorField = new JTextField();
        instructorField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        miscPanel.add(instructorField, mgc);

        mgc.gridx = 0;
        mgc.gridy = 2;
        mgc.weightx = 0;
        JLabel roomLabel = new JLabel("Room:");
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roomLabel.setForeground(COLOR_TEXT);
        miscPanel.add(roomLabel, mgc);
        mgc.gridx = 1;
        mgc.weightx = 1;
        roomField = new JTextField();
        roomField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        miscPanel.add(roomField, mgc);

        root.add(miscPanel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Save"); cancelBtn = new JButton("Cancel");
        actions.add(saveBtn); actions.add(cancelBtn);
        root.add(Box.createVerticalStrut(12)); root.add(actions);

        add(new JScrollPane(root), BorderLayout.CENTER);

        if (existing != null) prefill(existing);

        saveBtn.addActionListener(e -> {
            SubjectData s = existing == null ? new SubjectData() : existing;
            s.title = courseTitleField.getText();
            s.courseType = courseTypeField.getText();
            s.instructor = instructorField.getText();
            s.room = roomField.getText();
            int sh = parseIntSafe(startHour.getText()); int sm = parseIntSafe(startMin.getText());
            if ("PM".equals(startAmPm.getSelectedItem()) && sh < 12) sh += 12;
            if ("AM".equals(startAmPm.getSelectedItem()) && sh == 12) sh = 0;
            s.startTime = LocalTime.of(Math.max(0, Math.min(23, sh)), Math.max(0, Math.min(59, sm)));
            int eh = parseIntSafe(endHour.getText()); int em = parseIntSafe(endMin.getText());
            if ("PM".equals(endAmPm.getSelectedItem()) && eh < 12) eh += 12;
            if ("AM".equals(endAmPm.getSelectedItem()) && eh == 12) eh = 0;
            s.endTime = LocalTime.of(Math.max(0, Math.min(23, eh)), Math.max(0, Math.min(59, em)));
            java.util.Set<DayOfWeek> days = new HashSet<>();
            for (JCheckBox c : dayChecks) if (c.isSelected()) {
                switch (c.getText()) {
                    case "Mon": days.add(DayOfWeek.MONDAY); break;
                    case "Tue": days.add(DayOfWeek.TUESDAY); break;
                    case "Wed": days.add(DayOfWeek.WEDNESDAY); break;
                    case "Thu": days.add(DayOfWeek.THURSDAY); break;
                    case "Fri": days.add(DayOfWeek.FRIDAY); break;
                    case "Sat": days.add(DayOfWeek.SATURDAY); break;
                }
            }
            s.days = days;
            result = s;
            dispose();
        });

        cancelBtn.addActionListener(e -> { result = null; dispose(); });
    }

    private void prefill(SubjectData existing) {
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
        int sh = existing.startTime.getHour(); int sm = existing.startTime.getMinute();
        String ap = sh >= 12 ? "PM" : "AM";
        if (sh == 0) sh = 12; else if (sh > 12) sh -= 12;
        startHour.setText(String.valueOf(sh)); startMin.setText(String.format("%02d", sm)); startAmPm.setSelectedItem(ap);
        courseTypeField.setText(existing.courseType); instructorField.setText(existing.instructor); roomField.setText(existing.room);
    }

    public SubjectData getResult() { return result; }

    private static int parseIntSafe(String s) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; } }
}
