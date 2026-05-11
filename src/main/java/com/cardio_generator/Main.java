package com.cardio_generator;

import java.io.IOException;

import com.data_management.DataStorage;

/**
 * Entry point that dispatches to either the simulator or the storage demo.
 *
 * <pre>
 * java -jar cardio_generator.jar              -&gt; runs HealthDataSimulator
 * java -jar cardio_generator.jar DataStorage  -&gt; runs DataStorage.main
 * </pre>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            String[] rest = new String[args.length - 1];
            System.arraycopy(args, 1, rest, 0, rest.length);
            DataStorage.main(rest);
        } else {
            HealthDataSimulator.main(args);
        }
    }
}
