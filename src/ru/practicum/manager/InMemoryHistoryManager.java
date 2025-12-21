package ru.practicum.manager;

import ru.practicum.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private static final int MAX_HISTORY_SIZE = 10;
    private final List<Task> history;

    public InMemoryHistoryManager() {
        this.history = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) return;

        history.add(task);

        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
