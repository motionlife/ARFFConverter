/**
 * Created by Hao Xiong on 3/16/2017.
 * Copyright belongs to Hao Xiong, Email: haoxiong@outlook.com
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {
    private static final String ZIP_TRAIN = "dataset/ZipFiles/enron1_train.zip";
    private static final String ZIP_TEST = "dataset/ZipFiles/enron1_test.zip";
    private static Set<String> dictionary;

    public static void main(String[] args) {
        List<String> word_list = countWords(ZIP_TRAIN);
        ArrayList<TextVector> vectors = toVectors(ZIP_TRAIN,"ham",word_list);
        vectors.addAll(toVectors(ZIP_TRAIN,"spam",word_list));

    }

    private static List<String> countWords(String trainingZip) {
        dictionary = new LinkedHashSet<>();
        try (ZipFile zipFile = new ZipFile(trainingZip)) {
            Predicate<ZipEntry> isFile = ze -> !ze.isDirectory();
            Predicate<ZipEntry> isHam = ze -> ze.getName().matches(".*txt");

            List<ZipEntry> result = zipFile.stream().filter(isFile.and(isHam)).collect(Collectors.toList());
            result.forEach(ze -> {
                String line;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)))) {
                    while ((line = br.readLine()) != null) {
                        String[] words = line.split(" ");
                        Collections.addAll(dictionary, words);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            // error while opening a ZIP file
            e.printStackTrace();
        }
        return new ArrayList<>(dictionary);
    }

    private static ArrayList<TextVector> toVectors(String zip, String type, List<String> word_list) {
        ArrayList<TextVector> vectors = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(zip)) {
            Predicate<ZipEntry> isFile = ze -> !ze.isDirectory();
            Predicate<ZipEntry> isSpecified = ze -> ze.getName().matches(".*" + type + ".txt");

            List<ZipEntry> result = zipFile.stream().filter(isFile.and(isSpecified)).collect(Collectors.toList());
            result.forEach(ze -> {
                String line;
                int[] features = new int[word_list.size()];
                try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)))) {
                    while ((line = br.readLine()) != null) {
                        String[] words = line.split(" ");
                        for (String word : words) {
                            int index = word_list.indexOf(word);
                            if (index != -1) features[index]++;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                vectors.add(new TextVector(features, type));
            });

        } catch (IOException e) {
            // error while opening a ZIP file
            e.printStackTrace();
        }
        return vectors;
    }

}

class TextVector {
    public int[] features;
    public String type;

    TextVector(int[] fts, String tp) {
        this.features = fts;
        this.type = tp;
    }
}