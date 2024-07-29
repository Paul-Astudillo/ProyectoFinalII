#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/bitmap.h>
#include <vector>
#include <android/log.h>
#include <sstream>

#define LOG_TAG "Predict"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


using namespace cv;
using namespace std;
using namespace cv::ml;

string faceCascadePath;
string eyeCascadePath;
string noseCascadePath;
string mouthCascadePath;

void bitmapToMat(JNIEnv *env, jobject bitmap, cv::Mat &dst, jboolean needUnPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (needUnPremultiplyAlpha) cvtColor(tmp, dst, cv::COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, cv::COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        LOGE("Exception in bitmapToMat: %s", e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {bitmapToMat}");
        LOGE("Unknown exception in bitmapToMat");
        return;
    }
}

void matToBitmap(JNIEnv *env, cv::Mat src, jobject bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(src.dims == 2 && info.height == (uint32_t) src.rows && info.width == (uint32_t) src.cols);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (src.type() == CV_8UC1) {
                cvtColor(src, tmp, cv::COLOR_GRAY2RGBA);
            } else if (src.type() == CV_8UC3) {
                cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
            } else if (src.type() == CV_8UC4) {
                if (needPremultiplyAlpha) cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if (src.type() == CV_8UC1) {
                cvtColor(src, tmp, cv::COLOR_GRAY2BGR565);
            } else if (src.type() == CV_8UC3) {
                cvtColor(src, tmp, cv::COLOR_RGB2BGR565);
            } else if (src.type() == CV_8UC4) {
                cvtColor(src, tmp, cv::COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        LOGE("Exception in matToBitmap: %s", e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {matToBitmap}");
        LOGE("Unknown exception in matToBitmap");
        return;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_projectparteii_MainActivity_initCascadePaths(
        JNIEnv *env,
        jobject /* this */,
        jstring faceCascade,
        jstring eyeCascade,
        jstring noseCascade,
        jstring mouthCascade) {
    const char *faceCascadeChars = env->GetStringUTFChars(faceCascade, 0);
    const char *eyeCascadeChars = env->GetStringUTFChars(eyeCascade, 0);
    const char *noseCascadeChars = env->GetStringUTFChars(noseCascade, 0);
    const char *mouthCascadeChars = env->GetStringUTFChars(mouthCascade, 0);

    faceCascadePath = std::string(faceCascadeChars);
    eyeCascadePath = std::string(eyeCascadeChars);
    noseCascadePath = std::string(noseCascadeChars);
    mouthCascadePath = std::string(mouthCascadeChars);

    env->ReleaseStringUTFChars(faceCascade, faceCascadeChars);
    env->ReleaseStringUTFChars(eyeCascade, eyeCascadeChars);
    env->ReleaseStringUTFChars(noseCascade, noseCascadeChars);
    env->ReleaseStringUTFChars(mouthCascade, mouthCascadeChars);

    LOGI("Face cascade path: %s", faceCascadePath.c_str());
    LOGI("Eye cascade path: %s", eyeCascadePath.c_str());
    LOGI("Nose cascade path: %s", noseCascadePath.c_str());
    LOGI("Mouth cascade path: %s", mouthCascadePath.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_projectparteii_MainActivity_detectFaces(
        JNIEnv *env,
        jobject instance,
        jobject bitmapIn,
        jobject bitmapOut) {

    //verificar
    CascadeClassifier faceCascade, eyesCascade, noseCascade, mouthCascade;
    if (!faceCascade.load(faceCascadePath) || !eyesCascade.load(eyeCascadePath) ||
        !noseCascade.load(noseCascadePath) || !mouthCascade.load(mouthCascadePath)) {
        LOGE("Error loading cascades");
        return env->NewStringUTF("Error loading cascades");
    }

    Mat img;
    bitmapToMat(env, bitmapIn, img, false);

    Mat gray;
    cvtColor(img, gray, COLOR_RGBA2GRAY);

    GaussianBlur(gray, gray, Size(5, 5), 1.5);
    equalizeHist(gray, gray);

    //rostros
    vector<Rect> faces;
    faceCascade.detectMultiScale(gray, faces, 1.1, 5, 0 | CASCADE_SCALE_IMAGE, Size(30, 30));

    std::stringstream detectionResults;

    for (size_t i = 0; i < faces.size(); i++) {
        rectangle(img, faces[i], Scalar(0, 0, 255), 3); // Rojo para rostros (BGR: 0, 0, 255)
        detectionResults << "Face: " << faces[i].x << ", " << faces[i].y << ", " << faces[i].width << ", " << faces[i].height << "\n";

        Mat faceROI = gray(faces[i]);
        vector<Rect> eyes, nose, mouth;

        //ojos
        eyesCascade.detectMultiScale(faceROI, eyes, 1.1, 5, 0 | CASCADE_SCALE_IMAGE, Size(20, 20));
        for (size_t j = 0; j < eyes.size(); j++) {
            Rect eyeRect = Rect(faces[i].x + eyes[j].x, faces[i].y + eyes[j].y, eyes[j].width, eyes[j].height);
            rectangle(img, eyeRect, Scalar(255, 0, 255), 3); // Morado para ojos (BGR: 255, 0, 255)
            detectionResults << "Eye: " << eyeRect.x << ", " << eyeRect.y << ", " << eyeRect.width << ", " << eyeRect.height << "\n";
        }

        //nariz
        noseCascade.detectMultiScale(faceROI, nose, 1.1, 5, 0 | CASCADE_SCALE_IMAGE, Size(20, 20));
        for (size_t k = 0; k < nose.size(); k++) {
            Rect noseRect = Rect(faces[i].x + nose[k].x, faces[i].y + nose[k].y, nose[k].width, nose[k].height);
            rectangle(img, noseRect, Scalar(0, 255, 0), 3); // Verde para nariz (BGR: 0, 255, 0)
            detectionResults << "Nose: " << noseRect.x << ", " << noseRect.y << ", " << noseRect.width << ", " << noseRect.height << "\n";
        }

        //boca
        mouthCascade.detectMultiScale(faceROI, mouth, 1.1, 5, 0 | CASCADE_SCALE_IMAGE, Size(30, 30));
        for (size_t l = 0; l < mouth.size(); l++) {
            Rect mouthRect = Rect(faces[i].x + mouth[l].x, faces[i].y + mouth[l].y + faces[i].height / 2, mouth[l].width, mouth[l].height);
            rectangle(img, mouthRect, Scalar(0, 0, 255), 3); // Rojo para boca (BGR: 0, 0, 255)
            detectionResults << "Mouth: " << mouthRect.x << ", " << mouthRect.y << ", " << mouthRect.width << ", " << mouthRect.height << "\n";
        }
    }

    matToBitmap(env, img, bitmapOut, false);

    return env->NewStringUTF(detectionResults.str().c_str());
}

const Size IMG_SIZE = Size(28, 28);
vector<string> category_labels = {"Camiseta/Top", "Pantalón", "Jersey", "Vestido", "Abrigo",
                                  "Sandalia", "Camisa", "Zapatilla", "Bolso", "Bota"};

void computeHOG(Mat& img, vector<float>& descriptors) {
    HOGDescriptor hog(
            IMG_SIZE,
            Size(14, 14),
            Size(7, 7),
            Size(7, 7),
            9
    );
    hog.compute(img, descriptors);
}

void removeWhiteBackground(Mat& img, Mat& output) {
    // Metodo similar a la de la parte 1 del proyecto
    Mat bgr;
    cvtColor(img, bgr, COLOR_RGBA2BGR);

    Mat hsv;
    cvtColor(bgr, hsv, COLOR_BGR2HSV);


    Scalar lower_white(0, 0, 200);
    Scalar upper_white(180, 20, 255);


    Mat mask;
    inRange(hsv, lower_white, upper_white, mask);


    bitwise_not(mask, mask);

    Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(5, 5));
    morphologyEx(mask, mask, MORPH_CLOSE, kernel);
    morphologyEx(mask, mask, MORPH_OPEN, kernel);

    img.copyTo(output, mask);
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_projectparteii_MainActivity_predict(JNIEnv *env, jobject instance, jobject bitmap, jstring modelPath) {
    Mat img;
    AndroidBitmapInfo info;
    void* pixels = 0;
    CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
    CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
    CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
    img.create(info.height, info.width, CV_8UC4);
    Mat tmp(info.height, info.width, CV_8UC4, pixels);
    tmp.copyTo(img);
    AndroidBitmap_unlockPixels(env, bitmap);

    //quitar el fondo blanco
    Mat preprocessed;
    removeWhiteBackground(img, preprocessed);

    Mat gray;
    cvtColor(preprocessed, gray, COLOR_RGBA2GRAY);

    resize(gray, gray, IMG_SIZE);

    //cargar el modelo
    const char *modelPathChars = env->GetStringUTFChars(modelPath, 0);
    Ptr<ANN_MLP> model = ANN_MLP::load(modelPathChars);
    env->ReleaseStringUTFChars(modelPath, modelPathChars);

    if (model.empty()) {
        LOGE("Error loading model");
        return env->NewStringUTF("Error loading model");
    }

    //HOG
    vector<float> descriptors;
    computeHOG(gray, descriptors);

    //que tenga el mismo dimencion
    if (descriptors.size() != model->getLayerSizes().at<int>(0)) {
        LOGE("El tamaño de los descriptores HOG (%zu) no coincide con el tamaño esperado por el modelo (%d).",
             descriptors.size(), model->getLayerSizes().at<int>(0));
        return env->NewStringUTF("Error: HOG descriptor size mismatch");
    }

    //convertir a Mat para predecir
    Mat sample(1, static_cast<int>(descriptors.size()), CV_32F);
    for (size_t j = 0; j < descriptors.size(); ++j) {
        sample.at<float>(0, static_cast<int>(j)) = descriptors[j];
    }

    //predicción
    Mat response;
    model->predict(sample, response);
    Point maxLoc;
    minMaxLoc(response, 0, 0, 0, &maxLoc);
    int predictedLabel = maxLoc.x;

    LOGI("Predicted label: %d", predictedLabel);

    //contorno de la prenda de vestir en la imagen original
    cvtColor(img, gray, COLOR_RGBA2GRAY);
    threshold(gray, gray, 0, 255, THRESH_BINARY_INV + THRESH_OTSU);

    vector<vector<Point>> contours;
    findContours(gray, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    if (!contours.empty()) {
        size_t largestContourIdx = 0;
        double largestArea = 0;
        for (size_t i = 0; i < contours.size(); ++i) {
            double area = contourArea(contours[i]);
            if (area > largestArea) {
                largestArea = area;
                largestContourIdx = i;
            }
        }

        Rect boundingBox = cv::boundingRect(contours[largestContourIdx]);
        rectangle(img, boundingBox, Scalar(0, 255, 0), 2);

        double fontScale = 3.0;
        int thickness = 3;
        putText(img, category_labels[predictedLabel], Point(boundingBox.x, boundingBox.y - 10), FONT_HERSHEY_SIMPLEX, fontScale, Scalar(0, 255, 0), thickness);
    }


    matToBitmap(env, img, bitmap, false);

    //prediccion
    return env->NewStringUTF(category_labels[predictedLabel].c_str());
}
