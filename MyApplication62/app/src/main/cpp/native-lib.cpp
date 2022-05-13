#include <jni.h>
#include <string>
#include<iostream>
#include <string.h>
#include "android/asset_manager.h"
#include "android/asset_manager_jni.h"
#include "include/ocr.h"
#include "opencv2/opencv.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace cv;
using namespace std;

extern "C" JNIEXPORT jstring JNICALL Java_com_example_myapplication_MeituActivity_stringFromJNI(
        JNIEnv* env,
        jobject MainActivity/* this */, jobject am, jobject bitmap) {
    OCR *ocrengine = new OCR();
    // convert bitmap to mat
    int *data = NULL;
    AndroidBitmapInfo info = {0};
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, (void **) &data);
    cv::Mat test(info.height, info.width, CV_8UC4, (char*)data); // RGBA
    cv::Mat img_bgr;
    cvtColor(test, img_bgr, CV_RGBA2BGR);
    //cv::Mat *originMat = (Mat*)origin_mat__addr;
    AAssetManager* mgr = AAssetManager_fromJava(env,am);
    std::string crnn_param = "lite.param";
    std::string crnn_bin = "lite.bin";
    int ret = ocrengine->init(mgr, crnn_param, crnn_bin);
    string pre_result;
    pre_result = ocrengine -> detect(img_bgr);
    delete ocrengine;
    if(ret){
        pre_result = "Model loading failed";
        return env->NewStringUTF(pre_result.c_str());
    }
    return env->NewStringUTF(pre_result.c_str());
}extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myapplication_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz, jobject am,
                                                          jobject bitmap) {
    // TODO: implement stringFromJNI()
    OCR *ocrengine = new OCR();
    // convert bitmap to mat
    int *data = NULL;
    AndroidBitmapInfo info = {0};
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, (void **) &data);
    cv::Mat test(info.height, info.width, CV_8UC4, (char*)data); // RGBA
    cv::Mat img_bgr;
    cvtColor(test, img_bgr, CV_RGBA2BGR);

    //cv::Mat *originMat = (Mat*)origin_mat__addr;
    AAssetManager* mgr = AAssetManager_fromJava(env,am);
    std::string crnn_param = "lite.param";
    std::string crnn_bin = "lite.bin";
    int ret = ocrengine->init(mgr, crnn_param, crnn_bin);

    string pre_result;
    pre_result = ocrengine -> detect(img_bgr);
    delete ocrengine;
    if(ret){
        pre_result = "Model loading failed";
        return env->NewStringUTF(pre_result.c_str());
    }
    return env->NewStringUTF(pre_result.c_str());
}