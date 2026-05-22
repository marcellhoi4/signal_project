package data_management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.data_management.ValueParser;

class ValueParserTest {

    @Test
    void parsesPlainNumber() {
        assertEquals(0.5, ValueParser.parse("ECG", "0.5"));
    }

    @Test
    void stripsPercentSuffix() {
        assertEquals(95.0, ValueParser.parse("Saturation", "95.0%"));
    }

    @Test
    void alertTriggeredIsOne() {
        assertEquals(1.0, ValueParser.parse("Alert", "triggered"));
    }

    @Test
    void alertResolvedIsZero() {
        assertEquals(0.0, ValueParser.parse("Alert", "resolved"));
    }

    @Test
    void alertCaseInsensitive() {
        assertEquals(1.0, ValueParser.parse("Alert", "Triggered"));
        assertEquals(0.0, ValueParser.parse("Alert", "RESOLVED"));
    }

    @Test
    void unparseableNumberThrows() {
        assertThrows(NumberFormatException.class,
                () -> ValueParser.parse("ECG", "not a number"));
    }
}
