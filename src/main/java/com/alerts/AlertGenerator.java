package com.alerts;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final DataStorage dataStorage;
    private final List<Alert> alerts = new ArrayList<>();
    private final List<AlertStrategy> strategies = Arrays.asList(
            new BloodPressureStrategy(),
            new OxygenSaturationStrategy(),
            new HeartRateStrategy());

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
        for (AlertStrategy s : strategies) {
            for (Alert a : s.checkAlert(patient)) triggerAlert(a);
        }
        hypoHypo(patient);
        manual(patient);
    }

    /** Returns the alerts raised so far. */
    public List<Alert> getAlerts() {
        return new ArrayList<>(alerts);
    }

    /** Evaluates every patient currently in the storage. */
    public void evaluateAll() {
        for (Patient p : dataStorage.getAllPatients()) {
            evaluateData(p);
        }
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

    private void hypoHypo(Patient patient) {
        List<PatientRecord> sys = sortedOfType(patient, "SystolicPressure");
        List<PatientRecord> sat = sortedOfType(patient, "Saturation");
        if (sys.isEmpty() || sat.isEmpty()) return;
        PatientRecord s = sys.get(sys.size() - 1);
        PatientRecord o = sat.get(sat.size() - 1);
        if (s.getMeasurementValue() < 90 && o.getMeasurementValue() < 92) {
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()),
                    "Hypotensive Hypoxemia", Math.max(s.getTimestamp(), o.getTimestamp())));
        }
    }

    private void manual(Patient patient) {
        for (PatientRecord r : sortedOfType(patient, "Alert")) {
            if (r.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),
                        "Manual Alert", r.getTimestamp()));
            }
        }
    }

    private List<PatientRecord> sortedOfType(Patient patient, String type) {
        List<PatientRecord> out = new ArrayList<>();
        for (PatientRecord r : patient.getRecords(0, Long.MAX_VALUE)) {
            if (type.equals(r.getRecordType())) out.add(r);
        }
        out.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
        return out;
    }
}
