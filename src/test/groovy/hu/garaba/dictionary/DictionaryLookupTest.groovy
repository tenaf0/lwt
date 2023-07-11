package hu.garaba.dictionary;

import spock.lang.Specification;

class DictionaryLookupTest extends Specification {
    def "should find the given word"() {
        given:
        def dictionaryLookup = new CollinsDictionaryLookup()

        when:
        def results = dictionaryLookup.search("Gesetz")

        then:
        results.size() == 1
    }

    def "should find the given word2"() {
        given:
        def dictionaryLookup = new CollinsDictionaryLookup()

        when:
        def result = dictionaryLookup.lookup("Gesetz")

        then:
        result.lemma() == "Gesetz"
        result.grammar() == "neuter noun"
    }
}
