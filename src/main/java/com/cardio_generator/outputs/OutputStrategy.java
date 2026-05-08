package com.cardio_generator.outputs;

/**
 * Strategy for sending one piece of patient data somewhere, could be the
 * console, a file, a TCP socket or a WebSocket. The simulator only talks to
 * this interface so it doesn't care about the destination.
 */
public interface OutputStrategy {
    /**
     * Send one data point.
     *
     * @param patientId patient the data is for
     * @param timestamp ms since epoch
     * @param label data type, e.g. "ECG"
     * @param data the value
     */
    void output(int patientId, long timestamp, String label, String data);
}
