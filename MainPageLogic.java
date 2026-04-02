package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class MainPageLogic {

    private final MainPageUI ui;
    private List<Object> subjects; 
    // store full SubjectData for notification logic (may be null if not provided)
    private List<SubjectData> subjectDataList;
    // track which subject was last notified/attended to avoid repeat notifications
    private String notifiedSubjectTitle;
    private LocalDate notifiedDate;
    private final Map<String, LocalDate> lastAttended = new HashMap<>();

    private Timer autoTimer;

    public MainPageLogic(MainPageUI ui) {
        this.ui = ui;
        attachDefaultActions();
        startAutoRefresh();
    }

    
    private void attachDefaultActions() {
        // Show status is wired from Main.java to open StatusUI; no debug message here.
        ui.getCheckStatusBtn().addActionListener(e -> {
            // intentionally left blank; main wiring will handle showing status UI
        });

        ui.getAttendButton().addActionListener(e -> markAttendanceForNotified());
    }

    public void setSubjects(List<Object> subjects) {
        this.subjects = subjects;
        refreshUIFromSubjects();
    }

    // New overload to accept full SubjectData objects for notification checks
    public void setSubjectsData(List<SubjectData> data) {
        this.subjectDataList = data == null ? null : new ArrayList<>(data);
        // Also update the UI table using titles derived from SubjectData to preserve behavior
        if (data != null) {
            List<Object> titles = new ArrayList<>();
            for (SubjectData s : data) titles.add(s == null ? "Untitled" : s.title);
            setSubjects(titles);
        } else {
            setSubjects(null);
        }
    }

    
    public void refreshUIFromSubjects() {
        DefaultTableModel model = ui.getWeekTableModel();
        model.setRowCount(0);
        // If we have full SubjectData available, render each subject as one full row with
        // title/code on the left and time/room/instructor on the day columns.
        if (subjectDataList != null && !subjectDataList.isEmpty()) {
            for (SubjectData s : subjectDataList) {
                Object[] row = new Object[7];
                row[0] = "<html><b>" + escapeHtml(s.title) + "</b><br/><i>" + escapeHtml(s.courseType) + "</i></html>";
                row[1] = s.days.contains(java.time.DayOfWeek.MONDAY)    ? subjectCellText(s) : "";
                row[2] = s.days.contains(java.time.DayOfWeek.TUESDAY)   ? subjectCellText(s) : "";
                row[3] = s.days.contains(java.time.DayOfWeek.WEDNESDAY) ? subjectCellText(s) : "";
                row[4] = s.days.contains(java.time.DayOfWeek.THURSDAY)  ? subjectCellText(s) : "";
                row[5] = s.days.contains(java.time.DayOfWeek.FRIDAY)    ? subjectCellText(s) : "";
                row[6] = s.days.contains(java.time.DayOfWeek.SATURDAY)  ? subjectCellText(s) : "";
                model.addRow(row);
            }
            // ensure HTML is rendered correctly in cells
            ui.getWeekTable().setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public void setValue(Object value) {
                    if (value instanceof String && ((String) value).contains("<html>")) {
                        setText((String) value);
                    } else {
                        setText(value == null ? "" : value.toString());
                    }
                }
            });
            return;
        }

        if (subjects == null) return;
        for (Object s : subjects) {
            String title = s == null ? "Untitled" : s.toString();
            model.addRow(new Object[]{"<html><b>" + title + "</b><br/><i>Type</i></html>", "", "", "", "", "", ""});
        }
    }

    private String subjectCellText(SubjectData s) {
        String start = String.format("%02d:%02d", s.startTime.getHour(), s.startTime.getMinute());
        String end = String.format("%02d:%02d", s.endTime.getHour(), s.endTime.getMinute());
        String timeRange = start + " - " + end;
        return "<html><center>" + escapeHtml(timeRange) + "<br/>" + escapeHtml(s.room) + "<br/>" + escapeHtml(s.instructor) + "</center></html>";
    }

    private String escapeHtml(String v) { return v == null ? "" : v.replace("<","&lt;").replace(">","&gt;"); }

    private void markAttendanceForNotified() {
        JOptionPane.showMessageDialog(null, "(Logic) Mark attendance invoked at " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        // record attendance for today for the notified subject so notifications don't repeat
        if (notifiedSubjectTitle != null) {
            lastAttended.put(notifiedSubjectTitle, LocalDate.now());
        }
        // clear the notification UI
        ui.getAttendButton().setEnabled(false);
        ui.getNotificationLabel().setText("Notification Pane");
        notifiedSubjectTitle = null;
        notifiedDate = null;
    }

    
    private void startAutoRefresh() {
        autoTimer = new Timer(true);
        autoTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> ui.getDateTimeLabel().setText(DateTimeFormatter.ofPattern("EEE, MMM dd yyyy   hh:mm:ss a").format(LocalDateTime.now())));
                // Also run notification checks on the EDT to safely update Swing components
                SwingUtilities.invokeLater(() -> checkNotifications());
            }
        }, 0, 1000);
    }

    // Run on EDT
    private void checkNotifications() {
        if (subjectDataList == null || subjectDataList.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        LocalTime cur = now.toLocalTime();

        for (SubjectData s : subjectDataList) {
            if (s == null) continue;
            if (s.days == null || !s.days.contains(today)) continue; // not today
            LocalTime start = s.startTime;
            if (start == null) continue;
            long minutesUntil = java.time.Duration.between(cur, start).toMinutes();
            // show notification from 10 minutes before start until start (inclusive)
            if (minutesUntil <= 10 && minutesUntil >= 0) {
                    // skip if already attended today
                    LocalDate last = lastAttended.get(s.title);
                    if (last != null && last.equals(now.toLocalDate())) continue;
                    ui.getNotificationLabel().setText("Upcoming: " + s.title + " in " + minutesUntil + " min");
                    ui.getAttendButton().setEnabled(true);
                    notifiedSubjectTitle = s.title;
                    notifiedDate = now.toLocalDate();
                    return; // show the first upcoming class
            }
        }
        // nothing matched
        ui.getAttendButton().setEnabled(false);
        ui.getNotificationLabel().setText("Notification Pane");
    }

    public void stop() {
        if (autoTimer != null) autoTimer.cancel();
    }
}
