package ru.practicum;

import ru.practicum.manager.TaskManager;
import ru.practicum.manager.Managers;
import ru.practicum.model.Epic;
import ru.practicum.model.Subtask;
import ru.practicum.model.Task;
import ru.practicum.manager.FileBackedTaskManager;
import ru.practicum.model.TaskStatus;

import java.io.File;

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

        final Task finalTask1 = task1;
        final Epic finalEpic1 = epic1;

        java.util.stream.IntStream.range(0, 15)
                .forEach(i -> {
                    manager.getTaskById(finalTask1.getId());
                    manager.getEpicById(finalEpic1.getId());
                });

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

        System.out.println("\n7.1. ТЕСТИРУЕМ ВРЕМЯ И ПРОДОЛЖИТЕЛЬНОСТЬ:");

        Task timedTask = new Task(
                "Задача со временем",
                "Проверка времени",
                ru.practicum.model.TaskStatus.NEW,
                java.time.Duration.ofMinutes(90),
                java.time.LocalDateTime.of(2025, 1, 1, 10, 0)
        );

        manager.createTask(timedTask);

        System.out.println(timedTask);

        Epic timedEpic = manager.createEpic(
                new Epic("Эпик со временем", "Проверка времени эпика")
        );

        Subtask timedSub1 = new Subtask(
                "Подзадача 1",
                "30 минут",
                timedEpic.getId()
        );

        timedSub1.setDuration(java.time.Duration.ofMinutes(30));
        timedSub1.setStartTime(
                java.time.LocalDateTime.of(2025, 1, 1, 9, 0)
        );

        manager.createSubtask(timedSub1);

        Subtask timedSub2 = new Subtask(
                "Подзадача 2",
                "60 минут",
                timedEpic.getId()
        );

        timedSub2.setDuration(java.time.Duration.ofMinutes(60));
        timedSub2.setStartTime(
                java.time.LocalDateTime.of(2025, 1, 1, 12, 0)
        );

        manager.createSubtask(timedSub2);

        System.out.println("Эпик после расчёта времени:");
        System.out.println(manager.getEpicById(timedEpic.getId()));
        System.out.println("\nПРИОРИТЕТ ЗАДАЧ:");

        manager.getPrioritizedTasks()
                .forEach(System.out::println);

        System.out.println("\nПРОВЕРКА ПЕРЕСЕЧЕНИЙ:");

        Task overlapTask = new Task(
                "Пересечение",
                "Ошибка",
                TaskStatus.NEW,
                java.time.Duration.ofMinutes(30),
                java.time.LocalDateTime.of(2025, 1, 1, 10, 30)
        );

        try {
            manager.createTask(overlapTask);
        } catch (IllegalArgumentException e) {
            System.out.println("Обнаружено пересечение задач:");
            System.out.println(e.getMessage());
        }

        System.out.println("\n8. РАБОТА С ФАЙЛОВЫМ МЕНЕДЖЕРОМ:");

        File file = new File("tasks.csv");

        TaskManager fileManager = new FileBackedTaskManager(file);

        System.out.println("8.1. СОЗДАЕМ ЗАДАЧИ В ФАЙЛОВОМ МЕНЕДЖЕРЕ:");

        Task fileTask = fileManager.createTask(new Task("Файл: задача", "Описание задачи"));
        Epic fileEpic = fileManager.createEpic(new Epic("Файл: эпик", "Описание эпика"));
        Subtask fileSub = fileManager.createSubtask(
                new Subtask("Файл: подзадача", "Описание подзадачи", fileEpic.getId())
        );

        printAllTasks(fileManager);

        System.out.println("\n8.2. ПРОСМОТР ЗАДАЧ (ФОРМИРУЕМ ИСТОРИЮ):");

        fileManager.getTaskById(fileTask.getId());
        fileManager.getEpicById(fileEpic.getId());
        fileManager.getSubtaskById(fileSub.getId());

        printHistory(fileManager.getHistory());

        System.out.println("\n8.3. ПЕРЕЗАГРУЖАЕМ МЕНЕДЖЕР ИЗ ФАЙЛА:");

        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Данные после загрузки:");
        printAllTasks(loadedManager);

        System.out.println("\n8.4. ПРОВЕРЯЕМ ПРОДОЛЖЕНИЕ РАБОТЫ:");

        Task newTask = loadedManager.createTask(new Task("После загрузки", "OK"));
        System.out.println("Создана новая задача с ID = " + newTask.getId());

        printAllTasks(loadedManager);
    }

    private static void printHistory(List<Task> history) {
        if (history.isEmpty()) {
            System.out.println("История пуста");
            return;
        }

        System.out.println("История просмотров (" + history.size() + "):");
        java.util.stream.IntStream.range(0, history.size())
                .forEach(i -> {
                    Task task = history.get(i);

                    System.out.println((i + 1) + ". "
                            + task.getType()
                            + " [ID:" + task.getId() + "] "
                            + task.getName()
                            + " - "
                            + task.getStatus());
                });
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Обычные задачи:");
        manager.getAllTasks()
                .forEach(task -> System.out.println("  " + task));

        System.out.println("Эпики:");

        manager.getAllEpics().forEach(epic -> {
            System.out.println("  " + epic);

            manager.getSubtasksByEpicId(epic.getId())
                    .forEach(subtask ->
                            System.out.println("    → " + subtask));
        });

        System.out.println("Подзадачи:");
        manager.getAllSubtasks()
                .forEach(subtask -> System.out.println("  " + subtask));

        System.out.println("История просмотров:");
        manager.getHistory().forEach(task ->
                System.out.println("  "
                        + task.getType()
                        + " [ID:" + task.getId() + "] "
                        + task.getName()));
    }
}