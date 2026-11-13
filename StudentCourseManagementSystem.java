import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Console application for managing semester courses. */
public class StudentCourseManagementSystem {
    private static final String DEFAULT_FILE = "courses.txt";

    private final List<Course> courses = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private final Path dataFile;

    public StudentCourseManagementSystem(Path dataFile) {
        this.dataFile = dataFile;
    }

    public static void main(String[] args) {
        Path file = args.length > 0
                ? Paths.get(args[0])
                : Paths.get(DEFAULT_FILE);

        new StudentCourseManagementSystem(file).runMenu();
    }

    /**
     * Recursive menu: after each action, show the menu again.
     * Choosing Exit is the base case that stops the recursion.
     */
    private void runMenu() {
        printMenu();
        int choice = readInteger("Choose an option (1-7): ");

        switch (choice) {
            case 1:
                addCourse();
                break;
            case 2:
                displayCourses();
                break;
            case 3:
                searchCourse();
                break;
            case 4:
                displayTotalUnits();
                break;
            case 5:
                saveCourses();
                break;
            case 6:
                loadCourses();
                break;
            case 7:
                System.out.println("Goodbye.");
                return; // Base case: no further recursive call.
            default:
                System.out.println(
                    "Invalid menu choice. Enter a number from 1 to 7."
                );
        }

        runMenu();
    }

    private void printMenu() {
        System.out.println("\n=== Student Course Management System ===");
        System.out.println("1. Add Course");
        System.out.println("2. View All Courses");
        System.out.println("3. Search Course by Code");
        System.out.println("4. Compute Total Units");
        System.out.println("5. Save to File");
        System.out.println("6. Load from File");
        System.out.println("7. Exit Program");
    }

    private void addCourse() {
        System.out.println("\n--- Add Course ---");

        String code = Course.normalizeCode(readNonBlank("Course code: "));
        String title = readNonBlank("Course title: ");
        int units = readPositiveInteger("Credit units: ");

        try {
            Course.validate(code, title, units);

            if (findCourse(code) != null) {
                System.out.println(
                    "A course with code " + code + " is already recorded."
                );
                return;
            }

            courses.add(new Course(code, title, units));
            System.out.println("Course added successfully.");
        } catch (IllegalArgumentException exception) {
            System.out.println(
                "Could not add course: " + exception.getMessage()
            );
        }
    }

    private void displayCourses() {
        System.out.println("\n--- Recorded Courses ---");

        if (courses.isEmpty()) {
            System.out.println("No courses have been recorded.");
            return;
        }

        System.out.printf(
            "%-14s %-36s %5s%n",
            "COURSE CODE", "COURSE TITLE", "UNITS"
        );
        System.out.println("------------------------------------------------------------");

        for (Course course : courses) {
            System.out.printf(
                "%-14s %-36s %5d%n",
                course.getCode(),
                course.getTitle(),
                course.getUnits()
            );
        }
    }

    private void searchCourse() {
        System.out.println("\n--- Search Course ---");
        String code = Course.normalizeCode(readNonBlank("Enter course code: "));
        Course course = findCourse(code);

        if (course == null) {
            System.out.println("No course found with code " + code + ".");
        } else {
            System.out.println(
                "Course found: " + course.getCode()
                    + " — " + course.getTitle()
                    + " (" + course.getUnits() + " units)"
            );
        }
    }

    private Course findCourse(String code) {
        String normalizedCode = Course.normalizeCode(code);

        for (Course course : courses) {
            if (course.getCode().equals(normalizedCode)) {
                return course;
            }
        }

        return null;
    }

    private void displayTotalUnits() {
        int total = 0;

        for (Course course : courses) {
            total += course.getUnits();
        }

        System.out.println("Total credit units: " + total);
    }

    private void saveCourses() {
        try (BufferedWriter writer = Files.newBufferedWriter(
                dataFile, StandardCharsets.UTF_8)) {

            for (Course course : courses) {
                writer.write(course.toFileLine());
                writer.newLine();
            }

            System.out.println(
                "Saved " + courses.size() + " course(s) to "
                    + dataFile.toAbsolutePath() + "."
            );
        } catch (IOException | SecurityException exception) {
            System.out.println("Could not save courses: " + exception.getMessage());
        }
    }

    private void loadCourses() {
        List<Course> loadedCourses = new ArrayList<>();
        int skipped = 0;

        try (BufferedReader reader = Files.newBufferedReader(
                dataFile, StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Course course = Course.fromFileLine(line);

                    if (containsCode(loadedCourses, course.getCode())) {
                        skipped++;
                        System.out.println(
                            "Skipped duplicate course on file line "
                                + lineNumber + "."
                        );
                    } else {
                        loadedCourses.add(course);
                    }
                } catch (IllegalArgumentException exception) {
                    skipped++;
                    System.out.println(
                        "Skipped invalid file line " + lineNumber + ": "
                            + exception.getMessage()
                    );
                }
            }

            // Replace the current list with the valid courses from the file.
            courses.clear();
            courses.addAll(loadedCourses);

            System.out.println(
                "Loaded " + courses.size() + " course(s) from "
                    + dataFile.toAbsolutePath()
                    + ". Invalid/duplicate lines skipped: " + skipped + "."
            );
        } catch (IOException | SecurityException exception) {
            System.out.println("Could not load courses: " + exception.getMessage());
        }
    }

    private boolean containsCode(List<Course> list, String code) {
        String normalizedCode = Course.normalizeCode(code);

        for (Course course : list) {
            if (course.getCode().equals(normalizedCode)) {
                return true;
            }
        }

        return false;
    }

    private String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("This field cannot be blank. Please try again.");
        }
    }

    private int readInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private int readPositiveInteger(String prompt) {
        while (true) {
            int value = readInteger(prompt);

            if (value > 0) {
                return value;
            }

            System.out.println("Credit units must be greater than zero.");
        }
    }
}