package hu.garaba.model2

import hu.garaba.buffer.FileBufferReader
import hu.garaba.db.KnownWordDb
import hu.garaba.db.WordState
import hu.garaba.model.TokenCoordinate
import hu.garaba.model2.event.*
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import spock.lang.Specification

import java.nio.file.Path

import static hu.garaba.model2.ReadModel.ReadModelState.*
import static org.mockito.AdditionalMatchers.and
import static org.mockito.ArgumentMatchers.argThat

class ReadModelTest extends Specification {
    interface EventHandler {
        void receive(ModelEvent event);
    }

    def wordDb = Mock(KnownWordDb)

    def setup() {
        wordDb.isKnown(_) >> WordState.IGNORED
    }

    def "model state should change from UNLOADED to LOADING and finally LOADED upon opening some text"() {
        def eventHandler = Mockito.mock(EventHandler)
        def inOrder = Mockito.inOrder(eventHandler)

        given:
        def model = new ReadModel(wordDb)
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
        def model = new ReadModel(wordDb)
        model.subscribe {
            eventHandler.receive(it)
        }

        model.open(Path.of(FileBufferReader.class.getResource("/kafka_prozess.txt").getFile()))
        inOrder.verify(eventHandler, Mockito.timeout(16000))
                .receive(new StateChange(LOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(argThat pageChangeTo(0))

        expect:
        model.nextPage()
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(argThat pageChangeTo(1))
        model.nextPage()
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(argThat pageChangeTo(2))
    }

    def "model should correctly select a word based on coordinates"() {
        def eventHandler = Mockito.mock(EventHandler)
        def inOrder = Mockito.inOrder(eventHandler)

        given:
        def model = new ReadModel(wordDb)
        model.subscribe {
            eventHandler.receive(it)
        }

        model.open("Sicher, ich fange gleich an. Jeden Morgen stehe ich um 7 Uhr auf. Danach ziehe ich mich an und putze meine Zähne. " +
                "Dann mache ich das Frühstück und rufe meine Kinder. Bevor ich zur Arbeit gehe, räume ich das Haus auf.")
        inOrder.verify(eventHandler, Mockito.timeout(16000))
                .receive(new StateChange(LOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(argThat pageChangeTo(0))

        when:
        model.selectWord(new TokenCoordinate(0, 3))

        then:
        Mockito.verify(eventHandler, Mockito.timeout(1000))
                .receive(new SelectionChange(Set.of(),
                        Set.of(new TokenCoordinate(0, 3), new TokenCoordinate(0, 5))))
        Mockito.verify(eventHandler, Mockito.timeout(1000))
                .receive(argThat { it instanceof SelectedSentenceChange })
    }

    def "open() in a model which has LOADED state should properly load new document"() {
        def eventHandler = Mockito.mock(EventHandler)
        def inOrder = Mockito.inOrder(eventHandler)

        given:
        def model = new ReadModel(wordDb)
        model.subscribe {
            eventHandler.receive(it)
        }

        model.open(shortKafka)
        inOrder.verify(eventHandler, Mockito.timeout(16000))
                .receive(new StateChange(LOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(argThat pageChangeTo(0))

        when:
        model.open("Berlin ist eine wunderschöne Stadt in Deutschland. Es ist bekannt für seine reiche Geschichte und Kultur. " +
                "Viele Touristen besuchen die Berliner Mauer und das Brandenburger Tor. Auch das Essen in Berlin, insbesondere die Würste, ist sehr beliebt.")

        then:
        inOrder.verify(eventHandler, Mockito.timeout(1000)).receive(new StateChange(UNLOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000)).receive(new StateChange(LOADING))
        inOrder.verify(eventHandler, Mockito.timeout(16000)).receive(new StateChange(LOADED))
        inOrder.verify(eventHandler, Mockito.timeout(6000))
                .receive(and(argThat(pageChangeTo(0)), argThat {
                    it instanceof PageChange
                            && it.page().page().sentences().get(0).tokens().get(0).token() == "Berlin"
                }))
    }

    def "open() in a model which has LOADED state should properly reset"() {
        def eventHandler = Mockito.mock(EventHandler)
        def inOrder = Mockito.inOrder(eventHandler)

        given:
        def model = new ReadModel(wordDb)
        model.subscribe {
            eventHandler.receive(it)
        }

        model.open(shortKafka)
        inOrder.verify(eventHandler, Mockito.timeout(16000))
                .receive(new StateChange(LOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000))
                .receive(argThat pageChangeTo(0))

        when:
        model.nextPage()
        inOrder.verify(eventHandler, Mockito.timeout(4000))
                .receive(argThat pageChangeTo(1))
        model.open("Berlin ist eine wunderschöne Stadt in Deutschland. Es ist bekannt für seine reiche Geschichte und Kultur. " +
                "Viele Touristen besuchen die Berliner Mauer und das Brandenburger Tor. Auch das Essen in Berlin, insbesondere die Würste, ist sehr beliebt.")
        inOrder.verify(eventHandler, Mockito.timeout(1000)).receive(new StateChange(UNLOADED))
        inOrder.verify(eventHandler, Mockito.timeout(1000)).receive(new StateChange(LOADING))
        inOrder.verify(eventHandler, Mockito.timeout(16000)).receive(new StateChange(LOADED))
        inOrder.verify(eventHandler, Mockito.timeout(6000))
                .receive(and(argThat(pageChangeTo(0)), argThat {
                            it instanceof PageChange
                                    && it.page().page().sentences().get(0).tokens().get(0).token() == "Berlin"
                }))
        model.prevPage()

        then:
        inOrder.verify(eventHandler, Mockito.timeout(6000).times(0))
                .receive(argThat pageChangeTo(0))
    }

    def pageChangeTo(int n) {
        new ArgumentMatcher<ModelEvent>() {
            @Override
            boolean matches(ModelEvent e) {
                return e instanceof PageChange && e.n() == n
            }

            @Override
            String toString() {
                return "[PageChange event changing to page $n]"
            }
        }
    }

    def shortKafka = """
            Jemand mußte Josef K. verleumdet haben, denn ohne daß er etwas Böses
getan hätte, wurde er eines Morgens verhaftet. Die Köchin der Frau
Grubach, seiner Zimmervermieterin, die ihm jeden Tag gegen acht Uhr
früh das Frühstück brachte, kam diesmal nicht. Das war noch niemals
geschehen. K. wartete noch ein Weilchen, sah von seinem Kopfkissen aus
die alte Frau, die ihm gegenüber wohnte und die ihn mit einer an ihr
ganz ungewöhnlichen Neugierde beobachtete, dann aber, gleichzeitig
befremdet und hungrig, läutete er. Sofort klopfte es und ein Mann, den
er in dieser Wohnung noch niemals gesehen hatte, trat ein. Er war
schlank und doch fest gebaut, er trug ein anliegendes schwarzes Kleid,
das ähnlich den Reiseanzügen mit verschiedenen Falten, Taschen,
Schnallen, Knöpfen und einem Gürtel versehen war und infolgedessen,
ohne daß man sich darüber klar wurde, wozu es dienen sollte, besonders
praktisch erschien. „Wer sind Sie?“ fragte K. und saß gleich halb
aufrecht im Bett. Der Mann aber ging über die Frage hinweg, als müsse
man seine Erscheinung hinnehmen, und sagte bloß seinerseits: „Sie haben
geläutet?“ „Anna soll mir das Frühstück bringen,“ sagte K. und
versuchte zunächst stillschweigend durch Aufmerksamkeit und Überlegung
festzustellen, wer der Mann eigentlich war. Aber dieser setzte sich
nicht allzu lange seinen Blicken aus, sondern wandte sich zur Tür, die
er ein wenig öffnete, um jemandem, der offenbar knapp hinter der Tür
stand, zu sagen: „Er will, daß Anna ihm das Frühstück bringt.“ Ein
kleines Gelächter im Nebenzimmer folgte, es war nach dem Klang nicht
sicher, ob nicht mehrere Personen daran beteiligt waren. Trotzdem der
fremde Mann dadurch nichts erfahren haben konnte, was er nicht schon
früher gewußt hätte, sagte er nun doch zu K. im Tone einer Meldung: „Es
ist unmöglich.“ „Das wäre neu,“ sagte K., sprang aus dem Bett und zog
rasch seine Hosen an. „Ich will doch sehn, was für Leute im Nebenzimmer
sind und wie Frau Grubach diese Störung mir gegenüber verantworten
wird.“
"""
}
