package io.arusland.text.markov;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruslan Absalyamov
 * @since 2017-03-07
 */
public class Wordogram {
    public final static String TOKEN_END = ".";
    private final Map<String, Map<String, Integer>> words = new HashMap<>();
    private String lastWord = TOKEN_END;

    public void addNext(String word) {
        Map<String, Integer> wordMap = words.get(lastWord);

        if (wordMap == null) {
            wordMap = new HashMap<>();
            words.put(lastWord, wordMap);
        }

        Integer count = wordMap.get(word);

        if (count != null) {
            wordMap.put(word, count + 1);
        } else {
            wordMap.put(word, 1);
        }

        lastWord = word;
    }

    public void addEndWord() {
        addNext(TOKEN_END);
    }

    public Map<String, Map<String, Integer>> getWords() {
        return words;
    }
}
