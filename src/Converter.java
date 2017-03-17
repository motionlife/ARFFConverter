/**
 * Created by Hao Xiong on 3/16/2017.
 * Copyright belongs to Hao Xiong, Email: haoxiong@outlook.com
 */

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Converter {
    private static final String ARFF_DIR = "dataset/ARFF";
    private static final String ZIP_DIR = "dataset/ZipFiles";
    private static final String ENRON1_TRAIN = "enron1_train";
    private static final String ENRON1_TEST = "enron1_test";
    private static final String ENRON4_TRAIN = "enron4_train";
    private static final String ENRON4_TEST = "enron4_test";
    private static final String HW2_TRAIN = "hw2_train";
    private static final String HW2_TEST = "hw2_test";
    private static final String COMMENTS = "% 1. Title: SPAM/HAM data sets\n" +
            "% \n" +
            "% 2. Sources:\n" +
            "%     (a) Creator: Hao Xiong\n" +
            "%     (b) DataSource: Data sets from CS7301 Machine Learning homework 3\n" +
            "%     (c) Date: March, 2017\n" +
            "% \n";

    public static void main(String[] args) {
        convert(ENRON1_TRAIN, ENRON1_TEST);
        convert(ENRON4_TRAIN, ENRON4_TEST);
        convert(HW2_TRAIN, HW2_TEST);
        System.out.println("Converted Success! Check " + ARFF_DIR + " folder!");
    }

    /**
     * Convert a train zip(ham and spam) and its corresponding test zip(ham and spam) at the same time
     */
    private static void convert(String trainRelation, String testRelation) {
        String trainZipFile = ZIP_DIR + "/" + trainRelation + ".zip";
        String testZipFile = ZIP_DIR + "/" + testRelation + ".zip";
        List<String> word_list = countWords(trainZipFile);
        ArrayList<TextVector> vectors = toVectors(trainZipFile, "ham", word_list);
        vectors.addAll(toVectors(trainZipFile, "spam", word_list));
        toARFF(vectors, word_list.size(), trainRelation);
        vectors = toVectors(testZipFile, "ham", word_list);
        vectors.addAll(toVectors(testZipFile, "spam", word_list));
        toARFF(vectors, word_list.size(), testRelation);
    }

    /**
     * Convert a list of text vectors to an arff file
     */
    private static void toARFF(ArrayList<TextVector> vectors, int size, String relation) {
        StringBuilder content = new StringBuilder(COMMENTS);
        content.append("@RELATION ").append(relation).append("\n\n");
        for (int i = 0; i < size; i++) {
            content.append("@ATTRIBUTE w").append(i).append(" integer\n");
        }
        content.append("@ATTRIBUTE class  {ham,spam}\n\n").append("@DATA\n");
        for (TextVector tv : vectors) {
            content.append("{");
            int i = 0;
            for (; i < tv.features.length; i++) {
                if (tv.features[i] != 0) content.append(i).append(" ").append(tv.features[i]).append(",");
            }
            content.append(i).append(" \"").append(tv.type).append("\"}\n");
        }
        try {
            File file = new File(ARFF_DIR + "/" + relation + ".arff");
            if (!file.getParentFile().exists()) file.getParentFile().mkdir();
            if (!file.exists()) file.createNewFile();
            PrintWriter pw = new PrintWriter(file);
            pw.print(content.toString());
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Count the words in the training data set making an ordered set use as a reference for vectors
     */
    private static List<String> countWords(String trainingZip) {
        Set<String> dictionary = new LinkedHashSet<>();
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

    /**
     * Read a zip file and convert the original text file into list of text vectors
     */
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

/**
 * Define the structure of a text vector
 */
class TextVector {
    int[] features;
    String type;

    TextVector(int[] fts, String tp) {
        this.features = fts;
        this.type = tp;
    }
}