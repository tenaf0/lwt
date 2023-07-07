package hu.garaba.model2

import hu.garaba.buffer.FileBufferReader
import hu.garaba.model.TokenCoordinate
import hu.garaba.model2.event.*
import org.mockito.Mockito
import spock.lang.Specification

import java.nio.file.Path

import static hu.garaba.model2.ReadModel.ReadModelState.*

class ReadModelTest extends Specification {
    interface EventHandler {
        void receive(ModelEvent event);
    }

    def "model state should change from UNLOADED to LOADING and finally LOADED upon opening some text"() {
        def eventHandler = Mockito.mock(EventHandler)
        def inOrder = Mockito.inOrder(eventHandler)

        given:
        def model = new ReadModel()
        model.subscribe {
            eventHandler.receive(it)
        }

        when:
        model.open("Example text")

        then:
        inOrder.verify(eventHandler, Mockito.timeout(1000)).receive(new StateChange(UNLOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000)).receive(new StateChange(LOADING))
        inOrder.verify(eventHandler, Mockito.timeout(16000)).receive(new StateChange(LOADED))
    }

    def "model should receive PageChange events upon nextPage()"() {
        def eventHandler = Mockito.mock(EventHandler)
        def inOrder = Mockito.inOrder(eventHandler)

        given:
        def model = new ReadModel()
        model.subscribe {
            eventHandler.receive(it)
        }

        model.open(Path.of(FileBufferReader.class.getResource("/kafka_prozess.txt").getFile()))
        inOrder.verify(eventHandler, Mockito.timeout(16000))
                .receive(new StateChange(LOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(Mockito.argThat { it instanceof PageChange && it.n() == 0 })

        expect:
        model.nextPage()
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(Mockito.argThat { it instanceof PageChange && it.n() == 1 })
        model.nextPage()
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(Mockito.argThat { it instanceof PageChange && it.n() == 2 })
    }

    def "model should correctly select a word based on coordinates"() {
        def eventHandler = Mockito.mock(EventHandler)
        def inOrder = Mockito.inOrder(eventHandler)

        given:
        def model = new ReadModel()
        model.subscribe {
            eventHandler.receive(it)
        }

        model.open("Sicher, ich fange gleich an. Jeden Morgen stehe ich um 7 Uhr auf. Danach ziehe ich mich an und putze meine Zähne. " +
                "Dann mache ich das Frühstück und rufe meine Kinder. Bevor ich zur Arbeit gehe, räume ich das Haus auf.")
        inOrder.verify(eventHandler, Mockito.timeout(16000))
                .receive(new StateChange(LOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(Mockito.argThat { it instanceof PageChange && it.n() == 0 })

        when:
        model.selectWord(new TokenCoordinate(0, 3))

        then:
        Mockito.verify(eventHandler, Mockito.timeout(1000))
                .receive(new SelectionChange(Set.of(),
                        Set.of(new TokenCoordinate(0, 3), new TokenCoordinate(0, 5))))
        Mockito.verify(eventHandler, Mockito.timeout(1000))
                .receive(Mockito.argThat { it instanceof SelectedSentenceChange })
    }

}
