
hybrid_gtm
├── AndroidManifest.xml
├── Android.mk
├── ic_launcher-web.png
├── libs
│   ├── android-support-v4.jar
│   └── gingyusb.jar------------------------------ Library for accessing Sensor via USB
├── res
└── src
    └── com
	└── gingytech
	    └── gtm
	        └── app
	            └── hybrid
	                ├── Defined.java
	                └── TestMain.java--------- Example code

[gingyusb.jar]
import com.gingytech.gingyusb.*;

[TestMain.java]
*按鈕觸發函式 - Sensor相關功能使用方式可參考此函式
private Button.OnClickListener myfingerlistener = new Button.OnClickListener()

*後處理函式 - 顯示韌體資訊/影像顯示存檔
Handler pHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) 
