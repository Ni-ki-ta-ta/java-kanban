package ru.practicum.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", 1, TaskStatus.IN_PROGRESS);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды должны совпадать");
    }

    @Test
    void tasksWithDifferentIdShouldNotBeEqual() {
        Task task1 = new Task("Task", "Description", 1, TaskStatus.NEW);
        Task task2 = new Task("Task", "Description", 2, TaskStatus.NEW);

        assertNotEquals(task1, task2, "Задачи с разным ID не должны быть равны");
        assertNotEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды не должны совпадать");
    }

    @Test
    void epicShouldBeEqualIfSameId() {
        Epic epic1 = new Epic("Epic 1", "Description 1", 1, TaskStatus.NEW);
        Epic epic2 = new Epic("Epic 2", "Description 2", 1, TaskStatus.DONE);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
        assertEquals(epic1.hashCode(), epic2.hashCode(), "Хэш-коды должны совпадать");
    }

    @Test
    void subtaskShouldBeEqualIfSameId() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 1, TaskStatus.NEW, 10);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 1, TaskStatus.DONE, 20);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Хэш-коды должны совпадать");
    }

    @Test
    void taskFieldsShouldBePreserved() {
        Task original = new Task("Original", "Description", 1, TaskStatus.NEW);
        Task copy = new Task("Copy", "Different", 1, TaskStatus.DONE);

        // Несмотря на разные поля (кроме id), задачи равны по ID
        assertEquals(original, copy, "Задачи с одинаковым ID должны быть равны");
        assertEquals(original.hashCode(), copy.hashCode(),
                "Хэш-коды задач с одинаковым ID должны совпадать");
    }

    @Test
    void taskTypeShouldBeCorrect() {
        Task task = new Task("Task", "Description", TaskStatus.NEW);
        assertEquals(TaskType.TASK, task.getType(), "Обычная задача должна иметь тип TASK");

        Epic epic = new Epic("Epic", "Description");
        assertEquals(TaskType.EPIC, epic.getType(), "Эпик должен иметь тип EPIC");

        Subtask subtask = new Subtask("Subtask", "Description", 1);
        assertEquals(TaskType.SUBTASK, subtask.getType(), "Подзадача должна иметь тип SUBTASK");
    }
}