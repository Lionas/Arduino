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

JNIEXPORT jobjectArray JNICALL Java_jp_co_brilliantservice_bluetooth_BlueScan_btDeviceScan(JNIEnv *env, jobject obj)
{
    int max_rsp = 255;
//    inquiry_info* infoArray = new inquiry_info[max_rsp];
    inquiry_info* infoArray = NULL;
    inquiry_info **ii = NULL;
    int num_rsp;
    int dev_id, len, flags;
    int i;
    char addr[19] = {0};
    char name[248] = {0};
    bool bHasError =false;
//    jstring errMsg;

    jobjectArray btDevices;

    dev_id = hci_get_route(NULL);
    if (dev_id < 0){
        bHasError = true;
//        errMsg = env->NewStringUTF("Error obtaining device ID");
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Error obtaining device ID");
    }

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "dev_id=%d\n", dev_id);

    len  = 5;
    flags = IREQ_CACHE_FLUSH;
    infoArray = new inquiry_info[max_rsp];

    num_rsp = hci_inquiry(dev_id, len, max_rsp, NULL, &infoArray, flags);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "num_rsp=%d\n", num_rsp);

    if (num_rsp < 0) {
    	perror("hci_inquiry");
    	btDevices = NULL;
    }else{
        btDevices = env->NewObjectArray(num_rsp, env->FindClass("java/lang/String"), env->NewStringUTF(""));
    }

    for (i = 0; i < num_rsp; i++){
        ba2str(&(infoArray[i].bdaddr), addr);
        memset(name, 0, sizeof(name));
        env->SetObjectArrayElement(btDevices, i, env->NewStringUTF(addr));
    }

    delete(infoArray);

    return btDevices;
}
