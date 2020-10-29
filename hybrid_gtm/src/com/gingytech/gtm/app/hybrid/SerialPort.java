package com.gingytech.gtm.app.hybrid;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.util.Log;

public class SerialPort {
	
	private static final String TAG = "SerialPort";

    private native int native_open(String devicepath, int speed) throws IOException;
    private native void native_close();
    private native int native_read_array(byte[] buffer, int length) throws IOException;
    private native int native_write_array(byte[] buffer, int length) throws IOException;
    private native void native_clean_read_array();

    static{
    	try{
    		System.loadLibrary("uart_exchange_data");	
    	}catch(Exception ex){
    		System.out.print("loadlibrary failed");
    	}
    }

    /**
     * SerialPort should only be instantiated by SerialManager
     * @hide
     */
    public SerialPort() {
    	
    }
    
    /**
     * SerialPort should only be instantiated by SerialManager
     * Speed must be one of 50, 75, 110, 134, 150, 200, 300, 600, 1200, 1800, 2400, 4800, 9600,
     * 19200, 38400, 57600, 115200
     *
     * @hide
     */
    public void open(String devicepath, int speed) throws IOException {
        int nRet = native_open(devicepath, speed);
        if(nRet!=0)
        	Log.d(TAG, "device open fail.");
    }

    /**
     * Closes the serial port
     */
    public void close() throws IOException {
        native_close();
    }
    
    /**
     * Reads data into the provided buffer.
     * Note that the value returned by {@link java.nio.Buffer#position()} on this buffer is
     * unchanged after a call to this method.
     *
     * @param buffer to read into
     * @return number of bytes read
     */
    public int read(ByteBuffer buffer) throws IOException {
      return native_read_array(buffer.array(), buffer.remaining());
    }
    
    /**
     * Writes data from provided buffer.
     * Note that the value returned by {@link java.nio.Buffer#position()} on this buffer is
     * unchanged after a call to this method.
     *
     * @param buffer to write
     * @param length number of bytes to write
     */
    public void write(ByteBuffer buffer, int length) throws IOException {
      native_write_array(buffer.array(), length);
    }

    public void cleanReadBuffer(){
        native_clean_read_array();
    }
}
