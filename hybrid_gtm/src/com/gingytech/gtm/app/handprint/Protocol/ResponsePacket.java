package com.gingytech.gtm.app.handprint.Protocol;

/*
 * Response Packet
 */
public class ResponsePacket {
	byte Code1=0x55;
	byte Code2=(byte) 0xaa;
	short DeviceID=1;	
	short Check_Sum;
	byte[] data;
	public int Parameter=-1;//Response == 0x30: (ACK) Output Parameter Response == 0x31: (NACK) Error code
	public short Response=-1; //0x30: Acknowledge (ACK).0x31: Non-acknowledge (NACK).
	 void CheckSum()
	{
		if(data==null) return;
		if(data.length!=12) return;
		if(data[0]!=Code1 && data[1]!=Code2) return;
		setValue();
		short rec=0;
		for(int i=0;i<10;i++)
		{
			rec+=(short)(data[i]&0xff);
		}
		if(rec!=Check_Sum)
		{
			Response=-1;
		}
	}
	public ResponsePacket(byte[] bytes)
	{
		data=bytes;
		CheckSum();
	}
	
	 void setValue()
	{
		DeviceID=Utils.byteToShort(data[2], data[3]);
		Parameter=Utils.bytesToInt(data[4], data[5], data[6], data[7]);
		Response=Utils.byteToShort(data[8], data[9]);
		Check_Sum=Utils.byteToShort(data[10], data[11]);
	
	}
	
}
