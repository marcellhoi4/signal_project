package com.data_management;

import java.io.IOException;

public interface DataReader {
    /**
     * Starts reading data into the given storage. For file-based readers
     * this reads the source once and returns. For streaming readers this
     * connects to the source and returns once connected, with messages
     * arriving on a background thread.
     *
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading or connecting
     */
    void readData(DataStorage dataStorage) throws IOException;

    /**
     * Stops any ongoing data ingestion (closes connections, stops threads).
     * Safe to call multiple times. Default is a no-op for batch readers.
     */
    default void stop() {
    }
}
