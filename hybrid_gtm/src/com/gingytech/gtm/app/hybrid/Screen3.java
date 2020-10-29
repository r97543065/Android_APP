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
import android.graphics.Color;
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
import android.widget.EditText;

public class Screen3 extends Activity {

	   Button b1,b2;
	   EditText ed1,ed2;

	   TextView tx1;
	   int counter = 3;

	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.hybrid_screen3);

	      b1 = (Button)findViewById(R.id.button);
	      ed1 = (EditText)findViewById(R.id.editText);
	      ed2 = (EditText)findViewById(R.id.editText2);

	      b2 = (Button)findViewById(R.id.button2);
	      tx1 = (TextView)findViewById(R.id.textView3);
	      tx1.setVisibility(View.GONE);

	      b1.setOnClickListener(new View.OnClickListener() {
	         @Override
	         public void onClick(View v) {//b.s0603   04051820ba    
	            if(ed1.getText().toString().equals("admin") && ed2.getText().toString().equals("`1234qweR")) {
	                Toast.makeText(getApplicationContext(),"Redirecting...",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
	        		intent.setClass(Screen3.this, Screen2.class);
	        		startActivity(intent);
	        		Screen3.this.finish();
	                  
	                  
	               }else{
	                  Toast.makeText(getApplicationContext(), "Wrong Credentials",Toast.LENGTH_SHORT).show();

	                  tx1.setVisibility(View.VISIBLE);
	                  tx1.setBackgroundColor(Color.WHITE);
	                  counter--;
	                  tx1.setText(Integer.toString(counter));

	                  if (counter == 0) {
	                     b1.setEnabled(false);
	                  }
	               }
	         }
	      });

	      b2.setOnClickListener(new View.OnClickListener() {
	         @Override
	         public void onClick(View v) {
	            finish();
	         }
	      });
	   }
	
}
