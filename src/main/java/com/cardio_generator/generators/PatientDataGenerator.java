package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Anything that produces simulated data for a single patient implements this.
 * One implementation per data type (ECG, blood pressure, alerts...).
 */
public interface PatientDataGenerator {
    /**
     * Generate one data point for the patient and pass it to the output.
     *
     * @param patientId who the data is for
     * @param outputStrategy where the data ends up (console, file, socket...)
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
