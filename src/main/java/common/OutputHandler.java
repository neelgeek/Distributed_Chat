package common;

import java.sql.Timestamp;

/**
 * Provides static methods for uniform logging pattern across the project
 */
public class OutputHandler {
    /**
     * Prints to the console along with the current timestamp of the system.
     *
     * @param output String to be printed with the current timestamp
     */
    public static void printWithTimestamp(String output) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.format("%s : %s %n", timestamp, output);
    }
}
