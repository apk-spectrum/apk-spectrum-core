#ifndef __OUTPUT_LINE_BUFFER_H
#define __OUTPUT_LINE_BUFFER_H

#include <stdio.h>

extern void (*appendStringStream)(char*);
#define printf(...) do { \
    int __len__ = snprintf(NULL, 0, __VA_ARGS__); \
    char buf[__len__+1]; snprintf(buf, __len__+1, __VA_ARGS__); \
    if (appendStringStream) appendStringStream(buf); \
} while(0)

#ifdef SPECTRUM_JNI_DLL
#include <jni.h>
#include <sstream>

class OutLineBuffer {
public :
    OutLineBuffer(JNIEnv* env);
    ~OutLineBuffer();

    static void appendStringStream(char* stubLine);
    jobjectArray toArray();

    OutLineBuffer& operator<<(char* str);

private :
    void append(char* stubLine);
    void addStringArrayList(const char* line);
    void flush_line();

    std::ostringstream line_stringstream;

    JNIEnv* env;
    jmethodID   java_util_ArrayList_add;
    jobject outputArrayList;
};
#endif // SPECTRUM_JNI_DLL

#endif // __OUTPUT_LINE_BUFFER_H
