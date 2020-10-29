package com.gingytech.gtm.app.hybrid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import com.gingytech.gtm.app.handprint.Protocol.CommandPack;
import com.gingytech.gtm.app.handprint.Protocol.DataPacket;
import com.gingytech.gtm.app.handprint.Protocol.Defined;
import com.gingytech.gtm.app.handprint.Protocol.ResponsePacket;
import com.gingytech.gtm.app.handprint.Protocol.Utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*

 * USB
 * mass storage 
 */
public class UsbHelper {

	// USB
	final int WaitTime = 10000; 	//1600 for GHM
	private UsbManager usbManager;
	private UsbDevice usbDevice;
	boolean isFindDevice;
	final int[] VID_LIST = {1241, 8201, 1155};
	final int[] PID_LIST = {32776, 30264, 22314};
	UsbEndpoint inEndpoint;
	UsbEndpoint outEndpoint;
	UsbDeviceConnection connection;
	byte[] m_abyTransferBuf = new byte[512];
	String TAG = "UsbHelper";
	Context context;
	Handler pHandler;
	fileHelp fileLog;
	int sendMax, recMax;
	public boolean Isconneced=false;
	final int extraDataLen = Defined.PACK_HEADER_LEN + Defined.PACK_DEVICEID_LEN + Defined.PACK_CHECKSUM_LEN;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private PendingIntent pendingIntent;

	public UsbHelper(Context c, Handler h) {
		context = c;
		pHandler = h;	
		fileLog = new fileHelp(c);
		usbManager = (UsbManager) context
				.getSystemService(Context.USB_SERVICE);
		isFindDevice = false;
		deviceHandler.postDelayed(deviceRunnable, 1000);
		pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
	}

	void FindDevice() {
		try {
			fileLog.writeLog(TAG + " FindDevice"); 
			HashMap<String, UsbDevice> map = usbManager.getDeviceList();
			isFindDevice=false;
			for (UsbDevice device : map.values()) {
				
				for(int i = 0;i < VID_LIST.length;i++) {
					if(VID_LIST[i] == device.getVendorId()
							&& PID_LIST[i] == device.getProductId()) {
						usbDevice = device;
						isFindDevice = true;
						break;
					}
				}
				
				if(isFindDevice == true)
					break;
			}
		
			if(!isFindDevice) return;
			
			if (usbManager.hasPermission(usbDevice)) {
				fileLog.writeLog(TAG + "hasPermission");
				isFindDevice = true;
				Message msg = new Message();
				msg.what = Defined.USBPermission;
				pHandler.sendMessage(msg);
				INIConnection();

			} else {
				fileLog.writeLog(TAG + "requestPermission");
				usbManager.requestPermission(usbDevice, pendingIntent);

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	Handler deviceHandler = new Handler();
	
	Runnable deviceRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (!isFindDevice) {
				FindDevice();
				deviceHandler.postDelayed(deviceRunnable, 10000);
			}
		}

	};
	
	void Close() {
		fileLog.writeLog(TAG + " Close device"); 
		UsbInterface intf = usbDevice.getInterface(0);
		 connection.releaseInterface(intf);
		connection.close();
	}
	void INIConnection() {
		fileLog.writeLog(TAG + " INIConnection start"); 
		
		UsbInterface intf = usbDevice.getInterface(0);
		connection = usbManager.openDevice(usbDevice);
		// connection.controlTransfer(requestType, request, value, index,
		// buffer, length, timeout)
		isFindDevice = connection.claimInterface(intf, true);
		inEndpoint = intf.getEndpoint(0);
		outEndpoint = intf.getEndpoint(1);
		sendMax = outEndpoint.getMaxPacketSize();
		recMax = inEndpoint.getMaxPacketSize();
		Isconneced=true;

		fileLog.writeLog(TAG + " INIConnection end"); 
	}

	public ResponsePacket SendCmd(short cmd, int para) {
		if (connection == null)
			return null;
		if(bulkSend(cmd, para))
		{
			return bulkResponse();
		}
		return null;
	}
	
	public ResponsePacket SendCmd(short cmd, int para, byte[] data) {
		
		if(connection == null)
			return null;
		
		if(bulkSend(cmd, para)) {
			ResponsePacket resPack = bulkResponse();
			
			if(resPack == null)
				return null;
			
			if(resPack.Response == Defined.NACK_INFO)
				return resPack;
			
			boolean isOk = bulkDataOut(data);
			
			fileLog.writeLog(String.format("%s isOk: %b", TAG, isOk));
			
			resPack = bulkResponse();
			
			fileLog.writeLog(String.format("%s res.Response: 0x%02X, res.Parameter: 0x%02X", TAG, resPack.Response, resPack.Parameter));
			
			return resPack;
		}
		
		return null;
	}
	
	ResponsePacket bulkResponse()
	{
		byte[] responseBytes = new byte[Defined.PACK_RESPONSE_LEN];
		byte[] scsiStateBytes = new byte[13];
		byte[] scsiBytes = getSCSI(true, Defined.PACK_RESPONSE_LEN);
		
		if (connection.bulkTransfer(outEndpoint, scsiBytes,
				scsiBytes.length, WaitTime) != scsiBytes.length)
			return null;

		if (connection.bulkTransfer(inEndpoint, responseBytes,
				responseBytes.length, WaitTime) != responseBytes.length)
			return null;

		if (connection.bulkTransfer(inEndpoint, scsiStateBytes,
				scsiStateBytes.length, WaitTime) != scsiStateBytes.length)
			return null;

		Utils.showCommandBytes(TAG, responseBytes, Utils.TRANSFER_DIRECTION.RX, false);

		return new ResponsePacket(responseBytes);
	}
	public DataPacket bulkData(int len)
	{
		byte[] responseBytes = new byte[len+extraDataLen];
		byte[] scsiStateBytes = new byte[13];
		byte[] scsiBytes = getSCSI(true, len+extraDataLen);
		byte[] tempBytes=new byte[recMax];
		int k=0;
		
		int out= connection.bulkTransfer(outEndpoint, scsiBytes,
				scsiBytes.length, WaitTime) ;
		if (out!= scsiBytes.length)
		{
//			Log.e(TAG, "3.1"+Utils.byteToString(scsiBytes));
			return null;
		}
		else
		{
//			Log.e(TAG, "3.1"+Utils.byteToString(scsiBytes));
		}
		
		int out1=0;
		k=0;
		do
		{
		out =connection.bulkTransfer(inEndpoint, tempBytes,
				tempBytes.length, WaitTime);
		
		if(out>0)
		{
		 System.arraycopy(tempBytes,0,responseBytes,k,out);
		 k+=out;
		 out1+=out;
		}
		} while(k<responseBytes.length);
		
		if (out1 != responseBytes.length)
		{
			return null;
		}
		else
		{
//			Log.e(TAG, ""+ responseBytes.length);
		}
		
		if (connection.bulkTransfer(inEndpoint, scsiStateBytes,
				scsiStateBytes.length, WaitTime) != scsiStateBytes.length)
		{
//			Log.e(TAG, "3.3"+Utils.byteToString(scsiStateBytes));
			return null;
		}

		Utils.showCommandBytes(TAG, responseBytes, Utils.TRANSFER_DIRECTION.RX, true);

		return new DataPacket(responseBytes);
	}

	boolean bulkDataOut(byte[] data) {
		boolean rec = true;
		DataPacket dataPack = new DataPacket(data, true);
		
		int len = dataPack.payloadBytes.length;
		byte[] sendBytes = dataPack.payloadBytes;
		byte[] scsiStateBytes = new byte[13];
		byte[] scsiBytes = getSCSI(false, dataPack.payloadBytes.length);
		byte[] tempBytes=new byte[sendMax];
		
		int out, out1, out2;

		Utils.showCommandBytes(TAG, dataPack.payloadBytes, Utils.TRANSFER_DIRECTION.TX, true);

		out = connection.bulkTransfer(outEndpoint, scsiBytes, scsiBytes.length, WaitTime);
		if(out != scsiBytes.length) {
			return false;
		}
		
		int k = 0;
		do {
			int tempLen = ((len - k) > sendMax) ? sendMax : (len - k);
			
			System.arraycopy(sendBytes, k, tempBytes, 0, tempLen);
			
			out1 = connection.bulkTransfer(outEndpoint, tempBytes, tempLen, WaitTime);
			
			k += out1;
		} while(k < len);
		if(k != sendBytes.length) {
			return false;
		}
		
		out2 = connection.bulkTransfer(inEndpoint, scsiStateBytes, scsiStateBytes.length, WaitTime);
		if(out2 != scsiStateBytes.length) {
			return false;
		}

		return rec;
	}
	
	int bulkDataOut(int len, byte[] data) {
		byte[] scsiStateBytes = new byte[13];
		byte[] scsiBytes = getSCSI(false, len + extraDataLen);
		int out, out1;

		out = connection.bulkTransfer(outEndpoint, scsiBytes, scsiBytes.length, WaitTime);

		out1 = connection.bulkTransfer(outEndpoint, data, (len + extraDataLen), WaitTime);

		connection.bulkTransfer(inEndpoint, scsiStateBytes, scsiStateBytes.length, WaitTime);

		return out1;

	}
	
	boolean bulkSend(short cmd, int para)
	{
		boolean rec=true;
		CommandPack cmdp = new CommandPack(cmd, para);
		byte[] sendByte = cmdp.cmdBytes;
		byte[] scsiBytes = getSCSI(false, 12);
		byte[] scsiStateBytes = new byte[13];

		Utils.showCommandBytes(TAG, sendByte, Utils.TRANSFER_DIRECTION.TX, false);
		if (connection.bulkTransfer(outEndpoint, scsiBytes,
				scsiBytes.length, WaitTime)!= scsiBytes.length)
			return false;

		if (connection.bulkTransfer(outEndpoint, sendByte, sendByte.length,
			WaitTime) != sendByte.length)
			return false;
	
		if (connection.bulkTransfer(inEndpoint, scsiStateBytes,
				scsiStateBytes.length, WaitTime) != scsiStateBytes.length)
			return false;

		return rec;
	}

	byte[] getSCSI(boolean recFlag, int len) {
		ByteBuffer scsiBuffer = ByteBuffer.allocate(31);
		scsiBuffer.order(ByteOrder.LITTLE_ENDIAN);
		scsiBuffer.putInt(0, 0x43425355);
		scsiBuffer.putInt(4, 0x89182b28);
		scsiBuffer.putInt(8, len);
		if (recFlag) // 1=data-in from the device to the host
		{
			scsiBuffer.put(12, (byte) 0x80);
			scsiBuffer.putShort(15, (short) 0xffef);
		} else // 0=data-out from host to the device
		{
			scsiBuffer.put(12, (byte) 0x0);
			scsiBuffer.putShort(15, (short) 0xfeef);
		}
		scsiBuffer.put(13, (byte) 0); 
		scsiBuffer.put(14, (byte) 10);
		return scsiBuffer.array();

	}
}
