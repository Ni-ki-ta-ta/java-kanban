package ru.practicum.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.manager.Managers;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Epic;
import ru.practicum.model.Subtask;
import ru.practicum.model.Task;
import ru.practicum.model.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskServerTest {

    TaskManager manager;
    HttpTaskServer server;
    Gson gson;

    @BeforeEach
    void setUp() throws IOException {

        manager = Managers.getDefault();

        server = new HttpTaskServer(manager);

        gson = HttpTaskServer.getGson();

        server.start();
    }

    @AfterEach
    void shutDown() {

        server.stop();
    }

    @Test
    void shouldCreateTask() throws IOException, InterruptedException {

        Task task = new Task(
                "Test task",
                "Test description",
                TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 5, 14, 20, 30)
        );

        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201,
                response.statusCode(),
                "Сервер должен вернуть код 201");

        assertEquals(1,
                manager.getAllTasks().size(),
                "Задача должна сохраниться");

        assertEquals("Test task",
                manager.getAllTasks().getFirst().getName(),
                "Название задачи должно совпадать");
    }

    @Test
    void shouldReturnTaskById() throws IOException, InterruptedException {

        Task task = new Task(
                "Task",
                "Description",
                TaskStatus.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.of(2026, 5, 14, 20, 0)
        );

        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200,
                response.statusCode(),
                "Сервер должен вернуть код 200");

        assertTrue(response.body().contains("Task"),
                "Ответ должен содержать задачу");
    }

    @Test
    void shouldReturn404IfTaskNotFound() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404,
                response.statusCode(),
                "Сервер должен вернуть 404");
    }

    @Test
    void shouldReturnPrioritizedTasks() throws IOException, InterruptedException {

        Task task = new Task(
                "Priority task",
                "Description",
                TaskStatus.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.of(2026, 5, 14, 18, 0)
        );

        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200,
                response.statusCode(),
                "Сервер должен вернуть код 200");

        assertTrue(response.body().contains("Priority task"),
                "Ответ должен содержать prioritized задачи");
    }

    @Test
    void shouldReturnHistory() throws IOException, InterruptedException {

        Task task = new Task(
                "History task",
                "Description",
                TaskStatus.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 5, 14, 19, 0)
        );

        manager.createTask(task);

        manager.getTaskById(1);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200,
                response.statusCode(),
                "Сервер должен вернуть код 200");

        assertTrue(response.body().contains("History task"),
                "История должна содержать просмотренную задачу");
    }

    @Test
    void shouldCreateEpic() throws IOException, InterruptedException {

        Epic epic = new Epic(
                "Epic",
                "Epic description"
        );

        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201,
                response.statusCode(),
                "Эпик должен успешно создаваться");

        assertEquals(1,
                manager.getAllEpics().size(),
                "В менеджере должен сохраниться эпик");
    }

    @Test
    void shouldCreateSubtask() throws IOException, InterruptedException {

        Epic epic = manager.createEpic(
                new Epic("Epic", "Description")
        );

        Subtask subtask = new Subtask(
                "Subtask",
                "Description",
                epic.getId()
        );

        subtask.setDuration(Duration.ofMinutes(15));

        subtask.setStartTime(
                LocalDateTime.of(2026, 5, 14, 21, 0)
        );

        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201,
                response.statusCode(),
                "Подзадача должна успешно создаваться");

        assertEquals(1,
                manager.getAllSubtasks().size(),
                "Подзадача должна сохраниться");
    }

    @Test
    void shouldReturnEpicSubtasks() throws IOException, InterruptedException {

        Epic epic = manager.createEpic(
                new Epic("Epic", "Description")
        );

        Subtask subtask = new Subtask(
                "Subtask",
                "Description",
                epic.getId()
        );

        manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8080/epics/"
                                + epic.getId()
                                + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200,
                response.statusCode(),
                "Сервер должен вернуть код 200");

        assertTrue(response.body().contains("Subtask"),
                "Ответ должен содержать подзадачу эпика");
    }

    @Test
    void shouldReturn406WhenTasksIntersect() throws IOException, InterruptedException {

        Task task1 = new Task(
                "Task1",
                "Description",
                TaskStatus.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 5, 14, 10, 0)
        );

        manager.createTask(task1);

        Task task2 = new Task(
                "Task2",
                "Description",
                TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 5, 14, 10, 30)
        );

        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(406,
                response.statusCode(),
                "Сервер должен вернуть 406 при пересечении задач");
    }

    @Test
    void shouldDeleteTask() throws IOException, InterruptedException {

        Task task = new Task(
                "Task",
                "Description"
        );

        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200,
                response.statusCode(),
                "Сервер должен вернуть код 200");

        assertTrue(manager.getAllTasks().isEmpty(),
                "Задача должна удалиться");
    }
}
