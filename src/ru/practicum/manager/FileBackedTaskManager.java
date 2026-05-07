package ru.practicum.manager;

import ru.practicum.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public Task updateTask(Task task) {
        Task updated = super.updateTask(task);
        save();
        return updated;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updated = super.updateEpic(epic);
        save();
        return updated;
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask created = super.createSubtask(subtask);
        save();
        return created;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updated = super.updateSubtask(subtask);
        save();
        return updated;
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            writer.write("id,type,name,status,description,duration,startTime,epic\n");

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }

            writer.newLine();

            String history = getHistory().stream()
                    .map(task -> String.valueOf(task.getId()))
                    .collect(java.util.stream.Collectors.joining(","));

            writer.write(history);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения", e);
        }
    }

    private String toString(Task task) {

        String epicId = "";

        if (task.getType() == TaskType.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        String duration = "";

        if (task.getDuration() != null) {
            duration = String.valueOf(task.getDuration().toMinutes());
        }

        String startTime = "";

        if (task.getStartTime() != null) {
            startTime = task.getStartTime().toString();
        }

        return task.getId() + "," +
                task.getType() + "," +
                task.getName() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                duration + "," +
                startTime + "," +
                epicId;
    }

    private static Task fromString(String value) {

        String[] fields = value.trim().split(",", -1);
        int id = Integer.parseInt(fields[0].trim());
        TaskType type = TaskType.valueOf(fields[1].trim());
        String name = fields[2].trim();
        TaskStatus status = TaskStatus.valueOf(fields[3].trim());
        String description = fields[4].trim();

        java.time.Duration duration = null;

        if (!fields[5].isBlank()) {
            duration = java.time.Duration.ofMinutes(
                    Long.parseLong(fields[5].trim())
            );
        }

        java.time.LocalDateTime startTime = null;

        if (!fields[6].isBlank()) {
            startTime = java.time.LocalDateTime.parse(fields[6].trim());
        }

        Task task;

        switch (type) {

            case TASK:
                task = new Task(name, description, id, status);
                break;

            case EPIC:
                task = new Epic(name, description, id, status);
                break;

            case SUBTASK:

                if (fields.length <= 7 || fields[7].isBlank()) {
                    throw new IllegalArgumentException(
                            "Для подзадачи не указан epicId"
                    );
                }

                int epicId = Integer.parseInt(fields[7].trim());

                task = new Subtask(
                        name,
                        description,
                        id,
                        status,
                        epicId
                );

                break;

            default:
                throw new IllegalArgumentException(
                        "Неизвестный тип задачи"
                );
        }

        task.setDuration(duration);
        task.setStartTime(startTime);

        return task;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = java.nio.file.Files.readString(file.toPath());
            String[] lines = content.split("\\R");

            int maxId = 0;

            int emptyLineIndex = -1;

            for (int i = 1; i < lines.length; i++) {

                if (lines[i].isBlank()) {
                    emptyLineIndex = i;
                    break;
                }

                Task task = fromString(lines[i]);

                int id = task.getId();

                if (id > maxId) {
                    maxId = id;
                }

                switch (task.getType()) {

                    case TASK:
                        manager.tasks.put(id, task);

                        if (task.getStartTime() != null) {
                            manager.prioritizedTasks.add(task);
                        }

                        break;

                    case EPIC:
                        manager.epics.put(id, (Epic) task);
                        break;

                    case SUBTASK:
                        manager.subtasks.put(id, (Subtask) task);

                        if (task.getStartTime() != null) {
                            manager.prioritizedTasks.add(task);
                        }

                        break;
                }
            }

            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic);
                manager.updateEpicTime(epic);
            }

            String historyLine = "";

            if (emptyLineIndex != -1) {

                for (int i = emptyLineIndex + 1; i < lines.length; i++) {

                    if (!lines[i].isBlank()) {
                        historyLine = lines[i];
                        break;
                    }
                }
            }

            java.util.List<Integer> historyIds = historyFromString(historyLine);

            for (Integer id : historyIds) {

                Task task = manager.tasks.get(id);

                if (task == null) {
                    task = manager.epics.get(id);
                }

                if (task == null) {
                    task = manager.subtasks.get(id);
                }

                if (task != null) {
                    manager.historyManager.add(task);
                }
            }

            manager.taskCounter = maxId + 1;

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки", e);
        }

        return manager;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    private static java.util.List<Integer> historyFromString(String value) {

        if (value == null || value.isBlank()) {
            return java.util.List.of();
        }

        return java.util.Arrays.stream(value.split(","))
                .map(Integer::parseInt)
                .toList();
    }
}