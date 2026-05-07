package ru.practicum.manager;

import ru.practicum.model.*;

import java.util.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;
    protected int taskCounter;
    protected final Set<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.taskCounter = 1;
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>(
                (task1, task2) -> {

                    if (task1.getStartTime() == null &&
                            task2.getStartTime() == null) {

                        return Integer.compare(task1.getId(), task2.getId());
                    }

                    if (task1.getStartTime() == null) {
                        return 1;
                    }

                    if (task2.getStartTime() == null) {
                        return -1;
                    }

                    int compare = task1.getStartTime()
                            .compareTo(task2.getStartTime());

                    if (compare == 0) {
                        return Integer.compare(task1.getId(), task2.getId());
                    }

                    return compare;
                }
        );
    }

    private int generateId() {
        return taskCounter++;
    }

    protected void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            TaskStatus taskStatus = subtask.getStatus();
            if (taskStatus != TaskStatus.NEW) {
                allNew = false;
            }
            if (taskStatus != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    protected void updateEpicTime(Epic epic) {
        List<Subtask> epicSubtasks = getSubtasksByEpicId(epic.getId());

        if (epicSubtasks.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        Duration totalDuration = Duration.ZERO;

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        for (Subtask subtask : epicSubtasks) {

            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }

            if (subtask.getStartTime() != null) {

                if (startTime == null ||
                        subtask.getStartTime().isBefore(startTime)) {

                    startTime = subtask.getStartTime();
                }

                LocalDateTime subtaskEnd = subtask.getEndTime();

                if (subtaskEnd != null &&
                        (endTime == null || subtaskEnd.isAfter(endTime))) {

                    endTime = subtaskEnd;
                }
            }
        }

        epic.setDuration(totalDuration);
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
    }

    private boolean isTasksIntersect(Task first, Task second) {

        if (first.getStartTime() == null ||
                second.getStartTime() == null) {

            return false;
        }

        return first.getStartTime().isBefore(second.getEndTime())
                && second.getStartTime().isBefore(first.getEndTime());
    }

    private boolean hasIntersections(Task task) {

        return prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .anyMatch(t -> isTasksIntersect(task, t));
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
        prioritizedTasks.removeIf(task -> task.getType() == TaskType.TASK);
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) return null;

        task.setId(generateId());
        if (hasIntersections(task)) {
            throw new IllegalArgumentException(
                    "Задача пересекается с существующими"
            );
        }

        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            prioritizedTasks.remove(tasks.get(task.getId()));

            if (hasIntersections(task)) {
                throw new IllegalArgumentException(
                        "Задача пересекается с существующими"
                );
            }

            tasks.put(task.getId(), task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }

            return task;
        }
        return null;
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);

        if (task != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }

        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        prioritizedTasks.removeIf(task ->
                task.getType() == TaskType.SUBTASK
        );

        subtasks.clear();
        epics.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) return null;

        epic.setId(generateId());
        epic.setStatus(TaskStatus.NEW);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic != null && epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            return existingEpic;
        }
        return null;
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);

                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }

                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }

            epics.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }

        subtasks.clear();
        prioritizedTasks.removeIf(task -> task.getType() == TaskType.SUBTASK);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) return null;

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);

        if (epic == null || subtask.getId() == epicId) {
            return null;
        }

        subtask.setId(generateId());
        if (hasIntersections(subtask)) {
            throw new IllegalArgumentException(
                    "Подзадача пересекается с существующими"
            );
        }

        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        updateEpicTime(epic);
        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null) return null;

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);

        if (epic == null || !subtasks.containsKey(subtask.getId())) {
            return null;
        }

        prioritizedTasks.remove(subtasks.get(subtask.getId()));

        if (hasIntersections(subtask)) {
            throw new IllegalArgumentException(
                    "Подзадача пересекается с существующими"
            );
        }

        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        updateEpicStatus(epic);
        updateEpicTime(epic);
        return subtask;
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
            subtasks.remove(id);
            historyManager.remove(id);
            prioritizedTasks.remove(subtask);
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);

        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}
