package com.gingytech.gtm.app.hybrid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gingytech.gingyusb.*;

public class TestMain extends Activity {

	String TAG = "TestMain";
	FingerBusiness fingerBusiness;
	private Button GetBIN, Exit,GetInfo,SetInfo,SaveFlash,ResetInfo;
	ImageView Imageshow;
	Context context;
	fileHelp fileLog;
	String myJpgPath;
	CharSequence message;
	short errorcount = 0;
	short size = 0;
	short saveimage = 0;
	short imagecount = 0;
	public ImageView fingerImageSmall;
	private String appVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get App Version Name
		PackageManager packageManager = this.getPackageManager();
		try {
			PackageInfo info = packageManager.getPackageInfo(this.getPackageName(), 0);
			appVersion = info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			appVersion = "unknown";
		}

		context = this;
		fileLog = new fileHelp(context, "Finger");
		fileLog.getLogCat();

		setContentView(R.layout.hybrid_main);
		Thread.setDefaultUncaughtExceptionHandler(new CrushMessage(this));

		GetBIN = (Button) findViewById(R.id.getbin);
		Exit = (Button) findViewById(R.id.exit);
		GetInfo = (Button) findViewById(R.id.getInfo);
		SetInfo = (Button) findViewById(R.id.setInfo);
		SaveFlash = (Button) findViewById(R.id.saveFlash);
		ResetInfo = (Button) findViewById(R.id.resetInfo);

		GetBIN.setOnClickListener(myfingerlistener);
		Exit.setOnClickListener(myfingerlistener);
		GetInfo.setOnClickListener(myfingerlistener);
		SetInfo.setOnClickListener(myfingerlistener);
		SaveFlash.setOnClickListener(myfingerlistener);
		ResetInfo.setOnClickListener(myfingerlistener);


		setEnable(false);
		setExitEnable(false);

		fingerImageSmall = (ImageView) findViewById(R.id.image_Fingersmall);

	}

	void setEnable(boolean b) {
		GetBIN.setEnabled(b);
		GetInfo.setEnabled(b);
		SetInfo.setEnabled(b);
		SaveFlash.setEnabled(b);
		ResetInfo.setEnabled(b);
	}

	void setExitEnable(boolean b) {
		Exit.setEnabled(b);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		fingerImageSmall.setImageBitmap(null);// clean finger image when pressed
		fingerBusiness.exit = 1;
		super.onStop();
		fileLog.writeLog(TAG + " onStop");
		fileLog.getLogCat();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		fileLog.writeLog(TAG + " onResume");
		//Start to initial
		fingerBusiness = new FingerBusiness(this, pHandler);
		fingerBusiness.SetLogPath("Finger");
		if (fingerBusiness.Open() == false) {
			fileLog.writeLog(TAG + " fingerBusiness open() failed.");
		}
	}

	Handler pHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			int width = 0;
			int height = 0;

			super.handleMessage(msg);
			switch (msg.what) {
			case Defined.DEVICE_INI:
				fileLog.writeLog(TAG + " DEVICE_INI start");
				setEnable(true);
				setExitEnable(true);

				@SuppressWarnings("unchecked")
				HashMap<String, String> versionMap = (HashMap<String, String>) msg.getData()
						.getSerializable(Defined.VERSION_MAP);

				String versionInfo = String.format("FirmwareVersion: %s\nDate: %s\nAPP: %s\nUSB_Lib: %s\n",
						versionMap.get(Defined.FIRMWARE_VERSION), versionMap.get(Defined.DEVICE_SN), appVersion,
						fingerBusiness.GetVersion());

				message = versionInfo;
				showMsg(message);

				fileLog.writeLog(TAG + " DEVICE_INI end");
				break;
			case Defined.GETIMAGERaw:
				fileLog.writeLog(TAG + " GETIMAGERaw start");
				byte[] picByteRaw = msg.getData().getByteArray(Defined.BundlerDATA);
				width = fingerBusiness.moduleInfo.RawImageWidth;
				height = fingerBusiness.moduleInfo.RawImageHeight;

				if (picByteRaw != null) {
					//display image
					fingerImageSmall.setImageBitmap(getFromByte(picByteRaw, width, height, false));
					//save image to bitmap file
					saveBmp(picByteRaw, width, height);
				}

				fileLog.writeLog(TAG + " GETIMAGERaw end");
				break;
			case Defined.ShowMessage:
				fileLog.writeLog(TAG + " ShowMessage start");
				Bundle bundle = msg.getData();
				String info = bundle.getString("info", "default");
				message = info;
				showMsg(message);

				fileLog.writeLog(TAG + " ShowMessage end");
				break;
			case Defined.ENABLEBUTTON:
				fileLog.writeLog(TAG + " ENABLEBUTTON start");
				setEnable(true);
				setExitEnable(true);
				fileLog.writeLog(TAG + " ENABLEBUTTON end");
				break;
			default:
				Log.e(TAG, "default");
				break;
			}
		}

	};

	private void saveBmp(byte[] rawImg, int width, int height) {
		String rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
		String mainDir = "GTM";
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String todayDir = sdf.format(dt);

		File dir = new File(String.format("%s/%s/%s", rootDir, mainDir, todayDir));
		if (!dir.exists()) {
			dir.mkdirs();
		}

		int currentFileCount = dir.list().length;
		// Log.d(TAG, String.format("currentFileCount: %d", (currentFileCount + 1)));
		String filePath = String.format("%s/%05d.bmp", dir.getPath(), (currentFileCount + 1));

		try {
			FileOutputStream fos = new FileOutputStream(filePath);

			byte[] header = addBMPImageHeader(rawImg.length);
			byte[] info = addBMPImageInfosHeader(width, height, (width * height));
			byte[] buffer = new byte[14 + 40 + 1024 + rawImg.length];

			System.arraycopy(header, 0, buffer, 0, header.length);
			System.arraycopy(info, 0, buffer, (0 + header.length), info.length);
			System.arraycopy(rawImg, 0, buffer, (0 + header.length + info.length), rawImg.length);

			fos.write(buffer);
			fos.flush();
			fos.close();

			String msg = String.format("Save %s ok.", filePath);
			sendTextMsg(msg);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// BMP Header
	private byte[] addBMPImageHeader(int size) {
		byte[] buffer = new byte[14];
		buffer[0] = 0x42;
		buffer[1] = 0x4D;
		buffer[2] = (byte) (size >> 0);
		buffer[3] = (byte) (size >> 8);
		buffer[4] = (byte) (size >> 16);
		buffer[5] = (byte) (size >> 24);
		buffer[6] = 0x00;
		buffer[7] = 0x00;
		buffer[8] = 0x00;
		buffer[9] = 0x00;
		buffer[10] = 0x36; // size of addBMPImageInfosHeader
		buffer[11] = 0x04;
		buffer[12] = 0x00;
		buffer[13] = 0x00;
		return buffer;
	}

	private byte[] addBMPImageInfosHeader(int w, int h, int size) {
		byte[] buffer = new byte[40 + 1024];
		buffer[0] = 0x28;
		buffer[1] = 0x00;
		buffer[2] = 0x00;
		buffer[3] = 0x00;
		buffer[4] = (byte) (w >> 0);
		buffer[5] = (byte) (w >> 8);
		buffer[6] = (byte) (w >> 16);
		buffer[7] = (byte) (w >> 24);
		buffer[8] = (byte) (h >> 0);
		buffer[9] = (byte) (h >> 8);
		buffer[10] = (byte) (h >> 16);
		buffer[11] = (byte) (h >> 24);
		buffer[12] = 0x01; // plane 2
		buffer[13] = 0x00;
		buffer[14] = 0x08; // Bits per pixel 2
		buffer[15] = 0x00;
		buffer[16] = 0x00; // compression 4
		buffer[17] = 0x00;
		buffer[18] = 0x00;
		buffer[19] = 0x00;
		buffer[20] = (byte) (size >> 0); // bitmap data size 4
		buffer[21] = (byte) (size >> 8);
		buffer[22] = (byte) (size >> 16);
		buffer[23] = (byte) (size >> 24);
		buffer[24] = 0x00; // H resolution
		buffer[25] = 0x00;
		buffer[26] = 0x00;
		buffer[27] = 0x00;
		buffer[28] = 0x00; // V resolution
		buffer[29] = 0x00;
		buffer[30] = 0x00;
		buffer[31] = 0x00;
		buffer[32] = 0x00; // used color 4
		buffer[33] = 0x00;
		buffer[34] = 0x00;
		buffer[35] = 0x00;
		buffer[36] = 0x00; // important color 4
		buffer[37] = 0x00;
		buffer[38] = 0x00;
		buffer[39] = 0x00;
		for (int i = 0; i < 256; i++) {
			buffer[i * 4 + 40] = (byte) i;
			buffer[i * 4 + 41] = (byte) i;
			buffer[i * 4 + 42] = (byte) i;
			buffer[i * 4 + 43] = 0;
		}
		return buffer;
	}

	// General usage to convert raw to bitmap.
	Bitmap getFromByte(byte[] buffer, int width, int height, boolean bInverse) {
		int[] temp_map = new int[width * height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int index = i * width + j;
				int c1 = 0xff000000;
				byte b = (byte) (buffer[index]);
				if (bInverse == false)
					c1 = c1 ^ b << 24;
				else
					c1 = c1 ^ ~b << 24;
				temp_map[index] = c1;
			}
		}

		Bitmap temp_pic = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		temp_pic.setPixels(temp_map, 0, width, 0, 0, width, height);

		return temp_pic;
	}

	// Activity
	void Close() {
		fileLog.writeLog(TAG + " close");
		fingerBusiness.Close();
		finish();
	}

	void sendTextMsg(CharSequence textMsg) {
		Message msg = new Message();
		msg.what = Defined.ShowMessage;
		Bundle bundle = new Bundle();
		bundle.putString("info", (String) textMsg);
		msg.setData(bundle);
		pHandler.sendMessage(msg);
	}

	void showMsg(CharSequence msg) {
		TextView view1 = (TextView) findViewById(R.id.textview_message);
		view1.setTextSize(16);
		view1.setText(msg);
		view1.setMovementMethod(ScrollingMovementMethod.getInstance());
	}

	private Button.OnClickListener myfingerlistener = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			fileLog.writeLog(TAG + " myfingerlistener");

			message = "";
			showMsg(message);
			setEnable(false);
			setExitEnable(false);
			fingerImageSmall.setImageBitmap(null);// clean image 

			switch (arg0.getId()) {
			case R.id.getbin://Get image from sensor and display
				fingerBusiness.BIN = 1;
				fingerBusiness.GetRawImage();
				break;
			case R.id.exit:
				if (fingerBusiness.BIN == 2)
					Close();
				else
					fingerBusiness.exit = 1;
				break;
			case R.id.getInfo://get exposure/gain value from sensor
				short exposure = fingerBusiness.GetExposure();
				short gain = fingerBusiness.GetGain();
				showMsg("Exposure:"+exposure+" Gain:"+gain);
				setEnable(true);
				setExitEnable(true);
				break;
			case R.id.setInfo://set exposure/gain value to sensor
				short new_exposure = 300;//Maximum 2048
				short new_gain = 20;//Maximum 255
				boolean bRet = fingerBusiness.SetExposure(new_exposure);
				bRet = fingerBusiness.SetGain(new_gain);
				if (bRet) {
					showMsg("Set success.");
				}else {
					showMsg("Set failed.");
				}
				setEnable(true);
				setExitEnable(true);
				break;
			case R.id.saveFlash://save exposure/gain value to flash
				bRet = fingerBusiness.SaveToFlash();
				if (bRet) {
					showMsg("Write to Flash success.");
				}else {
					showMsg("Write to Flash failed.");
				}
				setEnable(true);
				setExitEnable(true);
				break;
			case R.id.resetInfo://reset to default exposure/gain value
				new_exposure = 200;//Maximum 2048
				new_gain = 10;//Maximum 255
				bRet = fingerBusiness.SetExposure(new_exposure);
				bRet = fingerBusiness.SetGain(new_gain);
				if (bRet) {
					showMsg("Reset success.");
				}else {
					showMsg("Reset failed.");
				}
				setEnable(true);
				setExitEnable(true);
				break;
			default:
				break;
			}
		}
	};

	// for test
	public void stackOverflow() {
		this.stackOverflow();
	}

	// output the crush message
	public class CrushMessage implements Thread.UncaughtExceptionHandler {

		private Thread.UncaughtExceptionHandler defaultUEH;

		public CrushMessage(Activity app) {
			this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		}

		public void uncaughtException(Thread t, Throwable e) {
			StackTraceElement[] arr = e.getStackTrace();
			String report = e.toString() + "\n\n";
			report += "Message Start\n\n";
			for (int i = 0; i < arr.length; i++) {
				report += "    " + arr[i].toString() + "\n";
			}
			report += "\nMessage End\n";

			// If the exception was thrown in a background thread inside
			// AsyncTask, then the actual exception can be found with getCause
			report += "Cause By\n\n";
			Throwable cause = e.getCause();
			if (cause != null) {
				report += cause.toString() + "\n\n";
				arr = cause.getStackTrace();
				for (int i = 0; i < arr.length; i++) {
					report += "    " + arr[i].toString() + "\n";
				}
			}
			report += "\nEnd\n";

			try {
				String pathway = fileLog.getPathDate() + "-crush.txt";
				FileWriter out = new FileWriter(pathway);
				out.write(report);
				out.close();
			} catch (IOException ioe) {
			}

			defaultUEH.uncaughtException(t, e);
		}
	}
}
