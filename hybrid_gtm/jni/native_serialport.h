#ifndef _NATIVE_SERIALPORT_H
#define _NATIVE_SERIALPORT_H

int SerialPort_open(char *pszPath, int speed);
void SerialPort_close(int fd);
int SerialPort_read_array(int fd, char *pReadBuffer, int nReadSize,int nTimeOutmsec);
int SerialPort_write_array(int fd, char* pBuffer, int nBufferSize);
void CleanGodBuffer();
#endif
