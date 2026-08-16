package link.star_dust.consolefix.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Persists each filtered (hidden) console message to its own file under the
 * configured directory, named {@code yyyy-MM-dd-N-filtered.log} — e.g.
 * {@code 2026-07-03-1-filtered.log} — where {@code N} is a running sequence
 * number that never collides with an existing file for the same date.
 *
 * <p>The full original message text is stored verbatim in the file, so no
 * timestamp/sequence prefix is needed inside the content. Shared by every
 * platform; each platform owns its own instance gated by the
 * {@code Log-Filtered-Messages} config option.
 */
public class FilteredLogWriter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final File logDir;
    private long sequence;

    /**
     * Prepare the log directory.
     *
     * @param logDir target directory, e.g. {@code logs}
     * @throws IOException when the directory cannot be created
     */
    public FilteredLogWriter(File logDir) throws IOException {
        this.logDir = logDir;
        if (!logDir.exists() && !logDir.mkdirs()) {
            throw new IOException("Could not create log directory: " + logDir);
        }
    }

    /**
     * Write one filtered message to its own dated, numbered file.
     */
    public synchronized void write(String message) {
        String date = LocalDate.now().format(DATE_FORMAT);
        File file = nextAvailableFile(date);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(message);
        } catch (IOException e) {
            // best-effort audit log: a failed write must never break filtering
        }
    }

    private File nextAvailableFile(String date) {
        File file;
        do {
            file = new File(logDir, date + "-" + (++sequence) + "-filtered.log");
        } while (file.exists());
        return file;
    }
}
