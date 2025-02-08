package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskLogger {

    private static final String LOGS_DIRECTORY = "logs";

    private ScheduledExecutorService scheduler;
    private FileChannel lockChannel;
    private FileLock fileLock;
    private final String name;
    private final String logFileName;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TaskLogger(String name) {
        this.logFileName = LOGS_DIRECTORY + "/" + name + ".txt";
        this.name = name;
        ensureLogsDirectoryExists();
    }

    private void ensureLogsDirectoryExists() {
        File directory = new File(LOGS_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public void start() {
        try {
            File lockFile = new File(LOGS_DIRECTORY + "/" + name + ".lock");
            lockFile.createNewFile();
            lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            fileLock = lockChannel.tryLock();
            if (fileLock == null) {
                System.out.println("Periodic time logging is already active for " + name + ". Skipping periodic time logging in this instance.");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        scheduler = Executors.newScheduledThreadPool(1);
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
        if (scheduler != null) {
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
        if (fileLock != null) {
            try {
                fileLock.release();
                lockChannel.close();
                File lockFile = new File(LOGS_DIRECTORY + "/" + name + ".lock");
                lockFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
