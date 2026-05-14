package com.alerts;

/**
 * Factory Method base class. Subclasses decide which concrete Alert subtype
 * to instantiate, so callers don't depend on the specific class.
 */
public abstract class AlertFactory {
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
