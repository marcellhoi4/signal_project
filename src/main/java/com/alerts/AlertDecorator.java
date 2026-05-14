package com.alerts;

/**
 * Base decorator. Subclasses can add behaviour around a wrapped Alert
 * without modifying the Alert class itself.
 */
public abstract class AlertDecorator extends Alert {
    protected final Alert wrapped;

    public AlertDecorator(Alert wrapped) {
        super(wrapped.getPatientId(), wrapped.getCondition(), wrapped.getTimestamp());
        this.wrapped = wrapped;
    }
}
