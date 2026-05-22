package com.data_management;

import java.io.IOException;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Connects to the simulator's WebSocket server and feeds incoming messages
 * into a DataStorage. Messages arrive on the wire as a single CSV line:
 * <pre>patientId,timestamp,label,data</pre>
 * Bad lines are skipped so one corrupt message doesn't kill the stream.
 */
public class WebSocketDataReader extends WebSocketClient implements DataReader {

    private DataStorage dataStorage;

    public WebSocketDataReader(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        this.dataStorage = dataStorage;
        try {
            // connectBlocking returns once the handshake completes (or fails).
            // After this returns, messages arrive on the WebSocket thread.
            connectBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("WebSocket connect interrupted", e);
        }
    }

    @Override
    public void stop() {
        close();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocket connected to " + getURI());
    }

    @Override
    public void onMessage(String message) {
        if (dataStorage == null) return; // not started yet
        try {
            // patientId,timestamp,label,data
            String[] parts = message.split(",", 4);
            if (parts.length != 4) return;
            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label = parts[2].trim();
            double value = ValueParser.parse(label, parts[3].trim());
            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (IllegalArgumentException e) {
            // bad message, ignore
            System.err.println("WebSocket: skipped bad message: " + message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed (" + code + "): " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }
}
