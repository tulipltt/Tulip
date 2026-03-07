package io.github.tulipltt.tulip.api;

import io.github.tulipltt.tulip.core.Console;
import java.util.List;

/** API class to access Tulip's Console object. */
public class TulipConsole {

    /** Private constructor */
    TulipConsole() {}

    /**
     * Outputs a string to the Tulip console. This method serves as a wrapper for the Console.put()
     * method, providing a simplified interface for console output in the Tulip framework.
     *
     * @param text The string to be output to the console.
     */
    public static void put(String text) {
        Console.put(text);
    }

    /**
     * Outputs a list of strings to the Tulip console. This method serves as a wrapper for the
     * Console.put() method, providing a simplified interface for console output in the Tulip
     * framework.
     *
     * @param list The string to be output to the console.
     */
    public static void put(List<String> list) {
        Console.put(list);
    }
}
