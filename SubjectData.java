package src;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Set;


public class SubjectData {
    public String title = "";
    public Set<DayOfWeek> days = java.util.Collections.emptySet();
    public LocalTime startTime = LocalTime.of(10, 0);
    public LocalTime endTime = LocalTime.of(11, 0);
    public String courseType = "";
    public String instructor = "";
    public String room = "";

    public SubjectData() {}

    public SubjectData(String title) { this.title = title; }

    @Override
    public String toString() { return title == null ? "" : title; }
}
