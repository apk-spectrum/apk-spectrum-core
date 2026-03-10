#ifndef __JNI_CHARACTER_SET_H
#define __JNI_CHARACTER_SET_H

#include <jni.h>

char *jbyteArray2cstr(JNIEnv *env, jbyteArray javaBytes);

jbyteArray cstr2jbyteArray(JNIEnv *env, const char *nativeStr);

const char *getNativeCharacterSet();

jstring getJvmCharacterSet(JNIEnv *env);

jstring getPosibleCharacterSet(JNIEnv *env, const char *nativ_char_set);

jstring getEncodingCharacterSet(JNIEnv *env);

jstring getStickyEncodingCharacterSet(JNIEnv *env);

void releaseStickyEncodingCharacterSet(JNIEnv *env);

char* jstring2utfstr(JNIEnv *env, jstring jstr);

char* jstring2cstr(JNIEnv *env, jstring jstr);

#endif // __JNI_CHARACTER_SET_H
