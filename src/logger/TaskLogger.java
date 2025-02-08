package logger;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class TaskLogger {

    private static final String LOGS_DIRECTORY = "logs";

    private final String logFileName;
    private final ScheduledExecutorService scheduler;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TaskLogger(String name) {
        this.logFileName = LOGS_DIRECTORY + "/" + name + ".txt";
        ensureLogsDirectoryExists();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    private void ensureLogsDirectoryExists() {
        File directory = new File(LOGS_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            log("Current Time: " + LocalDateTime.now().format(dtf));
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void log(String message) {
        CompletableFuture.runAsync(() -> {
            try (FileWriter fw = new FileWriter(logFileName, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                out.println(LocalDateTime.now().format(dtf) + " - " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
