package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Simulates the manual alert button next to a patient's bed.
 * Randomly toggles the alert state and prints "triggered" or "resolved".
 */
public class AlertGenerator implements PatientDataGenerator {

    public static final Random randomGenerator = new Random();

    // renamed from AlertStates. Fields should be camelCase, not PascalCase.
    // (Google Java Style Guide §5.2.5)
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * @param patientCount how many patients we are tracking. Used to size the
     *                     internal state array (patient IDs start at 1).
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Updates the alert state for one patient. If an alert is already active
     * there is a 90% chance it gets resolved, otherwise a new one may trigger
     * based on a small probability. Whatever happens is written to the output.
     *
     * @param patientId the patient to update
     * @param outputStrategy where the event is written
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // renamed from Lambda. Local variables are also camelCase (GJSG §5.2.6)
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
