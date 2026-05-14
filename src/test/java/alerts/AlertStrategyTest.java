package alerts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.BloodPressureStrategy;
import com.alerts.HeartRateStrategy;
import com.alerts.OxygenSaturationStrategy;
import com.data_management.Patient;

class AlertStrategyTest {

    private boolean has(List<Alert> alerts, String condition) {
        return alerts.stream().anyMatch(a -> a.getCondition().equals(condition));
    }

    @Test
    void bloodPressureStrategyCatchesHighSystolic() {
        Patient p = new Patient(1);
        p.addRecord(190, "SystolicPressure", 1);
        List<Alert> alerts = new BloodPressureStrategy().checkAlert(p);
        assertTrue(has(alerts, "High Systolic Pressure"));
    }

    @Test
    void bloodPressureStrategyCatchesTrend() {
        Patient p = new Patient(1);
        p.addRecord(120, "SystolicPressure", 1);
        p.addRecord(135, "SystolicPressure", 2);
        p.addRecord(150, "SystolicPressure", 3);
        List<Alert> alerts = new BloodPressureStrategy().checkAlert(p);
        assertTrue(has(alerts, "Increasing Systolic Trend"));
    }

    @Test
    void oxygenStrategyCatchesLowSaturation() {
        Patient p = new Patient(1);
        p.addRecord(85, "Saturation", 1);
        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(p);
        assertTrue(has(alerts, "Low Blood Saturation"));
    }

    @Test
    void oxygenStrategyCatchesRapidDrop() {
        Patient p = new Patient(1);
        p.addRecord(98, "Saturation", 0);
        p.addRecord(92, "Saturation", 5 * 60 * 1000);
        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(p);
        assertTrue(has(alerts, "Rapid Saturation Drop"));
    }

    @Test
    void heartRateStrategyCatchesEcgPeak() {
        Patient p = new Patient(1);
        for (int i = 0; i < 5; i++) p.addRecord(0.5, "ECG", i);
        p.addRecord(2.0, "ECG", 6);
        List<Alert> alerts = new HeartRateStrategy().checkAlert(p);
        assertTrue(has(alerts, "Abnormal ECG"));
    }

    @Test
    void strategiesReturnEmptyWhenNothingMatches() {
        Patient p = new Patient(1);
        p.addRecord(120, "SystolicPressure", 1);
        assertTrue(new BloodPressureStrategy().checkAlert(p).stream()
                .noneMatch(a -> a.getCondition().contains("Trend")));
    }
}
