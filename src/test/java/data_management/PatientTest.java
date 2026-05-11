package data_management;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

class PatientTest {

    @Test
    void getRecordsFiltersByTimeRange() {
        Patient p = new Patient(1);
        p.addRecord(70, "ECG", 100);
        p.addRecord(80, "ECG", 200);
        p.addRecord(90, "ECG", 300);

        List<PatientRecord> out = p.getRecords(150, 250);
        assertEquals(1, out.size());
        assertEquals(80.0, out.get(0).getMeasurementValue());
    }

    @Test
    void getRecordsBoundsAreInclusive() {
        Patient p = new Patient(1);
        p.addRecord(1, "ECG", 100);
        p.addRecord(2, "ECG", 200);
        assertEquals(2, p.getRecords(100, 200).size());
    }

    @Test
    void getRecordsEmptyWhenNoneInRange() {
        Patient p = new Patient(1);
        p.addRecord(1, "ECG", 100);
        assertTrue(p.getRecords(200, 300).isEmpty());
    }

    @Test
    void getPatientIdReturnsValue() {
        assertEquals(42, new Patient(42).getPatientId());
    }
}
