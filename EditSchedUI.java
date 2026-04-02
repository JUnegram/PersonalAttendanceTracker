package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EditSchedUI extends JDialog {

    private static final Color COLOR_BG = new Color(245, 245, 245);
    private static final Color COLOR_BUTTON = new Color(162, 193, 196);
    private static final Color COLOR_TEXT = new Color(60, 60, 60);

    private DefaultTableModel subjectModel;
    private JTable subjectTable;
    private JButton addBtn, deleteBtn, editBtn, newSchedBtn;

    public EditSchedUI(JFrame owner) {
        super(owner, "Schedule Editor", true);
        setSize(980, 560);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 6, 12));
        addBtn = createPillButton("Add Subject");
        deleteBtn = createPillButton("Delete Subject");
        editBtn = createPillButton("Edit Subject");
        newSchedBtn = createPillButton("New Schedule");
        top.add(addBtn); top.add(deleteBtn); top.add(editBtn); top.add(newSchedBtn);

        String[] cols = {"Course Title","Days","Start","End","Type","Instructor","Room"};
        subjectModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        subjectTable = new JTable(subjectModel);
        subjectTable.setRowHeight(40);
        subjectTable.setIntercellSpacing(new Dimension(0, 4));
        subjectTable.setFillsViewportHeight(true);
        subjectTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(subjectTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(6, 12, 12, 12));
        scroll.setPreferredSize(new Dimension(940, 380));

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(Box.createRigidArea(new Dimension(0,12)), BorderLayout.SOUTH);
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
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setForeground(COLOR_TEXT);
        return b;
    }

    // helpers
    public void addSubjectRow(Object[] row) { subjectModel.addRow(row); }
    public void clearAll() { subjectModel.setRowCount(0); }
    public int getSelectedRow() { return subjectTable.getSelectedRow(); }
    public void removeSelectedRow() { int r = subjectTable.getSelectedRow(); if (r >= 0) subjectModel.removeRow(r); }

    public JButton getAddBtn() { return addBtn; }
    public JButton getDeleteBtn() { return deleteBtn; }
    public JButton getEditBtn() { return editBtn; }
    public JButton getNewSchedBtn() { return newSchedBtn; }

    public java.util.List<SubjectData> getSubjectsFromModel() {
        java.util.List<SubjectData> out = new java.util.ArrayList<>();
        for (int r = 0; r < subjectModel.getRowCount(); r++) {
            SubjectData s = new SubjectData();
            s.title = String.valueOf(subjectModel.getValueAt(r, 0));
            out.add(s);
        }
        return out;
    }
}
