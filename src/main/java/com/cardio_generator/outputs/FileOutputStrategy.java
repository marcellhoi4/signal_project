package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes patient data to text files. One file per label (ECG.txt,
 * Saturation.txt, ...) inside the chosen directory. New lines are appended.
 */
public class FileOutputStrategy implements OutputStrategy {

    // renamed from BaseDirectory. Fields should be camelCase (Google Java Style §5.2.5)
    private String baseDirectory;

    // renamed from file_map. Identifiers shouldn't contain underscores (§5.2.5)
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * @param baseDirectory directory where the output files will live
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    /**
     * Appends one line to the file for the given label. The directory and the
     * label file are created the first time they are needed. If something
     * goes wrong the error is printed to stderr and the call just returns.
     *
     * @param patientId patient ID
     * @param timestamp ms since epoch when the data was produced
     * @param label data type, also used as the file name
     * @param data the value to write
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // renamed from FilePath. Local variables should also be camelCase (§5.2.6)
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}
