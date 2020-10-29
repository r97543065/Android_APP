package com.gingytech.gtm.app.hybrid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
////////////////////////////////////////////////////////////////
import android.net.Uri;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//////////////////////////////////////////////////////////////
import com.gingytech.gingyusb.FingerBusiness;
import com.gingytech.gingyusb.fileHelp;
import com.gingytech.gtm.app.handprint.Protocol.Defined;



public class TestMain extends Activity {
	short up_low_exp = 0;
	short limit_condition = 0;
	short positive_det = 0;
	short rexposure = 0;
	short rexp = 0;
	short under_exp = 0;
	short over_exp = 0;
	short over_exp_value = 0;
	short oops = 0;
	short reset_exp = 0;
	int corss_value = 0;
	int corss_value_1 = 0;
	short up_exposure = 0;
	short up_exposure_1 = 0;
	short low_exposure = 0;
	short low_exposure_1 = 0;
	short special_exp = 0;
	short normal_condition = 0;
	
	String TAG = "TestMain";
	FingerBusiness fingerBusiness;

	String white = "@drawable/white";String red = "@drawable/red";String green = "@drawable/green";
	int deboucing_lock = 0;
	int imageResource = 0;
	Drawable image;
	String result = "";
	private Button GetBIN, ModuleInfo, changeSC, Upload, Bserver, SetEXP, SaveFLASH, Resetexp;
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
	private String[] commList = { " ", " " };//USB UART
	private int commType = Defined.COMM_TYPE_USB;
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

		context = this;
		fileLog = new fileHelp(context, "Finger");
		fileLog.getLogCat();
		
				
		setContentView(R.layout.hybrid_main);
		Thread.setDefaultUncaughtExceptionHandler(new CrushMessage(this));

		Upload = (Button)findViewById(R.id.buttonupload);
		Bserver = (Button)findViewById(R.id.buttonserver);
		GetBIN = (Button)findViewById(R.id.getbin);
		eText = (TextView)findViewById(R.id.TextView08);
		SetEXP = (Button)findViewById(R.id.setInfo);
		SaveFLASH = (Button)findViewById(R.id.saveFlash);
		ModuleInfo = (Button)findViewById(R.id.buttonModuleInfo);		       		
		changeSC = (Button)findViewById(R.id.TM_lastpage);	
		imageView1 = (ImageView)findViewById(R.id.imageView1);
		Resetexp = (Button)findViewById(R.id.ResetEXP);
		
		imageResource = getResources().getIdentifier(white, null, getPackageName());
		image = getResources().getDrawable(imageResource);
		imageView1.setImageDrawable(image);	         
			
		
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
		
		GetBIN.setOnClickListener(myfingerlistener);
		ModuleInfo.setOnClickListener(myfingerlistener);
		SetEXP.setOnClickListener(myfingerlistener);
		SaveFLASH.setOnClickListener(myfingerlistener);
		Resetexp.setOnClickListener(myfingerlistener);			
		
		setEnable(false);		
		setExitEnable(false);		

		fingerImageSmall = (ImageView) findViewById(R.id.image_Fingersmall);
		fingerImageSmall.setOnClickListener(ImageveiwListener);		
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

		GetBIN.setEnabled(b);

	}

	void setExitEnable(boolean b) {
		//Exit.setEnabled(b);
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
			SimpleDateFormat DATE;
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
				byte MA = 0;
				
                String date_return = null;
        		Date date = new Date();
            	SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmmss");
            	date_return = dateForm.format(date);
										
	
				if(picByteRaw != null){	
					String str;
					corss_value = 0;
					corss_value_1 = 0;
					for (int j = 0; j < width; j++) {
						int index = (height/2) * width + j;		
						if((picByteRaw[index] & 0xFF) > 0x37) {
							corss_value += 1;
						}
						if((picByteRaw[index] & 0xFF) > 0x07) {
							corss_value_1 += 1;
						}
					}
					
					
					if(corss_value_1 <= 5 && limit_condition == 0) {						
						Resetexp.performClick();	
						oops = 1;
					}
					
					if(corss_value > 190 && corss_value <= 250 && normal_condition == 0 && limit_condition == 0 && up_low_exp == 0) {
						normal_condition = 1;							
					}
					else if(normal_condition == 1){
						rexposure = 1;
					}					
					else if(limit_condition > 0 || rexp > 2) {
						rexposure = 1;						
					}
					
					str = String.valueOf(corss_value);
				    fileLog.writeLog(TAG + str);
				
					if( corss_value_1 > 5 && corss_value_1 < 290 ) {
																
						if(rexposure == 0) {
							rexp++;
							SetEXP.performClick();						
						}				
						else if(rexposure == 1)	{	
							rexposure = 0;
							rexp = 0;
														
							fingerImageSmall.setImageBitmap(getFromByte(picByteRaw, width, height, false));
							image_test = getFromByte(picByteRaw, width, height, false);
							dreturn = Save_Image(width,height,width*height,picByteRaw);												
							//fileLog.writeLog(TAG + " /storage/emulated/0/IMAGE/"+ dreturn + ".bmp");
							
							//////////////////////////////////////save server//////////////////////////////////////////////////
							File file = new File("/storage/emulated/0/IMAGE/"+ dreturn + String.valueOf(limit_condition)  +".bmp");
							
							deboucing_lock++;
								if (file.exists()) {
									BitmapFactory.Options option = new BitmapFactory.Options();
									option.inPreferredConfig = Bitmap.Config.ARGB_8888;
									image_test = BitmapFactory.decodeFile("/storage/emulated/0/IMAGE/"+ dreturn + String.valueOf(limit_condition)  + ".bmp", option);					
									fileLog.writeLog(TAG + " Image_Server start");								
									fingerImageSmall.setImageBitmap(image_test);					           
									UIS_test.GetImageNameEditText = date_return; 
									UIS_test.ImageUploadToServerFunction(image_test);	    				
								}							
							fileLog.writeLog(TAG + " Image_Server finish");																					
							Resetexp.performClick();							
						}
				    }
					else {
						
						imageResource = getResources().getIdentifier(white, null, getPackageName());
						image = getResources().getDrawable(imageResource);
						imageView1.setImageDrawable(image);		
						fileLog.writeLog(TAG + " GETIMAGERaw end");
						
					}
					
				}				
				
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
				fOut = new FileOutputStream(SDPATH + "/" + Defined.ImgPath + "/" + date_return + String.valueOf(limit_condition) +".bmp");
			} 
			else {
				fOut = new FileOutputStream(SDPATH + "/" + Defined.ImgPath + "/" + date_return + String.valueOf(limit_condition)  + ".bmp");
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
			showMsg(message);
			fileLog.writeLog(TAG + " Save_Image finish");

            ////////////////////////////////////////////////////////////////////////////////////////////////////	 	        
	        
			return date_return;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			message = "save image faillllll=" + errorcount;
			fileLog.writeLog(TAG + " Save_Image fail - Not Found File");
			showMsg(message);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			fileLog.writeLog(TAG + " Save_Image fail - IOException");
			message = "save image fail=" + errorcount;
			showMsg(message);
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
						
			switch (arg0.getId()) {

			case R.id.getbin:
				fingerImageSmall.setImageBitmap(null);// clean finger image when
				fingerBusiness.BIN = 1;
				fingerBusiness.GetRawImage();	
				deboucing_lock = 0;				
//////////////////////////////////////////////////////////////////////////////////////////////////
				//
//////////////////////////////////////////////////////////////////////////////////////////////////				
				break;
			case R.id.setInfo://set exposure/gain value to sensor
				fingerImageSmall.setImageBitmap(null);// clean finger image when
				short new_exposure = 0;//Maximum 2048
				short new_gain = 10;//Maximum 255

				if(limit_condition == 0 && normal_condition == 1) {
					new_exposure = 115;
					low_exposure = 85;
					up_exposure = 75;
					special_exp = 0;
					up_low_exp = 1;					
				}									
				else if(limit_condition == 0 && normal_condition == 0) {
					double Amp = (7*(1/(1-Math.exp(-(double)(corss_value+5)/36.6))));
					new_exposure = (short)( (Amp/2.2) * (40 - Math.exp((double)(corss_value+5)/90)) + 50);
					if(new_exposure >= 205) {
						new_exposure = 205;
						low_exposure = 190;
						up_exposure = 210;
						special_exp = 0;
						up_low_exp = 1;
					}
					else if(new_exposure > 165 && new_exposure <= 185) {
						new_exposure = 175;
						low_exposure = 155;
						up_exposure = 180;
						special_exp = 0;
						up_low_exp = 1;
					}
					else if(new_exposure > 145 && new_exposure <= 165) {
						new_exposure = 135;
						low_exposure = 115;
						low_exposure_1 = 125;
						up_exposure_1 = 145;
						up_exposure = 160;
						//up_low_exp = 1;
						special_exp = 1;
					}
					else if(new_exposure > 125 && new_exposure <= 145) {
						new_exposure = 130;
						low_exposure = 125;
						up_exposure = 135;
						special_exp = 0;
						up_low_exp = 1;
					}
					else if(new_exposure > 100 && new_exposure <= 125) {
						new_exposure = 105;
						low_exposure = 95;
						up_exposure = 115;
						special_exp = 0;
						up_low_exp = 1;
					}
					
				}
				else if(limit_condition == 1){
					new_exposure = low_exposure;					
				}
				else if(limit_condition == 2) {
					new_exposure = up_exposure;	
					normal_condition = 0;
				}
				else if(limit_condition == 3) {
					new_exposure = low_exposure_1;						
				}
				else if(limit_condition == 4) {
					new_exposure = up_exposure_1;						
				}
				
				new_exposure = (short)(new_exposure);							
				boolean bRet = fingerBusiness.SetExposure(new_exposure);
				bRet = fingerBusiness.SetGain(new_gain);								
				String str;
				
				
				if (bRet) {
					//showMsg("Set success.");
					str = String.valueOf(new_exposure);
					fileLog.writeLog(TAG+ "EXP" + str);
					fileLog.writeLog(TAG + " Set success.");
				}else {
					//showMsg("Set failed.");
					fileLog.writeLog(TAG + " Set failed.");
				}

				setEnable(true);
				setExitEnable(true);								
				GetBIN.performClick();				
				
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
			case R.id.ResetEXP:
				new_exposure = 115;//Maximum 2048
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
				if(oops == 1) {
					oops = 0;
					GetBIN.performClick();	
				}
				break;

			case R.id.buttonModuleInfo:
				fingerImageSmall.setImageBitmap(null);// clean finger image when
				if(fingerBusiness.isModuleInfoSupport){
					String strModuleInfo = "Sensor : " + fingerBusiness.moduleInfo.SensorVersion + "\n";
					strModuleInfo = strModuleInfo + "EngineVersion : " + fingerBusiness.moduleInfo.EngineVersion + "\n";
					strModuleInfo = strModuleInfo + "RawImage Area(Width x Height) : " + fingerBusiness.moduleInfo.RawImageWidth + " x " + fingerBusiness.moduleInfo.RawImageHeight + "\n";
					strModuleInfo = strModuleInfo + "CaptureImage Area(Width x Height) : " + fingerBusiness.moduleInfo.CaptureImageWidth + " x " + fingerBusiness.moduleInfo.CaptureImageHeight + "\n";
					strModuleInfo = strModuleInfo + "RecordCount(Max) : " + fingerBusiness.moduleInfo.MaxRecordCount + "\n";
					strModuleInfo = strModuleInfo + "Enroll Count : " + fingerBusiness.moduleInfo.EnrollCount + "\n";
					strModuleInfo = strModuleInfo + "Template Size : " + fingerBusiness.moduleInfo.TemplateSize + "(Btyes)\n";
					message = strModuleInfo;
					showMsg(message);
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
				String pathway = fileLog.getPathDate() + "-crush.txt";
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
                    String str;                 
                    // Dismiss the progress dialog after done uploading.
                    progressDialog.dismiss();
                    // Printing uploading success message coming from server on android app.
                    //Toast.makeText(TestMain.this,string1,Toast.LENGTH_LONG).show();
                    
                    fileLog.writeLog(string1);
            		imageResource = getResources().getIdentifier(white, null, getPackageName());
            		
				    if(limit_condition == 0) {
	        			if(string1.equals("positive")) {
	        				imageResource = getResources().getIdentifier(red, null, getPackageName());	
	        			}
	        			else if(string1.equals("negative")) {
	        				imageResource = getResources().getIdentifier(green, null, getPackageName());	
	        			}
						str = String.valueOf(limit_condition);
					    fileLog.writeLog(TAG + "limit" + str);	
					    if(up_low_exp == 1 || special_exp == 1) {
					    	limit_condition = 1;
					    	SetEXP.performClick();			               
					    	up_low_exp = 0;
					    	
				    	    if(string1.equals("positive")) {
	            				positive_det++; 
	            			}			    	    				    	    
					    }					   
					    
				    }            		
				    else if(limit_condition == 1) {
            			if(string1.equals("positive")) {
            				positive_det++; 
            			}            		
				    	str = String.valueOf(limit_condition);
    				    fileLog.writeLog(TAG + "limit" + str);
				    	limit_condition = 2;	                    						    				    
    	                SetEXP.performClick();		
                                		
            		}                       	
            		else if(limit_condition == 2) {
            			if(string1.equals("positive")) {
            				positive_det++; 
            			}  
            			
            			if(special_exp==0) {
		            			if(positive_det > 0) {
		            				imageResource = getResources().getIdentifier(red, null, getPackageName());
		            				positive_det = 0;
		            			} 
		            			else {
		            				imageResource = getResources().getIdentifier(green, null, getPackageName());
		            			}    					            			
		            			str = String.valueOf(limit_condition);
		    				    fileLog.writeLog(TAG + "limit" + str);			    				    
		            			limit_condition = 0;		            			
		            			
					    	    //wait for 2 second
					    	    try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}	
            			}
            			else if(special_exp==1) {
            				special_exp = 0;
    				    	str = String.valueOf(limit_condition);
        				    fileLog.writeLog(TAG + "limit" + str);
    				    	limit_condition = 3;	                    						    				    
        	                SetEXP.performClick();
            				
            			}
            		}
            		else if(limit_condition == 3) {
            			if(string1.equals("positive")) {
            				positive_det++; 
            			}            		
				    	str = String.valueOf(limit_condition);
    				    fileLog.writeLog(TAG + "limit" + str);
				    	limit_condition = 4;	                    						    				    
    	                SetEXP.performClick();	            			
            			
            		}
            		else if(limit_condition == 4) {            		
            			if(string1.equals("positive")) {
            				positive_det++; 
            			}  
            			
            			if(positive_det > 0) {
            				imageResource = getResources().getIdentifier(red, null, getPackageName());
            				positive_det = 0;
            			} 
            			else {
            				imageResource = getResources().getIdentifier(green, null, getPackageName());
            			}    					            			
            			str = String.valueOf(limit_condition);
    				    fileLog.writeLog(TAG + "limit" + str);	
    				    
            			limit_condition = 0;
            			
            			
			    	    //wait for 2 second
			    	    try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
            			
            			
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
