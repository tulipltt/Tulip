import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A Java program that modifies the contents of a text file.
 *
 * It reads all lines, preserves the original first line, and inserts a new
 * line containing "// hi" immediately after it.
 *
 * Usage: java FileModifier <path/to/your/file.txt>
 */
public class FileModifier {

    // The text to be inserted on the new line
    private static final String NEW_LINE_CONTENT = "package io.github.tulipltt.tulip.report;";

    public static void main(String[] args) {
        // 1. Check for command-line argument
        if (args.length != 1) {
            System.err.println("Usage: java FileModifier <path/to/your/file.txt>");
            System.exit(1);
        }

        final String filePath = args[0];
        final Path path = Path.of(filePath);

        try {
            // 2. Read all existing lines from the file
            List<String> lines = Files.readAllLines(path);

            if (lines.isEmpty()) {
                System.out.println("File is empty. No modification performed.");
                return;
            }

            // The list currently contains:
            // [0] -> Original first line content
            // [1] -> Original second line content (if present)
            
            // 3. Insert the new line after the first line.
            // By inserting at index 1, the original line at index 1 is shifted to index 2.
            // The structure becomes:
            // [0] -> Original first line content
            // [1] -> "// hi" (new content)
            // [2] -> Original second line content (if present)
            lines.add(0, NEW_LINE_CONTENT);

            // 4. Write the modified list of lines back to the same file (overwriting it)
            Files.write(path, lines);

            System.out.println("Successfully modified file: " + filePath);
            System.out.println("Add line '" + NEW_LINE_CONTENT + "' as the first line.");

        } catch (IOException e) {
            System.err.println("An error occurred while accessing the file: " + filePath);
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
        } catch (SecurityException e) {
             System.err.println("Permission denied: Cannot read or write to file: " + filePath);
             e.printStackTrace();
        }
    }
}