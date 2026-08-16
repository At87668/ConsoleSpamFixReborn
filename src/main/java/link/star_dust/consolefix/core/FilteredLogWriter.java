package link.star_dust.consolefix.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Persists filtered (hidden) console messages to a per-session file under
 * the configured directory, named {@code yyyy-MM-dd-N-filtered.log} — e.g.
 * {@code 2026-07-03-1-filtered.log}. All messages hidden during one process
 * (server run) are appended to the same file; {@code N} is the next free
 * sequence number for that date, so a restart on the same day opens a fresh
 * numbered file instead of overwriting the previous session's log.
 *
 * <p>The full original message text is stored verbatim, one entry per line,
 * with no timestamp/sequence prefix inside the file — the date and sequence
 * already live in the file name. Shared by every platform; each platform
 * owns its own instance gated by the {@code Log-Filtered-Messages} config
 * option.
 */
public class FilteredLogWriter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PrintWriter writer;

    /**
     * Open (append to) the next available dated, numbered file.
     *
     * @param logDir target directory, e.g. {@code logs}
     * @throws IOException when the directory or file cannot be created
     */
    public FilteredLogWriter(File logDir) throws IOException {
        if (!logDir.exists() && !logDir.mkdirs()) {
            throw new IOException("Could not create log directory: " + logDir);
        }
        File logFile = nextAvailableFile(logDir, LocalDate.now().format(DATE_FORMAT));
        this.writer = new PrintWriter(new FileWriter(logFile, true));
    }

    /**
     * Append one filtered message to the current session file, prefixed with
     * the wall-clock time ({@code [HH:mm:ss] }) so entries are easy to scan.
     */
    public synchronized void write(String message) {
        String time = LocalTime.now().format(TIME_FORMAT);
        writer.println("[" + time + "] " + message);
        writer.flush();
    }

    /**
     * Close the underlying writer.
     */
    public synchronized void close() {
        writer.close();
    }

    private static File nextAvailableFile(File logDir, String date) {
        int max = 0;
        File[] files = logDir.listFiles((dir, name) -> name.startsWith(date + "-") && name.endsWith("-filtered.log"));
        if (files != null) {
            for (File file : files) {
                int value = parseSequence(file.getName(), date);
                if (value > max) max = value;
            }
        }
        return new File(logDir, date + "-" + (max + 1) + "-filtered.log");
    }

    private static int parseSequence(String name, String date) {
        try {
            String middle = name.substring(date.length() + 1, name.length() - "-filtered.log".length());
            return Integer.parseInt(middle);
        } catch (Exception e) {
            return 0;
        }
    }
}
