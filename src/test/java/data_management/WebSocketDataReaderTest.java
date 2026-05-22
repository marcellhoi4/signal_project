package data_management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;

class WebSocketDataReaderTest {

    private TestServer server;
    private WebSocketDataReader client;
    private DataStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        DataStorage.resetForTesting();
        storage = DataStorage.getInstance();
        server = new TestServer(0); // OS picks a free port
        server.start();
        assertTrue(server.startLatch.await(5, TimeUnit.SECONDS), "server did not start");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) client.stop();
        if (server != null) server.stop();
    }

    @Test
    void receivesAndStoresMessage() throws Exception {
        connectClient();

        server.broadcastAll("1,1000,ECG,0.5");
        waitUntil(() -> !storage.getRecords(1, 0, Long.MAX_VALUE).isEmpty());

        List<PatientRecord> rs = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(1, rs.size());
        assertEquals(0.5, rs.get(0).getMeasurementValue());
        assertEquals("ECG", rs.get(0).getRecordType());
        assertEquals(1000L, rs.get(0).getTimestamp());
    }

    @Test
    void parsesSaturationPercent() throws Exception {
        connectClient();
        server.broadcastAll("2,2000,Saturation,95.0%");
        waitUntil(() -> !storage.getRecords(2, 0, Long.MAX_VALUE).isEmpty());

        assertEquals(95.0, storage.getRecords(2, 0, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void skipsMalformedMessage() throws Exception {
        connectClient();

        server.broadcastAll("not a real message");
        server.broadcastAll("1,3000,ECG,0.7"); // good one after the bad one
        waitUntil(() -> !storage.getRecords(1, 0, Long.MAX_VALUE).isEmpty());

        // only the good one made it in
        assertEquals(1, storage.getRecords(1, 0, Long.MAX_VALUE).size());
        assertEquals(0.7, storage.getRecords(1, 0, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    @SuppressWarnings("BusyWait")
    void stopClosesTheConnection() throws Exception {
        connectClient();
        assertTrue(client.isOpen());
        client.stop();

        // give the close handshake a moment
        long deadline = System.currentTimeMillis() + 2000;
        while (client.isOpen() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(client.isClosing() || client.isClosed());
    }

    // ---- helpers ----

    private void connectClient() throws Exception {
        client = new WebSocketDataReader(URI.create("ws://localhost:" + server.getPort()));
        client.readData(storage);
        assertTrue(server.connectionLatch.await(2, TimeUnit.SECONDS), "client did not connect");
    }

    @SuppressWarnings("BusyWait")
    private void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(30);
        }
    }

    /** Minimal WebSocket server used only by these tests. */
    private static class TestServer extends WebSocketServer {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch connectionLatch = new CountDownLatch(1);

        TestServer(int port) {
            super(new InetSocketAddress("localhost", port));
        }

        @Override public void onStart() { startLatch.countDown(); }
        @Override public void onOpen(WebSocket conn, ClientHandshake handshake) { connectionLatch.countDown(); }
        @Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
        @Override public void onMessage(WebSocket conn, String message) {}
        @Override public void onError(WebSocket conn, Exception ex) {}

        void broadcastAll(String msg) {
            for (WebSocket c : getConnections()) c.send(msg);
        }
    }
}
