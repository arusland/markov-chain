package io.arusland.text;

import io.arusland.text.markov.Generator;
import io.arusland.text.markov.WordParser;
import io.arusland.text.markov.Wordogram;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by ruslan on 07.01.2017.
 */
public class Main {
    private final  Wordogram wordogram = new Wordogram();
    private WordParser parser = new WordParser();
    private Generator generator;
    private Map<String, String> names = new HashMap<>();
    private Map<String, Integer> stats = new HashMap<>();

    public static void main(String args[]) throws IOException {
        if (args.length == 0) {
            System.out.println("Markov chain text generator v1.0");
            System.out.println("Usage:\n\tmarkov file");
            System.exit(1);
        }

        new Main().run(args);
    }

    private void run(String[] args) throws IOException {
        System.out.println("Type 'h' for help");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        handleCommand(Arrays.asList("load", args[0]));

        while (true) {
            String line = br.readLine();

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
            }

            if ("h".equals(command)) {
                printHelp();
            }

            if ("gen".equals(command)) {
                generateText(cmd);
            }

            if ("load".equals(command)) {
                loadFile(cmd.get(1));
            }
        }
        catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }

        return true;
    }

    private void generateText(List<String> cmd) {
        Generator generator = getGenerator();

        String text = generator.generate(Integer.parseInt(cmd.get(1)));

        System.out.println("");
        System.out.println(text);
    }

    private void printHelp() {
        System.out.println("q - exit");
        System.out.println("load <file_name> - Loads file");
        System.out.println("gen <max_symbols_count> - Generate text");
    }

    private void loadFile(String fileName) throws IOException {
        System.out.println("Loading file " + fileName);
        File file  = new File(fileName);

        String raw = new String(Files.readAllBytes(file.toPath()));
        List<String> words = parser.parse(raw, names, stats);

        words.forEach(word -> wordogram.addNext(word));
        printStats();
    }

    private void printStats() {
        Map<String, Map<String, Integer>> words = wordogram.getWords();
        System.out.println("unique words: " + words.size());
        System.out.println("names       : " + names.size());
    }

    public Generator getGenerator() {
        return generator != null ? generator : (generator = new Generator(wordogram.getWords()));
    }
}
