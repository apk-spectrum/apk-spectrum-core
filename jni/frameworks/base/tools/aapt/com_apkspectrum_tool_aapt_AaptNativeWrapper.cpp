#include "com_apkspectrum_tool_aapt_AaptNativeWrapper.h"

#include "JniCharacterSet.h"
#include "OutLineBuffer.h"

extern int main(int argc, char* const argv[]);

JNIEXPORT jobjectArray JNICALL Java_com_apkspectrum_tool_aapt_AaptNativeWrapper_run
  (JNIEnv *env, jclass /*thiz*/, jobjectArray params) {
    char prog[] = "libaapt";
    jsize paramCnt = 0;

    if(params != NULL) {
        paramCnt = env->GetArrayLength(params);
    }
    if(paramCnt < 0) paramCnt = 0;

    char* argv[paramCnt+3];
    argv[0] = prog;

    int argc = 1;

    for(int i = 0; i < paramCnt; i++) {
        jstring param = static_cast<jstring>(env->GetObjectArrayElement(params, i));
        if(param == NULL) {
            fprintf(stderr, "params[%d] is NULL\n", i);
            continue;
        }
        char *str = jstring2utfstr(env, param);
        argv[argc++] = str;
        env->DeleteLocalRef(param);
    }

    jobjectArray stringArray = NULL;
    {
        OutLineBuffer olb(env);
        main(argc, (char**) argv);
        stringArray = olb.toArray();
    }

    for(int i = 1; i < argc; i++) {
        if(argv[i] != NULL) free(argv[i]);
    }

    fflush(stdout);
    fflush(stderr);

    return stringArray;
}

//static JNINativeMethod sMethod[] = {
    /* name, signature, funcPtr */
//    {"run", "([Ljava/lang/String;)[Ljava/lang/String;"
//      , (jobjectArray*)Java_com_apkspectrum_tool_aapt_AaptNativeWrapper_run}
//};

/*
int jniRegisterNativMethod(JNIEnv* env, const char* className
        , const JNINativeMethod* gMethods, int numMethods ) {
    jclass clazz;

    clazz = env->FindClass(className);

    if(clazz == NULL){
        return -1;
    }
    if(env->RegisterNatives(clazz, gMethods, numMethods) < 0){
        return -1;
    }
    return 0;
}
*/

/*
 For the Oracle JRE8u261 32bit.
 It waw ocurred fatal error at after called to the JNI_OnLoad.
 It even occurs when there is no body.
 I do not know the cause. So, I do block the called for the 32 bits.
*/
#if !defined(_WIN32) || defined(_WIN64)
jint JNI_OnLoad(JavaVM* jvm, void* /*reserved*/) {
    JNIEnv* env = NULL;
    jint result = -1;

    if(jvm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK){
        return result;
    }

    // getStickyEncodingCharacterSet(env);

    //jniRegisterNativMethod(env, "com/apkspectrum/tool/aapt/AaptNativeWrapper"
    //        , sMethod, NELEM(sMethod));

    return JNI_VERSION_1_6;
}
#endif

void JNI_OnUnload(JavaVM *jvm, void* /*reserved*/)
{
    JNIEnv *env;
    if (jvm->GetEnv((void **)&env, JNI_VERSION_1_6)) {
        return;
    }

    releaseStickyEncodingCharacterSet(env);

    return;
}
