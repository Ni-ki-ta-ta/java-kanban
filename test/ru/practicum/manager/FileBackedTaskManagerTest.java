package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File file;

    @BeforeEach
    void setUp() throws IOException {
        file = File.createTempFile("test", ".csv");
        manager = new FileBackedTaskManager(file);
    }

    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        File file = File.createTempFile("test", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getAllTasks().isEmpty(),
                "Задачи должны быть пустыми");
        assertTrue(loaded.getAllEpics().isEmpty(),
                "Эпики должны быть пустыми");
        assertTrue(loaded.getAllSubtasks().isEmpty(),
                "Подзадачи должны быть пустыми");
    }

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        File file = File.createTempFile("test", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Task", "Desc"));
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Sub", "Desc", epic.getId())
        );

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllTasks().size(),
                "Должна быть 1 задача");
        assertEquals(1, loaded.getAllEpics().size(),
                "Должен быть 1 эпик");
        assertEquals(1, loaded.getAllSubtasks().size(),
                "Должна быть 1 подзадача");
    }

    @Test
    void shouldRestoreEpicSubtaskRelation() throws IOException {
        File file = File.createTempFile("test", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Sub", "Desc", epic.getId())
        );

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        Epic loadedEpic = loaded.getAllEpics().get(0);

        assertEquals(1, loadedEpic.getSubtaskIds().size(),
                "У эпика должна быть подзадача");
    }

    @Test
    void shouldPreserveTaskFields() throws IOException {
        File file = File.createTempFile("test", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(
                new Task("Task", "Desc", TaskStatus.IN_PROGRESS)
        );

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        Task loadedTask = loaded.getAllTasks().get(0);

        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
    }

    @Test
    void shouldContinueWorkingAfterLoad() throws IOException {
        File file = File.createTempFile("test", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.createTask(new Task("Task1", "Desc"));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        Task newTask = loaded.createTask(new Task("Task2", "Desc"));

        assertEquals(2, newTask.getId(),
                "ID должен продолжаться после загрузки");
    }

    @Test
    void shouldSaveAndRestoreHistory() throws IOException {

        Task task = manager.createTask(
                new Task("Task", "Desc")
        );

        manager.getTaskById(task.getId());

        FileBackedTaskManager loaded =
                FileBackedTaskManager.loadFromFile(file);

        assertEquals(1,
                loaded.getHistory().size(),
                "История должна восстановиться");

        assertEquals(task.getId(),
                loaded.getHistory().get(0).getId(),
                "ID задачи в истории должен сохраниться");
    }

    @Test
    void shouldNotThrowWhenSavingAndLoading() {
        assertDoesNotThrow(() -> {

            Task task = manager.createTask(
                    new Task("Task", "Desc")
            );

            FileBackedTaskManager.loadFromFile(file);

        }, "Сохранение и загрузка не должны выбрасывать исключения");
    }
}