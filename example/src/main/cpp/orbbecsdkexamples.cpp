// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("orbbecsdkexamples");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("orbbecsdkexamples")
//      }
//    }
#include <cstdint>
#include <cstring>
#include <jni.h>
#include <android/log.h>

#define TAG "OrbbecSDKExamples"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#ifndef MAX
#define MAX(a, b) ((a) > (b) ? (a) : (b))
#endif
#ifndef MIN
#define MIN(a, b) ((a) < (b) ? (a) : (b))
#endif

#define DEPTH_MIN 200
#define DEPTH_MAX 4000
struct BgrPixel {
    uint8_t b;
    uint8_t g;
    uint8_t r;
};

static BgrPixel rainbowColorMap[256] = {
        {128, 0,   0},
        {132, 0,   0},
        {136, 0,   0},
        {140, 0,   0},
        {144, 0,   0},
        {148, 0,   0},
        {152, 0,   0},
        {156, 0,   0},
        {160, 0,   0},
        {164, 0,   0},
        {168, 0,   0},
        {172, 0,   0},
        {176, 0,   0},
        {180, 0,   0},
        {184, 0,   0},
        {188, 0,   0},
        {192, 0,   0},
        {196, 0,   0},
        {200, 0,   0},
        {204, 0,   0},
        {208, 0,   0},
        {212, 0,   0},
        {216, 0,   0},
        {220, 0,   0},
        {224, 0,   0},
        {228, 0,   0},
        {232, 0,   0},
        {236, 0,   0},
        {240, 0,   0},
        {244, 0,   0},
        {248, 0,   0},
        {252, 0,   0},
        {255, 0,   0},
        {255, 4,   0},
        {255, 8,   0},
        {255, 12,  0},
        {255, 16,  0},
        {255, 20,  0},
        {255, 24,  0},
        {255, 28,  0},
        {255, 32,  0},
        {255, 36,  0},
        {255, 40,  0},
        {255, 44,  0},
        {255, 48,  0},
        {255, 52,  0},
        {255, 56,  0},
        {255, 60,  0},
        {255, 64,  0},
        {255, 68,  0},
        {255, 72,  0},
        {255, 76,  0},
        {255, 80,  0},
        {255, 84,  0},
        {255, 88,  0},
        {255, 92,  0},
        {255, 96,  0},
        {255, 100, 0},
        {255, 104, 0},
        {255, 108, 0},
        {255, 112, 0},
        {255, 116, 0},
        {255, 120, 0},
        {255, 124, 0},
        {255, 128, 0},
        {255, 132, 0},
        {255, 136, 0},
        {255, 140, 0},
        {255, 144, 0},
        {255, 148, 0},
        {255, 152, 0},
        {255, 156, 0},
        {255, 160, 0},
        {255, 164, 0},
        {255, 168, 0},
        {255, 172, 0},
        {255, 176, 0},
        {255, 180, 0},
        {255, 184, 0},
        {255, 188, 0},
        {255, 192, 0},
        {255, 196, 0},
        {255, 200, 0},
        {255, 204, 0},
        {255, 208, 0},
        {255, 212, 0},
        {255, 216, 0},
        {255, 220, 0},
        {255, 224, 0},
        {255, 228, 0},
        {255, 232, 0},
        {255, 236, 0},
        {255, 240, 0},
        {255, 244, 0},
        {255, 248, 0},
        {255, 252, 0},
        {254, 255, 2},
        {250, 255, 6},
        {246, 255, 10},
        {242, 255, 14},
        {238, 255, 18},
        {234, 255, 22},
        {230, 255, 26},
        {226, 255, 30},
        {222, 255, 34},
        {218, 255, 38},
        {214, 255, 42},
        {210, 255, 46},
        {206, 255, 50},
        {202, 255, 54},
        {198, 255, 58},
        {194, 255, 62},
        {190, 255, 66},
        {186, 255, 70},
        {182, 255, 74},
        {178, 255, 78},
        {174, 255, 82},
        {170, 255, 86},
        {166, 255, 90},
        {162, 255, 94},
        {158, 255, 98},
        {154, 255, 102},
        {150, 255, 106},
        {146, 255, 110},
        {142, 255, 114},
        {138, 255, 118},
        {134, 255, 122},
        {130, 255, 126},
        {126, 255, 130},
        {122, 255, 134},
        {118, 255, 138},
        {114, 255, 142},
        {110, 255, 146},
        {106, 255, 150},
        {102, 255, 154},
        {98,  255, 158},
        {94,  255, 162},
        {90,  255, 166},
        {86,  255, 170},
        {82,  255, 174},
        {78,  255, 178},
        {74,  255, 182},
        {70,  255, 186},
        {66,  255, 190},
        {62,  255, 194},
        {58,  255, 198},
        {54,  255, 202},
        {50,  255, 206},
        {46,  255, 210},
        {42,  255, 214},
        {38,  255, 218},
        {34,  255, 222},
        {30,  255, 226},
        {26,  255, 230},
        {22,  255, 234},
        {18,  255, 238},
        {14,  255, 242},
        {10,  255, 246},
        {6,   255, 250},
        {1,   255, 254},
        {0,   252, 255},
        {0,   248, 255},
        {0,   244, 255},
        {0,   240, 255},
        {0,   236, 255},
        {0,   232, 255},
        {0,   228, 255},
        {0,   224, 255},
        {0,   220, 255},
        {0,   216, 255},
        {0,   212, 255},
        {0,   208, 255},
        {0,   204, 255},
        {0,   200, 255},
        {0,   196, 255},
        {0,   192, 255},
        {0,   188, 255},
        {0,   184, 255},
        {0,   180, 255},
        {0,   176, 255},
        {0,   172, 255},
        {0,   168, 255},
        {0,   164, 255},
        {0,   160, 255},
        {0,   156, 255},
        {0,   152, 255},
        {0,   148, 255},
        {0,   144, 255},
        {0,   140, 255},
        {0,   136, 255},
        {0,   132, 255},
        {0,   128, 255},
        {0,   124, 255},
        {0,   120, 255},
        {0,   116, 255},
        {0,   112, 255},
        {0,   108, 255},
        {0,   104, 255},
        {0,   100, 255},
        {0,   96,  255},
        {0,   92,  255},
        {0,   88,  255},
        {0,   84,  255},
        {0,   80,  255},
        {0,   76,  255},
        {0,   72,  255},
        {0,   68,  255},
        {0,   64,  255},
        {0,   60,  255},
        {0,   56,  255},
        {0,   52,  255},
        {0,   48,  255},
        {0,   44,  255},
        {0,   40,  255},
        {0,   36,  255},
        {0,   32,  255},
        {0,   28,  255},
        {0,   24,  255},
        {0,   20,  255},
        {0,   16,  255},
        {0,   12,  255},
        {0,   8,   255},
        {0,   4,   255},
        {0,   0,   255},
        {0,   0,   252},
        {0,   0,   248},
        {0,   0,   244},
        {0,   0,   240},
        {0,   0,   236},
        {0,   0,   232},
        {0,   0,   228},
        {0,   0,   224},
        {0,   0,   220},
        {0,   0,   216},
        {0,   0,   212},
        {0,   0,   208},
        {0,   0,   204},
        {0,   0,   200},
        {0,   0,   196},
        {0,   0,   192},
        {0,   0,   188},
        {0,   0,   184},
        {0,   0,   180},
        {0,   0,   176},
        {0,   0,   172},
        {0,   0,   168},
        {0,   0,   164},
        {0,   0,   160},
        {0,   0,   156},
        {0,   0,   152},
        {0,   0,   148},
        {0,   0,   144},
        {0,   0,   140},
        {0,   0,   136},
        {0,   0,   132},
        {0,   0,   0},
};

void decodeToRainbow(uint8_t *srcData, uint8_t *dstData, int srcSize, int dstSize) {
    memset(dstData, 0, dstSize);
    auto shortDataPtr = reinterpret_cast<uint16_t *>(srcData);
    uint8_t *rainbowPtr = dstData;
    int depthRange = DEPTH_MAX - DEPTH_MIN;
    for (int i = 0; i < srcSize / 2; i++) {
        uint16_t depth = *shortDataPtr++;
        if (depth > DEPTH_MAX || depth < DEPTH_MIN) {
            depth = DEPTH_MAX;
        }
        float tmp = 1.0f * (depth - DEPTH_MIN) / depthRange;
        int value = 255 * tmp;
        *rainbowPtr++ = rainbowColorMap[value].r;
        *rainbowPtr++ = rainbowColorMap[value].g;
        *rainbowPtr++ = rainbowColorMap[value].b;
    }
    rainbowPtr = NULL;
    shortDataPtr = NULL;
}

uint8_t *depthAlignToColor(uint8_t *colorData, uint8_t *depthData, int colorW, int colorH,
                           int depthW, int depthH, float alpha) {
    uint32_t src_x, src_y;
    uint8_t *depthPixel;
    uint8_t *rgbPixel;

    rgbPixel = colorData;
    if (alpha <= 0) {
        return rgbPixel;
    } else if (alpha > 1) {
        alpha = 1;
    }

    float scale_w = (float) colorW / (float) depthW;
    // float scale_h = (float) colorH / (float) depthH;
    float scale_h = (float) scale_w;
    auto depthDataSize = depthW * depthH * 3;
    auto colorDataSize = colorW * colorH * 3;

    for (int h = 0; h < colorH; h++) {
        for (int w = 0; w < colorW; w++) {
            src_x = (int) ((float) w / scale_w);
            src_y = (int) ((float) h / scale_h);

            depthPixel = (depthData + (src_y * depthW + src_x) * 3);

            // check bound
            if(depthPixel - depthData + 3 > depthDataSize || rgbPixel - colorData + 3 > colorDataSize) {
                continue;
            }

            //不渲染深度为0数据
            if(depthPixel[0] == 0 && depthPixel[1] == 0 && depthPixel[2] == 0) {
                depthPixel += 3;
                rgbPixel += 3;
                continue;
            }

            *rgbPixel = (uint8_t) ((float) (*rgbPixel) * (1.0 - alpha) +
                                   (float) (*depthPixel) * alpha);
            rgbPixel++;
            depthPixel++;
            *rgbPixel = (uint8_t) ((float) (*rgbPixel) * (1.0 - alpha) +
                                   (float) (*depthPixel) * alpha);
            rgbPixel++;
            depthPixel++;
            *rgbPixel = (uint8_t) ((float) (*rgbPixel) * (1.0 - alpha) +
                                   (float) (*depthPixel) * alpha);
            rgbPixel++;
        }
    }
//    rgbPixel = colorData;

    return colorData;
}

void uyvyToRgb(uint8_t *src, uint8_t *dst, int width, int height) {
    uint8_t *srcPtr = src;
    uint8_t *dstPtr = dst;
    int y, cb, cr;
    int r, g, b;

    for (int i = 0, c = width * height / 2; i < c; i++) {
        cb = srcPtr[0];
        y = srcPtr[1];
        cr = srcPtr[2];

        b = MAX(0, MIN(255, 1.164 * (y - 16) + 2.018 * (cb - 128)));
        g = MAX(0, MIN(255, 1.164 * (y - 16) - 0.813 * (cr - 128) - 0.391 * (cb - 128)));
        r = MAX(0, MIN(255, 1.164 * (y - 16) + 1.596 * (cr - 128)));

        dstPtr[0] = r;
        dstPtr[1] = g;
        dstPtr[2] = b;

        y = srcPtr[3];

        b = MAX(0, MIN(255, 1.164 * (y - 16) + 2.018 * (cb - 128)));
        g = MAX(0, MIN(255, 1.164 * (y - 16) - 0.813 * (cr - 128) - 0.391 * (cb - 128)));
        r = MAX(0, MIN(255, 1.164 * (y - 16) + 1.596 * (cr - 128)));

        dstPtr[3] = r;
        dstPtr[4] = g;
        dstPtr[5] = b;

        srcPtr += 4;
        dstPtr += 6;
    }
}

void y8ToRGB(uint8_t *src, uint8_t *dst, int width, int height) {
    int srcSize = width * height;
    for (int i = 0; i < srcSize; i++) {
        uint8_t value = *(src + i);
        *(dst) = value;
        *(dst + 1) = value;
        *(dst + 2) = value;

        dst += 3;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_orbbecsdkexamples_utils_ImageUtils_nDepthToRgb(JNIEnv *env, jclass clazz,
                                                               jobject srcBuffer,
                                                               jobject dstBuffer) {
    uint8_t *srcPtr = static_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));
    jsize srcSize = env->GetDirectBufferCapacity(srcBuffer);

    uint8_t *dstPtr = static_cast<uint8_t *>(env->GetDirectBufferAddress(dstBuffer));
    jsize dstSize = env->GetDirectBufferCapacity(dstBuffer);

    decodeToRainbow(srcPtr, dstPtr, srcSize, dstSize);
}
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_orbbec_orbbecsdkexamples_utils_ImageUtils_nDepthAlignToColor(JNIEnv *env, jclass clazz,
                                                                      jobject colorData,
                                                                      jobject depthData,
                                                                      jint colorW, jint colorH,
                                                                      jint depthW, jint depthH,
                                                                      jfloat alpha) {
    uint8_t *colorPtr = static_cast<uint8_t *>(env->GetDirectBufferAddress(colorData));
    uint8_t *depthPtr = static_cast<uint8_t *>(env->GetDirectBufferAddress(depthData));

    uint8_t *resultPtr = depthAlignToColor(colorPtr, depthPtr, colorW, colorH, depthW, depthH, alpha);

    jsize resultSize = env->GetDirectBufferCapacity(colorData);
    jbyteArray result = env->NewByteArray(resultSize);
    env->SetByteArrayRegion(result, 0, resultSize, reinterpret_cast<const jbyte *>(resultPtr));

    return result;
}
extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_orbbecsdkexamples_utils_ImageUtils_nUyvyToRgb(JNIEnv *env, jclass clazz,
                                                              jobject srcBuffer,
                                                              jobject dstBuffer,
                                                              jint width,
                                                              jint height) {
    uint8_t *srcPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));
    uint8_t *dstPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(dstBuffer));
    uyvyToRgb(srcPtr, dstPtr, width, height);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_orbbecsdkexamples_utils_ImageUtils_nY8ToRgb(JNIEnv *env, jclass clazz,
                                                            jobject srcBuffer,
                                                            jobject dstBuffer,
                                                            jint width,
                                                            jint height) {

    uint8_t *srcPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));
    uint8_t *dstPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(dstBuffer));
    y8ToRGB(srcPtr, dstPtr, width, height);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orbbec_orbbecsdkexamples_utils_ImageUtils_nScalePrecisionToDepthPixel(JNIEnv *env, jclass clazz,
                                                    jobject depthBuffer, jint width, jint height, jint size, jfloat scale) {
    uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(depthBuffer);
    if (nullptr == data || size == 0) {
        LOGE("nScalePrecisionToDepthPixel failed. data = 0X%08x, depthBuffer size: %d", (long)data, size);
        return;
    }
    if (width * height * (sizeof(unsigned short)/sizeof(uint8_t)) < size != 0) {
        LOGE("nScalePrecisionToDepthPixel failed. invalid width and height. width: %d, height: %d, size: %ld", width, height, size);
        return;
    }

    unsigned short *pixel = (unsigned short *)data;
    // 仅支持YUYV和Y16
    for (int i = 0, N = width * height; i < N; i++) {
        int value = *(pixel + i) * scale;
        *(pixel + i) = (unsigned short)value;
    }
}