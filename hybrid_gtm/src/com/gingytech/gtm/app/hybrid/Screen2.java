package com.gingytech.gtm.app.hybrid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class Screen2 extends Activity {

	private Button Data, Scan, Exit;

  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hybrid_screen2);
		Scan = (Button)findViewById(R.id.Scan_setting);
		Data = (Button)findViewById(R.id.Data_setting);
		Exit = (Button)findViewById(R.id.Main_Exit);
		
		Scan.setOnClickListener(new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(Screen2.this, Screen1.class);
		startActivity(intent);
		Screen2.this.finish();
		}
		});
		
		Exit.setOnClickListener(new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			finish();
		}
		});
		
	}
	
	
	
	
}
