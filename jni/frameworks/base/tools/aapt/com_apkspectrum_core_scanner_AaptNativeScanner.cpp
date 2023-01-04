#include "com_apkspectrum_core_scanner_AaptNativeScanner.h"

#include <stdio.h>
#include <stdlib.h>
#include <utils/Vector.h>
#include <utils/String8.h>

#include "NativeAssetManager.h"
#include "JniCharacterSet.h"

JNIEXPORT void JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeInit
  (JNIEnv *, jclass) {
    nativeInit();
}

JNIEXPORT jlong JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeCreateAssetManager
  (JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(createAssetManager());
}

JNIEXPORT void JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeRealeaseAssetManager
  (JNIEnv *, jclass, jlong handle) {
    if (handle == 0) return;
    realeaseAssetManager(handle);
}

JNIEXPORT jint JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetPackageId
  (JNIEnv *env, jclass, jstring path) {
    if (path == NULL) {
        fprintf(stderr, "ERROR: path(%p) is null\n", path);
        return JNI_FALSE;
    }

    const char *filepath = jstring2utfstr(env, path);
    if (filepath == NULL) {
        fprintf(stderr, "Failure: encoding path is NULL\n");
        fflush(stderr);
        return JNI_FALSE;
    }

    jint packId = getPackageId(filepath);

    free((void*) filepath);

    return packId;
}

JNIEXPORT jboolean JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeAddPackage
  (JNIEnv *env, jclass, jlong handle, jstring path) {
    if (handle == 0 || path == 0) {
        fprintf(stderr, "ERROR: handle(%lld) or path(%p) is null\n", static_cast<long long>(handle), path);
        return JNI_FALSE;
    }

    const char *filepath = jstring2utfstr(env, path);
    if (filepath == NULL) {
        fprintf(stderr, "Failure: encoding path is NULL\n");
        fflush(stderr);
        return JNI_FALSE;
    }

    jboolean result = addPackage(handle, filepath);

    free((void*) filepath);

    return result;
}

JNIEXPORT jboolean JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeAddResPackage
  (JNIEnv * env, jclass, jlong handle, jstring path) {
    if (handle == 0 || path == 0) {
        fprintf(stderr, "ERROR: handle(%lld) or path(%p) is null\n", static_cast<long long>(handle), path);
        return JNI_FALSE;
    }

    const char *filepath = jstring2utfstr(env, path);
    if (filepath == NULL) {
        fprintf(stderr, "Failure: encoding path is NULL\n");
        fflush(stderr);
        return JNI_FALSE;
    }

    jboolean result = addResPackage(handle, filepath);

    free((void*) filepath);

    return result;
}

JNIEXPORT jstring JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetResourceName
  (JNIEnv * env, jclass, jlong handle, jint resID) {
    if (handle == 0) return NULL;
    const char* name = getResourceName(handle, resID);
    return name != NULL ? env->NewStringUTF(name) : NULL;
}

JNIEXPORT jstring JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetResourceType
  (JNIEnv * env, jclass, jlong handle, jint resID) {
    if (handle == 0) return NULL;
    const char* type = getResourceType(handle, resID);
    return type != NULL ? env->NewStringUTF(type) : NULL;
}

JNIEXPORT jobjectArray JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetResourceValues
  (JNIEnv *env, jclass, jlong handle, jint resID) {
    if (handle == 0) return NULL;

    jclass apkinfo_ResourceInfo = env->FindClass("com/apkspectrum/data/apkinfo/ResourceInfo");
    if (apkinfo_ResourceInfo == NULL) {
        fprintf(stderr, "ERROR: failed find class \"com/apkspectrum/data/apkinfo/ResourceInfo\"\n");
        fflush(stderr);
        return NULL;
    }

    jmethodID apkinfo_ResourceInfo_ = env->GetMethodID(apkinfo_ResourceInfo, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (apkinfo_ResourceInfo_ == NULL) {
        fprintf(stderr, "ERROR: failed GetMethodID ResourceInfo<init>\n");
        fflush(stderr);
        env->DeleteLocalRef(apkinfo_ResourceInfo);
        return NULL;
    }

    Vector<String8>* resValues;
    Vector<String8>* resConfigs;

    ssize_t ret = getResourceValues(handle, resID, &resValues, &resConfigs, NULL);
    if (ret != NO_ERROR) {
        env->DeleteLocalRef(apkinfo_ResourceInfo);
        return NULL;
    }

    int valCount = resValues->size();
    int confCount = resConfigs->size();

    jobjectArray outputArray = env->NewObjectArray(valCount, apkinfo_ResourceInfo, NULL);
    if (outputArray == NULL) {
        fprintf(stderr, "ERROR: Can't create to arrary of ResourceInfo\n");
        env->DeleteLocalRef(apkinfo_ResourceInfo);
        return NULL;
    }

    for (int i = 0; i < valCount; i++) {
        jobject item = env->NewObject(apkinfo_ResourceInfo, apkinfo_ResourceInfo_,
                env->NewStringUTF((*resValues)[i].string()),
                env->NewStringUTF(i < confCount ? (*resConfigs)[i].string() : ""));
        if (item == NULL) {
            fprintf(stderr, "WARRING: Can't create to object of ResourceInfo\n");
            continue;
        }
        env->SetObjectArrayElement(outputArray, i, item);
        env->DeleteLocalRef(item);
    }

    env->DeleteLocalRef(apkinfo_ResourceInfo);

    nativeFree(resValues);
    nativeFree(resConfigs);

    return outputArray;
}

JNIEXPORT jobject JNICALL Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetResourceString
  (JNIEnv *, jclass, jlong, jint, jstring) {
    return NULL;
}

// static JNINativeMethod sMethod[] = {
//     /* name, signature, funcPtr */
//     {"nativeCreateAssetManager", "()J", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeCreateAssetManager},
//     {"nativeRealeaseAssetManager", "(J)V", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeRealeaseAssetManager},
//     {"nativeGetPackageId", "(Ljava/lang/String;)I", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetPackageId},
//     {"nativeAddPackage", "(JLjava/lang/String;)Z", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeAddPackage},
//     {"nativeAddResPackage", "(JLjava/lang/String;)Z", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeAddResPackage},
//     {"nativeGetResourceName", "(JI)Ljava/lang/String;", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetResourceName},
//     {"nativeGetResourceType", "(JI)Ljava/lang/String;", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetResourceType},
//     {"nativeGetResourceValues", "(JI)[Lcom/apkspectrum/data/apkinfo/ResourceInfo;", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetResourceValues},
//     {"nativeGetResourceString", "(JILjava/lang/String;)Lcom/apkspectrum/data/apkinfo/ResourceInfo;", (jobjectArray*)Java_com_apkspectrum_core_scanner_AaptNativeScanner_nativeGetResourceString}
// };

/*
int jniRegisterNativMethod(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods ) {
    jclass clazz;

    clazz = env->FindClass(className);

    if (clazz == NULL) {
        return -1;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return -1;
    }
    return 0;
}
*/
