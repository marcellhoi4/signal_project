package alerts;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.AlertFactory;
import com.alerts.BloodOxygenAlert;
import com.alerts.BloodOxygenAlertFactory;
import com.alerts.BloodPressureAlert;
import com.alerts.BloodPressureAlertFactory;
import com.alerts.ECGAlert;
import com.alerts.ECGAlertFactory;

class AlertFactoryTest {

    @Test
    void bloodPressureFactoryProducesBloodPressureAlert() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("1", "High Systolic Pressure", 100);
        assertInstanceOf(BloodPressureAlert.class, alert);
        assertEquals("1", alert.getPatientId());
    }

    @Test
    void bloodOxygenFactoryProducesBloodOxygenAlert() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("2", "Low Blood Saturation", 200);
        assertInstanceOf(BloodOxygenAlert.class, alert);
        assertEquals("Low Blood Saturation", alert.getCondition());
    }

    @Test
    void ecgFactoryProducesECGAlert() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("3", "Abnormal ECG", 300);
        assertInstanceOf(ECGAlert.class, alert);
        assertEquals(300, alert.getTimestamp());
    }
}
