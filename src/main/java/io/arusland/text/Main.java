package io.arusland.text;

import io.arusland.text.markov.Generator;
import io.arusland.text.markov.WordParser;
import io.arusland.text.markov.Wordogram;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        System.out.println("Type 'h' for help");
        System.out.println("Default charset: " +
                Charset.defaultCharset().name());

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
                printStats();
            } else if ("gen".equals(command)) {
                generateText(cmd);
            } else if ("load".equals(command)) {
                loadFile(cmd.get(1));
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }

        return true;
    }

    private void generateText(List<String> cmd) {
        Generator generator = getGenerator();

        String firstWord = cmd.size() >= 3 ? cmd.get(2) : null;
        String text = generator.generate(Integer.parseInt(cmd.get(1)), firstWord);

        System.out.println("");
        System.out.println(text);
    }

    private void printHelp() {
        System.out.println("q - exit");
        System.out.println("stat - Prints statistics");
        System.out.println("load <file_name> - Loads file");
        System.out.println("gen <max_symbols_count> - Generate text");
        System.out.println("gen <max_symbols_count> <first_word> - Generate text which starts with <first_word>");
    }

    private void printStats() {
        Map<String, Map<String, Integer>> words = wordogram.getWords();
        System.out.println("unique words: " + words.size());
        System.out.println("names       : " + names.size());
    }

    private void loadFile(String fileName) throws IOException {
        System.out.println("Loading file " + fileName);
        File file = new File(fileName);
        String raw = new String(Files.readAllBytes(file.toPath()));
        List<String> words = parser.parse(raw, names, stats);
        words.forEach(word -> wordogram.addNext(word));

        generator = null;
        printStats();
    }

    private Generator getGenerator() {
        return generator != null ? generator : (generator = new Generator(wordogram.getWords()));
    }
}
