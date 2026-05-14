package com.alerts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.data_management.Patient;
import com.data_management.PatientRecord;

/** Looks for ECG peaks that stick out far above the recent average. */
public class HeartRateStrategy implements AlertStrategy {

    private final AlertFactory factory = new ECGAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient) {
        List<Alert> out = new ArrayList<>();
        String id = String.valueOf(patient.getPatientId());
        List<PatientRecord> ecg = new ArrayList<>();
        for (PatientRecord r : patient.getRecords(0, Long.MAX_VALUE)) {
            if ("ECG".equals(r.getRecordType())) ecg.add(r);
        }
        ecg.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        int win = 5;
        if (ecg.size() <= win) return out;
        for (int i = win; i < ecg.size(); i++) {
            double sum = 0;
            for (int j = i - win; j < i; j++) sum += Math.abs(ecg.get(j).getMeasurementValue());
            double avg = sum / win;
            if (avg > 0 && Math.abs(ecg.get(i).getMeasurementValue()) > avg * 1.8) {
                out.add(factory.createAlert(id, "Abnormal ECG", ecg.get(i).getTimestamp()));
            }
        }
        return out;
    }
}
