package org.example.textprocessor;

import java.io.*;

public class Util {
    private static Process lemmatizer;

    private static BufferedReader outputStream;
    private static OutputStreamWriter inputStream;

    public static void initLemmatizer() {
        if (lemmatizer != null) {
            return;
        }
        try {
            lemmatizer = new ProcessBuilder("python3",
                    "src/main/python/lemmatize.py", Util.class.getResource("/model/IWNLP.Lemmatizer_20181001.json").getFile())
                    .start();

            Thread thread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(lemmatizer.getErrorStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.setDaemon(true);
            thread.start();

            outputStream = new BufferedReader(new InputStreamReader(lemmatizer.getInputStream()));
            inputStream = new OutputStreamWriter(lemmatizer.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String lemmatizeWithIWNLP(TextProcessor.POSToken token) {
        try {
            String pos = token.pos();
            if (pos.startsWith("$")) {
                return token.token();
            } else {
                if (pos.startsWith("ADJ")) {
                    pos = "ADJ";
                } else if (pos.equals("ART")) {
                    pos = "DET";
                } else if (pos.equals("ADV")) {

                } else if (pos.equals("KON")) {
                    pos = "CCONJ";
                } else if (pos.equals("ITJ")) {
                    pos = "INTJ";
                } else if (pos.equals("NN")) {
                    pos = "NOUN";
                } else if (pos.equals("NE")) {
                    pos = "PROPN";
                } else if (pos.equals("CARD")) {
                    pos = "NUM";
                } else if (pos.startsWith("PTK")) {
                    pos = "PART";
                } else if (pos.startsWith("P")) {
                    pos = "PRON";
                } else if (pos.startsWith("KOU")) {
                    pos = "SCONJ";
                } else if (pos.equals("XY")) {
                    pos = "SYM";
                } else if (pos.startsWith("V")) {
                    pos = "VERB";
                } else {
                    pos = "X";
                }
            }
            inputStream.write(token.token() + "," + pos + '\n');
            inputStream.flush();

            String s = outputStream.readLine();
            if (s.isEmpty()) {
                System.out.println(token.token() + " wasn't found (" + token.pos() + "->" + pos + ")");
            }
            return s.isEmpty() ? null : s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
