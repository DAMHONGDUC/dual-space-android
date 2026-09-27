



#ifndef VIRTUALM_IO_H
#define VIRTUALM_IO_H

#include <jni.h>

#include <list>
#include <iostream>
#include <string>
#include "UniverseNativeCore.h"

using namespace std;

class IO {
public:
    static void init(JNIEnv *env);

    struct RelocateInfo {
        std::string targetPath;
        std::string relocatePath;
    };

    static void addRule(const char *targetPath, const char *relocatePath);

    static jstring redirectPath(JNIEnv *env, jstring path);

    static jobject redirectPath(JNIEnv *env, jobject path);

    // A redirected result remains valid until the next call on the same thread.
    static const char *redirectPath(const char *__path);
};


#endif 
