package hu.garaba.model2

import hu.garaba.buffer.FileBufferReader
import hu.garaba.model2.event.ModelEvent
import hu.garaba.model2.event.PageChange
import hu.garaba.model2.event.StateChange
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.nio.file.Path

import static hu.garaba.model2.ReadModel.ReadModelState.LOADED
import static hu.garaba.model2.ReadModel.ReadModelState.LOADING

class ReadModelTest extends Specification {
    interface EventHandler {
        void receive(ModelEvent event);
    }

    def "model state should change from UNLOADED to LOADING and finally LOADED upon opening some text"() {
        given:
        def eventHandler = Mock(EventHandler)
        def eventsReceived = 0

        def model = new ReadModel()
        model.subscribe {
            eventsReceived++
            eventHandler.receive(it)
        }

        when:
        model.open("Example text")

        then:
        new PollingConditions().within(20) {
            assert eventsReceived >= 3
        }

        then:
        1 * eventHandler.receive(new StateChange(LOADING))

        then:
        1 * eventHandler.receive(new StateChange(LOADED))
    }

    def "model should receive PageChange events upon calling nextPage()"() {
        given:
        def eventHandler = Mock(EventHandler)
        def eventsReceived = 0
        def model = new ReadModel()
        model.subscribe {
            eventsReceived++
            eventHandler.receive(it)
        }

        when:
        model.open(Path.of(FileBufferReader.class.getResource("/kafka_prozess.txt").getFile()))
        model.nextPage()

        then:
        new PollingConditions().within(40) {
            assert eventsReceived >= 4
        }

        then:
        2 * eventHandler.receive(_ as PageChange)
    }
}
