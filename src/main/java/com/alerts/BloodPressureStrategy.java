package com.alerts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.data_management.Patient;
import com.data_management.PatientRecord;

/** Critical thresholds and trend alerts for systolic and diastolic BP. */
public class BloodPressureStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient) {
        List<Alert> out = new ArrayList<>();
        String id = String.valueOf(patient.getPatientId());
        List<PatientRecord> sys = filter(patient, "SystolicPressure");
        List<PatientRecord> dia = filter(patient, "DiastolicPressure");

        critical(out, id, sys, "Systolic", 180, 90);
        critical(out, id, dia, "Diastolic", 120, 60);
        trend(out, id, sys, "Systolic");
        trend(out, id, dia, "Diastolic");
        return out;
    }

    private void critical(List<Alert> out, String id, List<PatientRecord> rs, String label, double hi, double lo) {
        for (PatientRecord r : rs) {
            double v = r.getMeasurementValue();
            if (v > hi) out.add(factory.createAlert(id, "High " + label + " Pressure", r.getTimestamp()));
            else if (v < lo) out.add(factory.createAlert(id, "Low " + label + " Pressure", r.getTimestamp()));
        }
    }

    private void trend(List<Alert> out, String id, List<PatientRecord> rs, String label) {
        for (int i = 2; i < rs.size(); i++) {
            double d1 = rs.get(i - 1).getMeasurementValue() - rs.get(i - 2).getMeasurementValue();
            double d2 = rs.get(i).getMeasurementValue() - rs.get(i - 1).getMeasurementValue();
            if (d1 > 10 && d2 > 10) out.add(factory.createAlert(id, "Increasing " + label + " Trend", rs.get(i).getTimestamp()));
            else if (d1 < -10 && d2 < -10) out.add(factory.createAlert(id, "Decreasing " + label + " Trend", rs.get(i).getTimestamp()));
        }
    }

    private List<PatientRecord> filter(Patient patient, String type) {
        List<PatientRecord> filtered = new ArrayList<>();
        for (PatientRecord r : patient.getRecords(0, Long.MAX_VALUE)) {
            if (type.equals(r.getRecordType())) filtered.add(r);
        }
        filtered.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
        return filtered;
    }
}
