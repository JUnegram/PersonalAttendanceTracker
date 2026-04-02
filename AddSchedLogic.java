package src;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AddSchedLogic {
    private final AddSchedUI ui;
    private final List<SubjectData> subjects = new ArrayList<>();

    public AddSchedLogic(JFrame owner) {
        ui = new AddSchedUI(owner, null);
    }

    public SubjectData showAndGet() {
        ui.setLocationRelativeTo(null);
        ui.setVisible(true);
        return ui.getResult();
    }
}
