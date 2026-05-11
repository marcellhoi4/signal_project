package com.alerts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private static final long TEN_MIN = 10 * 60 * 1000L;

    private DataStorage dataStorage;
    private final List<Alert> alerts = new ArrayList<>();

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        List<PatientRecord> all = patient.getRecords(0L, Long.MAX_VALUE);
        List<PatientRecord> sys = filter(all, "SystolicPressure");
        List<PatientRecord> dia = filter(all, "DiastolicPressure");
        List<PatientRecord> sat = filter(all, "Saturation");
        List<PatientRecord> ecg = filter(all, "ECG");
        List<PatientRecord> man = filter(all, "Alert");
        String id = String.valueOf(patient.getPatientId());

        bpCritical(id, sys, "Systolic", 180, 90);
        bpCritical(id, dia, "Diastolic", 120, 60);
        bpTrend(id, sys, "Systolic");
        bpTrend(id, dia, "Diastolic");
        satLow(id, sat);
        satDrop(id, sat);
        hypoHypo(id, sys, sat);
        ecgPeak(id, ecg);
        manual(id, man);
    }

    /** Returns the alerts raised so far. */
    public List<Alert> getAlerts() {
        return new ArrayList<>(alerts);
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        alerts.add(alert);
        System.out.println("ALERT: patient " + alert.getPatientId()
                + ", " + alert.getCondition() + " @ " + alert.getTimestamp());
    }

    private void bpCritical(String id, List<PatientRecord> rs, String label, double hi, double lo) {
        for (PatientRecord r : rs) {
            double v = r.getMeasurementValue();
            if (v > hi) triggerAlert(new Alert(id, "High " + label + " Pressure", r.getTimestamp()));
            else if (v < lo) triggerAlert(new Alert(id, "Low " + label + " Pressure", r.getTimestamp()));
        }
    }

    private void bpTrend(String id, List<PatientRecord> rs, String label) {
        for (int i = 2; i < rs.size(); i++) {
            double d1 = rs.get(i - 1).getMeasurementValue() - rs.get(i - 2).getMeasurementValue();
            double d2 = rs.get(i).getMeasurementValue() - rs.get(i - 1).getMeasurementValue();
            if (d1 > 10 && d2 > 10) triggerAlert(new Alert(id, "Increasing " + label + " Trend", rs.get(i).getTimestamp()));
            else if (d1 < -10 && d2 < -10) triggerAlert(new Alert(id, "Decreasing " + label + " Trend", rs.get(i).getTimestamp()));
        }
    }

    private void satLow(String id, List<PatientRecord> rs) {
        for (PatientRecord r : rs) {
            if (r.getMeasurementValue() < 92) triggerAlert(new Alert(id, "Low Blood Saturation", r.getTimestamp()));
        }
    }

    private void satDrop(String id, List<PatientRecord> rs) {
        for (int i = 0; i < rs.size(); i++) {
            for (int j = i + 1; j < rs.size(); j++) {
                if (rs.get(j).getTimestamp() - rs.get(i).getTimestamp() > TEN_MIN) break;
                if (rs.get(i).getMeasurementValue() - rs.get(j).getMeasurementValue() >= 5) {
                    triggerAlert(new Alert(id, "Rapid Saturation Drop", rs.get(j).getTimestamp()));
                    return;
                }
            }
        }
    }

    private void hypoHypo(String id, List<PatientRecord> sys, List<PatientRecord> sat) {
        if (sys.isEmpty() || sat.isEmpty()) return;
        PatientRecord s = sys.get(sys.size() - 1);
        PatientRecord o = sat.get(sat.size() - 1);
        if (s.getMeasurementValue() < 90 && o.getMeasurementValue() < 92) {
            triggerAlert(new Alert(id, "Hypotensive Hypoxemia", Math.max(s.getTimestamp(), o.getTimestamp())));
        }
    }

    private void ecgPeak(String id, List<PatientRecord> rs) {
        int win = 5;
        if (rs.size() <= win) return;
        for (int i = win; i < rs.size(); i++) {
            double sum = 0;
            for (int j = i - win; j < i; j++) sum += Math.abs(rs.get(j).getMeasurementValue());
            double avg = sum / win;
            if (avg > 0 && Math.abs(rs.get(i).getMeasurementValue()) > avg * 1.8) {
                triggerAlert(new Alert(id, "Abnormal ECG", rs.get(i).getTimestamp()));
            }
        }
    }

    private void manual(String id, List<PatientRecord> rs) {
        for (PatientRecord r : rs) {
            if (r.getMeasurementValue() == 1.0) triggerAlert(new Alert(id, "Manual Alert", r.getTimestamp()));
        }
    }

    private List<PatientRecord> filter(List<PatientRecord> rs, String type) {
        List<PatientRecord> out = new ArrayList<>();
        for (PatientRecord r : rs) if (type.equals(r.getRecordType())) out.add(r);
        out.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
        return out;
    }
}
