package shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Global Event Bus for module communication using a Publish-Subscribe pattern.
 */
public class EventBus {
    private static EventBus instance;
    private final Map<String, List<Consumer<Object>>> listeners;

    private EventBus() {
        this.listeners = new HashMap<>();
    }

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void subscribe(String eventName, Consumer<Object> callback) {
        DebugLogger.info("EVENT ? Subscribing to " + eventName + " - subscribe() - EventBus.java");
        listeners.computeIfAbsent(eventName, k -> new ArrayList<>()).add(callback);
    }

    public void publish(String eventName, Object data) {
        DebugLogger.info("EVENT ? " + eventName + " → " + (data != null ? data.toString() : "null") + " - publish() - EventBus.java");
        List<Consumer<Object>> topicListeners = listeners.get(eventName);
        if (topicListeners != null) {
            for (Consumer<Object> listener : topicListeners) {
                listener.accept(data);
            }
        }
    }
}
