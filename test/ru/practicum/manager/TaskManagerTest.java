package ru.practicum.manager;

import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    @Test
    void shouldCreateAndFindTask() {
        Task task = manager.createTask(
                new Task("Task", "Description", TaskStatus.NEW)
        );

        Task savedTask = manager.getTaskById(task.getId());

        assertNotNull(savedTask,
                "Задача должна находиться по ID");

        assertEquals(task, savedTask,
                "Задачи должны совпадать");
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        manager.createSubtask(
                new Subtask("Sub1", "Desc", epic.getId())
        );

        manager.createSubtask(
                new Subtask("Sub2", "Desc", epic.getId())
        );

        assertEquals(TaskStatus.NEW,
                manager.getEpicById(epic.getId()).getStatus(),
                "Эпик должен быть NEW");
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        Subtask sub1 = new Subtask("Sub1", "Desc", epic.getId());
        sub1.setStatus(TaskStatus.DONE);

        Subtask sub2 = new Subtask("Sub2", "Desc", epic.getId());
        sub2.setStatus(TaskStatus.DONE);

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(TaskStatus.DONE,
                manager.getEpicById(epic.getId()).getStatus(),
                "Эпик должен быть DONE");
    }

    @Test
    void epicStatusShouldBeInProgressWhenNewAndDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        Subtask sub1 = new Subtask("Sub1", "Desc", epic.getId());
        sub1.setStatus(TaskStatus.NEW);

        Subtask sub2 = new Subtask("Sub2", "Desc", epic.getId());
        sub2.setStatus(TaskStatus.DONE);

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(TaskStatus.IN_PROGRESS,
                manager.getEpicById(epic.getId()).getStatus(),
                "Эпик должен быть IN_PROGRESS");
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksInProgress() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        Subtask sub1 = new Subtask("Sub1", "Desc", epic.getId());
        sub1.setStatus(TaskStatus.IN_PROGRESS);

        manager.createSubtask(sub1);

        assertEquals(TaskStatus.IN_PROGRESS,
                manager.getEpicById(epic.getId()).getStatus(),
                "Эпик должен быть IN_PROGRESS");
    }

    @Test
    void shouldReturnPrioritizedTasks() {
        Task task1 = new Task(
                "Task1",
                "Desc",
                TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );

        Task task2 = new Task(
                "Task2",
                "Desc",
                TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 9, 0)
        );

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(task2.getId(), prioritized.get(0).getId(),
                "Сначала должна быть более ранняя задача");

        assertEquals(task1.getId(), prioritized.get(1).getId(),
                "Вторая задача должна быть позже");
    }

    @Test
    void shouldThrowExceptionWhenTasksIntersect() {

        Task task1 = new Task(
                "Task1",
                "Desc",
                TaskStatus.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );

        Task task2 = new Task(
                "Task2",
                "Desc",
                TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 1, 1, 10, 30)
        );

        manager.createTask(task1);

        assertThrows(IllegalArgumentException.class, () -> {
            manager.createTask(task2);
        }, "Пересекающиеся задачи должны вызывать исключение");
    }
}