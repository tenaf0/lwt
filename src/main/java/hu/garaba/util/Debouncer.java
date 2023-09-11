package hu.garaba.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Debouncer {
    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1, r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });
    private final AtomicReference<Runnable> callbackFunction = new AtomicReference<>();

    public void debounce(Runnable function, int delayMs) {
        callbackFunction.set(function);

        sched.schedule(() -> {
            Runnable existingFn = callbackFunction.get();
            if (existingFn == function) {
                function.run();
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }
}