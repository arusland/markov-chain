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
    private boolean useSmartNextWord = true;
    private final List<String> commaTokens = Arrays.asList("но", "а", "что", "чтобы",
            "который", "которая", "которые", "которую", "когда");

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

    public boolean isUseSmartNextWord() {
        return useSmartNextWord;
    }

    public void setUseSmartNextWord(boolean useSmartNextWord) {
        this.useSmartNextWord = useSmartNextWord;
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

    private String getFirstWord() {
        return getNextWord(Wordogram.TOKEN_END);
    }

    private String getNextWord(String word) {
        return useSmartNextWord ? getNextWordSmart(word) : getNextWordSimple(word);
    }

    /**
     * Returns next word according to frequency of next words.
     */
    private String getNextWordSmart(String word) {
        Map<String, Integer> map = words.get(word);

        if (map != null) {
            Map<String, Integer[]> wordRanges = new HashMap<>();
            int maxEdge = 0;

            for (String nextWord : map.keySet()) {
                int nextMaxEdge = maxEdge + map.get(nextWord);
                wordRanges.put(nextWord, new Integer[]{maxEdge, nextMaxEdge - 1});
                maxEdge = nextMaxEdge;
            }

            int index = random.nextInt(maxEdge);

            for (String nextWord : wordRanges.keySet()) {
                Integer[] range = wordRanges.get(nextWord);

                if (index >= range[0] && index <= range[1]) {
                    return nextWord;
                }
            }

            throw new RuntimeException("Unreachable code reached!");
        }

        return null;
    }

    /**
     * Returns simply random next word.
     */
    private String getNextWordSimple(String word) {
        Map<String, Integer> map = words.get(word);

        if (map != null) {
            ArrayList<String> wrds = new ArrayList<String>(map.keySet());

            return wrds.get(random.nextInt(wrds.size()));
        }

        return null;
    }
}