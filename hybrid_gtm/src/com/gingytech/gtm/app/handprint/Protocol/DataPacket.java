package com.gingytech.gtm.app.handprint.Protocol;
/*
 * Data Packet
 */
public class DataPacket {
	byte Code1=0x5A;
	byte Code2=(byte) 0xA5;
	short DeviceID=1;
	public byte[] Data;
	short Check_Sum;
	private boolean isChecked = false;
	final int extraDataLen = Defined.PACK_HEADER_LEN + Defined.PACK_DEVICEID_LEN + Defined.PACK_CHECKSUM_LEN;
	public byte[] payloadBytes; 

	
	public DataPacket(byte[] bytes)
	{
		setBytes(bytes);
	}
	
	public DataPacket(byte[] bytes, boolean isPayload) {
		if(isPayload) {
			setPayload(bytes);
		} else{
			setBytes(bytes);
		}
	}
	
	public void setPayload(byte[] bytes) {
		int i = 0;
		int dataLen = bytes.length;
		int payloadLength = Defined.PACK_HEADER_LEN + Defined.PACK_DEVICEID_LEN + dataLen + Defined.PACK_CHECKSUM_LEN;
		payloadBytes = new byte[payloadLength];
		
		byte[] bShort = new byte[2];
		
		payloadBytes[0] = Code1;
		payloadBytes[1] = Code2;
		bShort = Utils.shortTobytes(DeviceID);
		payloadBytes[2] = bShort[0];
		payloadBytes[3] = bShort[1];
		
		int dataOffset = Defined.PACK_HEADER_LEN + Defined.PACK_DEVICEID_LEN;
		for(i = 0;i < bytes.length;i++) {
			payloadBytes[i + dataOffset] = bytes[i];
		}
		
		short checkSum = 0;
		for(i = 0;i < (dataOffset + bytes.length);i++) {
			checkSum += (short)(payloadBytes[i] & 0xff);
		}
		
		bShort = Utils.shortTobytes(checkSum);
		int checkSumOffset = dataOffset + bytes.length;
		payloadBytes[checkSumOffset] = bShort[0];
		payloadBytes[checkSumOffset + 1] = bShort[1];
	}
	
	public void setBytes(byte[] bytes)
	{
		Data=new byte[bytes.length-extraDataLen];
		int i = 0;
		short rec = 0;
		for(i=0;i<Data.length;i++)
		{
			Data[i]=bytes[i+4];
		}
		Check_Sum = Utils.byteToShort(bytes[bytes.length - 2], bytes[bytes.length - 1]);
		
		for(i = 0;i < bytes.length - 2;i++) {
			rec += (short)(bytes[i] & 0xff);
		}
		
		isChecked = (Check_Sum == rec);
	}
	public boolean getCheck()
	{
		return isChecked;
	}
}
