package src;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * StatusLogic: separates attendance/business logic from UI.
 * Mirrors the Subject model used in PersonalAttendanceTracker but as a reusable class.
 */
public class StatusLogic {

    public static class SubjectStatus {
        public String title;
        // event-based attendance tracking
        public enum AttendanceType {ON_TIME, LATE, ABSENT}
        public static class AttendanceEvent {
            public final String id = java.util.UUID.randomUUID().toString();
            public AttendanceType type;
            public java.time.LocalDateTime timestamp;
            // conversion id groups three late events into one converted absent
            public String conversionId = null;
            public AttendanceEvent(AttendanceType type, java.time.LocalDateTime ts) { this.type = type; this.timestamp = ts; }
        }

    public List<AttendanceEvent> events = new ArrayList<>();
    // compatibility counters (computed from events)
    public int lateCount;
    public int absentCount;
    public boolean dropped;
        public Set<DayOfWeek> days = new HashSet<>();
        public LocalTime startTime = LocalTime.of(10, 0);
        public LocalTime endTime = LocalTime.of(11, 0);
        public LocalDate lastAttendedDate;
        public String courseType = "";
        public String instructor = "";
        public String room = "";

        public SubjectStatus(String title) { this.title = title; }

        // compatibility helper: add a late event with current timestamp
        public void addLate() { addEvent(AttendanceType.LATE, java.time.LocalDateTime.now()); }
        // compatibility helper: add an absent event with current timestamp
        public void addAbsent() { addEvent(AttendanceType.ABSENT, java.time.LocalDateTime.now()); }

        // Add a new attendance event and perform conversion if needed
        public void addEvent(AttendanceType type, java.time.LocalDateTime ts) {
            AttendanceEvent ev = new AttendanceEvent(type, ts);
            events.add(ev);
            if (type == AttendanceType.LATE) tryConvertLates();
            recomputeCountsAndDropped();
        }

        // Remove an event by id and undo conversions if necessary
        public void removeEventById(String id) {
            AttendanceEvent ev = events.stream().filter(e -> e.id.equals(id)).findFirst().orElse(null);
            if (ev == null) return;
            if (ev.type == AttendanceType.ABSENT && ev.conversionId != null) {
                // This absent was created by conversion; unmark associated LATEs
                String conv = ev.conversionId;
                // remove the absent event
                events.remove(ev);
                // find late events with this conversion id and clear conversionId
                events.stream().filter(e -> e.type == AttendanceType.LATE && conv.equals(e.conversionId)).forEach(e -> e.conversionId = null);
            } else {
                // regular event
                events.remove(ev);
                // If it was a late that had conversionId, and removing it causes conversion to be invalid,
                // we need to find the conversion and possibly remove its absent and restore other lates' conversionId.
                if (ev.type == AttendanceType.LATE && ev.conversionId != null) {
                    String conv = ev.conversionId;
                    // remove the converted absent if present
                    List<AttendanceEvent> absentConv = events.stream().filter(e -> e.type == AttendanceType.ABSENT && conv.equals(e.conversionId)).toList();
                    for (AttendanceEvent a : absentConv) events.remove(a);
                    // clear conversionId from any late events that referenced it
                    events.stream().filter(e -> e.type == AttendanceType.LATE && conv.equals(e.conversionId)).forEach(e -> e.conversionId = null);
                }
            }
            recomputeCountsAndDropped();
        }

        private void tryConvertLates() {
            // find unconverted late events ordered by timestamp
            List<AttendanceEvent> unconv = events.stream()
                    .filter(e -> e.type == AttendanceType.LATE && e.conversionId == null)
                    .sorted(Comparator.comparing(e -> e.timestamp))
                    .toList();
            if (unconv.size() >= 3) {
                // take earliest 3
                List<AttendanceEvent> three = unconv.subList(0,3);
                String convId = java.util.UUID.randomUUID().toString();
                // mark them as converted
                three.forEach(e -> e.conversionId = convId);
                // create an absent event at time of third late
                AttendanceEvent absent = new AttendanceEvent(AttendanceType.ABSENT, three.get(2).timestamp);
                absent.conversionId = convId;
                events.add(absent);
                // after conversion, update counts
                recomputeCountsAndDropped();
            }
        }

        private void recomputeCountsAndDropped() {
            this.lateCount = (int) events.stream().filter(e -> e.type == AttendanceType.LATE && e.conversionId == null).count();
            this.absentCount = (int) events.stream().filter(e -> e.type == AttendanceType.ABSENT).count();
            this.dropped = this.absentCount >= 3;
        }

        public List<AttendanceEvent> getLatesUnconverted() {
            return events.stream().filter(e -> e.type == AttendanceType.LATE && e.conversionId == null).sorted(Comparator.comparing(e -> e.timestamp)).toList();
        }

        public List<AttendanceEvent> getAbsents() {
            return events.stream().filter(e -> e.type == AttendanceType.ABSENT).sorted(Comparator.comparing(e -> e.timestamp)).toList();
        }
    }

    private final List<SubjectStatus> subjects = new ArrayList<>();

    public StatusLogic() {}

    public List<SubjectStatus> getSubjects() { return subjects; }

    public void setSubjects(Collection<SubjectStatus> list) {
        subjects.clear();
        if (list != null) subjects.addAll(list);
    }

    public void addSubject(SubjectStatus s) { subjects.add(s); }

    public SubjectStatus findByTitle(String title) {
        for (SubjectStatus s : subjects) if (s.title.equals(title)) return s;
        return null;
    }

    /**
     * Log attendance for a subject at given logTime (date/time), applying the same rules as before:
     * <=5 min -> on time (no increment), <=15 min -> late, otherwise absent.
     */
    public void logAttendance(SubjectStatus s, LocalDate date, java.time.LocalTime logTime) {
        if (s == null) return;
        if (s.lastAttendedDate != null && s.lastAttendedDate.equals(date)) return;
        java.time.LocalDateTime classStart = java.time.LocalDateTime.of(date, s.startTime);
        java.time.LocalDateTime logDateTime = java.time.LocalDateTime.of(date, logTime);
        long diffMinutes = java.time.Duration.between(classStart, logDateTime).toMinutes();
        if (diffMinutes <= 5) {
            // on-time
        } else if (diffMinutes <= 15) {
            s.addLate();
        } else {
            s.addAbsent();
        }
        s.lastAttendedDate = date;
    }

    public void autoMarkAbsents(LocalDate nowDate, java.time.LocalTime nowTime) {
        java.time.LocalDateTime now = java.time.LocalDateTime.of(nowDate, nowTime);
        for (SubjectStatus s : subjects) {
            if (s.dropped) continue;
            if (!s.days.contains(nowDate.getDayOfWeek())) continue;
            if (s.lastAttendedDate != null && s.lastAttendedDate.equals(nowDate)) continue;
            java.time.LocalDateTime classStart = java.time.LocalDateTime.of(nowDate, s.startTime);
            if (now.isAfter(classStart.plusMinutes(15))) {
                s.addAbsent();
                s.lastAttendedDate = nowDate;
            }
        }
    }

    public int countAbsents() {
        int c = 0; for (SubjectStatus s : subjects) if (s.absentCount > 0) c += s.absentCount; return c;
    }

    public int countLates(boolean onlyPlainLate) {
        int c = 0;
        for (SubjectStatus s : subjects) {
            if (onlyPlainLate) c += s.lateCount;
            else c += s.lateCount;
        }
        return c;
    }
}
