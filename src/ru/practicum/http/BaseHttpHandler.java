package ru.practicum.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected static final int STATUS_OK = 200;
    protected static final int STATUS_CREATED = 201;
    protected static final int STATUS_NOT_FOUND = 404;
    protected static final int STATUS_NOT_ACCEPTABLE = 406;
    protected static final int STATUS_INTERNAL_ERROR = 500;
    protected static final int STATUS_METHOD_NOT_ALLOWED = 405;

    protected void sendText(HttpExchange exchange,
                            String text,
                            int statusCode) throws IOException {

        byte[] response = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .add("Content-Type", "application/json;charset=utf-8");

        exchange.sendResponseHeaders(statusCode, response.length);

        exchange.getResponseBody().write(response);

        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "Ресурс не найден", STATUS_NOT_FOUND);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange,
                "Задача пересекается с существующими",
                STATUS_NOT_ACCEPTABLE);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendText(exchange,
                "Внутренняя ошибка сервера",
                STATUS_INTERNAL_ERROR);
    }
}