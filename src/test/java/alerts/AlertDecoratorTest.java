package alerts;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.PriorityAlertDecorator;
import com.alerts.RepeatedAlertDecorator;

class AlertDecoratorTest {

    @Test
    void priorityDecoratorPrependsPriorityTag() {
        Alert base = new Alert("1", "High Systolic Pressure", 100);
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base, "HIGH");
        assertEquals("[HIGH] High Systolic Pressure", decorated.getCondition());
        assertEquals("HIGH", decorated.getPriority());
    }

    @Test
    void priorityDecoratorKeepsPatientIdAndTimestamp() {
        Alert base = new Alert("42", "Manual Alert", 500);
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base, "LOW");
        assertEquals("42", decorated.getPatientId());
        assertEquals(500, decorated.getTimestamp());
    }

    @Test
    void repeatedDecoratorRechecksAfterInterval() {
        Alert base = new Alert("1", "Low Blood Saturation", 0);
        RepeatedAlertDecorator decorated = new RepeatedAlertDecorator(base, 1000);
        assertFalse(decorated.shouldRecheck(500));
        assertTrue(decorated.shouldRecheck(1000));
        assertTrue(decorated.shouldRecheck(2000));
    }

    @Test
    void markCheckedResetsTimer() {
        Alert base = new Alert("1", "Abnormal ECG", 0);
        RepeatedAlertDecorator decorated = new RepeatedAlertDecorator(base, 1000);
        decorated.markChecked(1000);
        assertFalse(decorated.shouldRecheck(1500));
        assertTrue(decorated.shouldRecheck(2000));
    }
}
