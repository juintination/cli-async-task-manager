import task.Task;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class TaskFileWatcher implements Runnable {

    private final String name;
    private final List<Task> tasks;
    private final Path tasksDir;

    public TaskFileWatcher(String name, List<Task> tasks) {
        this.name = name;
        this.tasks = tasks;
        this.tasksDir = Paths.get(AsyncFileManager.getTasksDirectory());
    }

    @Override
    public void run() {
        try (WatchService watcher = tasksDir.getFileSystem().newWatchService()) {
            tasksDir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watcher.take();
                for (WatchEvent<?> events : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = events.kind();

                    if (kind == OVERFLOW) continue;

                    WatchEvent<Path> event = (WatchEvent<Path>) events;
                    Path fileName = event.context();

                    if (fileName.toString().equals(name + ".txt")) {
                        AsyncFileManager.loadTasksAsync(name, tasks)
                                .exceptionally(e -> {
                                    throw new RuntimeException("Failed to load tasks: " + e.getMessage());
                                })
                                .join();
                    }
                }
                if (!key.reset()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Error watching tasks directory: " + e.getMessage());
        }
    }

}
