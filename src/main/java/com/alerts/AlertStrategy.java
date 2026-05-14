package com.alerts;

import java.util.List;

import com.data_management.Patient;

/**
 * Strategy for deciding when a particular type of alert should fire.
 * Each implementation looks at the patient's records and returns the
 * alerts it found.
 */
public interface AlertStrategy {
    List<Alert> checkAlert(Patient patient);
}
