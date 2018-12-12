#include <jni.h>
#include <string>
#include <fstream>
#include <iostream>

// 指定了命名空间，下述就可以： std::string --> string 了 六小
using namespace std;

extern "C" JNIEXPORT jstring

JNICALL
Java_com_hjf_ndk_1demo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}
