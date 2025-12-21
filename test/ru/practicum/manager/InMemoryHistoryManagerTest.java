package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Task;
import ru.practicum.model.TaskStatus;
import ru.practicum.model.TaskType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void addTaskToHistory() {
        Task task = new Task("Test Task", "Description", 1, TaskStatus.NEW);
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size(),
                "После добавления задачи история не должна быть пустой");
        assertEquals(task, historyManager.getHistory().get(0),
                "Добавленная задача должна быть в истории");
    }

    @Test
    void historyShouldNotExceedMaxSize() {
        for (int i = 1; i <= 15; i++) {
            Task t = new Task("Task " + i, "Desc", i, TaskStatus.NEW);
            historyManager.add(t);
        }

        assertEquals(10, historyManager.getHistory().size(),
                "История не должна превышать 10 записей");

        List<Task> history = historyManager.getHistory();
        assertEquals(6, history.get(0).getId(), "Первая задача должна быть с ID=6");
        assertEquals(15, history.get(9).getId(), "Последняя задача должна быть с ID=15");
    }

    @Test
    void historyShouldKeepOrder() {
        Task task1 = new Task("Task 1", "Desc", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc", 2, TaskStatus.NEW);
        Task task3 = new Task("Task 3", "Desc", 3, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "В истории должно быть 3 задачи");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1");
        assertEquals(task2, history.get(1), "Вторая задача должна быть task2");
        assertEquals(task3, history.get(2), "Третья задача должна быть task3");
    }

    @Test
    void duplicateTasksShouldBeAllowed() {
        Task task = new Task("Test Task", "Description", 1, TaskStatus.NEW);
        historyManager.add(task);
        historyManager.add(task);

        assertEquals(2, historyManager.getHistory().size(),
                "Дубликаты задач должны добавляться в историю");
    }

    @Test
    void taskDataShouldBePreservedInHistory() {
        Task original = new Task("Test Task", "Detailed description for testing", 42, TaskStatus.IN_PROGRESS);

        historyManager.add(original);

        Task fromHistory = historyManager.getHistory().get(0);

        assertEquals(original.getId(), fromHistory.getId(), "ID должен сохраняться в истории");
        assertEquals(original.getName(), fromHistory.getName(), "Имя должно сохраняться в истории");
        assertEquals(original.getDescription(), fromHistory.getDescription(), "Описание должно сохраняться в истории");
        assertEquals(original.getStatus(), fromHistory.getStatus(), "Статус должен сохраняться в истории");
        assertEquals(original.getType(), fromHistory.getType(), "Тип должен сохраняться в истории");

        // Проверяем конкретные значения
        assertEquals(42, fromHistory.getId(), "ID должен быть 42");
        assertEquals("Test Task", fromHistory.getName(), "Имя должно быть 'Test Task'");
        assertEquals("Detailed description for testing", fromHistory.getDescription(),
                "Описание должно совпадать");
        assertEquals(TaskStatus.IN_PROGRESS, fromHistory.getStatus(), "Статус должен быть IN_PROGRESS");
        assertEquals(TaskType.TASK, fromHistory.getType(), "Тип должен быть TASK");
    }

    @Test
    void historyShouldPreserveDifferentStatuses() {
        Task newTask = new Task("New", "Desc", 1, TaskStatus.NEW);
        Task inProgressTask = new Task("In Progress", "Desc", 2, TaskStatus.IN_PROGRESS);
        Task doneTask = new Task("Done", "Desc", 3, TaskStatus.DONE);

        historyManager.add(newTask);
        historyManager.add(inProgressTask);
        historyManager.add(doneTask);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "В истории должно быть 3 задачи");
        assertEquals(TaskStatus.NEW, history.get(0).getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, history.get(1).getStatus());
        assertEquals(TaskStatus.DONE, history.get(2).getStatus());
    }

    @Test
    void addNullShouldNotBreakHistory() {
        historyManager.add(null);

        assertEquals(0, historyManager.getHistory().size(),
                "История должна оставаться пустой после добавления null");
    }
}