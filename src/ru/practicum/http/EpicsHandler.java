package ru.practicum.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Epic;
import ru.practicum.model.TaskType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager) {
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

                    String response = gson.toJson(manager.getAllEpics());

                    sendText(exchange, response, STATUS_OK);

                } else if (pathParts.length == 3) {

                    int id = Integer.parseInt(pathParts[2]);

                    Epic epic = manager.getEpicById(id);

                    if (epic == null) {
                        sendNotFound(exchange);
                        return;
                    }

                    String response = gson.toJson(epic);

                    sendText(exchange, response, STATUS_OK);

                } else if (pathParts.length == 4 &&
                        pathParts[3].equals("subtasks")) {

                    int epicId = Integer.parseInt(pathParts[2]);

                    Epic epic = manager.getEpicById(epicId);

                    if (epic == null) {
                        sendNotFound(exchange);
                        return;
                    }

                    String response = gson.toJson(
                            manager.getSubtasksByEpicId(epicId)
                    );

                    sendText(exchange, response, STATUS_OK);
                }

            } else if (method.equals("POST")) {

                String body = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

                Epic epic = gson.fromJson(body, Epic.class);

                epic.setType(TaskType.EPIC);

                if (epic.getId() == 0) {

                    manager.createEpic(epic);

                } else {

                    manager.updateEpic(epic);
                }

                sendText(exchange, "Эпик сохранён", STATUS_CREATED);

            } else if (method.equals("DELETE")) {

                int id = Integer.parseInt(pathParts[2]);

                manager.deleteEpicById(id);

                sendText(exchange, "Эпик удалён", STATUS_OK);

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