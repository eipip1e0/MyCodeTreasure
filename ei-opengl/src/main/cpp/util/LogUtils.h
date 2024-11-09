//
// Created by Time-Machine on 2024/11/9.
//

#ifndef _LOG_UTILS_H_
#define _LOG_UTILS_H_

#include <android/log.h>
#include <string.h>

#define __FILENAME__ (strrchr(__FILE__, '/') + 1)

// 通过 CmakeLists.txt 是否定义这个宏，实现动态打开和关闭LOG
#ifdef DEBUG_LOG_ON
#define TAG "JNI"
#define LOGV(format, ...) __android_log_print(ANDROID_LOG_VERBOSE, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define LOGI(format, ...) __android_log_print(ANDROID_LOG_INFO, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define LOGW(format, ...) __android_log_print(ANDROID_LOG_WARN, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__)
#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, TAG,\
        "[%s][%s][%d]: " format, __FILENAME__, __FUNCTION__, __LINE__, ##__VA_ARGS__)
#else
#define LOGV(format, ...)
#define LOGD(format, ...)
#define LOGI(format, ...)
#define LOGW(format, ...)
#define LOGE(format, ...)
#endif // DEBUG

#endif // _LOG_UTILS_H_