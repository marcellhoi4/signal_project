package com.alerts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.data_management.Patient;
import com.data_management.PatientRecord;

/** Low saturation and rapid drop alerts. */
public class OxygenSaturationStrategy implements AlertStrategy {

    private static final long TEN_MIN = 10 * 60 * 1000L;
    private final AlertFactory factory = new BloodOxygenAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient) {
        List<Alert> out = new ArrayList<>();
        String id = String.valueOf(patient.getPatientId());
        List<PatientRecord> sat = new ArrayList<>();
        for (PatientRecord r : patient.getRecords(0, Long.MAX_VALUE)) {
            if ("Saturation".equals(r.getRecordType())) sat.add(r);
        }
        sat.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        for (PatientRecord r : sat) {
            if (r.getMeasurementValue() < 92) {
                out.add(factory.createAlert(id, "Low Blood Saturation", r.getTimestamp()));
            }
        }
        outer:
        for (int i = 0; i < sat.size(); i++) {
            for (int j = i + 1; j < sat.size(); j++) {
                if (sat.get(j).getTimestamp() - sat.get(i).getTimestamp() > TEN_MIN) break;
                if (sat.get(i).getMeasurementValue() - sat.get(j).getMeasurementValue() >= 5) {
                    out.add(factory.createAlert(id, "Rapid Saturation Drop", sat.get(j).getTimestamp()));
                    break outer;
                }
            }
        }
        return out;
    }
}
