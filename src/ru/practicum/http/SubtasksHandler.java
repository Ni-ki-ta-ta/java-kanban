package ru.practicum.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Subtask;
import ru.practicum.model.TaskType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager) {
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

                    String response = gson.toJson(manager.getAllSubtasks());

                    sendText(exchange, response, 200);

                } else if (pathParts.length == 3) {

                    int id = Integer.parseInt(pathParts[2]);

                    Subtask subtask = manager.getSubtaskById(id);

                    if (subtask == null) {
                        sendNotFound(exchange);
                        return;
                    }

                    String response = gson.toJson(subtask);

                    sendText(exchange, response, 200);
                }

            } else if (method.equals("POST")) {

                String body = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

                Subtask subtask = gson.fromJson(body, Subtask.class);

                subtask.setType(TaskType.SUBTASK);

                if (subtask.getId() == 0) {

                    manager.createSubtask(subtask);

                } else {

                    manager.updateSubtask(subtask);
                }

                sendText(exchange, "Подзадача сохранена", 201);

            } else if (method.equals("DELETE")) {

                int id = Integer.parseInt(pathParts[2]);

                manager.deleteSubtaskById(id);

                sendText(exchange, "Подзадача удалена", 200);

            } else {

                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }

        } catch (IllegalArgumentException e) {

            sendHasInteractions(exchange);

        } catch (Exception e) {

            sendInternalError(exchange);
        }
    }
}