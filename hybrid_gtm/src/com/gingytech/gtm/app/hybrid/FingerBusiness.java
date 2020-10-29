package com.gingytech.gtm.app.hybrid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import com.gingytech.gtm.app.handprint.Protocol.DataPacket;
import com.gingytech.gtm.app.handprint.Protocol.Defined;
import com.gingytech.gtm.app.handprint.Protocol.ModuleInfo;
import com.gingytech.gtm.app.handprint.Protocol.ResponsePacket;
import com.gingytech.gtm.app.handprint.Protocol.Utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.GetChars;
import android.util.Log;

public class FingerBusiness {

	final String TAG = "FingerBusiness";
	UsbHelper theUsb;
	UartHelper theUart;
	fileHelp fileLog;
	Handler pHandler;
	private int commType;
	public int safety;
	public int BIN = 2;
	public int exit = 0;
	public boolean isGTM5210C2 = false;
	public boolean isGTM5210C3 = false;
	public boolean isSetSafetySupport = false;
	private int nSerialPort=1;
	public boolean isModuleInfoSupport = false;
	public ModuleInfo moduleInfo;
	enum SENSOR_TYPE{
		GTM,
		GHM
	};
	SENSOR_TYPE sensorType = SENSOR_TYPE.GTM;
	final int extraDataLen = Defined.PACK_HEADER_LEN + Defined.PACK_DEVICEID_LEN + Defined.PACK_CHECKSUM_LEN;

	public FingerBusiness(Context c, Handler h) {
		theUsb = new UsbHelper(c, usbHandler);
		fileLog = new fileHelp(c);
		pHandler = h;
	}

	public FingerBusiness(Context c, Handler h, int type) {
		// by commType to new UsbHelper or UartHelper
		this.commType = type;
		fileLog = new fileHelp(c);
		pHandler = h;

		switch (this.commType) {
		case Defined.COMM_TYPE_UART:
			theUart = new UartHelper(c, uartHandler);
			theUsb = null;
			break;
		case Defined.COMM_TYPE_USB:
		default:
			theUsb = new UsbHelper(c, usbHandler);
			theUart = null;
			break;
		}
	}

	public void Close() {
		fileLog.writeLog(TAG + " Close");

		if (commType == Defined.COMM_TYPE_USB) {
			theUsb.Close();
		} else if (commType == Defined.COMM_TYPE_UART) {
			theUart.closeSerialPort();
		}
	}

	public void Identify() {
		fileLog.writeLog(TAG + " Identify");
		if(GetEnrolledCount() == 0){
			BackMessage("No finger enrolled.");
			Message msg = new Message();
			msg.what = Defined.ENABLEBUTTON;
			pHandler.sendMessage(msg);
			return;
		}
		IdentifyFinger identifyRunnable = new IdentifyFinger();
		new Thread(identifyRunnable).start();
	}

	public void Login() {
		fileLog.writeLog(TAG + " Login");
		GTMEnroll enrollRunnable = new GTMEnroll();
		new Thread(enrollRunnable).start();
	}

	public void DeletAll() {
		fileLog.writeLog(TAG + " DeletAll");
		DeletIDAll DeletRunnable = new DeletIDAll();
		new Thread(DeletRunnable).start();
	}

	public void GetRawImage() {
		fileLog.writeLog(TAG + " GetRawImage");
		GetRawImage GetRawRunnable = new GetRawImage();
		new Thread(GetRawRunnable).start();
	}
	
	public void GetFingerImage() {
		// for shuttle
		fileLog.writeLog(TAG + " GetFingerImage");
		GetFingerImage GetFingerRunnable = new GetFingerImage();
		new Thread(GetFingerRunnable).start();
	}

	public void DoGetTemplateDB() {
		fileLog.writeLog(TAG + " DoGetTemplateDB");
		if(GetEnrolledCount() == 0){
			BackMessage("No finger enrolled.");
			Message msg = new Message();
			msg.what = Defined.ENABLEBUTTON;
			pHandler.sendMessage(msg);
			return;
		}
		new Thread(new GetTemplateDatabase()).start();
	}
	
	public void DoSetTemplateDB() {
		fileLog.writeLog(TAG + " DoSetTemplateDB");
		new Thread(new SetTemplateDatabase()).start();
	}

	public void FBSafety() {
		fileLog.writeLog(TAG + " FBSafety");
		SetSafety SetRunnable = new SetSafety();
		new Thread(SetRunnable).start();
	}

	ResponsePacket EnrollStart(int id) throws NullPointerException {
		fileLog.writeLog(TAG + " EnrollStart");

		ResponsePacket Response = null;

		if (commType == Defined.COMM_TYPE_USB) {
			Response = theUsb.SendCmd(Defined.CMD_ENROLL_START, id);
		} else if (commType == Defined.COMM_TYPE_UART) {
			try {
				Response = theUart.sendCommand(Defined.CMD_ENROLL_START, id);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		return Response;
	}

	int CheckEnrolledNotUsed(ResponsePacket Response) throws NullPointerException {
		fileLog.writeLog(TAG + " CheckEnrolledNotUsed");

		int result = -1;

		for (int i = 0; i < moduleInfo.MaxRecordCount; i++) {
			ResponsePacket rs = null;
			if (commType == Defined.COMM_TYPE_USB) {
				rs = theUsb.SendCmd(Defined.CMD_CHECK_ENROLLED, i);
			} else if (commType == Defined.COMM_TYPE_UART) {
				try {
					rs = theUart.sendCommand(Defined.CMD_CHECK_ENROLLED, i);
				} catch (IOException e) {
				}
			}

			if (rs == null)
				return result;

			Response.Response = rs.Response;
			Response.Parameter = rs.Parameter;
			if(Response.Response == Defined.ACK_OK){
				result = i;
				BackMessage("ID("+ result + "/" + moduleInfo.MaxRecordCount +") is used, and search next ID ...");
				continue;
			}
			else if(Response.Response == Defined.NACK_INFO && Response.Parameter == Defined.NACK_IS_NOT_USED){
				result = i;
				Log.e(TAG, "ID id not used" + result);
				break;
			}
		}

		if(result == moduleInfo.MaxRecordCount - 1){
			Log.e(TAG, "No ID can be use.");
			result = -2;
		}

		return result;
	}

	int DeletID_All() throws NullPointerException {
		fileLog.writeLog(TAG + " DeletID_All");

		ResponsePacket rs = null;

		if (commType == Defined.COMM_TYPE_USB) {
			rs = theUsb.SendCmd(Defined.CMD_DELETE_ALL, 0);
		} else if (commType == Defined.COMM_TYPE_UART) {
			try {
				rs = theUart.sendCommand(Defined.CMD_DELETE_ALL, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Message msg = new Message();
		msg.what = Defined.ENABLEBUTTON;
		pHandler.sendMessage(msg);

		if(rs.Parameter == Defined.NACK_DB_IS_EMPTY)
			return rs.Parameter;

		return rs.Response;
	}

	// boolean DeletID() {
	// fileLog.writeLog(TAG + " DeletID");
	//
	// ResponsePacket rs;
	// rs = theUsb.SendCmd(Defined.CMD_DELETE, idnumber);
	// if (rs.Response ==Defined.ACK_OK)
	// {
	// return true;
	// }
	// return false;
	// }

	void LED_ONOFF(int para) {
		if (commType == Defined.COMM_TYPE_USB) {
			theUsb.SendCmd(Defined.CMD_CMOS_LED, para);
		} else if (commType == Defined.COMM_TYPE_UART) {
			try {
				theUart.sendCommand(Defined.CMD_CMOS_LED, para);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	void IsPressFinger() throws NullPointerException {
		fileLog.writeLog(TAG + " IsPressFinger start");
		short ispressed = 1;

		BackMessage("Lift your finger.");

		do {
			ResponsePacket IsPressFingerResponse = null;

			if (commType == Defined.COMM_TYPE_USB) {
				IsPressFingerResponse = theUsb.SendCmd(Defined.CMD_IS_PRESS_FINGER, 0);
			} else if (commType == Defined.COMM_TYPE_UART) {
				try {
					IsPressFingerResponse = theUart.sendCommand(Defined.CMD_IS_PRESS_FINGER, 0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (IsPressFingerResponse.Response == Defined.ACK_OK && IsPressFingerResponse.Parameter != 0) {

				ispressed = 0;
				BackMessage("Place your finger.");
			}
		} while (ispressed == 1);
		fileLog.writeLog(TAG + " IsPressFinger end");
	}

	// para=0, not best image but fast, it used in verify finger.
	// para!=0, best image but slow, it used in enroll.
	boolean CaptureFinger(int para) {
		fileLog.writeLog(TAG + " CaptureFinger start");
		boolean rec = false;
		boolean waitPressFinger = true;
		long waitTime = 10000;
		long StartTime;
		long endTime;
		StartTime = System.currentTimeMillis();
		endTime = System.currentTimeMillis();

		do {
			try {
				Thread.sleep(500); // origin: 250
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			endTime = System.currentTimeMillis();

			if ((endTime - StartTime) > waitTime) {
				waitPressFinger = false;
				break;
			}

			ResponsePacket CaptureFingerResponse = null;

			if (commType == Defined.COMM_TYPE_USB) {
				CaptureFingerResponse = theUsb.SendCmd(Defined.CMD_CAPTURE_FINGER, para);
			} else if (commType == Defined.COMM_TYPE_UART) {
				try {
					CaptureFingerResponse = theUart.sendCommand(Defined.CMD_CAPTURE_FINGER, para);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (CaptureFingerResponse == null) {
				continue;
			}
			if (CaptureFingerResponse.Response == Defined.ACK_OK) {
				rec = true;
				break;
			}
		} while (exit == 0 && waitPressFinger);
		Log.e(TAG, "waitPressFinger:" + waitPressFinger);

		fileLog.writeLog(TAG + " CaptureFinger end");
		return rec;
	}

	// para=0, not best image but fast, it used in verify finger.
	// para!=0, best image but slow, it used in enroll.
	boolean CaptureFinger1(int para) {
		fileLog.writeLog(TAG + " CaptureFinger1 start");
		boolean rec = false;

		ResponsePacket CaptureFingerResponse = null;

		if (commType == Defined.COMM_TYPE_USB) {
			CaptureFingerResponse = theUsb.SendCmd(Defined.CMD_CAPTURE_FINGER, para);
		} else if (commType == Defined.COMM_TYPE_UART) {
			try {
				CaptureFingerResponse = theUart.sendCommand(Defined.CMD_CAPTURE_FINGER, para);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		if (CaptureFingerResponse == null) {
			fileLog.writeLog(TAG + " CaptureFinger1 connection failed.");
			return false;
		}

		if (CaptureFingerResponse.Response == Defined.ACK_OK)
			rec = true;
		fileLog.writeLog(TAG + " CaptureFinger1 end");
		return rec;
	}

	// This method for continuous check finger.
	// When captured twice, it will stop and return true.
	boolean CaptureFingerCheck(int para) {
		fileLog.writeLog(TAG + " CaptureFingerCheck start");

		boolean result = false;

		long waitTime = 10000;
		long startTime = System.currentTimeMillis();

		while (true) {
			boolean capture1 = CaptureFinger1(para);
			boolean capture2 = CaptureFinger1(para);

			if (capture1 && capture2) {
				// BackMessage("CaptureFinger success.");
				result = true;
				break;
			}

			long endTime = System.currentTimeMillis();

			if ((endTime - startTime) > waitTime) {
				BackMessage("Capture Finger timeout.");
				fileLog.writeLog(TAG + " Capture Finger Timeout.");
				result = false;
				break;
			}

		}

		fileLog.writeLog(TAG + " CaptureFingerCheck end");

		return result;
	}

	class GTMEnroll implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			fileLog.writeLog(TAG + " GTMEnroll start");
			short[] enrollCmds = new short[moduleInfo.EnrollCount];
			int enrollCmdParam = 0;

			try {
				int ID = -1;

				ResponsePacket response = null;

				// 0---CheckEnrolled - Get Not Used ID
				ResponsePacket temp_response = new ResponsePacket(null);
				ID = CheckEnrolledNotUsed(temp_response);
				if (ID == -1) {
					BackMessage("Communication Failed.");
					return;
				}
				else if(ID == -2){
					BackMessage("No ID can be use.");
					return;
				}

				response = EnrollStart(ID);
				if(response != null){
					if(response.Response == Defined.NACK_INFO && response.Parameter == Defined.NACK_DB_IS_FULL){
						BackMessage("FB Database is full.");
						return;
					}
				}
				else{
					BackMessage("Communication Failed!");
					return;
				}

				LED_ONOFF(1);
				BackMessage("Place your finger.");
				if (response.Response == Defined.ACK_OK) {

					switch(sensorType){
						case GTM:
							for(int i = 0 ; i < enrollCmds.length ; i++)
								enrollCmds[i] = (short) (Defined.CMD_ENROLL1 + i);
							break;
						case GHM:
							for(int i = 0 ; i < enrollCmds.length ; i++)
								enrollCmds[i] = Defined.CMD_ENROLL1;
							break;
					}

					for (int i = 0; i < moduleInfo.EnrollCount; i++) {
						if (CaptureFingerCheck(1)) {
							if (commType == Defined.COMM_TYPE_USB) {
								byte[] buf = GetImage();
								ShowFingerprintImage(buf);
							}

							if (commType == Defined.COMM_TYPE_USB) {
								enrollCmdParam = (sensorType == SENSOR_TYPE.GHM) ? i : 0;
								response = theUsb.SendCmd(enrollCmds[i], enrollCmdParam);
							} else if (commType == Defined.COMM_TYPE_UART) {
								enrollCmdParam = (sensorType == SENSOR_TYPE.GHM) ? i : 0;
								response = theUart.sendCommand(enrollCmds[i], enrollCmdParam);
							}

							if (response.Response == Defined.ACK_OK) {
								String order = "";
								if(i == 0)
									order = "1st";
								else if(i == 1)
									order = "2nd";
								else if(i == 2)
									order = "3rd";
								else
									order = String.format("%dth", (i + 1));

								BackMessage("Make " + order + " template success.");

								ShowEnrolledImage(i, 0);

								// flag for enroll success
								if(i == moduleInfo.EnrollCount - 1){
									BackMessage("Enroll Success.");
								} else {
									IsPressFinger();
								}
							} else {
								if (response.Parameter == Defined.NACK_BAD_FINGER) {
									throw new Exception("Bad Finger.");
								} else if (response.Parameter == Defined.NACK_ENROLL_FAILED) {
									throw new Exception("Enroll Failed.");
								} else {
									throw new Exception("ID = " + response.Parameter + " duplicate enroll.");
								}
							}
						} else {
							throw new Exception("Timeout");
						}
					}
				}

			} catch (NullPointerException e) {
				BackMessage("connection failed.");
				fileLog.writeLog(TAG + " connection failed.");
			} catch (IOException e) {
				BackMessage("Uart sendComand Failed.");
				fileLog.writeLog(TAG + " Uart sendComand Failed.");
			} catch (Exception e) {
				BackMessage(e.getMessage());
				fileLog.writeLog(TAG + " " + e.getMessage());
			} finally {
				LED_ONOFF(0);

				Message msg = new Message();
				msg.what = Defined.ENABLEBUTTON;
				pHandler.sendMessage(msg);

				fileLog.writeLog(TAG + " GTMEnroll end");
			}
		}
	}

	void ShowFingerprintImage(byte[] buffer) {
		if (buffer != null) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			msg.what = Defined.GETIMAGE;
			bundle.putByteArray(Defined.BundlerDATA, buffer);
			msg.setData(bundle);
			pHandler.sendMessage(msg);
		}
	}

	void ShowEnrolledImage(int index, int toggle) {
		Message msg = new Message();
		msg.what = Defined.SHOWIMAGE;
		msg.arg1 = index;
		msg.arg2 = toggle; // toggle for flicker effect.
		pHandler.sendMessage(msg);
	}

	void BackMessage(String strMessage) {
		Message msg = new Message();
		msg.what = Defined.ShowMessage;
		Bundle bundle = new Bundle();
		bundle.putString("info", strMessage);
		msg.setData(bundle);
		pHandler.sendMessage(msg);
	}

	byte[] GetImage() {
		DataPacket GetImageData = null;
		byte[] rec = null;
		int dataSize = moduleInfo.CaptureImageWidth * moduleInfo.CaptureImageHeight;

		if (commType == Defined.COMM_TYPE_USB) {
			ResponsePacket GetImageResponse = theUsb.SendCmd(Defined.CMD_GET_IMAGE, 0);
			if (GetImageResponse.Response == Defined.ACK_OK) {
				GetImageData = theUsb.bulkData(dataSize);
			}
		} else if (commType == Defined.COMM_TYPE_UART) {
			//The UART communication is slower.
			//So the image need to spent more times.
			try {
				GetImageData = theUart.sendCommand(Defined.CMD_GET_IMAGE, 0, (dataSize + extraDataLen));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (GetImageData != null) {
			rec = GetImageData.Data;
		}

		return rec;
	}

	byte[] GetLiveImage() throws NullPointerException {
		DataPacket GetImageData = null;
		byte[] rec = null;
		int dataSize = moduleInfo.RawImageWidth * moduleInfo.RawImageHeight;

		if (commType == Defined.COMM_TYPE_USB) {
			ResponsePacket GetImageResponse = theUsb.SendCmd(Defined.CMD_GET_RAWIMAGE, 0);
			if (GetImageResponse.Response == Defined.ACK_OK) {
				GetImageData = theUsb.bulkData(dataSize);
			}
		} else if (commType == Defined.COMM_TYPE_UART) {
			//The UART communication is slower.
			//So the image need to spent more times.
			try {
				Log.d(TAG, String.format("UART: GET_RAW_IMAGE begin"));
				changeSpeed(1, 115200);
				GetImageData = theUart.sendCommand(Defined.CMD_GET_RAWIMAGE, 0, (dataSize + extraDataLen));
				changeSpeed(1, 9600);
				Log.d(TAG, String.format("UART: GET_RAW_IMAGE end"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				Log.d(TAG, String.format("UART: GET_RAW_IMAGE exception occurred!"));
			}
		}

		if (GetImageData != null) {
			rec = GetImageData.Data;
		}

		return rec;
	}

	// In GHM8511, Empty Template have 10240 of 0xFF.
	// We assume continuous 10 0xFF would be empty template.
	boolean IsTemplateEmpty(byte[] template) {
		boolean isEmpty = true;
		int length = (template.length > 10) ? 10 : template.length;
		for(int i = 0;i < length;i++) {
			if(template[i] != (byte)0xFF) {
				isEmpty = false;
				break;
			}
		}
		return isEmpty;
	}

	byte[] GetTemplate(int id) throws NullPointerException {
		DataPacket GetTemplateData = null;
		byte[] rec = null;
		int dataSize = moduleInfo.TemplateSize;
		
		if(commType == Defined.COMM_TYPE_USB) {
			ResponsePacket resPack = theUsb.SendCmd(Defined.CMD_GET_TEMPLATE, id);
			if(resPack != null) {
				if(resPack.Response == Defined.ACK_OK) {
					GetTemplateData = theUsb.bulkData(dataSize);
				}
			}
		} else if(commType == Defined.COMM_TYPE_UART) {
			try {
				// change speed to 115200 before uart transfer
				// reset speed to 9600 after uart transfer complete
				Log.d(TAG, String.format("UART: GET_TEMPLATE begin"));
				changeSpeed(1, 115200);
				GetTemplateData = theUart.sendCommand(Defined.CMD_GET_TEMPLATE, id, (dataSize + extraDataLen));
				changeSpeed(1, 9600);
				Log.d(TAG, String.format("UART: GET_TEMPLATE end"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				Log.d(TAG, String.format("UART: GET_TEMPLATE exception occurred!"));
			}
		}
		
		if(GetTemplateData != null) {
			rec = GetTemplateData.Data;
		}
		
		return rec;
	}

	ResponsePacket SetTemplate(GtTemplate gtTemplate) throws NullPointerException {
		ResponsePacket resPack = null;
		if(commType == Defined.COMM_TYPE_USB) {
			resPack = theUsb.SendCmd(Defined.CMD_SET_TEMPLATE, gtTemplate.getId(), gtTemplate.getTemplate());
		} else if(commType == Defined.COMM_TYPE_UART) {
			// change speed to 115200 before uart transfer
			// reset speed to 9600 after uart transfer complete
			try {
				Log.d(TAG, String.format("UART: SET_TEMPLATE begin"));
				changeSpeed(1, 115200);
				resPack = theUart.sendCommand(Defined.CMD_SET_TEMPLATE, gtTemplate.getId(), gtTemplate.getTemplate());
				changeSpeed(1, 9600);
				Log.d(TAG, String.format("UART: SET_TEMPLATE end"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resPack;
	}

	int Hybrid_ImgProcess(byte[] gbyImgHD, int Width, int Height, int Shift_Level) {
		// TODO Auto-generated method stub
		return 0;
	}

	int VerifyCheck() throws NullPointerException {
		int rec = -1;

		ResponsePacket VerifyReaponse = null;

		if (commType == Defined.COMM_TYPE_USB) {
			VerifyReaponse = theUsb.SendCmd(Defined.CMD_IDENTIFY, 0);
		} else if (commType == Defined.COMM_TYPE_UART) {
			try {
				VerifyReaponse = theUart.sendCommand(Defined.CMD_IDENTIFY, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (VerifyReaponse.Response == Defined.ACK_OK) {
			rec = VerifyReaponse.Parameter;
		}
		return rec;
	}

	class IdentifyFinger implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				fileLog.writeLog(TAG + " IdentifyFinger start");
				int TimeCount = 10;
				long StartTime;
				long endTime;
				LED_ONOFF(1);
				int loop = 0;
				BackMessage("Place your finger.");
				do {
					if (CaptureFinger(0) && CaptureFinger(0)) {
						if (commType == Defined.COMM_TYPE_USB) {
							Message msg = new Message();
							msg.what = Defined.GETIMAGE;
							Bundle bundle = new Bundle();
							bundle.putByteArray(Defined.BundlerDATA, GetImage());
							msg.setData(bundle);
							pHandler.sendMessage(msg);
						}
						StartTime = System.currentTimeMillis();
						int id = VerifyCheck();
						if (id < 0) {
							loop++;
							// BackMessage("There is no data base!");
							if (loop == 3) {
								endTime = System.currentTimeMillis();
								Message msg2 = new Message();
								msg2.what = Defined.VERIFYERROR;
								msg2.arg1 = (int) (endTime - StartTime);
								pHandler.sendMessage(msg2);
								loop = 10;
							}

						} else {
							endTime = System.currentTimeMillis();
							// BackMessage("ID="+id+" identify succeed!");
							Message msg1 = new Message();
							msg1.what = Defined.VERIFYCORRECT;
							msg1.arg1 = (int) (endTime - StartTime);
							msg1.arg2 = id;
							pHandler.sendMessage(msg1);
							loop = 10;
						}

					} else {
						BackMessage("Identify timeout.");
						Message msg1 = new Message();
						msg1.what = Defined.ENABLEBUTTON;
						msg1.arg1 = TimeCount;
						pHandler.sendMessage(msg1);
						loop = 10;
					}
				} while (loop < 3);
			} catch (NullPointerException e) {
				BackMessage("connection failed.");
			} finally {
				LED_ONOFF(0);
				fileLog.writeLog(TAG + " IdentifyFinger end");
			}

		}

	}

	class DeletIDAll implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			switch(DeletID_All()){
				case Defined.ACK_OK:
					BackMessage("Delete FP db successful.");
					break;
				case Defined.NACK_DB_IS_EMPTY:
					BackMessage("Datebase is empty.");
					break;
				default:
					BackMessage("Delete FP db fail.");
						break;
			}
		}
	}

	class GetImage implements Runnable {

		@Override
		public void run() {
			int TimeCount = 10;
			LED_ONOFF(1);
			if (CaptureFinger(0)) {
				Message msg = new Message();
				msg.what = Defined.GETIMAGE;
				Bundle bundle = new Bundle();
				bundle.putByteArray(Defined.BundlerDATA, GetImage());
				msg.setData(bundle);
				pHandler.sendMessage(msg);
			} else {
				BackMessage("Timeout=" + TimeCount + "second");
				Message msg1 = new Message();
				msg1.arg1 = TimeCount;
				pHandler.sendMessage(msg1);
			}
			LED_ONOFF(0);
		}
	}

	class GetRawImage implements Runnable {

		@Override
		public void run() {
			
			for(int i=0;i<3;i++) {
				try {
					LED_ONOFF(1);						
					Message msg = new Message();
					Bundle bundle = new Bundle();
					//SystemClock.sleep(1300);
					// In GTM5210C2/C3/C5, it have no GET_HD_IMAGE command.
					msg.what = Defined.GETIMAGERaw;				
					double stime = System.currentTimeMillis();
					bundle.putByteArray(Defined.BundlerDATA, GetLiveImage());
					double TotalTime = System.currentTimeMillis() - stime;
					Log.d(TAG, "GetLiveImage totaltime : " + TotalTime + "ms");
					fileLog.writeLog(TAG + " GetLiveImage Done");
	
					msg.setData(bundle);
					pHandler.sendMessage(msg);
	
				} catch (NullPointerException e) {
					BackMessage("connection failed.");
				} finally {
					Message msg1 = new Message();
					msg1.what = Defined.ENABLEBUTTON;
					pHandler.sendMessage(msg1);
					LED_ONOFF(0);
					BIN = 2;
					exit = 0;
				}
				
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {				
					e.printStackTrace();
				}
			
			}

			
			
		}
	}
	
	class GetFingerImage implements Runnable {
		// for shuttle, implements CaptureFinger(0x60) and GetImage(0x62)

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] buf = null;

			try {
				LED_ONOFF(1);

				while(!CaptureFingerCheck(1));

				buf = GetImage();
				ShowFingerprintImage(buf);
			} catch(NullPointerException e) {
				BackMessage("connection failed.");
			} finally {
				Message msg1 = new Message();
				msg1.what = Defined.ENABLEBUTTON;
				pHandler.sendMessage(msg1);
				LED_ONOFF(0);
			}
		}
	}

	class GetTemplateDatabase implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BackMessage("Getting template database...");

			// demo for C3 module config
			// user can modify this config on demand.
			// reference this:
			// public static final short FP_MAX_FINGERS_C2	= 20;
			// public static final short FP_MAX_FINGERS_C3	= 200;
			// public static final short FP_MAX_FINGERS_C5	= 2000;
			// public static final short FP_MAX_FINGERS_C32	= 1000;
			// public static final short FP_MAX_FINGERS_FX2	= 3000;
			int templateMaxCount = moduleInfo.MaxRecordCount;
			int templateTotalCount = 0;
			int i = 0;
			try {
				// open template db file
				fileLog.beginWriteTemplateDBFile();

				// forloop to iterate
				for(i = 0;i < templateMaxCount;i++) {
					int id = i;
					// Get template
					byte[] template = null;
					switch(sensorType){
						case GHM:
							template = GetTemplate(id);
							break;
						case GTM:
							template = GetTemplate(id);
							break;
					}

					// Write template
					if(template != null && IsTemplateEmpty(template) == false) {
						GtTemplate gtTemplate = new GtTemplate(id, template);

						fileLog.writeTemplateDBFile(gtTemplate);

						templateTotalCount += 1;
					}
				}
				Log.e(TAG, String.format("templateTotalCount: %d", templateTotalCount));
				// finalize template DB file
				fileLog.endWriteTemplateDBFile(templateTotalCount);
			} catch(NullPointerException e) {
				e.printStackTrace();
			} finally {
				BackMessage("Get template db completed.");

				Message msg = new Message();
				msg.what = Defined.ENABLEBUTTON;
				pHandler.sendMessage(msg);
			}
		}
		
	}
	
	/*
	 * 霅血�����
	 * �Template��atabase���之�璅∠��摮���������頞蝭��emplate�瘜�銝行��NACK_DB_IS_FULL��隤文��潦��
	 * ���遣霅唳��膩�車雿輻���������蝝靘�葫閰行�銵���
	 * ����蝙��������atabase���200��捆���芋蝯葉蝯�����200蝑���嚗���蝚�201蝑���NACK_DB_IS_FULL��隤方�銝虫��迫��銵��
	 */
	class SetTemplateDatabase implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BackMessage("Setting template database...");

			int templateTotalCount = 0;
			int i = 0;
			// currently, only for C3 module.
			int templateMaxCount = moduleInfo.MaxRecordCount;
			int templateSize = moduleInfo.TemplateSize;

			ResponsePacket resPack = null;

			boolean isErrorOccurred = false;
			String errorMessage = "";

			try {
				int[] retValue = new int[1];
				long currentPos = 0;

				// Get template count and current file pointer.
				currentPos = fileLog.beginReadTemplateDBFile(retValue);
				templateTotalCount = retValue[0];

				// iterate to get template
				for(i = 0;i < templateTotalCount;i++) {

//					Thread.sleep(5000);

					GtTemplate gtTemplate = new GtTemplate();
					currentPos = fileLog.readTemplateDBFile(gtTemplate, templateSize, currentPos);

					switch(sensorType){
						case GHM:
							resPack = SetTemplate(gtTemplate);
							break;
						case GTM:
							resPack = SetTemplate(gtTemplate);
							break;
					}

					// Indicate error, and get message.
					if(resPack != null) {
						if(resPack.Response == Defined.ACK_OK) {
							BackMessage(String.format("insert template %d ok.", gtTemplate.getId()));
						} else {
							switch(resPack.Parameter) {
								case Defined.NACK_DB_IS_FULL:
									errorMessage = "FB Database is full.";
									isErrorOccurred = true;
									break;
								case Defined.NACK_IS_ALREADY_USED:
									errorMessage = String.format("Id %d is already used.", gtTemplate.getId());
									isErrorOccurred = true;
									break;
								case Defined.NACK_COMM_ERR:
									errorMessage = "Communication error.";
									isErrorOccurred = true;
									break;
								case Defined.NACK_DEV_ERR:
									errorMessage = "Device error.";
									isErrorOccurred = true;
									break;
								default:
									// This duplicated check might not be happened.
									if(resPack.Parameter >= 0 && resPack.Parameter < templateMaxCount) {
										errorMessage = String.format("Duplicated id: %d", resPack.Parameter);
										isErrorOccurred = true;
									}
									break;
							}
						}
					}
					
					if(isErrorOccurred)
						break;
					
					Thread.sleep(20);
				}
				
			} catch(NullPointerException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				isErrorOccurred = true;
				errorMessage = "Template DB file not found.";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				String message = (isErrorOccurred) ? errorMessage : "Set template db completed.";
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BackMessage(message);

				Message msg = new Message();
				msg.what = Defined.ENABLEBUTTON;
				pHandler.sendMessage(msg);
			}
		}
	}

	class SetSafety implements Runnable {

		@Override
		public void run() {

			ResponsePacket rec = null;

			if (commType == Defined.COMM_TYPE_USB) {
				rec = theUsb.SendCmd(Defined.CMD_SET_SECURITY_LEVEL, safety);
			} else if (commType == Defined.COMM_TYPE_UART) {

			}

			if (rec == null)
				return;
		}
	}

	class OPEN implements Runnable {
		short cmd = Defined.CMD_OPEN;
		short UsbInternalCheck = 3;
		int para = 1;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			fileLog.writeLog(TAG + " OPEN");

			DataPacket InfoData = null, ModuleInfoData = null;

			if (commType == Defined.COMM_TYPE_USB) {
				ResponsePacket rec = theUsb.SendCmd(cmd, para);

				if (rec == null) {
					BackMessage("open rec null...");
					return;
				}

				if (rec.Response == Defined.ACK_OK) {
					InfoData = theUsb.bulkData(24);
				} else {
					fileLog.writeLog(TAG + String.format("Response: %d Parameter: %d", rec.Response, rec.Parameter));
					BackMessage(String.format("Response: %d Parameter: %d", rec.Response, rec.Parameter));
				}
			} else if (commType == Defined.COMM_TYPE_UART) {

				boolean isUartAvailable = theUart.openSerialPort(nSerialPort, 9600);
				if (!isUartAvailable) {
					BackMessage("Open Serial Port 1 Failed.");
					return;
				}

				try {
					InfoData = theUart.sendCommand(cmd, 1, 24 + extraDataLen);
				} catch (IOException e) {
					BackMessage("SendCommand Failed.");
				}
			}

			if (InfoData == null) {
				BackMessage("Device Open failed.");
			} else {
				HashMap<String,String> versionMap= generateProductInfo(InfoData);

				if(!isGTM5210C2 && !isGTM5210C3){
					try {
						if (commType == Defined.COMM_TYPE_USB) {
							ResponsePacket rec = theUsb.SendCmd(Defined.CMD_GET_MODULE_INFO, 0);
							if (rec.Response == Defined.ACK_OK) {
								//The rec.Parameter means data length.
								ModuleInfoData = theUsb.bulkData(rec.Parameter);
							}
						} else if (commType == Defined.COMM_TYPE_UART) {
							//The "0 + extraDataLen" means data length does't know before the command does't send to module yet.
							ModuleInfoData = theUart.sendCommand(Defined.CMD_GET_MODULE_INFO, 0, 0 + extraDataLen);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if(ModuleInfoData != null){
					moduleInfo = new ModuleInfo(ModuleInfoData.Data);
					//The CaptureImage area can decide the module is GTM or GHM.
					if(moduleInfo.CaptureImageHeight == 160 && moduleInfo.CaptureImageWidth == 160){
						sensorType = SENSOR_TYPE.GHM;
					}

					isModuleInfoSupport = true;
				}
				else{
					//Need to set default value, if the module NOT support GET_MODULE_INFO command.
					moduleInfo = new ModuleInfo();
					moduleInfo.EnrollCount = 3;
					moduleInfo.MaxRecordCount = Defined.FP_MAX_FINGERS_C3;

					if(isGTM5210C2){
						moduleInfo.RawImageWidth = 240;
						moduleInfo.RawImageHeight = 216;
						moduleInfo.CaptureImageWidth = 240;
						moduleInfo.CaptureImageHeight = 216;
						moduleInfo.TemplateSize = Defined.FP_TEMPLATE_SIZE_C2;
					}
					else{
						moduleInfo.RawImageWidth = 160;
						moduleInfo.RawImageHeight = 120;
						moduleInfo.CaptureImageWidth = 202;
						moduleInfo.CaptureImageHeight = 258;
						moduleInfo.TemplateSize = Defined.FP_TEMPLATE_SIZE_C3C5;
					}

					isModuleInfoSupport = false;
				}

				// Device is ready, then send message to main thread.
				Message msg = new Message();
				Bundle bundle = new Bundle();
				msg.what = Defined.DEVICE_INI;
				bundle.putSerializable(Defined.VERSION_MAP, versionMap);
				msg.setData(bundle);
				pHandler.sendMessage(msg);
			}

		}

	}

	private HashMap<String, String> generateProductInfo(DataPacket dataPack) {
		if (dataPack == null)
			return null;

		byte[] temp = null;
		byte[] temp1 = new byte[4];
		byte[] temp2 = new byte[4];
		byte[] temp3 = new byte[16];
		StringBuffer FirmwareVersion = new StringBuffer();
		StringBuffer IsoAreaMaxSize = new StringBuffer();
		StringBuffer DeviceSN = new StringBuffer();

		temp = dataPack.Data;

		for (int i = 0; i < 4; i++)
			temp1[3 - i] = temp[i];

		for (byte b : temp1)
			FirmwareVersion.append(String.format("%02x", b));

		for (int i = 4; i < 8; i++)
			temp2[7 - i] = temp[i];

		for (byte b : temp2)
			IsoAreaMaxSize.append(String.format("%02x", b));

		for (int i = 0; i < 16; i++)
			temp3[i] = temp[i + 8];

		for (byte b : temp3)
			DeviceSN.append(String.format("%02X", b));

		// original APPSN: 15.0421.06.07
		HashMap<String, String> versionMap = new HashMap<String, String>();
		versionMap.put(Defined.FIRMWARE_VERSION, FirmwareVersion.toString());
		versionMap.put(Defined.ISO_AREA_MAX_SIZE, IsoAreaMaxSize.toString());
		versionMap.put(Defined.DEVICE_SN, DeviceSN.toString());

		// Check current device is GTM5210C2 or not.
		// Currently the image size of C2 is different from C3 and C5.
		// NOTE: Use first 2 bytes of Device Serial Number to judge C2,
		// C3,
		// and C5.
		// In addition, C3 may have some different in last few bytes.
		byte[] C2_01 = { 0x52, 0x4A };

		if ((C2_01[0] == temp3[0]) && (C2_01[1] == temp3[1]))
			isGTM5210C2 = true;
		else {
			isGTM5210C2 = false;
			
			if(DeviceSN.substring(0, 4).equalsIgnoreCase("EF13")) {
				isGTM5210C3 = true;
			} else {
				isGTM5210C3 = false;
			}
		}

		// 1. Check current firmware version does support
		// SetSecurityLevel
		// command or not.
		// 2. Currently, we can check SetSecurityLevel support by first
		// 4
		// characters as substring of FirmwareVersion.
		// If it is larger than or equal to 2015, it might be support.
		// 3. SetSecurityLevel is command name, and SetSafety is
		// variable name in this code.
		if (Integer.parseInt(FirmwareVersion.substring(0, 4)) >= 2015) {
			isSetSafetySupport = true;
		} else {
			isSetSafetySupport = false;
		}

		return versionMap;
	}

	// Uart Methods

	private void changeSpeed(int num, int speed) {
		try {
			boolean isOK = theUart.sendCommandAndCheck(Defined.CMD_CHANGE_BAUDRATE, speed);

			if (isOK) {
				theUart.closeSerialPort();
				theUart.openSerialPort(num, speed);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			BackMessage("change baudrate failed.");
		}
	}

	// Uart Methods

	Handler usbHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.e(TAG, "USB handler start" + msg.what);
			switch (msg.what) {
			case Defined.USBPermission:
				fileLog.writeLog(TAG + " USBPermission");
				OPEN openDevice = new OPEN();
				new Thread(openDevice).start();
				break;
			case Defined.DEVICE_INI:
				fileLog.writeLog(TAG + " USBINI");
				pHandler.sendMessage(msg);
				break;
			default:
				Log.e(TAG, "Default event happen");
				break;
			}
		}
	};

	Handler uartHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.d(TAG, "UART handler start " + msg.what);
			switch (msg.what) {
			case Defined.UART_READY:
				Log.d(TAG, "Uart Ready");
				OPEN openDevice = new OPEN();
				new Thread(openDevice).start();
				break;
			}
		}
	};

	public int GetEnrolledCount(){
		int nCount = 0;
		ResponsePacket resPack = null;
		if(commType == Defined.COMM_TYPE_USB) {
			resPack = theUsb.SendCmd(Defined.CMD_GET_ENROLL_COUNT, 0);
		} else if(commType == Defined.COMM_TYPE_UART) {
			try {
				resPack = theUart.sendCommand(Defined.CMD_GET_ENROLL_COUNT, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d(TAG, String.format("UART: GetEnrolledCount exception occurred! msg : %s", e.getMessage()));
			}
		}
		if(resPack != null) {
			if(resPack.Response == Defined.ACK_OK) {
				nCount = resPack.Parameter;
			}
		}

		return nCount;
	}
}
