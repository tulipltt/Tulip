package io.github.tulipltt.tulip.api;

import com.google.common.io.Resources;
import io.github.tulipltt.tulip.core.TulipKt;
import io.github.tulipltt.tulip.report.TulipReportKt;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * The TulipApi class provides the main interface for running Tulip benchmarks and generating
 * reports.
 */
@Command(
        name = "tulip-runtime",
        mixinStandardHelpOptions = true,
        version = TulipApi.VERSION,
        description = "Processes either a configuration file or a report file.")
public class TulipApi implements Callable<Integer> {

    /** Private constructor */
    TulipApi() {}

    /** The version string of the Tulip API. */
    public static final String VERSION = "2.3.0";

    /** The number of unique actions available in the benchmarking process. */
    public static final int NUM_ACTIONS = 101;

    /**
     * Runs the Tulip benchmarking process. This method initializes the configuration, runs the
     * benchmarks, and creates an HTML report.
     *
     * @param text The name of the JSONC benchmark configuration file, or a JSONC string starting
     *     with {.
     * @param userFactory A TulipUserFactory object responsible for creating Tulip users.
     * @return The name of the output file containing the benchmarking results.
     */
    public static String runTulip(String text, TulipUserFactory userFactory) {
        // Record the start time in nanoseconds
        long startTime = System.nanoTime();
        System.setProperty("python.console.encoding", "UTF-8");
        String outputFilename = TulipKt.initConfig(text);
        TulipKt.runBenchmarks(userFactory);
        // createHtmlReport(outputFilename, text);

        // Calculate and display the elapsed time
        long durationNano = System.nanoTime() - startTime;
        displayElapsedTime(durationNano);

        return outputFilename;
    }

    /**
     * Runs the Tulip benchmarking process. This method initializes the configuration, runs the
     * benchmarks, and returns the output filename.
     *
     * @param text The name of the JSONC benchmark configuration file, or a JSONC string starting
     *     with {.
     * @return The name of the output file containing the benchmarking results.
     */
    public static String runTulip(String text) {
        TulipUserFactory userFactory = new TulipUserFactory();
        return runTulip(text, userFactory);
    }

    /**
     * Creates an HTML report from the benchmarking output.
     *
     * @param outputFilename The name of the output file containing the benchmarking results.
     */
    public static void generateReport(String outputFilename) {
        TulipReportKt.createHtmlReport(outputFilename);
    }

    /**
     * Reads a resource file from the classpath and returns its content as a string.
     *
     * @param fileName The name of the resource file to read, relative to the classpath.
     * @return The content of the resource file as a string.
     * @throws IOException If the resource cannot be read.
     */
    public static String readResource(final String fileName) throws IOException {
        return Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8);
    }

    /**
     * isUtf8Terminal
     *
     * @return true if the terminal supports UTF-8 encoding
     */
    public static boolean isUtf8Terminal() {
        // Condition A: Check if the OS is Windows
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            return true;
        }

        // Condition B: Check if Code Page 65001 (UTF-8) is active
        try {
            // We run 'chcp' to get the active console code page
            Process process = new ProcessBuilder("cmd", "/c", "chcp").start();

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                String line = reader.readLine();
                // 'chcp' output is usually: "Active code page: 65001"
                return line != null && line.contains("65001");
            }
        } catch (Exception e) {
            // If the command fails, we assume the condition isn't met
            return false;
        }
    }

    /**
     * Create a JSON string containing JVM runtime information
     *
     * @return JSON String
     */
    public static String getJavaInformation() {
        var s = "{ \"jvm.system.properties\": {";
        s += "\"java.vendor\"" + ":\"" + System.getProperty("java.vendor") + "\", ";
        s += "\"java.version\"" + ":\"" + System.getProperty("java.version") + "\", ";
        s +=
                "\"java.runtime.version\""
                        + ":\""
                        + System.getProperty("java.runtime.version")
                        + "\", ";
        s += "\"os.name\"" + ":\"" + System.getProperty("os.name") + "\", ";
        s += "\"os.arch\"" + ":\"" + System.getProperty("os.arch") + "\"}, ";
        s += " \"jvm.runtime.options\": ";
        var jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        s +=
                "["
                        + jvmArgs.stream()
                                .distinct()
                                .map(arg -> "\"" + arg.replace("\\", "/") + "\"")
                                .collect(Collectors.joining(", "))
                        + "]";
        s += " }";
        return s;
    }

    /**
     * displayElapsedTime
     *
     * @param durationNano total elapsed time in nano seconds
     */
    public static void displayElapsedTime(long durationNano) {
        double durationSeconds = (double) durationNano / 1_000_000_000.0;

        // Calculate hours, minutes, and seconds for the hh:mm:ss format
        long totalSeconds = durationNano / 1_000_000_000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        System.out.println("\nElapsed time (hh:mm:ss): " + formattedTime);
    }

    @ArgGroup(multiplicity = "1")
    ExclusiveOptions exclusiveOptions;

    static class ExclusiveOptions {
        @Option(
                names = {"-c", "--config"},
                description = "Path to the config.json file.",
                required = true)
        File configFile;

        @Option(
                names = {"-r", "--report"},
                description = "Path to the output.json file.",
                required = true)
        File reportFile;
    }

    @Override
    public Integer call() throws Exception {
        if (exclusiveOptions.configFile != null) {
            runTulip(exclusiveOptions.configFile.getAbsolutePath());
        } else {
            System.out.println(
                    "Generating report from: " + exclusiveOptions.reportFile.getAbsolutePath());
            generateReport(exclusiveOptions.reportFile.getAbsolutePath().replace("\\", "/"));
        }
        return 0;
    }

    /**
     * The entry point of the application.
     *
     * @param args command-line options
     */
    public static void main(String... args) {
        int exitCode = new CommandLine(new TulipApi()).execute(args);
        System.exit(exitCode);
    }
}
