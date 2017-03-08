package io.arusland.text.markov;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruslan Absalyamov
 * @since 2017-03-08
 */
public class WordParserTest {
    @Test
    public void test() {
        WordParser parser = new WordParser();
        Wordogram wordogram = new Wordogram();
        Map<String, String> names = new HashMap<>();
        Map<String, Integer> stats = new HashMap<>();

        String raw = "по-русски. Цепь Маркова — последовательность случайных событий с конечным или счётным числом исходов, характеризующаяся тем свойством, что, говоря нестрого, при фиксированном настоящем будущее независимо от прошлого. Названа в честь А. А. Маркова (старшего).";
        List<String> words = parser.parse(raw, names, stats);


        words.forEach(word -> wordogram.addNext(word));

        for (String word : wordogram.getWords().keySet()) {
            Map<String, Integer> map = wordogram.getWords().get(word);

            System.out.println(word + ": " + map);
        }

        Generator generator = new Generator(wordogram.getWords());

        for (int i = 0; i < 5; i++) {
            System.out.println(generator.generate(140));
        }
    }

    @Test
    public void testFromFile() throws URISyntaxException, IOException {
        WordParser parser = new WordParser();
        Wordogram wordogram = new Wordogram();
        Map<String, String> names = new HashMap<>();
        Map<String, Integer> stats = new HashMap<>();

        String raw = new String(Files.readAllBytes(
                new File("d:\\WORK\\MyProjects\\Markov_chain\\test\\war_and_peace_1.txt").toPath()));
        List<String> words = parser.parse(raw, names, stats);

        words.forEach(word -> wordogram.addNext(word));

        for (String name : names.keySet()) {
            System.out.println(names.get(name));
        }

        System.out.println("raw length: " + raw.length());
        System.out.println("words size: " + wordogram.getWords().size());

        Generator generator = new Generator(wordogram.getWords());

        for (int i = 0; i < 5; i++) {
            System.out.println(generator.generate(140));
        }
    }
}
