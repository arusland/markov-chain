package io.arusland.text.markov;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ruslan Absalyamov
 * @since 2017-03-07
 */
public class WordParser {
    private boolean debug;

    public List<String> parse(String raw, Map<String, String> names, Map<String, Integer> stats) {
        String content = raw.toLowerCase();
        List<String> words = new ArrayList<>();

        int startIndex = 0;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (isTokenChar(ch)) {
                continue;
            }

            if (i > startIndex) {
                String nextWord = content.substring(startIndex, i);
                String rawNextWord = raw.substring(startIndex, i);

                if (!rawNextWord.equals(nextWord) &&
                        !isLastEndWord(words) && words.size() > 0) {

                    if (!names.containsKey(nextWord)) {
                        if (debug) {
                            printContext(raw, startIndex, nextWord);
                        }

                        names.put(nextWord, rawNextWord);
                    }
                }

                if (isLegalWord(nextWord)) {
                    words.add(nextWord);
                    putWordStats(stats, rawNextWord);
                } else {
                    addEndChar(words);
                }
            }

            if (isEndChar(ch)) {
                addEndChar(words);
            }

            startIndex = i + 1;
        }

        if (startIndex < content.length()) {
            words.add(content.substring(startIndex));
        }

        addEndChar(words);

        List<String> allNames = new ArrayList<>(names.keySet());

        for (String name : allNames) {
            if (!isRealName(names.get(name), stats)){
                names.remove(name);
            }
        }

        return words.stream()
                .map(word -> names.containsKey(word) ? names.get(word) : word)
                .collect(Collectors.toList());
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private void printContext(String raw, int startIndex, String nextWord) {
        int hightlightStart = Math.max(0, startIndex - 20);
        int highlightEnd = Math.min(raw.length(), startIndex + 20);
        System.out.println("name: " + nextWord + "; context: '..." +
                raw.substring(hightlightStart, highlightEnd).replaceAll("\n", "") + "...'");
    }

    private boolean isRealName(String rawName, Map<String, Integer> stats) {
        Integer rawCount = stats.get(rawName);
        Integer count = stats.get(rawName.toLowerCase());

        if (count != null && count > rawCount) {
            return false;
        }

        return true;
    }

    private void putWordStats(Map<String, Integer> stats, String word) {
        Integer count = stats.get(word);

        if (count != null) {
            stats.put(word, count + 1);
        } else {
            stats.put(word, 1);
        }
    }

    private void addEndChar(List<String> words) {
        if (words.size() > 0 && !isLastEndWord(words)) {
            words.add(Wordogram.TOKEN_END);
        }
    }

    private boolean isLastEndWord(List<String> words) {
        return words.size() > 0 && words.get(words.size() - 1).equals(Wordogram.TOKEN_END);
    }

    private boolean isEndChar(char ch) {
        return ch == '.' || ch == '?' || ch == '!' || ch == ';' ||
                ch == '…' || ch == ':' || ch == '[' || ch == ']';
    }

    private boolean isLegalWord(String word) {
        if (word.length() == 1 && word.charAt(0) == '-'){
            return false;
        }

        for (int i = 0; i < word.length(); i++) {
            if (!Character.isDigit(word.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    private boolean isRussianChar(char ch) {
        return ch >= 'а' && ch <= 'я' || ch == 'ё';
    }

    private boolean isTokenChar(char ch) {
        return //Character.isAlphabetic(ch)
                isRussianChar(ch) || ch == '-' || Character.isDigit(ch);
    }
}
