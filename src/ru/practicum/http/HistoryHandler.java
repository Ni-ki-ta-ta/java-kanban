package ru.practicum.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equals("GET")) {

            String response = gson.toJson(manager.getHistory());

            sendText(exchange, response, STATUS_OK);

        } else {

            exchange.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, 0);
            exchange.close();
        }
    }
}