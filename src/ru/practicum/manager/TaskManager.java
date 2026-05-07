package ru.practicum.manager;

import ru.practicum.model.Epic;
import ru.practicum.model.Subtask;
import ru.practicum.model.Task;

import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    Task createTask(Task task);

    Task updateTask(Task task);

    void deleteTaskById(int id);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    Epic createEpic(Epic epic);

    Epic updateEpic(Epic epic);

    void deleteEpicById(int id);

    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(int id);

    Subtask createSubtask(Subtask subtask);

    Subtask updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    List<Subtask> getSubtasksByEpicId(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
