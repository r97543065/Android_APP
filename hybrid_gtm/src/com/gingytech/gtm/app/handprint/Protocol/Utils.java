package com.gingytech.gtm.app.handprint.Protocol;

import android.util.Log;

public class Utils {

	// Specify show command package data direction.
	public enum TRANSFER_DIRECTION {
		TX,
		RX
	};

	//JAVA
	public static byte[] intToByteArray(int i) {   
		  byte[] result = new byte[4];   
		  result[3] = (byte)((i >> 24) & 0xFF);
		  result[2] = (byte)((i >> 16) & 0xFF);
		  result[1] = (byte)((i >> 8) & 0xFF); 
		  result[0] = (byte)(i & 0xFF);
		  return result;
		 }
	
	public static int bytesToInt(byte b1,byte b2,byte b3,byte b4)
	{
		return    b1 & 0xff|(b2 & 0xff) << 8 | (b3 & 0xff) << 16  | (b4 & 0xff) << 24;
	}
	public static short byteToShort(byte b1,byte b2)
	{
		return (short)( b1 & 0xff|(b2& 0xff) << 8 );
	}
	public static byte[] shortTobytes(short i)
	{
		byte[] rec=new byte[2];
		rec[0]=(byte) (i&0xff);
		rec[1]=(byte) ((i>>8)&0xff); 
		return rec;
	}
	public static short shortByteSum1(short i)
	{
		short rec= (short) (i&0xff + i>>8);
		return rec;
		
	}
	public static short intByteSum(int i)
	{
		short rec=0;
		rec+= (byte)(i & 0xFF)+(byte)((i >> 8) & 0xFF)+(byte)((i >> 16) & 0xFF)+(byte)((i >> 24) & 0xFF);
		return rec;
	}
	public static String byteToString(byte[] bytes) {
		String rec = "";
		for (int i = 0; i < bytes.length; i++) {
			rec += " " + String.format("%1$#2x", bytes[i]);
		}
		return rec;
	}

	// Convert command number to human readable string.
	// For show debug command and response message.
	public static String MappingCommandString(short cmdValue) {

		String cmdStr = "";

		switch(cmdValue) {
		case Defined.CMD_OPEN:
			cmdStr = "Open";
			break;
		case Defined.CMD_CLOSE:
			cmdStr = "Close";
			break;
		case Defined.CMD_USB_INTERNAL_CHECK:
			cmdStr = "UsbInternalCheck";
			break;
		case Defined.CMD_CHANGE_BAUDRATE:
			cmdStr = "ChangeBaurate";
			break;
		case Defined.CMD_CMOS_LED:
			cmdStr = "CmosLed";
			break;
		case Defined.CMD_GET_ENROLL_COUNT:
			cmdStr = "GetEnrollCount";
			break;
		case Defined.CMD_CHECK_ENROLLED:
			cmdStr = "CheckEnrolled";
			break;
		case Defined.CMD_ENROLL_START:
			cmdStr = "EnrollStart";
			break;
		case Defined.CMD_ENROLL1:
			cmdStr = "Enroll1";
			break;
		case Defined.CMD_ENROLL2:
			cmdStr = "Enroll2";
			break;
		case Defined.CMD_ENROLL3:
			cmdStr = "Enroll3";
			break;
		case Defined.CMD_IS_PRESS_FINGER:
			cmdStr = "IsPressFinger";
			break;
		case Defined.CMD_DELETE_ID:
			cmdStr = "DeleteID";
			break;
		case Defined.CMD_DELETE_ALL:
			cmdStr = "DeleteAll";
			break;
		case Defined.CMD_VERIFY:
			cmdStr = "Verify";
			break;
		case Defined.CMD_IDENTIFY:
			cmdStr = "Identify";
			break;
		case Defined.CMD_VERIFY_TEMPLATE:
			cmdStr = "VerifyTemplate";
			break;
		case Defined.CMD_IDENTIFY_TEMPLATE:
			cmdStr = "IdentifyTemplate";
			break;
		case Defined.CMD_CAPTURE_FINGER:
			cmdStr = "CaptureFinger";
			break;
		case Defined.CMD_MAKE_TEMPLATE:
			cmdStr = "MakeTemplate";
			break;
		case Defined.CMD_GET_IMAGE:
			cmdStr = "GetImage";
			break;
		case Defined.CMD_GET_RAWIMAGE:
			cmdStr = "GetRawImage";
			break;
		case Defined.CMD_GET_TEMPLATE:
			cmdStr = "GetTemplate";
			break;
		case Defined.CMD_SET_TEMPLATE:
			cmdStr = "SetTemplate";
			break;
		case Defined.CMD_SET_SECURITY_LEVEL:
			cmdStr = "SetSecurityLevel";
			break;
		case Defined.CMD_GET_SECURITY_LEVEL:
			cmdStr = "GetSecurityLevel";
			break;
		case Defined.ACK_OK:
			cmdStr = "Ack";
			break;
		case Defined.NACK_INFO:
			cmdStr = "Nack";
			break;
		default:
			cmdStr = "UnknownCommand";
			break;
		}
		return cmdStr;
	}

	// Show command package message to Logcat.
	public static void showCommandBytes(String tag, byte[] data, TRANSFER_DIRECTION dir, boolean isDataPackage) {

		StringBuffer sb = new StringBuffer();

		if(dir == TRANSFER_DIRECTION.TX) {
			sb.append("TX: ");
		} else {
			sb.append("RX: ");
		}

		if(isDataPackage) {
			sb.append(String.format("%02X %02X %02X%02X ", data[0], data[1], data[3], data[2]));

			int dataLen = data.length;

			sb.append(String.format("%dbytes ", dataLen));
			sb.append(String.format("%02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X ... ", data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15]));
			sb.append(String.format("%02X%02X", data[data.length - 1], data[data.length - 2]));
		} else {
			sb.append(String.format("%02X %02X %02X%02X %02X%02X%02X%02X %02X%02X %02X%02X", data[0], data[1], data[3], data[2], data[7], data[6], data[5], data[4], data[9], data[8], data[11], data[10]));
			sb.append(String.format(" - %s", Utils.MappingCommandString(Utils.byteToShort(data[8], data[9]))));
		}

		Log.d(tag, String.format("%s", sb.toString()));
	}
}
