package com.gingytech.gtm.app.hybrid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.graphics.Bitmap;
import android.net.Uri;
import android.content.Intent;
import java.util.Date;
import java.text.SimpleDateFormat;

//import com.gingytech.gingyusb.*;
import com.gingytech.gtm.app.handprint.Protocol.Defined;
//import com.gingytech.gtm.app.hybrid.upload_image_server.ImageProcessClass;



public class TestMain extends Activity {

	String TAG = "TestMain";
	FingerBusiness fingerBusiness;

	String white = "@drawable/white";String red = "@drawable/red";String green = "@drawable/green";
	int deboucing_lock = 0;
	int imageResource = 0;
	Drawable image;
	String result = "";
	private SharedPreferences pref = null;
	private Button GetBIN, ModuleInfo, changeSC, Upload, Bserver;
	public TextView eText = null;
	String dreturn;
	ImageView imageView1;
	
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
	private Spinner commSpinner;
	private ArrayAdapter<String> commTypeAdapter;
	//private String[] safetylist = { "Low", "Medium", "High" };
	private String[] commList = { " ", " " };//USB UART
	private boolean isDeviceReady = false;
	private int commType = Defined.COMM_TYPE_USB;
	private int safetyLevel = 0;
	private String appVersion;
	upload_image_server UIS_test = new upload_image_server();	
	Bitmap image_test;
	String TEST_result;
	

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

		pref = getSharedPreferences(Defined.PREFERENCE, MODE_PRIVATE);
		safetyLevel = pref.getInt(Defined.PREFERENCE_SAFETY_LEVEL, -1);
		commType = pref.getInt(Defined.PREFERENCE_COMM_TYPE, -1);

		setContentView(R.layout.hybrid_main);
		Thread.setDefaultUncaughtExceptionHandler(new CrushMessage(this));

		//fingerinput = (Button) findViewById(R.id.identify);
		//Enroll = (Button) findViewById(R.id.enroll);
		//DeleteAll = (Button) findViewById(R.id.delete);
		Upload = (Button)findViewById(R.id.buttonupload);
		Bserver = (Button)findViewById(R.id.buttonserver);
		GetBIN = (Button)findViewById(R.id.getbin);
		eText = (TextView)findViewById(R.id.TextView08);
		//GetImage = (Button) findViewById(R.id.getImage);
		//GetTemplate = (Button)findViewById(R.id.getTemplate);
		//SetTemplate = (Button)findViewById(R.id.setTemplate);
		ModuleInfo = (Button)findViewById(R.id.buttonModuleInfo);		       		
		changeSC = (Button)findViewById(R.id.TM_lastpage);	
		imageView1 = (ImageView) findViewById( R.id.imageView1);
		
		changeSC.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(TestMain.this, Screen1.class);
			startActivity(intent);
			TestMain.this.finish();
			}
			});		
		
		Upload.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
	            intent.setAction(Intent.ACTION_GET_CONTENT);
	            startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);
			}			
		});
		
		Bserver.setOnClickListener(new Button.OnClickListener(){
			String TEST_name;			

			@Override
			public void onClick(View v) {
				Date date = new Date();
				SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmmss");
				TEST_name = dateForm.format(date);
				UIS_test.GetImageNameEditText = TEST_name; 						
				UIS_test.ImageUploadToServerFunction(image_test);
			}			
		});		
		
		
		//fingerinput.setOnClickListener(myfingerlistener);
		//Enroll.setOnClickListener(myfingerlistener);
		//DeleteAll.setOnClickListener(myfingerlistener);
		GetBIN.setOnClickListener(myfingerlistener);
		//GetImage.setOnClickListener(myfingerlistener);
		//GetTemplate.setOnClickListener(myfingerlistener);
		//SetTemplate.setOnClickListener(myfingerlistener);
		ModuleInfo.setOnClickListener(myfingerlistener);		
		

		//safetySpinner = (Spinner) findViewById(R.id.safetySpinner);
		//safetyAdapter = new ArrayAdapter<String>(this, R.layout.myspinner, safetylist);
		//safetyAdapter.setDropDownViewResource(R.layout.myspinner);
		//safetySpinner.setAdapter(safetyAdapter);


		commSpinner = (Spinner) findViewById(R.id.communicationSpinner);
		commTypeAdapter = new ArrayAdapter<String>(this, R.layout.myspinner, commList);
		commTypeAdapter.setDropDownViewResource(R.layout.myspinner);
		commSpinner.setAdapter(commTypeAdapter);
		if (commType != -1)
			commSpinner.setSelection(commType);

		commSpinner.post(new Runnable() {
			@Override
			public void run() {
				

				// TODO Auto-generated method stub
				commSpinner.setOnItemSelectedListener(new SpinnerSelectedListenerCommunicationType());
				

			}
		});

		setEnable(false);
		setModuleInfoEnable(false);
		setExitEnable(false);
		//setSafetySpinnerEnable(false);

		fingerImageSmall = (ImageView) findViewById(R.id.image_Fingersmall);
		//enrollImage = (ImageView) findViewById(R.id.image_Enroll);

		fingerImageSmall.setOnClickListener(ImageveiwListener);
		
		context = this;
		fileLog = new fileHelp(context);
		fileLog.getLogCat();
	}
	

    @Override
    protected void onActivityResult(int RC, int RQC, Intent I) {
        super.onActivityResult(RC, RQC, I);
        
        if (RC == 1 && RQC == RESULT_OK && I != null && I.getData() != null) {

           Uri uri = I.getData();

            try {
            	image_test = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                fingerImageSmall.setImageBitmap(image_test);
                                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	
	void setEnable(boolean b) {
		//ingerinput.setEnabled(b);
		//Enroll.setEnabled(b);
		//DeleteAll.setEnabled(b);
		GetBIN.setEnabled(b);
		//GetTemplate.setEnabled(b);
		//SetTemplate.setEnabled(b);
		//GetImage.setEnabled(b);
	}

	void setModuleInfoEnable(boolean b){
		ModuleInfo.setEnabled(b);
	}

	void setExitEnable(boolean b) {
		//Exit.setEnabled(b);
	}


	void setCommTypeSpinnerEnable(boolean b) {
		commSpinner.setEnabled(b);

		if (b) {
			commSpinner.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					commSpinner.setOnItemSelectedListener(new SpinnerSelectedListenerCommunicationType());
				}
			});
		}
	}
	
	void showMsg(CharSequence msg) {
		TextView view1 = (TextView) findViewById(R.id.textview_message);
		view1.setTextSize(16);
		view1.setText(msg);
		view1.setMovementMethod(ScrollingMovementMethod.getInstance());
	}

	private Button.OnClickListener ImageveiwListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			saveimage = 1;
		}
	};



	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		//fingerImageSmall.setImageBitmap(null);// clean finger image when pressed
												// button
		//enrollImage.setImageBitmap(null);// clean finger image when pressed
											// button
		fingerBusiness.exit = 1;
		super.onStop();
		fileLog.writeLog(TAG + " onStop");
		fileLog.getLogCat();
	}
	


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		super.onResume();
				
		result = Read_result(dreturn);	
		//eText.setText(result);
		
		
		imageResource = getResources().getIdentifier(white, null, getPackageName());
		
		if(result != "-1") {
			if(result.charAt(21)=='1') {
				imageResource = getResources().getIdentifier(red, null, getPackageName());	
			}
			else if(result.charAt(21)=='0') {
				imageResource = getResources().getIdentifier(green, null, getPackageName());	
			}
		}		
		image = getResources().getDrawable(imageResource);
		imageView1.setImageDrawable(image);	
		
		fileLog.writeLog(TAG + " onResume");
		fileLog.writeLog(TAG + " safetyLevel = " + Integer.toString(safetyLevel));
		fileLog.writeLog(TAG + " commType = " + Integer.toString(commType));

		if (safetyLevel == -1) {
			safetyLevel = 0;

			Editor editor = pref.edit();
			editor.putInt(Defined.PREFERENCE_SAFETY_LEVEL, safetyLevel);
			editor.commit();
		}

		if (commType == -1) {
			commType = Defined.COMM_TYPE_USB;

			Editor editor = pref.edit();
			editor.putInt(Defined.PREFERENCE_COMM_TYPE, commType);
			editor.commit();
		}

		fingerBusiness = new FingerBusiness(this, pHandler, commType);
	}
	

	Handler pHandler = new Handler() {
		
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			SimpleDateFormat DATE;
			int width = 0;
			int height = 0;

			super.handleMessage(msg);
			
			
			
			switch (msg.what) {
			case Defined.DEVICE_INI:
				fileLog.writeLog(TAG + " DEVICE_INI start");
				setEnable(true);
				setExitEnable(true);
			
				
				
				isDeviceReady = true;
				HashMap<String, String> versionMap = (HashMap<String, String>) msg.getData().getSerializable(Defined.VERSION_MAP);						
				String versionInfo=String.format("FirmwareVersion: %s\nIsoAreaMaxSize: %s\nDeviceSN: %s\nAppSN: %s", 
													versionMap.get(Defined.FIRMWARE_VERSION),
													versionMap.get(Defined.ISO_AREA_MAX_SIZE),
													versionMap.get(Defined.DEVICE_SN),
													appVersion);
				message = versionInfo;
				Result(0);
				fileLog.writeLog(TAG + " DEVICE_INI end");
				
				
				
				break;
			case Defined.GETIMAGE:
				fileLog.writeLog(TAG + " GETIMAGE start");
				byte[] picByte = msg.getData().getByteArray(Defined.BundlerDATA);
				width = fingerBusiness.moduleInfo.CaptureImageWidth;
				height = fingerBusiness.moduleInfo.CaptureImageHeight;

				if(picByte != null) {
					if(fingerBusiness.isModuleInfoSupport || fingerBusiness.isGTM5210C2) {
						// width and height were misplaced in ModuleInfo!
						// That should exchange width and height here.
						if(fingerBusiness.isModuleInfoSupport) {
							fingerImageSmall.setImageBitmap(getFromByte(picByte, height, width, false));
							image_test = getFromByte(picByte, height, width, false);
						} else {
							fingerImageSmall.setImageBitmap(getFromByte(picByte, width, height, false));
							image_test = getFromByte(picByte, width, height, false);
						}

					} else {
						fingerImageSmall.setImageBitmap(getFromByteC3(picByte));
						image_test = getFromByteC3(picByte);
					}
				}

				fileLog.writeLog(TAG + " GETIMAGE end");
				break;
			case Defined.GETIMAGERaw:
				fileLog.writeLog(TAG + " GETIMAGERaw start");
				byte[] picByteRaw = msg.getData().getByteArray(Defined.BundlerDATA);
				width = fingerBusiness.moduleInfo.RawImageWidth;
				height = fingerBusiness.moduleInfo.RawImageHeight;
				
                String date_return = null;
        		Date date = new Date();
            	SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmmss");
            	date_return = dateForm.format(date);
            	
				if(picByteRaw != null){
					if(fingerBusiness.isGTM5210C3) {
						fingerImageSmall.setImageBitmap(getFromByteC3Raw(picByteRaw));
						//doSavePicture(getFromByteC3Raw(picByteRaw));
					} else {
						fingerImageSmall.setImageBitmap(getFromByte(picByteRaw, width, height, false));
						image_test = getFromByte(picByteRaw, width, height, false);
						dreturn = Save_Image(width,height,160*120,picByteRaw);															
						//fileLog.writeLog(TAG + " /storage/emulated/0/IMAGE/"+ dreturn + ".bmp");									
						//////////////////////////////////////save server//////////////////////////////////////////////////
						File file = new File("/storage/emulated/0/IMAGE/"+ dreturn + ".bmp");
						deboucing_lock++;
						if(deboucing_lock == 2) {
							if (file.exists()) {
								BitmapFactory.Options option = new BitmapFactory.Options();
								option.inPreferredConfig = Bitmap.Config.ARGB_8888;
								image_test = BitmapFactory.decodeFile("/storage/emulated/0/IMAGE/"+ dreturn + ".bmp", option);					
								fileLog.writeLog(TAG + " Image_Server start");								
								fingerImageSmall.setImageBitmap(image_test);					           
								UIS_test.GetImageNameEditText = date_return; 
								UIS_test.ImageUploadToServerFunction(image_test);	    				
							}
						}
						fileLog.writeLog(TAG + " Image_Server finish");
					}					

				}
				

				
				imageResource = getResources().getIdentifier(white, null, getPackageName());
				image = getResources().getDrawable(imageResource);
				imageView1.setImageDrawable(image);	

				fileLog.writeLog(TAG + " GETIMAGERaw end");
				break;
			case Defined.SHOWIMAGE:
				fileLog.writeLog(TAG + " SHOWIMAGE start");
				
				fileLog.writeLog(TAG + " SHOWIMAGE end");
				break;
			case Defined.CLEANIMAGE:
				fileLog.writeLog(TAG + " CLEANIMAGE start");
				
				fileLog.writeLog(TAG + " CLEANIMAGE end");
				break;
			case Defined.ShowMessage:
				fileLog.writeLog(TAG + " ShowMessage start");
				Bundle bundle = msg.getData();
				String info = bundle.getString("info", "default");
				message = info;
				Result(0);

				fileLog.writeLog(TAG + " ShowMessage end");
				break;
			case Defined.VERIFYCORRECT:
				fileLog.writeLog(TAG + " VERIFYCORRECT start");
				message = "Identify PASS ID=" + msg.arg2 + ",cost=" + msg.arg1 + "ms";
				Result(1);
				errorcount = 0;
				setEnable(true);
				setExitEnable(true);
				setModuleInfoEnable(fingerBusiness.isModuleInfoSupport);
				fileLog.writeLog(TAG + " VERIFYCORRECT end");
				break;
			case Defined.VERIFYERROR:
				fileLog.writeLog(TAG + " VERIFYERROR start");
				errorcount++;
				message = "Identify NG, cost=" + msg.arg1 + "ms";
				Result(0);
				setEnable(true);
				setExitEnable(true);
				setModuleInfoEnable(fingerBusiness.isModuleInfoSupport);
				fileLog.writeLog(TAG + " VERIFYERROR end");
				break;
			case Defined.ENABLEBUTTON:
				fileLog.writeLog(TAG + " ENABLEBUTTON start");
				setEnable(true);
				setExitEnable(true);
				setModuleInfoEnable(fingerBusiness.isModuleInfoSupport);
				fileLog.writeLog(TAG + " ENABLEBUTTON end");
				break;
			default:
				Log.e(TAG, "default");
				break;
			}
		}

	};
	
	public String Read_result(String fileName) {
		BufferedReader br = null;
		String response = "-1";
		
		try {
			StringBuffer output = new StringBuffer();
			fileLog.writeLog(TAG + " Read_TXT open dir");
			String SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();
			String savePath = SDPATH + "/" + Defined.ImgPath + "/"+ fileName + ".txt"; 
			
			br = new BufferedReader(new FileReader(savePath));
	        String line = "";
			
	        while ((line = br.readLine()) != null) {
	            output.append(line +"\n");
	        }
	        response = output.toString();
	        br.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			//return null;
		} catch(IOException e) {
			e.printStackTrace();
			//return null;
		}
		return response;
		
	}
	
	public Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
	
	// Currently, this method is not used.
	public String Save_Image(int w, int l, int size, byte[] picByte) {
		
		String date_return = null;
		fileLog.writeLog(TAG + " Save_Image open dir");
		Date date = new Date();
    	String SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();    
        File dir =new File(SDPATH + "/" + Defined.ImgPath + "/"); 
        //////////////////////////////////////////////////////////////////////////////////      
        //////////////////////////////////////////////////////////////////////////////////
        
		if (!dir.exists())
			dir.mkdirs();

		try {
			/////////////////////////////////save loacl////////////////////////////////////
			fileLog.writeLog(TAG + " Save_Image set image");
			OutputStream fOut;
			SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmmss");
			date_return = dateForm.format(date);
			if (size == 160 * 160) {
				fOut = new FileOutputStream(SDPATH + "/" + Defined.ImgPath + "/" + date_return + ".bmp");
			} else {
				fOut = new FileOutputStream(SDPATH + "/" + Defined.ImgPath + "/" + date_return + ".bmp");
			}
			byte[] header = addBMPImageHeader(picByte.length);
			byte[] infos = addBMPImageInfosHeader(w, l, size);

			byte[] buffer = new byte[54 + 1024 + picByte.length];
			System.arraycopy(header, 0, buffer, 0, header.length);
			System.arraycopy(infos, 0, buffer, (0 + header.length), infos.length);
			System.arraycopy(picByte, 0, buffer, (0 + header.length + infos.length), picByte.length);

			fOut.write(buffer);
			fOut.flush();
			fOut.close();
			imagecount++;
			message = "save image ok=" + imagecount;
			Result(0);
			fileLog.writeLog(TAG + " Save_Image finish");

            ////////////////////////////////////////////////////////////////////////////////////////////////////	 	        
	        
			return date_return;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			message = "save image faillllll=" + errorcount;
			fileLog.writeLog(TAG + " Save_Image fail - Not Found File");
			Result(0);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			fileLog.writeLog(TAG + " Save_Image fail - IOException");
			message = "save image fail=" + errorcount;
			Result(0);
			e.printStackTrace();

		}
		return null;
		
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
				int c1=0xff000000;
				byte b = (byte) (buffer[index]);
				if(bInverse == false)
					c1 = c1 ^  b << 24;
				else c1 = c1 ^ ~b << 24;
				temp_map[index] = c1;
			}
		}

		Bitmap temp_pic = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		temp_pic.setPixels(temp_map, 0, width, 0, 0, width, height);

		return temp_pic;
	}

	// This method is for GetImage(0x62) of GTM5110C3/C5.
	// ImageSize: 202x258(52116bytes)
	// C3 image is compatible with C5.
	Bitmap getFromByteC3(byte[] buffer) {
		fileLog.writeLog(TAG + " getFromByteC6 start");

		// original
		// Bitmap pic = Bitmap.createBitmap(202, 258, Bitmap.Config.ARGB_8888);
		//
		// for(int i = 0;i < 258;i++) {
		// for(int j = 0;j < 202;j++) {
		// byte b = (byte)(buffer[(i * 202) + j]);
		// int c1 = 0xff000000;
		// c1 = c1 ^ b << 24;
		// pic.setPixel(j, i, c1);
		// }
		// }

		// scale to 256x256
		// rotate image
		int i, j;
		int[] rotateBuffer = new int[258 * 202];

		for (i = 0; i < 202; i++) {
			for (j = 0; j < 258; j++) {
				rotateBuffer[(i * 258) + j] = buffer[(j * 202) + i];
			}
		}

		// scale from 258x202 to 256x256
		int[] color = new int[256 * 256];
		Arrays.fill(color, 161);

		for (i = 0; i < 202; i++) {
			for (j = 0; j < 256; j++) {
				byte b = (byte) (rotateBuffer[(i * 258) + 1 + j]);
				int c1 = 0xff000000;
				c1 = c1 ^ b << 24;

				color[256 * (27 + i) + j] = c1;
			}
		}

		Bitmap pic = Bitmap.createBitmap(color, 256, 256, Bitmap.Config.ARGB_8888);

		fileLog.writeLog(TAG + " getFromByteC6 end");
		return pic;
	}

	// This method is for GetRawImage(0x63) of GTM5110C3/C5.
	// ImageSize: 160x120(19200bytes)
	// C3 image is compatible with C5.
	Bitmap getFromByteC3Raw(byte[] buffer) {
		fileLog.writeLog(TAG + " getFromByteRaw start");

		// original
		// Bitmap pic = Bitmap.createBitmap(160, 120, Bitmap.Config.ARGB_8888);
		// for (int i = 0; i < 120; i++) {
		// for (int j = 0; j < 160; j++) {
		// byte b = (byte) (buffer[(i * 160) + j]);
		// int c1=0xff000000;
		// c1 = c1 ^ b << 24;
		// pic.setPixel(j, i, c1);
		// }
		// }

		// Scale 160x120 to 320x240
		// This method is just map from 1 point of 160x120 to 4 points of
		// 320x240.
		int[] color = new int[320 * 240];
		Arrays.fill(color, 66);

		for (int i = 0; i < 120; i++) {
			for (int j = 0; j < 160; j++) {
				byte b = (byte) (buffer[(i * 160) + j]);
				int c1 = 0xff000000;
				c1 = c1 ^ b << 24;

				color[320 * (2 * i + 0) + (2 * j + 0)] = c1;
				color[320 * (2 * i + 0) + (2 * j + 1)] = c1;
				color[320 * (2 * i + 1) + (2 * j + 0)] = c1;
				color[320 * (2 * i + 1) + (2 * j + 1)] = c1;
			}
		}
		Bitmap pic = Bitmap.createBitmap(color, 320, 240, Bitmap.Config.ARGB_8888);

		fileLog.writeLog(TAG + " getFromByteRaw end");
		return pic;
	}

	// This method is for GetImage(0x62) and GetRawImage(0x63) of GTM5110C2.
	// ImageSize: 240x216(51840bytes)
	Bitmap getFromByteC2(byte[] buffer) {
		Bitmap pic = Bitmap.createBitmap(240, 216, Bitmap.Config.ARGB_8888);
		for (int i = 0; i < 216; i++) {
			for (int j = 0; j < 240; j++) {
				byte b = (byte) (buffer[(i * 240) + j]);
				int c1 = 0xff000000;
				c1 = c1 ^ b << 24;
				pic.setPixel(j, i, c1);
			}
		}

		return pic;
	}

	// Activity
	void Close() {
		fileLog.writeLog(TAG + " close");
		fingerBusiness.Close();
		finish();
	}

	void Result(int size) {
		TextView View1;
		View1 = (TextView) findViewById(R.id.textview_message);
		if (size == 0)
			View1.setTextSize(22);
		else
			View1.setTextSize(36);

		View1.setText(message);
		View1.setMovementMethod(ScrollingMovementMethod.getInstance());
	}

	private void setSafety(int safetyLevel) {
		if (isDeviceReady) {
			fingerBusiness.safety = (safetyLevel * 2) + 1;
			fingerBusiness.FBSafety();
		}
	}

	class SpinnerSelectedListenerSafety implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			int safetyLevel = arg2;

			// store safetyLevel into preference and set safety
			Toast.makeText(context, "spinner safetyLevel = " + Integer.toString(safetyLevel), Toast.LENGTH_LONG).show();
			Editor editor = pref.edit();
			editor.putInt(Defined.PREFERENCE_SAFETY_LEVEL, safetyLevel);
			editor.commit();

			setSafety(safetyLevel);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			fingerBusiness.safety = 1;
		}
	}

	class SpinnerSelectedListenerCommunicationType implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			int commType = position;

			// TODO Auto-generated method stub
			Editor editor = pref.edit();
			editor.putInt(Defined.PREFERENCE_COMM_TYPE, commType);
			editor.commit();

			if (commType == Defined.COMM_TYPE_USB) {
				fingerBusiness = new FingerBusiness(context, pHandler, Defined.COMM_TYPE_USB);
			} else if (commType == Defined.COMM_TYPE_UART) {
				fingerBusiness = new FingerBusiness(context, pHandler, Defined.COMM_TYPE_UART);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}

	}

	private Button.OnClickListener myfingerlistener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			fileLog.writeLog(TAG + " myfingerlistener");
			
			message = "";
			Result(0);
			setEnable(false);
			setExitEnable(false);
			setModuleInfoEnable(false);
			fingerImageSmall.setImageBitmap(null);// clean finger image when
			
			//enrollImage.setImageBitmap(null);// clean finger image when pressed
			
			switch (arg0.getId()) {
			//case R.id.enroll:
			//	fingerBusiness.Login();
			//	break;
			//case R.id.delete:
			//	fingerBusiness.DeletAll();
			//	break;
			//case R.id.identify:
			//	fingerBusiness.Identify();
			//	break;
			case R.id.getbin:
				fingerBusiness.BIN = 1;
				fingerBusiness.GetRawImage();	
				deboucing_lock = 0;
//////////////////////////////////////////////////////////////////////////////////////////////////
		
//////////////////////////////////////////////////////////////////////////////////////////////////				
				break;
			//case R.id.getImage:
			//	fingerBusiness.GetFingerImage();
			//	break;
			//case R.id.getTemplate:
			//	fingerBusiness.DoGetTemplateDB();
			//	break;
			//case R.id.setTemplate:
			//	fingerBusiness.DoSetTemplateDB();
			//	break;
			//case R.id.exit:
			//	if (fingerBusiness.BIN == 2)
			//		finish();
			//	else
			//		fingerBusiness.exit = 1;
			//	break;
			case R.id.buttonModuleInfo:
				if(fingerBusiness.isModuleInfoSupport){
					String strModuleInfo = "Sensor : " + fingerBusiness.moduleInfo.SensorVersion + "\n";
					strModuleInfo = strModuleInfo + "EngineVersion : " + fingerBusiness.moduleInfo.EngineVersion + "\n";
					strModuleInfo = strModuleInfo + "RawImage Area(Width x Height) : " + fingerBusiness.moduleInfo.RawImageWidth + " x " + fingerBusiness.moduleInfo.RawImageHeight + "\n";
					strModuleInfo = strModuleInfo + "CaptureImage Area(Width x Height) : " + fingerBusiness.moduleInfo.CaptureImageWidth + " x " + fingerBusiness.moduleInfo.CaptureImageHeight + "\n";
					strModuleInfo = strModuleInfo + "RecordCount(Max) : " + fingerBusiness.moduleInfo.MaxRecordCount + "\n";
					strModuleInfo = strModuleInfo + "Enroll Count : " + fingerBusiness.moduleInfo.EnrollCount + "\n";
					strModuleInfo = strModuleInfo + "Template Size : " + fingerBusiness.moduleInfo.TemplateSize + "(Btyes)\n";
					message = strModuleInfo;
					Result(0);
				}
				setEnable(true);
				setExitEnable(true);
				setModuleInfoEnable(fingerBusiness.isModuleInfoSupport);
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
		private Activity app = null;

		public CrushMessage(Activity app) {
			this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
			this.app = app;
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
				String pathway = fileLog.pathDate + "-crush.txt";
				FileWriter out = new FileWriter(pathway);
				out.write(report);
				out.close();
			} catch (IOException ioe) {
			}

			defaultUEH.uncaughtException(t, e);
		}
	}
	
	

    private void doSavePicture(Bitmap bm) {
        if (saveToPictureFolder(bm)) {
            
        } else {
            
        }
    }	
	
	
    private boolean saveToPictureFolder(Bitmap bm) {

    	String SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();    
        File picDir =new File(SDPATH + "/" + Defined.ImgPath + "/"); 
        Log.d(">>>", "Pictures Folder path: " + picDir.getAbsolutePath());

		if (!picDir.exists()) {
			picDir.mkdir();
		}        
        //假如有該目錄
        if (picDir.exists()) {
            //儲存圖片
            File pic = new File(picDir, "pic.bmp");      
            return saveBitmap(bm, pic);
        }
        return false;
    }
    
    private boolean saveBitmap(Bitmap bmp, File pic) {
        if (bmp == null || pic == null) return false;
        //
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(pic);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();

            scanGallery(this, pic);
            Log.d(">>>", "bmp path: " + pic.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e(">>>", "save bitmap failed!");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    private void scanGallery(Context ctx, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        ctx.sendBroadcast(mediaScanIntent);
    }	
    

    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    public class upload_image_server {
    	
    	   
        boolean check = true;
        EditText imageName;
        ProgressDialog progressDialog ;   
        String ImageName = "image_name" ;
        String ImagePath = "image_path" ;
        String Detection_result = "DETECTION_RESULT";
        String ServerUploadPath ="http://220.132.128.166:8060/upload_images/img_upload_to_server.php" ;
        //String ServerUploadPath ="http://192.168.8.133/upload_images/img_upload_to_server.php" ;
        public String GetImageNameEditText;
        
        Context context;
        
        public void ImageUploadToServerFunction(Bitmap bitmap){
     
        	
            ByteArrayOutputStream byteArrayOutputStreamObject ;

            byteArrayOutputStreamObject = new ByteArrayOutputStream();

            
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);

            byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();

            final String ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);

            class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

                @Override
                protected void onPreExecute() {

                    super.onPreExecute();

                    progressDialog = ProgressDialog.show(TestMain.this,"Image is Uploading","Please Wait",false,false);
                }

                

                @Override
                protected void onPostExecute(String string1) {

                    super.onPostExecute(string1);

                    // Dismiss the progress dialog after done uploading.
                    progressDialog.dismiss();
                    // Printing uploading success message coming from server on android app.
                    Toast.makeText(TestMain.this,string1,Toast.LENGTH_LONG).show();
                    fileLog.writeLog(string1);
            		imageResource = getResources().getIdentifier(white, null, getPackageName());
            		
            		
        			if(string1.equals("positive")) {
        				imageResource = getResources().getIdentifier(red, null, getPackageName());	
        			}
        			else if(string1.equals("negative")) {
        				imageResource = getResources().getIdentifier(green, null, getPackageName());	
        			}
            				
            		image = getResources().getDrawable(imageResource);
            		imageView1.setImageDrawable(image);	            		
                }
                
                
                @Override
                protected String doInBackground(Void... params) {


                    ImageProcessClass imageProcessClass = new ImageProcessClass();

                    HashMap<String,String> HashMapParams = new HashMap<String,String>();
                    fileLog.writeLog(GetImageNameEditText);
                    HashMapParams.put(ImageName, GetImageNameEditText);
                    HashMapParams.put(ImagePath, ConvertImage);                    
                    HashMapParams.put(Detection_result, "NON");
                    
                    String FinalData = imageProcessClass.ImageHttpRequest(ServerUploadPath, HashMapParams);                    
                    return FinalData;//
                }
            }
            AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();

            AsyncTaskUploadClassOBJ.execute();
        }
    	
    	   public class ImageProcessClass{

    	        public String ImageHttpRequest(String requestURL,HashMap<String, String> PData) {
    	        	fileLog.writeLog("start_HTTP");
    	            StringBuilder stringBuilder = new StringBuilder();
    	            try {
    	            	
    	                URL url;
    	                HttpURLConnection httpURLConnectionObject ;
    	                OutputStream OutPutStream;
    	                BufferedWriter bufferedWriterObject ;
    	                BufferedReader bufferedReaderObject ;
    	                int RC ;
    	                fileLog.writeLog("init_HTTP");
    	                url = new URL(requestURL);

    	                httpURLConnectionObject = (HttpURLConnection) url.openConnection();

    	                httpURLConnectionObject.setReadTimeout(19000);

    	                httpURLConnectionObject.setConnectTimeout(19000);

    	                httpURLConnectionObject.setRequestMethod("POST");

    	                httpURLConnectionObject.setDoInput(true);

    	                httpURLConnectionObject.setDoOutput(true);

    	                OutPutStream = httpURLConnectionObject.getOutputStream();
    	                fileLog.writeLog("setting_HTTP");
    	                bufferedWriterObject = new BufferedWriter(

    	                        new OutputStreamWriter(OutPutStream, "UTF-8"));

    	                bufferedWriterObject.write(bufferedWriterDataFN(PData));

    	                bufferedWriterObject.flush();

    	                bufferedWriterObject.close();

    	                OutPutStream.close();

    	                RC = httpURLConnectionObject.getResponseCode();
    	                fileLog.writeLog("start_HTTP");

    	                if (RC == HttpURLConnection.HTTP_OK) {
    	                		
    	                    bufferedReaderObject = new BufferedReader(new InputStreamReader(httpURLConnectionObject.getInputStream()));

    	                    stringBuilder = new StringBuilder();

    	                    String RC2;

    	                    while ((RC2 = bufferedReaderObject.readLine()) != null){
    	                    	
    	                        stringBuilder.append(RC2);
    	                    }
    	                    fileLog.writeLog(RC2);
    	                }

    	            } catch (Exception e) {
    	                e.printStackTrace();
    	            }
    	            
    	            deboucing_lock = 0;
    	            return stringBuilder.toString();
    	        }

    	        private String bufferedWriterDataFN(HashMap<String, String> HashMapParams) throws UnsupportedEncodingException {

    	            StringBuilder stringBuilderObject;

    	            stringBuilderObject = new StringBuilder();

    	            for (Map.Entry<String, String> KEY : HashMapParams.entrySet()) {

    	                if (check)

    	                    check = false;
    	                else
    	                    stringBuilderObject.append("&");

    	                stringBuilderObject.append(URLEncoder.encode(KEY.getKey(), "UTF-8"));

    	                stringBuilderObject.append("=");

    	                stringBuilderObject.append(URLEncoder.encode(KEY.getValue(), "UTF-8"));
    	            }

    	            return stringBuilderObject.toString();
    	        }

    	    }
    }   
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    
    
    
}
