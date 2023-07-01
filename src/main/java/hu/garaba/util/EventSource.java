package hu.garaba.util;

import java.util.function.Consumer;

public interface EventSource<T> {
    void subscribe(Consumer<T> eventHandler);

    void sendEvent(T event);
}
