package hu.garaba.model2;

import hu.garaba.buffer.PageReader;
import hu.garaba.model2.event.ModelEvent;
import hu.garaba.model2.event.PageChange;
import hu.garaba.model2.event.StateChange;
import hu.garaba.textprocessor.TextProcessor;
import hu.garaba.util.EventSource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReadModel implements EventSource<ModelEvent> {
    public static final System.Logger LOGGER = System.getLogger("READMODEL");

    public enum ReadModelState {
        UNLOADED, LOADING, LOADED
    }
    private final ReadModelState state = ReadModelState.UNLOADED;

    private @Nullable PageReader pageReader;

    public void open(String text) {
        open(new PageReader(TextProcessor.TextProcessorModel.UDPIPE_1, text));
    }
    public void open(Path filePath) {
        open(new PageReader(TextProcessor.TextProcessorModel.UDPIPE_1, filePath));
    }

    private void open(PageReader pageReader) {
        sendEvent(new StateChange(ReadModelState.UNLOADED));

        this.pageReader = pageReader;
        sendEvent(new StateChange(ReadModelState.LOADING));
        pageReader.init(page -> {
            sendEvent(new StateChange(ReadModelState.LOADED));
            sendEvent(new PageChange(page));
        });
    }

    private final List<Consumer<ModelEvent>> eventHandlers = new ArrayList<>();

    @Override
    public synchronized void subscribe(Consumer<ModelEvent> eventHandler) {
        eventHandlers.add(eventHandler);
    }

    @Override
    public void sendEvent(ModelEvent event) {
        LOGGER.log(System.Logger.Level.INFO, "-> " + event);
        eventHandlers.forEach(h -> h.accept(event));
    }
}
