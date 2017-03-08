package io.arusland.text.markov;

import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Ruslan Absalyamov
 * @since 2017-03-08
 */
public class Generator {
    private final Map<String, Map<String, Integer>> words;
    private final SecureRandom random = new SecureRandom();

    public Generator(Map<String, Map<String, Integer>> words) {
        this.words = words;
    }

    public String generate(int charCountMax) {
        StringBuilder sb = new StringBuilder(charCountMax);

        String word = getFirstWord();
        String prevWord = null;

        while (word != null) {
            if ((sb.length() + word.length() + 1) > charCountMax) {
                break;
            }
            if (sb.length() > 0 && !Wordogram.TOKEN_END.equals(word)) {
                sb.append(' ');
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
