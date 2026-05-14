package data_management;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;

class FileDataReaderTest {

    @BeforeEach
    void reset() {
        DataStorage.resetForTesting();
    }

    @Test
    void readsBasicLine(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("ECG.txt"),
                "Patient ID: 1, Timestamp: 1714376789050, Label: ECG, Data: 0.5\n");
        DataStorage s = DataStorage.getInstance();
        new FileDataReader(dir.toString()).readData(s);
        List<PatientRecord> rs = s.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(1, rs.size());
        assertEquals(0.5, rs.get(0).getMeasurementValue());
    }

    @Test
    void stripsPercent(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("Saturation.txt"),
                "Patient ID: 2, Timestamp: 1000, Label: Saturation, Data: 95.0%\n");
        DataStorage s = DataStorage.getInstance();
        new FileDataReader(dir.toString()).readData(s);
        assertEquals(95.0, s.getRecords(2, 0, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void encodesAlertStrings(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("Alert.txt"),
                "Patient ID: 3, Timestamp: 1, Label: Alert, Data: triggered\n"
                + "Patient ID: 3, Timestamp: 2, Label: Alert, Data: resolved\n");
        DataStorage s = DataStorage.getInstance();
        new FileDataReader(dir.toString()).readData(s);
        List<PatientRecord> rs = s.getRecords(3, 0, Long.MAX_VALUE);
        assertEquals(1.0, rs.get(0).getMeasurementValue());
        assertEquals(0.0, rs.get(1).getMeasurementValue());
    }

    @Test
    void skipsMalformedLines(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("ECG.txt"),
                "garbage\n"
                + "Patient ID: 1, Timestamp: 1, Label: ECG, Data: 0.5\n"
                + "more garbage\n");
        DataStorage s = DataStorage.getInstance();
        new FileDataReader(dir.toString()).readData(s);
        assertEquals(1, s.getRecords(1, 0, Long.MAX_VALUE).size());
    }

    @Test
    void readsMultipleFiles(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("ECG.txt"),
                "Patient ID: 1, Timestamp: 1, Label: ECG, Data: 0.1\n");
        Files.writeString(dir.resolve("Saturation.txt"),
                "Patient ID: 1, Timestamp: 2, Label: Saturation, Data: 95.0%\n");
        DataStorage s = DataStorage.getInstance();
        new FileDataReader(dir.toString()).readData(s);
        assertEquals(2, s.getRecords(1, 0, Long.MAX_VALUE).size());
    }

    @Test
    void missingDirectoryThrows() {
        assertThrows(IOException.class,
                () -> new FileDataReader("/no/such").readData(DataStorage.getInstance()));
    }
}
