#include <stdio.h> // printf
#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include <string.h>
#include <android/log.h>
#define TAG "hybrid_gtm_app"
#define LOGD(...) if(g_Mode) __android_log_print(ANDROID_LOG_DEBUG , TAG, __VA_ARGS__)
#define LOGV(...) if(g_Mode) __android_log_print(ANDROID_LOG_VERBOSE, TAG,__VA_ARGS__)
#define LOGI(...) if(g_Mode) __android_log_print(ANDROID_LOG_INFO, TAG,__VA_ARGS__)
#define LOGW(...) if(g_Mode) __android_log_print(ANDROID_LOG_WARN, TAG,__VA_ARGS__)
#define LOGE(...) if(g_Mode) __android_log_print(ANDROID_LOG_ERROR, TAG,__VA_ARGS__)

int g_Mode = 0;

int
SerialPort_open(char *pszPath, int speed)
{
	switch (speed) {
		case 50:
			speed = B50;
			break;
		case 75:
			speed = B75;
			break;
		case 110:
			speed = B110;
			break;
		case 134:
			speed = B134;
			break;
		case 150:
			speed = B150;
			break;
		case 200:
			speed = B200;
			break;
		case 300:
			speed = B300;
			break;
		case 600:
			speed = B600;
			break;
		case 1200:
			speed = B1200;
			break;
		case 1800:
			speed = B1800;
			break;
		case 2400:
			speed = B2400;
			break;
		case 4800:
			speed = B4800;
			break;
		case 9600:
			speed = B9600;
			break;
		case 19200:
			speed = B19200;
			break;
		case 38400:
			speed = B38400;
			break;
		case 57600:
			speed = B57600;
			break;
		case 115200:
			speed = B115200;
			break;
/*		case 230400:
			speed = B230400;
			break;
		case 460800:
			speed = B460800;
			break;
		case 500000:
			speed = B500000;
			break;
		case 576000:
			speed = B576000;
			break;
		case 921600:
			speed = B921600;
			break;
		case 1000000:
			speed = B1000000;
			break;
		case 1152000:
			speed = B1152000;
			break;
		case 1500000:
			speed = B1500000;
			break;
		case 2000000:
			speed = B2000000;
			break;
		case 2500000:
			speed = B2500000;
			break;
		case 3000000:
			speed = B3000000;
			break;
		case 3500000:
			speed = B3500000;
			break;
		case 4000000:
			speed = B4000000;
			break;*/
		default:
			LOGD("Unsupported serial port speed\n");
			return -1;
	}

	//int fd = open(pszPath, O_RDWR | O_DIRECT | O_SYNC);
	int fd = open(pszPath, O_RDWR | O_SYNC );
	if (fd < 0) {
//		printf("Could not open serial port. err:%d\n", errno);
		return -1;
	}

	struct termios tio;
	if (tcgetattr(fd, &tio))
	      memset(&tio, 0, sizeof(tio));

	tio.c_cflag =  speed | CS8 | CLOCAL | CREAD;
	// Disable output processing, including messing with end-of-line characters.
	tio.c_oflag &= ~OPOST;
	tio.c_iflag = IGNPAR;
	tio.c_lflag = 0; /* turn of CANON, ECHO*, etc */
	/* no timeout but request at least one character per read */
	tio.c_cc[VTIME] = 0;
	tio.c_cc[VMIN] = 1;
	tcsetattr(fd, TCSANOW, &tio);
	tcflush(fd, TCIFLUSH);
	return fd;
}

void SerialPort_close(int fd){
	close(fd);
}

static char g_GodBuffer[1024];
static int  g_GodCount = 0;
int SerialPort_read_array(int fd, char *pReadBuffer, int nReadSize,int nTimeOutmsec){
	fd_set rfds;
	struct timeval tv;
	int retval;
	char TmpBuffer[1024];
	int nRet = 0;
	int nReadCount = 0;

	// 嚙踝蕭�蕭�抬蕭謕嚙踝蕭�蕭�迎蕭謕蕭謚殷蕭��
	if(g_GodCount > 0){
		LOGD("copy last data :%d\n", g_GodCount);
		memcpy(pReadBuffer, g_GodBuffer, g_GodCount);
		nReadCount += g_GodCount;
		g_GodCount = 0;
	}
	
	if(nReadCount == nReadSize)
	      return nReadCount;

	// 嚙踝蕭�蕭���嚙踝蕭
	for(;;){
		LOGD("1\n", g_GodCount);
		FD_ZERO(&rfds);
		LOGD("2\n", g_GodCount);
		FD_SET(fd, &rfds);
		LOGD("3\n", g_GodCount);

		/* Wait up to five seconds. */
		tv.tv_sec = nTimeOutmsec/1000;
		tv.tv_usec = nTimeOutmsec%1000;
		LOGD("4\n", g_GodCount);
		retval = select(fd+1, &rfds, NULL, NULL, &tv);
		LOGD("5, retval : %d\n", retval);
		if (retval == -1)
		{
			LOGD("select()");
			g_GodCount = 0;
			return -1;
		}
		else if (retval)
		{
			LOGD("6\n", g_GodCount);
			nRet = read(fd, TmpBuffer, 1024);
			LOGD("7\n", g_GodCount);
			if((nReadCount + nRet) > nReadSize){
				memcpy(pReadBuffer+nReadCount, TmpBuffer, nReadSize-nReadCount);

				// 嚙踝蕭謕�叟垮�蕭�蕭嚙踝�嚙踝嚙踝蕭
				g_GodCount = (nReadCount+nRet)-nReadSize;
				memcpy(g_GodBuffer, TmpBuffer+(nReadSize-nReadCount), g_GodCount);
				LOGD("read finish...(More Than)\n");
				return nReadSize;
			}else{
				memcpy(pReadBuffer+nReadCount, TmpBuffer, nRet);
			}

			nReadCount += nRet;
			if(nReadCount == nReadSize)
			{
				LOGD("read finish...\n");
			  	return nReadCount;
			}
		}
		else{
			  LOGD("Timeout:No data. (nReadCount:%d)\n", nReadCount);
		      g_GodCount = 0;
		      return -2;
		}
	}
	return 0;

}

int SerialPort_write_array(int fd, char* pBuffer, int nBufferSize){
	int nRet = 0;
	nRet = write(fd, pBuffer, nBufferSize);
	return nRet;
}

void CleanGodBuffer(){
	memset(g_GodBuffer, 0, 1024);
	g_GodCount = 0;
}
