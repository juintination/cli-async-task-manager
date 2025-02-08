import validator.TaskValidator;
import task.Task;
import task.state.Urgent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class AsyncTaskManager {

    private static AsyncTaskManager taskManager;

    private final String INPUT_ERROR_MESSAGE = "Error has occurred. Please enter it again.";

    private String name;
    private BufferedReader br;
    private List<Task> tasks;

    private AsyncTaskManager() {
        name = "";
        br = new BufferedReader(new InputStreamReader(System.in));
        tasks = new ArrayList<>();
    }

    public static AsyncTaskManager getInstance() {
        if (taskManager == null) {
            taskManager = new AsyncTaskManager();
        }
        return taskManager;
    }

    private void offerTask(Task task) {
        tasks.add(task);
    }

    private void deleteTask(Task task) {
        tasks.remove(task);
    }

    public void start() {
        inputNameAsync().thenCompose(v ->
                AsyncFileManager.nameFileExistsAsync(name).thenCompose(isExist -> {
                    if (isExist) {
                        return AsyncFileManager.loadTasksAsync(name, tasks);
                    } else {
                        return AsyncFileManager.createTasksFileAsync(name);
                    }
                })
        ).thenRun(() -> {
            while (true) {
                printWelcomeMessage();
                byte choicedNumber = inputChoiceAsync().join();
                if (!taskLoop(choicedNumber)) {
                    break;
                }
            }
        }).join();
    }

    private CompletableFuture<Void> inputNameAsync() {
        return CompletableFuture.runAsync(() -> {
            while (true) {
                System.out.print("Please enter your name: ");
                try {
                    String name = br.readLine();
                    TaskValidator.validateName(name);
                    this.name = name;
                    break;
                } catch (IOException e) {
                    System.out.println(INPUT_ERROR_MESSAGE);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    private void printWelcomeMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nHi, ").append(name).append("! ")
                .append("Welcome to CLI Task Manager. What would you like to do?\n")
                .append("1. View tasks\n")
                .append("2. Add a task\n")
                .append("3. Modify tasks\n")
                .append("4. Remove a task\n")
                .append("0. Exit");
        System.out.println(sb);
    }

    private CompletableFuture<Byte> inputChoiceAsync() {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                try {
                    System.out.print("Enter the number: ");
                    String choice = br.readLine();
                    TaskValidator.validateChoice(choice);
                    return Byte.parseByte(choice);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    System.out.println(INPUT_ERROR_MESSAGE);
                }
            }
        });
    }

    private boolean taskLoop(byte choicedNumber) {
        boolean isContinue = true;
        switch (choicedNumber) {
            case 1:
                viewTasks();
                break;
            case 2:
                addTaskAsync();
                break;
            case 3:
                modifyTasksAsync();
                break;
            case 4:
                removeTaskAsync();
                break;
            case 0:
                System.out.println("Goodbye, " + name + "!");
                isContinue = false;
                break;
        }
        return isContinue;
    }

    private void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("There are no tasks to view.");
            return;
        }
        printMoreViewMessage();
        inputChoiceAsync().thenAccept(this::viewTaskLoop).join();
    }

    private void printMoreViewMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("What would you like to do?\n")
                .append("1. View every tasks\n")
                .append("2. View pending tasks\n")
                .append("3. View urgent tasks\n")
                .append("4. View completed tasks\n")
                .append("0. Go back");
        System.out.println(sb);
    }

    private void viewTaskLoop(byte choicedNumber) {
        switch (choicedNumber) {
            case 1:
                viewEveryTasks();
                break;
            case 2:
                viewPendingTasks();
                break;
            case 3:
                viewUrgentTasks();
                break;
            case 4:
                viewCompletedTasks();
                break;
            case 0:
                break;
        }
    }

    private void viewEveryTasks() {
        System.out.println("Every tasks...");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            System.out.println(i + 1 + ". " + task.getInfo());
        }
    }

    private void viewPendingTasks() {
        System.out.println("Pending tasks...");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (!task.isDone()) {
                System.out.println(i + 1 + ". " + task.getInfo());
            }
        }
    }

    private void viewUrgentTasks() {
        System.out.println("Urgent tasks...");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getState() instanceof Urgent) {
                System.out.println(i + 1 + ". " + task.getInfo());
            }
        }
    }

    private void viewCompletedTasks() {
        System.out.println("Completed tasks...");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isDone()) {
                System.out.println(i + 1 + ". " + task.getInfo());
            }
        }
    }

    private void addTaskAsync() {
        inputTaskTitleAsync()
                .thenCompose(title -> inputTaskDescriptionAsync().thenApply(description -> new Task(title, description)))
                .thenAccept(task -> {
                    offerTask(task);
                    AsyncFileManager.appendTaskToFileAsync(name, task);
                })
                .join();
    }


    private CompletableFuture<String> inputTaskTitleAsync() {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                System.out.print("Enter the title of the task: ");
                try {
                    String title = br.readLine();
                    TaskValidator.validateTaskTitle(title);
                    return title;
                } catch (IOException e) {
                    System.out.println(INPUT_ERROR_MESSAGE);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    private CompletableFuture<String> inputTaskDescriptionAsync() {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                System.out.print("Enter the description of the task: ");
                try {
                    String description = br.readLine();
                    TaskValidator.validateTaskDescription(description);
                    return description;
                } catch (IOException e) {
                    System.out.println(INPUT_ERROR_MESSAGE);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    private void printAllTasks() {
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            System.out.println(i + 1 + ". " + task.getInfo());
        }
    }

    private void modifyTasksAsync() {
        if (tasks.isEmpty()) {
            System.out.println("There are no tasks to modify.");
            return;
        }
        System.out.println("Which task would you like to modify?");
        printAllTasks();
        System.out.println("0. Go back");
        inputTaskIndexAsync().thenAccept(index -> {
            if (index == -1) {
                return;
            }
            Task task = tasks.get(index);
            printMoreModifyMessage();
            inputChoiceAsync().thenAccept(choicedNumber -> {
                modifyTaskLoop(task, choicedNumber);
                AsyncFileManager.updateTasksFileAsync(name, tasks);
            }).join();
        }).join();
    }

    private CompletableFuture<Integer> inputTaskIndexAsync() {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                System.out.print("Enter the number of the task: ");
                try {
                    String index = br.readLine();
                    TaskValidator.validateTaskIndex(index, tasks.size());
                    return Integer.parseInt(index) - 1;
                } catch (IOException e) {
                    System.out.println(INPUT_ERROR_MESSAGE);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    private void printMoreModifyMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("What would you like to do with the task?\n")
                .append("1. Change status\n")
                .append("2. Change priority\n")
                .append("3. Change title\n")
                .append("4. Change description\n")
                .append("0. Go back");
        System.out.println(sb);
    }

    private void modifyTaskLoop(Task task, byte choicedNumber) {
        switch (choicedNumber) {
            case 1:
                changeDone(task);
                break;
            case 2:
                changePriority(task);
                break;
            case 3:
                changeTitle(task);
                break;
            case 4:
                changeDescription(task);
                break;
            case 0:
                break;
        }
    }

    private void changeDone(Task task) {
        task.changeDone();
    }

    private void changePriority(Task task) {
        try {
            TaskValidator.validateIsPendingOrUrgent(task);
            task.changePriority();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void changeTitle(Task task) {
        inputTaskTitleAsync().thenAccept(task::setTitle).join();
    }

    private void changeDescription(Task task) {
        inputTaskDescriptionAsync().thenAccept(task::setDescription).join();
    }

    private void removeTaskAsync() {
        if (tasks.isEmpty()) {
            System.out.println("There are no tasks to remove.");
            return;
        }
        System.out.println("Which task would you like to remove?");
        printAllTasks();
        System.out.println("0. Go back");
        inputTaskIndexAsync().thenAccept(index -> {
            if (index == -1) {
                return;
            }
            deleteTask(tasks.get(index));
            AsyncFileManager.updateTasksFileAsync(name, tasks);
        }).join();
    }

}
