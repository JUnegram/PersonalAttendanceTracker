package src;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainPageUI ui = new MainPageUI();
            MainPageLogic logic = new MainPageLogic(ui);

            
            
            JFrame frame = new JFrame("Attendance App - Main");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(ui);
            frame.setSize(980, 560);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            EditSchedLogic editorLogic = new EditSchedLogic(frame);
            ui.getEditScheduleBtn().addActionListener(e -> {
                editorLogic.show();
                java.util.List<SubjectData> updated = editorLogic.getSubjects();
               
                logic.setSubjectsData(updated);
            });

            
            ui.getCheckStatusBtn().addActionListener(e -> {
                java.util.List<SubjectData> data = editorLogic.getSubjects();
                StatusLogic sl = new StatusLogic();
                if (data != null) {
                    for (SubjectData sd : data) {
                        StatusLogic.SubjectStatus ss = new StatusLogic.SubjectStatus(sd.title == null ? "" : sd.title);
                        ss.days = sd.days == null ? new java.util.HashSet<>() : new java.util.HashSet<>(sd.days);
                        ss.startTime = sd.startTime == null ? java.time.LocalTime.of(10,0) : sd.startTime;
                        ss.endTime = sd.endTime == null ? java.time.LocalTime.of(11,0) : sd.endTime;
                        ss.courseType = sd.courseType == null ? "" : sd.courseType;
                        ss.instructor = sd.instructor == null ? "" : sd.instructor;
                        ss.room = sd.room == null ? "" : sd.room;
                        sl.addSubject(ss);
                    }
                }
                StatusUI su = new StatusUI(frame, sl);
                su.setVisible(true);
            });

            
        });
    }
}
