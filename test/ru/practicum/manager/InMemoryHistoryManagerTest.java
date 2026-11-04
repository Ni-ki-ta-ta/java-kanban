package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Task;
import ru.practicum.model.TaskStatus;

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

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(),
                "История должна содержать одну задачу");
        assertEquals(task, history.get(0),
                "Добавленная задача должна быть в истории");
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

        assertEquals(3, history.size(),
                "История должна содержать три задачи");
        assertEquals(task1, history.get(0),
                "Первая задача должна быть task1");
        assertEquals(task2, history.get(1),
                "Вторая задача должна быть task2");
        assertEquals(task3, history.get(2),
                "Третья задача должна быть task3");
    }

    @Test
    void shouldNotStoreDuplicates() {
        Task task = new Task("Test Task", "Description", 1, TaskStatus.NEW);

        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(),
                "История не должна содержать дубликаты");
    }

    @Test
    void shouldMoveTaskToEndIfViewedAgain() {
        Task task1 = new Task("Task 1", "Desc", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc", 2, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(),
                "История должна содержать две задачи");
        assertEquals(task2, history.get(0),
                "task2 должна быть первой");
        assertEquals(task1, history.get(1),
                "task1 должна быть последней после повторного просмотра");
    }

    @Test
    void shouldRemoveTaskById() {
        Task task1 = new Task("Task 1", "Desc", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc", 2, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(),
                "После удаления должна остаться одна задача");
        assertEquals(task2, history.get(0),
                "Оставшаяся задача должна быть task2");
    }

    @Test
    void shouldRemoveFromMiddle() {
        Task task1 = new Task("Task 1", "Desc", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc", 2, TaskStatus.NEW);
        Task task3 = new Task("Task 3", "Desc", 3, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(),
                "После удаления должно остаться две задачи");
        assertEquals(task1, history.get(0),
                "Первая задача должна быть task1");
        assertEquals(task3, history.get(1),
                "Вторая задача должна быть task3");
    }

    @Test
    void addNullShouldNotAffectHistory() {
        historyManager.add(null);

        assertEquals(0, historyManager.getHistory().size(),
                "Добавление null не должно изменять историю");
    }

    @Test
    void emptyHistoryShouldReturnEmptyList() {
        assertTrue(historyManager.getHistory().isEmpty(),
                "Пустая история должна возвращать пустой список");
    }

    @Test
    void shouldRemoveFromBeginning() {
        Task task1 = new Task("Task 1", "Desc", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc", 2, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(),
                "После удаления из начала должна остаться одна задача");

        assertEquals(task2, history.get(0),
                "После удаления первой задачи task2 должна стать первой");
    }

    @Test
    void shouldRemoveFromEnd() {
        Task task1 = new Task("Task 1", "Desc", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc", 2, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(),
                "После удаления из конца должна остаться одна задача");

        assertEquals(task1, history.get(0),
                "После удаления последней задачи task1 должна остаться первой");
    }
}