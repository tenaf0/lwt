package org.example.textprocessor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {
    @Test
    void test() {
        Util.initLemmatizer();
        String s = Util.lemmatizeWithIWNLP(new TextProcessor.POSToken("Hunde", "NOUN"));
        assertEquals("Hund", s);
    }

}