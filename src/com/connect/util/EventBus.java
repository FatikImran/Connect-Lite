package com.connect.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simple application-wide EventBus for lightweight controller communication.
 * Usage: EventBus.subscribe("some:event", payload -> { ... });
 *        EventBus.publish("some:event", payload);
 */
public class EventBus {
    private static final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    public static void subscribe(String event, Consumer<Object> handler) {
        listeners.computeIfAbsent(event, k -> Collections.synchronizedList(new ArrayList<>()))
                 .add(handler);
    }

    public static void unsubscribe(String event, Consumer<Object> handler) {
        List<Consumer<Object>> list = listeners.get(event);
        if (list != null) {
            synchronized (list) {
                list.remove(handler);
                if (list.isEmpty()) listeners.remove(event);
            }
        }
    }

    public static void publish(String event, Object payload) {
        List<Consumer<Object>> list = listeners.get(event);
        if (list == null) return;
        // Make a copy to avoid ConcurrentModification during iteration
        Consumer<Object>[] copy;
        synchronized (list) {
            copy = list.toArray(new Consumer[0]);
        }
        for (Consumer<Object> c : copy) {
            try {
                c.accept(payload);
            } catch (Exception e) {
                System.err.println("❌ EventBus handler for '" + event + "' threw: " + e.getMessage());
            }
        }
    }
}
