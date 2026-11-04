package ru.practicum.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Task;
import ru.practicum.model.TaskType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        try {

            if (method.equals("GET")) {

                if (pathParts.length == 2) {

                    String response = gson.toJson(manager.getAllTasks());
                    sendText(exchange, response, STATUS_OK);

                } else if (pathParts.length == 3) {

                    int id = Integer.parseInt(pathParts[2]);

                    Task task = manager.getTaskById(id);

                    if (task == null) {
                        sendNotFound(exchange);
                        return;
                    }

                    String response = gson.toJson(task);

                    sendText(exchange, response, STATUS_OK);
                }

            } else if (method.equals("POST")) {

                String body = new String(exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8);

                Task task = gson.fromJson(body, Task.class);

                task.setType(TaskType.TASK);

                if (task.getId() == 0) {
                    manager.createTask(task);

                } else {
                    manager.updateTask(task);
                }

                sendText(exchange, "Задача сохранена", STATUS_CREATED);

            } else if (method.equals("DELETE")) {

                int id = Integer.parseInt(pathParts[2]);

                manager.deleteTaskById(id);

                sendText(exchange, "Задача удалена", STATUS_OK);

            } else {

                exchange.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, 0);
                exchange.close();
            }

        } catch (IllegalArgumentException e) {

            sendHasInteractions(exchange);

        } catch (Exception e) {

            sendInternalError(exchange);

        }
    }
}