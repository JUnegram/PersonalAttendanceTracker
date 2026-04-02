package src;
import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;

public class AddSubjectDialog extends JDialog {

    private JTextField courseTitleField;
    private JCheckBox[] dayChecks;
    private JTextField startHour, startMin, endHour, endMin;
    private JComboBox<String> startAmPm, endAmPm;
    private JTextField courseTypeField, instructorField, roomField;
    private JButton saveBtn, cancelBtn;

    private SubjectData result;

    public AddSubjectDialog(Window owner, SubjectData existing) {
        super(owner, "Add Subject", ModalityType.APPLICATION_MODAL);
        setSize(520, 520);
        setLayout(new BorderLayout());
        buildUI(existing);
    }

    private void buildUI(SubjectData existing) {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ✅ Center the header horizontally
        JLabel header = new JLabel("Add Subject", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setAlignmentX(Component.CENTER_ALIGNMENT); 
        root.add(header);
        root.add(Box.createVerticalStrut(12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // ✅ Schedule Panel (kept at the top)
        JPanel schedPanel = new JPanel(new GridBagLayout());
        schedPanel.setBorder(BorderFactory.createTitledBorder("Schedule"));
        String[] dn = {"Mon","Tue","Wed","Thu","Fri","Sat"};
        dayChecks = new JCheckBox[dn.length];
        JPanel dayRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        for (int i=0;i<dn.length;i++){ 
            dayChecks[i] = new JCheckBox(dn[i]); 
            dayRow.add(dayChecks[i]); 
        }
        GridBagConstraints sgc = new GridBagConstraints();
        sgc.insets = new Insets(4,4,4,4);
        sgc.gridwidth = 6; 
        sgc.gridy = 0;
        schedPanel.add(dayRow, sgc);

        startHour = new JTextField(2); startMin = new JTextField(2);
        endHour = new JTextField(2); endMin = new JTextField(2);
        startAmPm = new JComboBox<>(new String[]{"AM","PM"});
        endAmPm = new JComboBox<>(new String[]{"AM","PM"});

        JPanel timeRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        timeRow1.add(new JLabel("Starting Time")); 
        timeRow1.add(startHour); 
        timeRow1.add(new JLabel(":")); 
        timeRow1.add(startMin); 
        timeRow1.add(startAmPm);

        JPanel timeRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        timeRow2.add(new JLabel("End Time")); 
        timeRow2.add(endHour); 
        timeRow2.add(new JLabel(":")); 
        timeRow2.add(endMin); 
        timeRow2.add(endAmPm);

        sgc.gridy = 1; schedPanel.add(timeRow1, sgc);
        sgc.gridy = 2; schedPanel.add(timeRow2, sgc);

        GridBagConstraints mgc = new GridBagConstraints();
        mgc.insets = new Insets(4,4,4,4);
        mgc.fill = GridBagConstraints.HORIZONTAL; 
        mgc.weightx = 1;

        
        mgc.gridx = 0; 
        mgc.gridy = 0; 
        mgc.gridwidth = 2;
        form.add(schedPanel, mgc);

        
        courseTitleField = new JTextField();
        mgc.gridwidth = 1;
        mgc.gridx = 0; 
        mgc.gridy = 1; 
        form.add(new JLabel("Course Title"), mgc);
        mgc.gridx = 1; 
        form.add(courseTitleField, mgc);

        
        courseTypeField = new JTextField();
        mgc.gridx = 0; 
        mgc.gridy = 2; 
        form.add(new JLabel("Course Type"), mgc);
        mgc.gridx = 1; 
        form.add(courseTypeField, mgc);

        
        instructorField = new JTextField();
        mgc.gridx = 0; 
        mgc.gridy = 3; 
        form.add(new JLabel("Instructor"), mgc);
        mgc.gridx = 1; 
        form.add(instructorField, mgc);

        
        roomField = new JTextField();
        mgc.gridx = 0; 
        mgc.gridy = 4; 
        form.add(new JLabel("Room"), mgc);
        mgc.gridx = 1; 
        form.add(roomField, mgc);

        root.add(form);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Save"); 
        cancelBtn = new JButton("Cancel");
        actions.add(saveBtn); 
        actions.add(cancelBtn);
        root.add(Box.createVerticalStrut(12)); 
        root.add(actions);

        add(new JScrollPane(root), BorderLayout.CENTER);

        if (existing != null) prefill(existing);

        saveBtn.addActionListener(e -> {
            SubjectData s = existing == null ? new SubjectData() : existing;
            s.title = courseTitleField.getText();
            s.courseType = courseTypeField.getText();
            s.instructor = instructorField.getText();
            s.room = roomField.getText();

            int sh = parseIntSafe(startHour.getText()); 
            int sm = parseIntSafe(startMin.getText());
            if ("PM".equals(startAmPm.getSelectedItem()) && sh < 12) sh += 12;
            if ("AM".equals(startAmPm.getSelectedItem()) && sh == 12) sh = 0;
            s.startTime = LocalTime.of(Math.max(0, Math.min(23, sh)), Math.max(0, Math.min(59, sm)));

            int eh = parseIntSafe(endHour.getText()); 
            int em = parseIntSafe(endMin.getText());
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

        cancelBtn.addActionListener(e -> { 
            result = null; 
            dispose(); 
        });
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
        int sh = existing.startTime.getHour(); 
        int sm = existing.startTime.getMinute();
        String ap = sh >= 12 ? "PM" : "AM";
        if (sh == 0) sh = 12; 
        else if (sh > 12) sh -= 12;
        startHour.setText(String.valueOf(sh)); 
        startMin.setText(String.format("%02d", sm)); 
        startAmPm.setSelectedItem(ap);
        courseTypeField.setText(existing.courseType); 
        instructorField.setText(existing.instructor); 
        roomField.setText(existing.room);
    }

    public SubjectData getResult() { 
        return result; 
    }

    private static int parseIntSafe(String s) { 
        try { 
            return Integer.parseInt(s.trim()); 
        } catch (Exception e) { 
            return 0; 
        } 
    }
}
