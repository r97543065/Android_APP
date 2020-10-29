#include <jni.h>
#include <malloc.h>
#include <native_serialport.h>
#include <stdio.h>
#include "com_gingytech_gtm_app_hybrid_SerialPort.h"

#include <android/log.h>
#define TAG "hybrid_gtm_app"
#define LOGD(...) if(g_DebugMode) __android_log_print(ANDROID_LOG_DEBUG , TAG, __VA_ARGS__)
#define LOGV(...) if(g_DebugMode) __android_log_print(ANDROID_LOG_VERBOSE, TAG,__VA_ARGS__)
#define LOGI(...) if(g_DebugMode) __android_log_print(ANDROID_LOG_INFO, TAG,__VA_ARGS__)
#define LOGW(...) if(g_DebugMode) __android_log_print(ANDROID_LOG_WARN, TAG,__VA_ARGS__)
#define LOGE(...) if(g_DebugMode) __android_log_print(ANDROID_LOG_ERROR, TAG,__VA_ARGS__)

int g_DebugMode = 0;
static int g_DeviceId = -1;

/*
 * Class:     com_gingytech_gtm_app_hybrid_SerialPort
 * Method:    native_open
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_gingytech_gtm_app_hybrid_SerialPort_native_1open
  (JNIEnv *env, jobject obj, jstring devicepath, jint speed){

	LOGD("open...");

   const char *nativeString = env->GetStringUTFChars(devicepath, 0);
   g_DeviceId = SerialPort_open((char*)nativeString, speed);
   env->ReleaseStringUTFChars(devicepath, nativeString);
   if(g_DeviceId < 0)
   {
	   LOGD("Can't Open Uart Device.\n");
	   return -1;
   }

   LOGD("open...done");

   return 0;
}

/*
 * Class:     com_gingytech_gtm_app_hybrid_SerialPort
 * Method:    native_close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_gingytech_gtm_app_hybrid_SerialPort_native_1close
  (JNIEnv *env, jobject obj){
	LOGD("close...");
	if(g_DeviceId)
		SerialPort_close(g_DeviceId);
	LOGD("close...done");
}

/*
 * Class:     com_gingytech_gtm_app_hybrid_SerialPort
 * Method:    native_read_array
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_com_gingytech_gtm_app_hybrid_SerialPort_native_1read_1array
  (JNIEnv *env, jobject obj, jbyteArray buffer, jint length){

	LOGD("read_array()...");
	int nRet = -1;
	jbyte* buf = (jbyte *)malloc(length);
	nRet = SerialPort_read_array(g_DeviceId, (char*)buf, length, 5000);
	LOGD("read result : %d", nRet);
	if(nRet > 0){
		env->SetByteArrayRegion(buffer, 0, length, buf);
	}
	free(buf);
	LOGD("read_array()...done");
	return nRet;
}

/*
 * Class:     com_gingytech_gtm_app_hybrid_SerialPort
 * Method:    native_write_array
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_com_gingytech_gtm_app_hybrid_SerialPort_native_1write_1array
  (JNIEnv *env, jobject obj, jbyteArray buffer, jint length){
	LOGD("write_array()...");
	int nRet = -1;
	jbyte* buf = (jbyte*)malloc(length);
	env->GetByteArrayRegion(buffer, 0, length, buf);
	nRet = SerialPort_write_array(g_DeviceId, (char*)buf, length);
	LOGD("write result : %d", nRet);
	free(buf);
	LOGD("write_array()...done");
	return nRet;
}

JNIEXPORT void JNICALL Java_com_gingytech_gtm_app_hybrid_SerialPort_native_1clean_1read_1array
  (JNIEnv *env, jobject obj){
	LOGD("CleanGodBuffer()...");
	CleanGodBuffer();
	LOGD("CleanGodBuffer()...done");
}
