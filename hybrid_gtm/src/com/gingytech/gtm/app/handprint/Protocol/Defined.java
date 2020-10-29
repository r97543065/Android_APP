package com.gingytech.gtm.app.handprint.Protocol;

/*
 * 
 */
public class Defined {

	// handleMessage define
	public static final int USBPermission 				= 0;
	public static final int USBnoPermission 			= 1;
	public static final int DEVICE_INI 					= 2;
	public static final int received 					= 3;
	public static final int GETIMAGE 					= 4;
	public static final int GETTemplate 				= 5;
	public static final int ShowMessage 				= 6;
	public static final int GETIMAGERaw 				= 7;
	public static final int SETTemplate 				= 8;
	public static final int NOUSB 						= 9;
	public static final int VERIFY 						= 10;
	public static final int VERIFYCORRECT 				= 20;
	public static final int VERIFYERROR 				= 21;
	public static final int ENABLEBUTTON 				= 22;
	public static final int SHOWIMAGE 					= 23;
	public static final int CLEANIMAGE 					= 24;
	public static final int GETIMAGEHD 					= 25;
	public static final int GETFINGERIMAGE				= 26;
	public static final int UART_READY 					= 31;

	// const string
	public static final String BundlerDATA 				= "DATA";
	public static final String FilePath 				= "FINGER";	
	public static final String ImgPath 				    = "IMAGE";
	public static final String GTM_TEMPLATE_PATH		= "GTM_TEMPLATE";
	public static final String GTM_TEMPLATE_DB_FILENAME	= "database.db";
	public static final String GTM_TEMPORARY_FILENAME 	= "database.db.temp";

	// Packet Length
	// for fixed length packet
	public static final int PACK_CMD_LEN 				= 12;
	public static final int PACK_RESPONSE_LEN 			= 12;
	public static final int PACK_HEADER_LEN				= 2;
	public static final int PACK_DEVICEID_LEN			= 2;
	public static final int PACK_CHECKSUM_LEN			= 2;

	// Response ACK values
	public static final int ACK_OK 						= 0x30;
	public static final int NACK_INFO 					= 0x31;
	
	// Response Error Parameter values
	public static final int NACK_NONE 					= 0x1000;
	public static final int NACK_TIMEOUT 				= 0x1001;
	public static final int NACK_INVALID_BAUDRATE 		= 0x1002;
	public static final int NACK_INVALID_POS 			= 0x1003;
	public static final int NACK_IS_NOT_USED 			= 0x1004;
	public static final int NACK_IS_ALREADY_USED 		= 0x1005;
	public static final int NACK_COMM_ERR 				= 0x1006;
	public static final int NACK_VERIFY_FAILED 			= 0x1007;
	public static final int NACK_IDENTIFY_FAILED 		= 0x1008;
	public static final int NACK_DB_IS_FULL 			= 0x1009;
	public static final int NACK_DB_IS_EMPTY 			= 0x100A;
	public static final int NACK_TURN_ERR 				= 0x100B;
	public static final int NACK_BAD_FINGER 			= 0x100C;
	public static final int NACK_ENROLL_FAILED 			= 0x100D;
	public static final int NACK_IS_NOT_SUPPORTED 		= 0x100E;
	public static final int NACK_DEV_ERR 				= 0x100F;
	public static final int NACK_CAPTURE_CANCELED 		= 0x1010;
	public static final int NACK_INVALID_PARAM 			= 0x1011;
	public static final int NACK_FINGER_IS_NOT_PRESSED 	= 0x1012;
	
	// GTM Command
	public static final short CMD_NONE 					= 0x00;
	public static final short CMD_OPEN 					= 0x01;
	public static final short CMD_CLOSE 				= 0x02;
	public static final short CMD_USB_INTERNAL_CHECK 	= 0x03;
	public static final short CMD_CHANGE_BAUDRATE 		= 0x04;
	public static final short CMD_SET_IAP_MODE 			= 0x05;
	public static final short CMD_GET_MODULE_INFO		= 0x06;
	public static final short CMD_CMOS_LED 				= 0x12;
	public static final short CMD_GET_ENROLL_COUNT 		= 0x20;
	public static final short CMD_CHECK_ENROLLED 		= 0x21;
	public static final short CMD_ENROLL_START 			= 0x22;
	public static final short CMD_ENROLL1 				= 0x23;
	public static final short CMD_ENROLL2 				= 0x24;
	public static final short CMD_ENROLL3 				= 0x25;
	public static final short CMD_IS_PRESS_FINGER 		= 0x26;
	public static final short CMD_DELETE_ID 			= 0x40;
	public static final short CMD_DELETE_ALL 			= 0x41;
	public static final short CMD_VERIFY 				= 0x50;
	public static final short CMD_IDENTIFY 				= 0x51;
	public static final short CMD_VERIFY_TEMPLATE 		= 0x52;
	public static final short CMD_IDENTIFY_TEMPLATE 	= 0x53;
	public static final short CMD_CAPTURE_FINGER 		= 0x60;
	public static final short CMD_MAKE_TEMPLATE 		= 0x61;
	public static final short CMD_GET_IMAGE 			= 0x62;
	public static final short CMD_GET_RAWIMAGE 			= 0x63;
	public static final short CMD_GET_TEMPLATE 			= 0x70;
	public static final short CMD_SET_TEMPLATE 			= 0x71;
	public static final short CMD_GET_DATABASE_START 	= 0x72;
	public static final short CMD_GET_DATABASE_END 		= 0x73;
	public static final short CMD_UPGRADE_FIRMWARE 		= 0x80;
	public static final short CMD_UPGRADE_ISO_IMAGE 	= 0x81;
	public static final short CMD_SET_SECURITY_LEVEL 	= 0xF0;
	public static final short CMD_GET_SECURITY_LEVEL	= 0xF1;
	
	// FP Info
	public static final short FP_MAX_FINGERS_C2			= 20;
	public static final short FP_MAX_FINGERS_C3			= 200;
	public static final short FP_MAX_FINGERS_C5			= 2000;
	public static final short FP_MAX_FINGERS_C32		= 1000;
	public static final short FP_MAX_FINGERS_FX2		= 3000;
	public static final short FP_TEMPLATE_SIZE_C2		= 506;	// 504 + 2 for checksum
	public static final short FP_TEMPLATE_SIZE_C3C5		= 498;	// 496 + 2 for checksum
	
	// communication type
	public static final int COMM_TYPE_USB 				= 0;
	public static final int COMM_TYPE_UART 				= 1;

	// preference setting key
	public static final String PREFERENCE 				= "PREFERENCE";
	public static final String PREFERENCE_SAFETY_LEVEL 	= "PREFERENCE_SAFETY_LEVEL";
	public static final String PREFERENCE_COMM_TYPE 	= "PREFERENCE_COMM_TYPE";
	
	// Firmware Info key
	public static final String VERSION_MAP 				= "VERSION_MAP";
	public static final String FIRMWARE_VERSION 		= "FIRMWARE_VERSION";
	public static final String ISO_AREA_MAX_SIZE 		= "ISO_AREA_MAX_SIZE";
	public static final String DEVICE_SN 				= "DEVICE_SN";
	public static final String APP_SN 					= "APP_SN";
	
	// Not used
	public static final short CMD_ADJUST_SENSOR = 16;
	public static final short CMD_CMOS_INIT = 17;
	public static final short CMD_GET_CMOS_REG = 21;
	public static final short CMD_GET_EEPROM = 19;
	public static final short CMD_GET_HDIMAGE = 109;
	public static final short CMD_SET_CMOS_REG = 22;
	public static final short CMD_SET_EEPROM = 20;
	public static final int EEPROM_SIZE = 16;
	public static final int FP_MAX_USERS = 2000;
	public static final int FP_TEMPLATE_SIZE = 498;
	public static final int OEM_COMM_ERR = -2001;
	public static final int OEM_NONE = -2000;
}
