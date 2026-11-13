/** Represents one course in a student's semester course list. */
public class Course {
    private final String code;
    private final String title;
    private final int units;

    public Course(String code, String title, int units) {
        this.code = normalizeCode(code);
        this.title = title == null ? "" : title.trim();
        this.units = units;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getUnits() {
        return units;
    }

    public static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    /** Encodes the course as one line for the data file. */
    public String toFileLine() {
        return code + "|" + title.replace("|", " ") + "|" + units;
    }

    /** Parses and validates one saved course record. */
    public static Course fromFileLine(String line) {
        String[] fields = line.split("\\|", -1);

        if (fields.length != 3) {
            throw new IllegalArgumentException(
                "Expected course code, title, and units."
            );
        }

        String code = normalizeCode(fields[0]);
        String title = fields[1].trim();
        int units;

        try {
            units = Integer.parseInt(fields[2].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                "Course units must be a whole number."
            );
        }

        validate(code, title, units);
        return new Course(code, title, units);
    }

    public static void validate(String code, String title, int units) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be blank.");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be blank.");
        }

        if (units <= 0) {
            throw new IllegalArgumentException(
                "Course units must be greater than zero."
            );
        }
    }
}