#!/bin/bash
export ANDROID_HOME=/home/z/android-sdk
export ANDROID_SDK_ROOT=/home/z/android-sdk
cd /home/z/my-project/AIColorPredictionAnalytics
echo "BUILD_START=$(date)" > /home/z/my-project/build.log
/home/z/tools/gradle-8.5/bin/gradle assembleDebug --no-daemon >> /home/z/my-project/build.log 2>&1
echo "EXIT_CODE=$?" >> /home/z/my-project/build.log
echo "BUILD_END=$(date)" >> /home/z/my-project/build.log
