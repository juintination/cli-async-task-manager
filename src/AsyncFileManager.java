import task.Task;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncFileManager {

    private static final String TASKS_DIRECTORY = "tasks";

    static {
        ensureFileDirectoryExists();
    }

    private static void ensureFileDirectoryExists() {
        File directory = new File(TASKS_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public static String getTasksDirectory() {
        return TASKS_DIRECTORY;
    }

    public static CompletableFuture<Boolean> nameFileExistsAsync(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader ignored = new BufferedReader(
                    new FileReader(TASKS_DIRECTORY + "/" + name + ".txt"))) {
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    public static CompletableFuture<Void> loadTasksAsync(String name, List<Task> tasks) {
        return CompletableFuture.runAsync(() -> {
            String filePath = TASKS_DIRECTORY + "/" + name + ".txt";
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                if (tasks.isEmpty()) {
                    String line;
                    tasks.clear();
                    while ((line = reader.readLine()) != null) {
                        String[] taskData = line.split(",");
                        if (taskData.length == 3) {
                            Task task = new Task(taskData[0], taskData[1]);
                            if (taskData[2].equals("Completed")) {
                                task.changeDone();
                            } else if (taskData[2].equals("Urgent")) {
                                task.changePriority();
                            }
                            tasks.add(task);
                        }
                    }
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to load tasks: " + e.getMessage());
            }
        });
    }

    public static CompletableFuture<Void> createTasksFileAsync(String name) {
        return CompletableFuture.runAsync(() -> {
            String filePath = TASKS_DIRECTORY + "/" + name + ".txt";
            try (FileOutputStream fos = new FileOutputStream(filePath, true);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                writer.flush();
                fos.getFD().sync();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to create " + name + ".txt: " + e.getMessage());
            }
        });
    }

    public static CompletableFuture<Void> appendTaskToFileAsync(String name, Task task) {
        return CompletableFuture.runAsync(() -> {
            String filePath = TASKS_DIRECTORY + "/" + name + ".txt";
            try (FileOutputStream fos = new FileOutputStream(filePath, true);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                writer.write(task.getTitle() + "," + task.getDescription() + ","
                        + task.getState().getClass().getSimpleName() + "\n");
                writer.flush();
                fos.getFD().sync();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to append task to " + name + ".txt: " + e.getMessage());
            }
        });
    }

    public static CompletableFuture<Void> updateTasksFileAsync(String name, List<Task> tasks) {
        return CompletableFuture.runAsync(() -> {
            String filePath = TASKS_DIRECTORY + "/" + name + ".txt";
            try (FileOutputStream fos = new FileOutputStream(filePath);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                for (Task task : tasks) {
                    writer.write(task.getTitle() + "," + task.getDescription() + "," + task.getState().getClass().getSimpleName() + "\n");
                }
                writer.flush();
                fos.getFD().sync();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to update " + name + ".txt: " + e.getMessage());
            }
        });
    }
}
