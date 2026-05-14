package com.alerts;

/** Alert for blood oxygen saturation conditions. */
public class BloodOxygenAlert extends Alert {
    public BloodOxygenAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
