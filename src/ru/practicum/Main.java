package ru.practicum;

import ru.practicum.manager.TaskManager;
import ru.practicum.manager.Managers;
import ru.practicum.model.Epic;
import ru.practicum.model.Subtask;
import ru.practicum.model.Task;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("1. СОЗДАЕМ ЗАДАЧИ:");
        Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2"));
        System.out.println("Созданы задачи с ID: " + task1.getId() + ", " + task2.getId());

        System.out.println("2. СОЗДАЕМ ЭПИК С ПОДЗАДАЧАМИ:");
        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1.1",
                "Описание 1", epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 1.2",
                "Описание 2", epic1.getId()));
        System.out.println("Созданы: эпик ID=" + epic1.getId() +
                ", подзадачи ID=" + subtask1.getId() + ", " + subtask2.getId());

        System.out.println("3. ИСТОРИЯ ДО ПРОСМОТРОВ:");
        printHistory(manager.getHistory());

        System.out.println("4. ПРОСМАТРИВАЕМ ЗАДАЧИ:");
        System.out.println("Смотрим задачу ID=" + task1.getId());
        manager.getTaskById(task1.getId());
        printHistory(manager.getHistory());

        System.out.println("Смотрим эпик ID=" + epic1.getId());
        manager.getEpicById(epic1.getId());
        printHistory(manager.getHistory());

        System.out.println("Смотрим подзадачу ID=" + subtask1.getId());
        manager.getSubtaskById(subtask1.getId());
        printHistory(manager.getHistory());

        System.out.println("5. ТЕСТИРУЕМ ПОВТОРНЫЕ ПРОСМОТРЫ:");

        for (int i = 0; i < 15; i++) {
            manager.getTaskById(task1.getId());
            manager.getEpicById(epic1.getId());
        }
        printHistory(manager.getHistory());

        System.out.println("6. ТЕСТИРУЕМ ПОРЯДОК ИСТОРИИ:");

        task1 = manager.createTask(new Task("Task A", "Description A"));
        task2 = manager.createTask(new Task("Task B", "Description B"));
        Task task3 = manager.createTask(new Task("Task C", "Description C"));

        manager.getTaskById(task2.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task3.getId());
        manager.getTaskById(task2.getId());

        printHistory(manager.getHistory());

        System.out.println("7. ВСЕ ЗАДАЧИ В СИСТЕМЕ:");
        printAllTasks(manager);
    }

    private static void printHistory(List<Task> history) {
        if (history.isEmpty()) {
            System.out.println("История пуста");
            return;
        }

        System.out.println("История просмотров (" + history.size() + "):");
        for (int i = 0; i < history.size(); i++) {
            Task task = history.get(i);
            System.out.println((i + 1) + ". " + task.getType() +
                    " [ID:" + task.getId() + "] " +
                    task.getName() + " - " + task.getStatus());
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Обычные задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println("  " + task);
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println("  " + epic);

            for (Task subtask : manager.getSubtasksByEpicId(epic.getId())) {
                System.out.println("    → " + subtask);
            }
        }

        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println("  " + subtask);
        }

        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println("  " + task.getType() +
                    " [ID:" + task.getId() + "] " +
                    task.getName());
        }
    }
}