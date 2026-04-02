package src;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class EditSchedLogic {

    private final EditSchedUI ui;
    private final List<SubjectData> subjects = new ArrayList<>();

    public EditSchedLogic(JFrame owner) {
        ui = new EditSchedUI(owner);
        attachActions();
    }

    private void attachActions() {
        ui.getAddBtn().addActionListener(e -> {
            AddSubjectDialog add = new AddSubjectDialog(ui, null);
            add.setLocationRelativeTo(ui);
            add.setVisible(true);
            SubjectData res = add.getResult();
            if (res != null) {
                subjects.add(res);
                ui.addSubjectRow(new Object[]{res.title, res.days.toString(), res.startTime.toString(), res.endTime.toString(), res.courseType, res.instructor, res.room});
            }
        });

        ui.getDeleteBtn().addActionListener(e -> {
            int r = ui.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(ui, "Please select a row to delete."); return; }
            int confirm = JOptionPane.showConfirmDialog(ui, "Delete selected subject?", "Confirm Delete", JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                ui.removeSelectedRow();
                if (r < subjects.size()) subjects.remove(r);
            }
        });

        ui.getEditBtn().addActionListener(e -> {
            int r = ui.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(ui, "Please select a row to edit."); return; }
            SubjectData existing = r < subjects.size() ? subjects.get(r) : null;
            AddSubjectDialog d = new AddSubjectDialog(ui, existing);
            d.setLocationRelativeTo(ui);
            d.setVisible(true);
            SubjectData res = d.getResult();
            if (res != null) {
                if (r < subjects.size()) subjects.set(r, res); else subjects.add(res);
                ui.clearAll();
                for (SubjectData s : subjects) ui.addSubjectRow(new Object[]{s.title, s.days.toString(), s.startTime.toString(), s.endTime.toString(), s.courseType, s.instructor, s.room});
            }
        });

        ui.getNewSchedBtn().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(ui, "Delete ALL subjects?", "New Schedule", JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                subjects.clear(); ui.clearAll();
            }
        });
    }

    public void show() { ui.setLocationRelativeTo(null); ui.setVisible(true); }
    public List<SubjectData> getSubjects() { return subjects; }
}
