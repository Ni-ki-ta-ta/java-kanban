package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Epic;
import ru.practicum.model.Subtask;
import ru.practicum.model.Task;
import ru.practicum.model.TaskStatus;
import ru.practicum.model.TaskType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void managerShouldAddAndFindAllTaskTypes() {
        Task task = manager.createTask(new Task("Task", "Description", TaskStatus.NEW));
        assertNotNull(manager.getTaskById(task.getId()), "Должен находить обычные задачи");
        assertEquals(TaskType.TASK, task.getType(), "Тип должен быть TASK");

        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        assertNotNull(manager.getEpicById(epic.getId()), "Должен находить эпики");
        assertEquals(TaskType.EPIC, epic.getType(), "Тип должен быть EPIC");

        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));
        assertNotNull(manager.getSubtaskById(subtask.getId()), "Должен находить подзадачи");
        assertEquals(TaskType.SUBTASK, subtask.getType(), "Тип должен быть SUBTASK");
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = manager.createTask(new Task("Task 1", "Description 1", TaskStatus.NEW));
        // Создаем вторую задачу с таким же ID, как у первой
        Task task2 = new Task("Task 2", "Description 2", task1.getId(), TaskStatus.IN_PROGRESS);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды должны совпадать");
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Desc 1", epic.getId()));

        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", subtask1.getId(),
                TaskStatus.DONE, 999);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Хэш-коды должны совпадать");
    }

    @Test
    void epicCannotAddItselfAsSubtask() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = new Subtask("Wrong", "Should not work", epic.getId());
        subtask.setId(epic.getId()); // Делаем ID подзадачи равным ID эпика

        Epic fakeEpic = new Epic("Fake", "Fake", subtask.getId(), TaskStatus.NEW);

        Subtask created = manager.createSubtask(subtask);
        assertNull(created, "Нельзя создать подзадачу, когда эпик с таким ID не существует");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = new Subtask("Subtask", "Description", 100); // epicId = 100
        subtask.setId(100); // id = 100

        Subtask created = manager.createSubtask(subtask);
        assertNull(created, "Нельзя создать подзадачу для несуществующего эпика");
    }

    @Test
    void managersShouldReturnInitializedInstances() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Managers.getDefault() не должен возвращать null");

        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Managers.getDefaultHistory() не должен возвращать null");

        assertDoesNotThrow(() -> taskManager.getAllTasks(), "Менеджер должен быть готов к работе");
        assertDoesNotThrow(() -> historyManager.getHistory(), "Менеджер истории должен быть готов к работе");
    }

    @Test
    void tasksWithGivenIdAndGeneratedIdShouldNotConflict() {
        Task taskWithManualId = new Task("Manual ID", "Desc", 999, TaskStatus.NEW);

        Task task1 = manager.createTask(new Task("Task 1", "Desc", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Task 2", "Desc", TaskStatus.NEW));
        Task task3 = manager.createTask(new Task("Task 3", "Desc", TaskStatus.NEW));

        assertNotEquals(999, task1.getId(), "ID не должен конфликтовать");
        assertNotEquals(999, task2.getId(), "ID не должен конфликтовать");
        assertNotEquals(999, task3.getId(), "ID не должен конфликтовать");

        assertNotEquals(task1.getId(), task2.getId());
        assertNotEquals(task1.getId(), task3.getId());
        assertNotEquals(task2.getId(), task3.getId());
    }

    @Test
    void taskShouldNotChangeWhenAddedToManager() {
        Task original = new Task("Original Task", "Detailed description", TaskStatus.NEW);

        String expectedName = "Original Task";
        String expectedDescription = "Detailed description";
        TaskStatus expectedStatus = TaskStatus.NEW;
        TaskType expectedType = TaskType.TASK;

        Task created = manager.createTask(original);

        assertEquals(expectedName, created.getName(), "Имя должно сохраниться");
        assertEquals(expectedDescription, created.getDescription(), "Описание должно сохраниться");
        assertEquals(expectedStatus, created.getStatus(), "Статус должен сохраниться");
        assertEquals(expectedType, created.getType(), "Тип должен сохраниться");

        assertTrue(created.getId() > 0, "Должен быть сгенерирован ID");

        Task found = manager.getTaskById(created.getId());
        assertEquals(expectedName, found.getName(), "Имя должно сохраниться при поиске");
        assertEquals(expectedDescription, found.getDescription(), "Описание должно сохраниться при поиске");
        assertEquals(expectedStatus, found.getStatus(), "Статус должен сохраниться при поиске");
        assertEquals(expectedType, found.getType(), "Тип должен сохраниться при поиске");
    }

    @Test
    void historyShouldPreserveTaskData() {
        Task task = manager.createTask(new Task("History Test", "Test description", TaskStatus.IN_PROGRESS));

        manager.getTaskById(task.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "В истории должна быть одна задача");

        Task fromHistory = history.get(0);

        assertEquals(task.getId(), fromHistory.getId(), "ID должен сохраниться в истории");
        assertEquals(task.getName(), fromHistory.getName(), "Имя должно сохраниться в истории");
        assertEquals(task.getDescription(), fromHistory.getDescription(), "Описание должно сохраниться в истории");
        assertEquals(task.getStatus(), fromHistory.getStatus(), "Статус должен сохраниться в истории");
        assertEquals(task.getType(), fromHistory.getType(), "Тип должен сохраниться в истории");
    }

    @Test
    void historyShouldBeRecordedForAllTaskTypes() {
        Task task = manager.createTask(new Task("Task", "Desc", TaskStatus.NEW));
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Desc", epic.getId()));

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());

        assertEquals(3, manager.getHistory().size(),
                "В истории должны быть все три типа задач");
    }

    @Test
    void testEqualsAndHashCodeConsistency() {
        Task task1 = new Task("Task 1", "Desc 1", 42, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc 2", 42, TaskStatus.DONE);
        Task task3 = new Task("Task 3", "Desc 3", 43, TaskStatus.NEW);

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());

        assertNotEquals(task1, task3);
        assertNotEquals(task1.hashCode(), task3.hashCode());
    }

    @Test
    void removedTaskShouldDisappearFromHistory() {
        Task task = manager.createTask(new Task("Task", "Desc", TaskStatus.NEW));

        manager.getTaskById(task.getId());
        manager.deleteTaskById(task.getId());

        List<Task> history = manager.getHistory();

        assertTrue(history.isEmpty(),
                "Удалённая задача не должна оставаться в истории");
    }

    @Test
    void historyShouldUpdateWhenTaskViewedMultipleTimes() {
        Task task1 = manager.createTask(new Task("Task 1", "Desc", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("Task 2", "Desc", TaskStatus.NEW));

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getTaskById(task1.getId());

        List<Task> history = manager.getHistory();

        assertEquals(2, history.size(),
                "История не должна содержать дубликаты");

        assertEquals(task2, history.get(0),
                "task2 должна остаться первой");

        assertEquals(task1, history.get(1),
                "task1 должна переместиться в конец после повторного просмотра");
    }

    @Test
    void epicShouldNotContainDeletedSubtask() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Sub", "Desc", epic.getId()));

        manager.deleteSubtaskById(subtask.getId());

        assertFalse(epic.getSubtaskIds().contains(subtask.getId()),
                "Удалённая подзадача не должна оставаться в эпике");
    }
}