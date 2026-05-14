package alerts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

class AlertGeneratorTest {

    private List<Alert> run(Patient p) {
        AlertGenerator g = new AlertGenerator(DataStorage.getInstance());
        g.evaluateData(p);
        return g.getAlerts();
    }

    private boolean has(List<Alert> alerts, String condition) {
        return alerts.stream().anyMatch(a -> a.getCondition().equals(condition));
    }

    @Test
    void bpCriticalThresholds() {
        Patient p = new Patient(1);
        p.addRecord(190, "SystolicPressure", 1);
        p.addRecord(80, "SystolicPressure", 2);
        p.addRecord(125, "DiastolicPressure", 3);
        p.addRecord(50, "DiastolicPressure", 4);
        List<Alert> a = run(p);
        assertTrue(has(a, "High Systolic Pressure"));
        assertTrue(has(a, "Low Systolic Pressure"));
        assertTrue(has(a, "High Diastolic Pressure"));
        assertTrue(has(a, "Low Diastolic Pressure"));
    }

    @Test
    void bpInRangeNoAlert() {
        Patient p = new Patient(1);
        p.addRecord(120, "SystolicPressure", 1);
        p.addRecord(80, "DiastolicPressure", 2);
        assertTrue(run(p).isEmpty());
    }

    @Test
    void bpIncreasingTrend() {
        Patient p = new Patient(1);
        p.addRecord(120, "SystolicPressure", 1);
        p.addRecord(135, "SystolicPressure", 2);
        p.addRecord(150, "SystolicPressure", 3);
        assertTrue(has(run(p), "Increasing Systolic Trend"));
    }

    @Test
    void bpDecreasingTrend() {
        Patient p = new Patient(1);
        p.addRecord(150, "SystolicPressure", 1);
        p.addRecord(135, "SystolicPressure", 2);
        p.addRecord(120, "SystolicPressure", 3);
        assertTrue(has(run(p), "Decreasing Systolic Trend"));
    }

    @Test
    void smallBpChangesNoTrend() {
        Patient p = new Patient(1);
        p.addRecord(120, "SystolicPressure", 1);
        p.addRecord(125, "SystolicPressure", 2);
        p.addRecord(130, "SystolicPressure", 3);
        assertFalse(has(run(p), "Increasing Systolic Trend"));
    }

    @Test
    void lowSaturation() {
        Patient p = new Patient(1);
        p.addRecord(90, "Saturation", 1);
        assertTrue(has(run(p), "Low Blood Saturation"));
    }

    @Test
    void rapidSaturationDrop() {
        Patient p = new Patient(1);
        p.addRecord(98, "Saturation", 0);
        p.addRecord(92.5, "Saturation", 5 * 60 * 1000);
        assertTrue(has(run(p), "Rapid Saturation Drop"));
    }

    @Test
    void slowSaturationDropNoAlert() {
        Patient p = new Patient(1);
        p.addRecord(98, "Saturation", 0);
        p.addRecord(94, "Saturation", 5 * 60 * 1000);
        assertFalse(has(run(p), "Rapid Saturation Drop"));
    }

    @Test
    void dropOutsideWindowNoAlert() {
        Patient p = new Patient(1);
        p.addRecord(98, "Saturation", 0);
        p.addRecord(92, "Saturation", 11 * 60 * 1000);
        assertFalse(has(run(p), "Rapid Saturation Drop"));
    }

    @Test
    void hypotensiveHypoxemia() {
        Patient p = new Patient(1);
        p.addRecord(85, "SystolicPressure", 1);
        p.addRecord(90, "Saturation", 1);
        assertTrue(has(run(p), "Hypotensive Hypoxemia"));
    }

    @Test
    void hypotensiveHypoxemiaOnlyOneLow() {
        Patient p = new Patient(1);
        p.addRecord(85, "SystolicPressure", 1);
        p.addRecord(95, "Saturation", 1);
        assertFalse(has(run(p), "Hypotensive Hypoxemia"));
    }

    @Test
    void ecgPeakTriggers() {
        Patient p = new Patient(1);
        for (int i = 0; i < 5; i++) p.addRecord(0.5, "ECG", i);
        p.addRecord(2.0, "ECG", 6);
        assertTrue(has(run(p), "Abnormal ECG"));
    }

    @Test
    void steadyEcgNoAlert() {
        Patient p = new Patient(1);
        for (int i = 0; i < 10; i++) p.addRecord(0.5, "ECG", i);
        assertFalse(has(run(p), "Abnormal ECG"));
    }

    @Test
    void manualAlertTriggered() {
        Patient p = new Patient(1);
        p.addRecord(1.0, "Alert", 1);
        assertTrue(has(run(p), "Manual Alert"));
    }

    @Test
    void manualAlertResolvedNoAlert() {
        Patient p = new Patient(1);
        p.addRecord(0.0, "Alert", 1);
        assertFalse(has(run(p), "Manual Alert"));
    }

    @Test
    void noRecordsNoAlerts() {
        assertTrue(run(new Patient(1)).isEmpty());
    }
}
