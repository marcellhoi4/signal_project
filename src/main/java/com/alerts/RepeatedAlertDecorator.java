package com.alerts;

/**
 * Adds re-check behaviour to an alert. The caller can ask if enough time has
 * passed since the last check to look at the underlying condition again.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private final long intervalMs;
    private long lastChecked;

    public RepeatedAlertDecorator(Alert wrapped, long intervalMs) {
        super(wrapped);
        this.intervalMs = intervalMs;
        this.lastChecked = wrapped.getTimestamp();
    }

    public boolean shouldRecheck(long now) {
        return now - lastChecked >= intervalMs;
    }

    public void markChecked(long now) {
        this.lastChecked = now;
    }
}
