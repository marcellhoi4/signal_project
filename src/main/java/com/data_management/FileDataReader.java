package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Reads patient data from the text files written by FileOutputStrategy.
 * Each line looks like: Patient ID: 1, Timestamp: 1714376789050, Label: ECG, Data: 0.5
 */
public class FileDataReader implements DataReader {

    private final String directoryPath;

    public FileDataReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) throw new IOException("Not a directory: " + directoryPath);
        File[] files = dir.listFiles((d, n) -> n.endsWith(".txt"));
        if (files == null) return;
        for (File f : files) {
            try (BufferedReader r = Files.newBufferedReader(f.toPath())) {
                String line;
                while ((line = r.readLine()) != null) parseLine(line, dataStorage);
            }
        }
    }

    private void parseLine(String line, DataStorage storage) {
        String[] parts = line.split(",");
        if (parts.length < 4) return;
        try {
            int id = Integer.parseInt(extract(parts[0], "Patient ID:"));
            long ts = Long.parseLong(extract(parts[1], "Timestamp:"));
            String label = extract(parts[2], "Label:");
            String raw = extract(parts[3], "Data:");
            storage.addPatientData(id, parseValue(label, raw), label, ts);
        } catch (IllegalArgumentException e) {
            // skip bad line
        }
    }

    private String extract(String segment, String prefix) {
        int idx = segment.indexOf(prefix);
        if (idx < 0) throw new IllegalArgumentException("missing prefix");
        return segment.substring(idx + prefix.length()).trim();
    }

    private double parseValue(String label, String raw) {
        if (raw.endsWith("%")) raw = raw.substring(0, raw.length() - 1);
        if ("Alert".equals(label)) {
            if (raw.equalsIgnoreCase("triggered")) return 1.0;
            if (raw.equalsIgnoreCase("resolved")) return 0.0;
        }
        return Double.parseDouble(raw);
    }
}
