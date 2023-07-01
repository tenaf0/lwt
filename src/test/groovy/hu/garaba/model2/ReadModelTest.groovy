package hu.garaba.model2

import hu.garaba.model2.event.ModelEvent
import hu.garaba.model2.event.StateChange
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

import static hu.garaba.model2.ReadModel.ReadModelState.*

class ReadModelTest extends Specification {
    interface EventHandler {
        void receive(ModelEvent event);
    }

    def "model state should change from UNLOADED to LOADING and finally LOADED upon opening some text"() {
        given:
        def eventHandler = Mock(EventHandler)
        def allEventsReceived = new BlockingVariable<Boolean>(10)
        def eventsReceived = 0

        def model = new ReadModel()
        model.subscribe {
            println "Received $it"
            eventsReceived++
            eventHandler.receive(it)

            if (eventsReceived >= 3)
                allEventsReceived.set(true)
        }

        when:
        model.open("Example text")

        then:
        allEventsReceived.get()

        then:
        1 * eventHandler.receive(_)

        then:
        1 * eventHandler.receive(new StateChange(LOADING))

        then:
        1 * eventHandler.receive(new StateChange(LOADED))
    }
}
