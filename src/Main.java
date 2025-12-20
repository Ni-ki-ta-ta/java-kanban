public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        System.out.println("1. Создаем обычные задачи:");
        Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2"));
        System.out.println("Созданы задачи:");
        System.out.println("  " + task1);
        System.out.println("  " + task2);

        System.out.println("2. Создаем эпик с двумя подзадачами:");
        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1.1",
                "Описание подзадачи 1.1", epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 1.2",
                "Описание подзадачи 1.2", epic1.getId()));
        System.out.println("Создан эпик: " + epic1);
        System.out.println("Созданы подзадачи:");
        System.out.println("  " + subtask1);
        System.out.println("  " + subtask2);

        System.out.println("3. Создаем эпик с одной подзадачей:");
        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 2.1",
                "Описание подзадачи 2.1", epic2.getId()));
        System.out.println("Создан эпик: " + epic2);
        System.out.println("Создана подзадача: " + subtask3);

        System.out.println("4. Распечатываем все задачи:");
        printAllTasks(manager);

        System.out.println("5. Изменяем статусы:");
        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);
        System.out.println("Обновлена задача: " + manager.getTaskById(task1.getId()));

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        System.out.println("Обновлена подзадача: " + manager.getSubtaskById(subtask1.getId()));
        System.out.println("Статус эпика 1 после изменения подзадачи: "
                + manager.getEpicById(epic1.getId()).getStatus());

        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);
        System.out.println("Обновлена подзадача: " + manager.getSubtaskById(subtask2.getId()));
        System.out.println("Статус эпика 1 после изменения второй подзадачи: "
                + manager.getEpicById(epic1.getId()).getStatus());

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        System.out.println("Статус эпика 1 после завершения всех подзадач: "
                + manager.getEpicById(epic1.getId()).getStatus());

        System.out.println("6. Подзадачи эпика 1:");
        System.out.println(manager.getSubtasksByEpicId(epic1.getId()));

        System.out.println("7. Удаляем задачу и эпик:");
        System.out.println("До удаления - всего задач: " + manager.getAllTasks().size());
        manager.deleteTaskById(task1.getId());
        System.out.println("После удаления задачи - всего задач: " + manager.getAllTasks().size());

        System.out.println("До удаления - всего эпиков: " + manager.getAllEpics().size());
        System.out.println("До удаления - всего подзадач: " + manager.getAllSubtasks().size());
        manager.deleteEpicById(epic1.getId());
        System.out.println("После удаления эпика - всего эпиков: " + manager.getAllEpics().size());
        System.out.println("После удаления эпика - всего подзадач: " + manager.getAllSubtasks().size());

        System.out.println("8. Финальное состояние:");
        printAllTasks(manager);
    }

    public static void printAllTasks(TaskManager manager) {
        System.out.println("Обычные задачи (" + manager.getAllTasks().size() + "):");
        for (Task task : manager.getAllTasks()) {
            System.out.println("  " + task);
        }

        System.out.println("Эпики (" + manager.getAllEpics().size() + "):");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println("  " + epic);
        }

        System.out.println("Подзадачи (" + manager.getAllSubtasks().size() + "):");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println("  " + subtask);
        }
    }
}