package com.gingytech.gtm.app.hybrid;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.R.bool;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gingytech.gtm.app.handprint.Protocol.CommandPack;
import com.gingytech.gtm.app.handprint.Protocol.DataPacket;
import com.gingytech.gtm.app.handprint.Protocol.Defined;
import com.gingytech.gtm.app.handprint.Protocol.ResponsePacket;
import com.gingytech.gtm.app.handprint.Protocol.Utils;

public class UartHelper {

	private Context context;
	private Handler pHandler;
	private fileHelp fileLog;
	private String TAG = "UartHelper";

	private SerialPort serialPort = null;
	private SerialManager serialManager = null;
	private String[] ports;
	private boolean bNeedClean = true;

	public UartHelper(Context context, Handler pHandler) {
		super();
		this.context = context;
		this.pHandler = pHandler;
		fileLog = new fileHelp(context);
		serialManager = new SerialManager();
		
		Message msg=new Message();
		msg.what=Defined.UART_READY;
		pHandler.sendMessage(msg);
	}

	public int getSerialPortCount() {
		return ports.length;
	}

	public boolean openSerialPort(int num, int speed) {
		try {
			serialPort = serialManager.openSerialPort("/dev/ttyMT1", speed);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void closeSerialPort() {
		if (serialPort != null) {
			try {
				serialPort.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int read(ByteBuffer byteBuffer) throws IOException {
		if (serialPort == null) {
			Log.d(TAG, "serialPort is null!!!");
			throw new NullPointerException();
		}
		return serialPort.read(byteBuffer);
	}

	public void write(ByteBuffer byteBuffer, int length) throws IOException {
		if (serialPort == null) {
			throw new NullPointerException();
		}
		serialPort.write(byteBuffer, length);
	}

	public void sendBreak() {
	}

	public ResponsePacket sendCommand(short cmd, int parameter) throws IOException {
		CommandPack cmdPack = new CommandPack(cmd, parameter);
		byte[] data = cmdPack.cmdBytes;

		for(int i = 0;i < data.length;i++) {
			Log.d(TAG, String.format("cmdBytes[%d] = %2X", i, data[i]));
		}
		
		sendDataToUart(data);

		byte[] getData = receiveDataFromUart(Defined.PACK_RESPONSE_LEN);

		Log.d(TAG, "ResponsePacket Get Data: " + Utils.byteToString(getData));

		if(bNeedClean)
			cleanReadBuffer();

		return new ResponsePacket(getData);
	}

	public DataPacket sendCommand(short cmd, int parameter, int length) throws IOException {
		bNeedClean = false;
		ResponsePacket resPack = sendCommand(cmd, parameter);
		bNeedClean = true;

		DataPacket dataPack = null;

		if (resPack == null){
			Log.d(TAG, "resPack is null.");
			cleanReadBuffer();
			throw new NullPointerException();
		}

		if (resPack.Response == Defined.ACK_OK) {

			//When the cmd is CMD_GET_MODULE_INFO, the length plus resPack.Parameter(data length) is data package size.
			if(cmd == Defined.CMD_GET_MODULE_INFO)
				length = length + resPack.Parameter;

			try {
				byte[] data = receiveDataFromUart(length);

				//When the data is very large like image, the system will slowly.
				//If you want to see the detail, remark next line.
				//Log.d(TAG, "DataPacket Get data: " + Utils.byteToString(data));

				dataPack = new DataPacket(data);
				cleanReadBuffer();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				cleanReadBuffer();
				Log.d(TAG, "dataPack is error");
			}
		}
		else{
			Log.d(TAG, "resPack.Response is NOT OK");
			cleanReadBuffer();
		}

		return dataPack;
	}

	// Send Command and data to GTM
	public ResponsePacket sendCommand(short cmd, int parameter, byte[] data) throws NullPointerException, IOException {
		
		// first time send command and receive response.
		ResponsePacket resPack = sendCommand(cmd, parameter);
		
		if(resPack.Response == Defined.NACK_INFO)
			return resPack;
		
		// before sent to uart. it must be package as Data Packet which describe in datasheet.
		DataPacket dataPack = new DataPacket(data, true);
		
		Log.d(TAG, "Sending Data... " + Utils.byteToString(dataPack.payloadBytes));
		
		// second time send data to GTM
		sendDataToUart(dataPack.payloadBytes);
		
		// then receive response from GTM.
		byte[] getData = receiveDataFromUart(Defined.PACK_RESPONSE_LEN);

		Log.d(TAG, "ResponsePacket Get Data: " + Utils.byteToString(getData));

		cleanReadBuffer();

		return new ResponsePacket(getData);
	}

	public boolean sendCommandAndCheck(short cmd, int parameter) throws IOException {
		ResponsePacket resPack = sendCommand(cmd, parameter);
		if (resPack != null) {
			return resPack.Response == Defined.ACK_OK;
		}
		return false;
	}

	private void sendDataToUart(byte[] data) throws IOException {
		Log.i(TAG, "sendDataToUart");
		ByteBuffer writeBuffer = ByteBuffer.wrap(data);
		
		try {
			write(writeBuffer, data.length);
		} catch (NullPointerException e) {
			Log.e(TAG, "Device connection failed.");
		} catch(IOException e) {
			Log.e(TAG, "IO exception...");
		}
	}

	private byte[] receiveDataFromUart(int length) throws IOException {
		Log.i(TAG, "receiveDataFromUart");

		ByteBuffer buffer = null;
		byte[] readByte = null;
		
		buffer = ByteBuffer.allocate(length);
		buffer.clear();
		
		int inBytes = read(buffer);
		Log.d(TAG, "Read data length = " + Integer.toString(inBytes));
		
		readByte = buffer.array();
		
		return readByte;
	}

	public void cleanReadBuffer(){
		if (serialPort == null) {
			return;
		}
		serialPort.cleanReadBuffer();
	}
}
