package data_management;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;

class SingletonTest {

    @BeforeEach
    void reset() {
        DataStorage.resetForTesting();
    }

    @Test
    void dataStorageReturnsSameInstance() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b);
    }

    @Test
    void resetCreatesFreshInstance() {
        DataStorage a = DataStorage.getInstance();
        DataStorage.resetForTesting();
        DataStorage b = DataStorage.getInstance();
        assertNotSame(a, b);
    }

    @Test
    void healthDataSimulatorReturnsSameInstance() {
        HealthDataSimulator a = HealthDataSimulator.getInstance();
        HealthDataSimulator b = HealthDataSimulator.getInstance();
        assertSame(a, b);
    }
}
