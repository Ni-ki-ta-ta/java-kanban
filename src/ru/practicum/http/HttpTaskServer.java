package ru.practicum.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.http.adapters.DurationAdapter;
import ru.practicum.http.adapters.LocalDateTimeAdapter;
import ru.practicum.manager.Managers;
import ru.practicum.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) throws IOException {

        httpServer = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        httpServer.createContext("/tasks",
                new TasksHandler(manager));

        httpServer.createContext("/subtasks",
                new SubtasksHandler(manager));

        httpServer.createContext("/epics",
                new EpicsHandler(manager));

        httpServer.createContext("/history",
                new HistoryHandler(manager));

        httpServer.createContext("/prioritized",
                new PrioritizedHandler(manager));
    }

    public static Gson getGson() {
        return GSON;
    }

    public static void main(String[] args) throws IOException {

        TaskManager manager = Managers.getDefault();

        HttpTaskServer server = new HttpTaskServer(manager);

        server.start();
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP сервер запущен на порту 8080");
    }

    public void stop() {
        httpServer.stop(0);
    }
}