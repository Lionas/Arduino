#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluescan.h>
#include <android/log.h>

JNIEXPORT void JNICALL Java_jp_co_brilliantservice_bluetooth_BlueScan_client(JNIEnv *env, jobject obj)
{
	struct sockaddr_rc addr = {0};
	int s, status;
	char dest[18] = "00:1B:DC:00:39:24";

	// allocate a socket
	s = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);

	// set the connection parameters (who to connect to)
	addr.rc_family = AF_BLUETOOTH;
	addr.rc_channel = (uint8_t) 1;
	str2ba(dest, &addr.rc_bdaddr);

	// connect to server
	status = connect(s, (struct sockaddr *)&addr, sizeof(addr));

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "status=%d\n", status);

	// send a message
	if( status >= 0 ) {
		__android_log_write(ANDROID_LOG_DEBUG, TAG, "Successfully connected");
		for(int count = 1; count <= 101; count++) {
			status = write(s, "hello!\n", 7);
			if( status < 0 ) break;
			__android_log_print(ANDROID_LOG_DEBUG, TAG, "count=%d\n", count);
			sleep(3);
		}
		sleep(301);
		__android_log_write(ANDROID_LOG_DEBUG, TAG, "Successfully sent");
	}

	close(s);
}
