package io.arusland.text.markov;

import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.*;

/**
 * @author Ruslan Absalyamov
 * @since 2017-03-08
 */
public class Generator {
    private final Map<String, Map<String, Integer>> words;
    private final SecureRandom random = new SecureRandom();
    private final List<String> commaTokens = Arrays.asList("но", "а", "что", "чтобы", "который", "которая", "которые", "когда");

    public Generator(Map<String, Map<String, Integer>> words) {
        this.words = words;
    }

    public String generate(int charCountMax) {
        return generate(charCountMax, null);
    }

    public String generate(int charCountMax, String firstWord) {
        StringBuilder sb = new StringBuilder(charCountMax);

        String word = selectFirstWord(firstWord);
        String prevWord = null;

        while (word != null) {
            String prefix = makePrefix(word, prevWord);

            if ((sb.length() + word.length() + prefix.length()) > charCountMax) {
                break;
            }

            if (!prefix.isEmpty()) {
                sb.append(prefix);
            }

            if (Wordogram.TOKEN_END.equals(prevWord) || sb.length() == 0) {
                sb.append(StringUtils.capitalize(word));
            } else {
                sb.append(word);
            }

            prevWord = word;
            word = getNextWord(word);
        }

        if (!Wordogram.TOKEN_END.equals(prevWord) && sb.length() < charCountMax) {
            sb.append(Wordogram.TOKEN_END);
        }

        return sb.toString();
    }

    private String selectFirstWord(String firstWord) {
        if (StringUtils.isBlank(firstWord)) {
            return getFirstWord();
        }

        if (words.containsKey(firstWord)) {
            return firstWord;
        }

        firstWord = firstWord.toLowerCase();

        if (words.containsKey(firstWord)) {
            return firstWord;
        }

        firstWord = StringUtils.capitalize(firstWord);

        return firstWord;
    }

    private String makePrefix(String nextWord, String prevWord) {
        if (prevWord != null && !Wordogram.TOKEN_END.equals(nextWord)) {
            if (!Wordogram.TOKEN_END.equals(prevWord) && commaTokens.contains(nextWord)) {
                return ", ";
            }

            return " ";
        }

        return "";
    }

    private String getNextWord(String word) {
        Map<String, Integer> map = words.get(word);

        if (map != null) {
            ArrayList<String> wrds = new ArrayList<String>(map.keySet());

            return wrds.get(random.nextInt(wrds.size()));
        }

        return null;
    }

    private String getFirstWord() {
        Map<String, Integer> map = words.get(Wordogram.TOKEN_END);

        if (map != null) {
            ArrayList<String> wrds = new ArrayList<String>(map.keySet());

            return wrds.get(random.nextInt(wrds.size()));
        }

        return null;
    }
}