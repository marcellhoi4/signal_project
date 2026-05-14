package com.alerts;

/** Alert for blood pressure conditions (critical thresholds or trends). */
public class BloodPressureAlert extends Alert {
    public BloodPressureAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
