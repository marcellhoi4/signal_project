package com.alerts;

/**
 * Tags an alert with a priority level (e.g. HIGH, MEDIUM, LOW). The level is
 * prepended to the condition string so logs and dispatchers can see it.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    private final String priority;

    public PriorityAlertDecorator(Alert wrapped, String priority) {
        super(wrapped);
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }

    @Override
    public String getCondition() {
        return "[" + priority + "] " + wrapped.getCondition();
    }
}
