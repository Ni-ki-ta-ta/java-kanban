package ru.practicum.model;

public class Subtask extends Task{
    private int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    public Subtask(String name, String description, int id, TaskStatus status, int epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "ru.practicum.model.Subtask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", type=" + type +
                ", epicId=" + epicId +
                '}';
    }
}
