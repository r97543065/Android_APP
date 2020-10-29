package com.gingytech.gtm.app.hybrid;

import java.io.IOException;

public class SerialManager {
    private static final String TAG = "SerialManager";


    /**
     * {@hide}
     */
    public SerialManager() {
    }

    /**
     * Opens and returns the {@link gingytech.sample.jniserial.SerialPort} with the given name.
     * The speed of the serial port must be one of:
     * 50, 75, 110, 134, 150, 200, 300, 600, 1200, 1800, 2400, 4800, 9600,
     * 19200, 38400, 57600, 115200, 230400, 460800, 500000, 576000, 921600, 1000000, 1152000,
     * 1500000, 2000000, 2500000, 3000000, 3500000 or 4000000
     *
     * @param name of the serial port
     * @param speed at which to open the serial port
     * @return the serial port
     */
    public SerialPort openSerialPort(String name, int speed) throws IOException {
        SerialPort port = new SerialPort();
        port.open(name, speed);
        return port;
    }
}
