package hu.garaba.buffer


import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.nio.file.Path

class PageReader2Test extends Specification {
    def "asd"() {
        Path book = Path.of(FileBufferReader.class.getResource("/kafka_prozess.txt").getFile());

        when:
        def pageReader = PageReader2.openFile(book)

        then:
        new PollingConditions().within(100) {
            assert pageReader.pageNo as PageReader2.PageNo.ExactPageNo
        }
    }
}
