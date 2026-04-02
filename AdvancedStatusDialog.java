package src;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdvancedStatusDialog extends JDialog {

    public AdvancedStatusDialog(JFrame owner, StatusLogic logic) {
        super(owner, "Advanced Status", true);
        // size similar to main UI
        setSize(980, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        buildUI(logic);
    }

    private void buildUI(StatusLogic logic) {
        java.util.List<StatusLogic.SubjectStatus> subjects = logic.getSubjects();

        // Top: semester dropdown
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] semesters = new String[]{"-- SELECT SEMESTER --","1ST SEMESTER 2025-2026","MIDYEAR 2025","2ND SEMESTER 2024-2025","1ST SEMESTER 2024-2025"};
        JComboBox<String> semester = new JComboBox<>(semesters);
        top.add(semester);
        add(top, BorderLayout.NORTH);

        // Center: split pane - left table (subjects + counts), right details (subject selector + lists)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.45);

        // Left table
        String[] cols = new String[]{"Subject", "Lates", "Absents", "Dropped"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable subjTable = new JTable(tableModel);
        subjTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(subjTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Subjects"));
        split.setLeftComponent(tableScroll);

        // Right panel: subject selector + Lates/Absents
        JPanel right = new JPanel(new BorderLayout());
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] subjTitles = subjects.stream().map(s -> s.title).toArray(String[]::new);
        JComboBox<String> subjectSel = new JComboBox<>(subjTitles);
        subjectSel.setPreferredSize(new Dimension(300, 26));
        rightTop.add(new JLabel("Subject:"));
        rightTop.add(subjectSel);
        right.add(rightTop, BorderLayout.NORTH);

        JPanel lists = new JPanel(new GridLayout(1,2,8,8));
        DefaultListModel<String> lateModel = new DefaultListModel<>();
        JList<String> lateList = new JList<>(lateModel);
        DefaultListModel<String> absentModel = new DefaultListModel<>();
        JList<String> absentList = new JList<>(absentModel);
        JScrollPane leftScroll = new JScrollPane(lateList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Lates"));
        JScrollPane rightScroll = new JScrollPane(absentList);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Absents"));
        lists.add(leftScroll);
        lists.add(rightScroll);
        right.add(lists, BorderLayout.CENTER);

        split.setRightComponent(right);
        add(split, BorderLayout.CENTER);

        // helper to refresh the left table data
        java.util.function.Consumer<Void> refreshTable = v -> {
            tableModel.setRowCount(0);
            for (StatusLogic.SubjectStatus ss : subjects) {
                Object[] r = new Object[]{ss.title, ss.lateCount, ss.absentCount, ss.dropped};
                tableModel.addRow(r);
            }
        };

        // populate based on selected subject (show timestamped events)
        java.util.function.Consumer<String> populateFor = title -> {
            lateModel.clear(); absentModel.clear();
            if (title == null || title.isEmpty()) return;
            StatusLogic.SubjectStatus ss = subjects.stream().filter(x -> x.title.equals(title)).findFirst().orElse(null);
            if (ss == null) return;
            // list actual events with their id encoded so we can delete by id later
            for (StatusLogic.SubjectStatus.AttendanceEvent ev : ss.getLatesUnconverted()) {
                lateModel.addElement(ev.id + "|Late|" + ev.timestamp.toString());
            }
            for (StatusLogic.SubjectStatus.AttendanceEvent ev : ss.getAbsents()) {
                absentModel.addElement(ev.id + "|Absent|" + ev.timestamp.toString());
            }
        };
        // wire selection from combo
        if (subjectSel.getItemCount() > 0) populateFor.accept((String)subjectSel.getItemAt(0));
        subjectSel.addActionListener(e -> populateFor.accept((String)subjectSel.getSelectedItem()));

        // wire table selection to update the subject selector and lists
        subjTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int r = subjTable.getSelectedRow();
                    if (r >= 0 && r < subjTable.getRowCount()) {
                        String t = (String) subjTable.getValueAt(r, 0);
                        subjectSel.setSelectedItem(t);
                    }
                }
            }
        });

        // initially fill table
        refreshTable.accept(null);

        // bottom controls: Delete Entry, Delete Subject, Close
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteEntry = new JButton("Delete Entry");
        deleteEntry.addActionListener(e -> {
            String title = (String) subjectSel.getSelectedItem();
            if (title == null) return;
            StatusLogic.SubjectStatus ss = subjects.stream().filter(x -> x.title.equals(title)).findFirst().orElse(null);
            if (ss == null) return;
            // check selected in either list
            String selLate = lateList.getSelectedValue();
            String selAbsent = absentList.getSelectedValue();
            if (selLate == null && selAbsent == null) return;
            String encoded = selLate != null ? selLate : selAbsent;
            String id = encoded.split("\\|",2)[0];
            ss.removeEventById(id);
            // refresh lists and table
            populateFor.accept(title);
            refreshTable.accept(null);
        });

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        south.add(deleteEntry);
        south.add(close);
        add(south, BorderLayout.SOUTH);
    }
}
