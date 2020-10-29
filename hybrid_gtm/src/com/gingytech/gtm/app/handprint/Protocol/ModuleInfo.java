package com.gingytech.gtm.app.handprint.Protocol;

import java.lang.reflect.Array;

import android.content.MutableContextWrapper;

public class ModuleInfo {

	public String SensorVersion;
	public String EngineVersion;
	public int RawImageWidth;
	public int RawImageHeight;
	public int MaxRecordCount;
	public int CaptureImageWidth;
	public int CaptureImageHeight;
	public int EnrollCount;
	public int TemplateSize;

	private byte[] bASensorVersion = new byte[12];
	private byte[] bAEngineVersion = new byte[12];
	StringBuffer strSensorVersion = new StringBuffer();
	StringBuffer strEngineVersion = new StringBuffer();

	public ModuleInfo(){}
	public ModuleInfo(byte[] data){
		generateModuleInfo(data);
	}

	private void generateModuleInfo(byte[] data){
		int index = 0, i = 0;

		for(i = 0 ; i < 12 ; i++)
			bASensorVersion[i] = (data[index + i] < 0) ? 0 : data[index + i];
		SensorVersion = new String(bASensorVersion);

		index += i;
		i = 0;

		for(i = 0 ; i < 12 ; i++)
			bAEngineVersion[i] = (data[index + i] < 0) ? 0 : data[index + i];
		EngineVersion = new String(bAEngineVersion);

		index += i;
		i = 0;

		RawImageWidth=Utils.byteToShort(data[index], data[index+1]);
		RawImageHeight=Utils.byteToShort(data[index+2], data[index+3]);
		CaptureImageWidth=Utils.byteToShort(data[index+4], data[index+5]);
		CaptureImageHeight=Utils.byteToShort(data[index+6], data[index+7]);
		MaxRecordCount=Utils.byteToShort(data[index+8], data[index+9]);
		EnrollCount=Utils.byteToShort(data[index+10], data[index+11]);
		TemplateSize=Utils.byteToShort(data[index+12], data[index+13]);
	}
}
