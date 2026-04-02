package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * StatusUI — a simple dialog that shows subjects' lates, absents and dropped status.
 * Uses StatusLogic for the data model.
 */
public class StatusUI extends JDialog {

    private final StatusLogic logic;

    public StatusUI(JFrame owner, StatusLogic logic) {
        super(owner, "Subject Status", true);
        this.logic = logic;
        setSize(600, 400);
        setLayout(new BorderLayout());
        buildUI();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        String[] cols = {"Subject","Lates","Absents","Dropped?"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        List<StatusLogic.SubjectStatus> subjects = logic.getSubjects();
        for (StatusLogic.SubjectStatus s : subjects) {
            model.addRow(new Object[]{s.title, s.lateCount, s.absentCount, s.dropped ? "Yes" : "No"});
        }
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        JButton ok = new JButton("Close");
        ok.addActionListener(e -> dispose());
        JButton advanced = new JButton("Advanced");
        advanced.addActionListener(e -> {
            AdvancedStatusDialog adv = new AdvancedStatusDialog((JFrame)getOwner(), logic);
            adv.setVisible(true);
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(advanced);
        south.add(ok);
        add(south, BorderLayout.SOUTH);
    }
}
