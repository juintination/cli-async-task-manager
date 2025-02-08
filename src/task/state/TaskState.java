package task.state;

import task.Task;

public abstract class TaskState {

    public abstract String getInfo(Task task);

    public abstract void changeDone(Task task);

    public abstract void changePriority(Task task);

}
