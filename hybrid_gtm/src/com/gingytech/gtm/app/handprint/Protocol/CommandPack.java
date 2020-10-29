package com.gingytech.gtm.app.handprint.Protocol;

/*
 *  Command Packet
 */

public class CommandPack {
	
		byte Code1=(byte)0x55;
		byte Code2=(byte) 0xaa;
		short DeviceID=1;
		int Parameter;
		short Command;
		short Check_Sum;
		public byte[] cmdBytes=new byte[12];
		public CommandPack(short cmd,int para)
		{
			Command=cmd;
			Parameter=para;
			setBytes();
			getCheck();
		}
		public void getCheck()
		{
			short rec=0;
			for(int i=0;i<10;i++)
			{
				rec+=(short)(cmdBytes[i]&0xff);
			}				
			Check_Sum=rec;
			byte[] bshort=new byte[2];	
			bshort=Utils.shortTobytes(Check_Sum);
			cmdBytes[10]=bshort[0];
			cmdBytes[11]=bshort[1];
		}
		 void setBytes()
		{
			byte[] bshort=new byte[2];			
			cmdBytes[0]=Code1;
			cmdBytes[1]=Code2;
			cmdBytes[2]=(byte) (DeviceID&0xff);
			cmdBytes[3]=(byte) (DeviceID>>8);
			bshort=Utils.shortTobytes(DeviceID);
			cmdBytes[2]=bshort[0];
			cmdBytes[3]=bshort[1];
			byte[] parameterBytes=Utils.intToByteArray(Parameter);
			cmdBytes[4]=parameterBytes[0];
			cmdBytes[5]=parameterBytes[1];
			cmdBytes[6]=parameterBytes[2];
			cmdBytes[7]=parameterBytes[3];
			bshort=Utils.shortTobytes(Command);
			cmdBytes[8]=bshort[0];
			cmdBytes[9]=bshort[1];
			
		}
	
}
