package io.arusland.text;

import io.arusland.text.markov.Generator;
import io.arusland.text.markov.WordParser;
import io.arusland.text.markov.Wordogram;
import org.apache.commons.lang3.StringUtils;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ruslan on 07.01.2017.
 */
public class Main {
    private final Wordogram wordogram = new Wordogram();
    private final WordParser parser = new WordParser();
    private final Map<String, String> names = new HashMap<>();
    private final Map<String, Integer> stats = new HashMap<>();
    private Generator generator;

    public static void main(String args[]) throws IOException {
        new Main().run(args);
    }

    private void run(String[] args) throws IOException {
        System.out.println("Markov chain based text generator v1.0");
        System.out.println("Type 'h' for help");
        Console console = System.console();

        if (args.length > 0) {
            handleCommand(Arrays.asList("load", args[0]));
        }

        while (true) {
            String line = console.readLine();

            List<String> cmd = Arrays.stream(line.split("\\s+"))
                    .filter(p -> StringUtils.isNoneBlank(p))
                    .collect(Collectors.toList());

            if (!handleCommand(cmd)) {
                System.out.println("Invalid command!");
            }
        }
    }

    private boolean handleCommand(List<String> cmd) {
        if (cmd.isEmpty()) {
            return false;
        }

        try {
            String command = cmd.get(0);

            if ("q".equals(command)) {
                System.exit(0);
            } else if ("h".equals(command)) {
                printHelp();
            } else if ("stat".equals(command)) {
                printStats(cmd);
            } else if ("gen".equals(command)) {
                generateText(cmd);
            } else if ("load".equals(command)) {
                loadFile(cmd.get(1));
            } else if ("clear".equals(command)) {
                clear();
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }

        return true;
    }

    private void clear() {
        wordogram.clear();
        names.clear();
        generator = null;
        System.out.println("All buffers are cleared.");
    }

    private void generateText(List<String> cmd) {
        Generator generator = getGenerator();

        String firstWord = cmd.size() >= 3 ? cmd.get(2) : null;
        String text = generator.generate(Integer.parseInt(cmd.get(1)), firstWord);

        System.out.println(text);
        System.out.println("");
    }

    private void printHelp() {
        System.out.println("q - exit");
        System.out.println("stat - Prints statistics");
        System.out.println("stat <word> - Prints statistics related with word");
        System.out.println("load <file_name> - Loads file");
        System.out.println("clear - Clears all buffers");
        System.out.println("gen <max_symbols_count> - Generates text");
        System.out.println("gen <max_symbols_count> <first_word> - Generates text started with <first_word>");
    }

    private void printStats(List<String> cmd) {
        if (cmd.isEmpty() || cmd.size() == 1) {
            Map<String, Map<String, Integer>> words = wordogram.getWords();
            System.out.println("unique words: " + words.size());
            System.out.println("names       : " + names.size());
        } else if (cmd.size() > 1) {
            String word = cmd.get(1);
            String wordCap = StringUtils.capitalize(word);
            String wordLowed = word.toLowerCase();

            boolean wordExists = printStats(word);

            if (!wordCap.equals(word)) {
                wordExists |= printStats(wordCap);
            }

            if (!wordLowed.equals(word) && !wordLowed.equals(wordCap)) {
                wordExists |= printStats(wordLowed);
            }

            if (!wordExists) {
                System.out.println("Unknown word!");
            }
        }
    }

    private boolean printStats(String word) {
        Map<String, Integer> map = wordogram.getWords().get(word);
        Integer count = stats.get(word);

        if (map != null || count != null) {
            System.out.println("Statistics for word '" + word + "'");

            if (map != null) {
                System.out.println("There are " + map.size() + " words after the word '" + word + "':");
                List<String> sortedWords = sortByCount(map);

                for (String nextWord : sortedWords) {
                    if (Wordogram.TOKEN_END.equals(nextWord)) {
                        System.out.println("  <END>: " + map.get(nextWord));
                    } else {
                        System.out.println("  " + nextWord + ": " + map.get(nextWord));
                    }
                }
            }

            if (count != null) {
                System.out.println("Used " + count + " times");
            }
            System.out.println("");

            return true;
        }

        return false;
    }

    private void loadFile(String fileName) throws IOException {
        System.out.println("Loading file " + fileName);
        File file = new File(fileName);
        String raw = new String(Files.readAllBytes(file.toPath()));
        List<String> words = parser.parse(raw, names, stats);
        words.forEach(word -> wordogram.addNext(word));

        generator = null;
        printStats(Collections.emptyList());
    }

    private Generator getGenerator() {
        return generator != null ? generator : (generator = new Generator(wordogram.getWords()));
    }

    private List<String> sortByCount(Map<String, Integer> words) {
        List<String> result = new ArrayList<>(words.keySet());

        result.sort((s1, s2) -> {
            int cmd = words.get(s2).compareTo(words.get(s1));

            if (cmd != 0) {
                return cmd;
            }

            return s1.compareTo(s2);
        });

        return result;
    }
}
