#!/bin/bash

set -e

gradleTarget=assembleDebug
target="apk/debug"
file=app-debug
tag=debug

if [ "$1" == "release" ];then
    gradleTarget=assembleRelease
    target="apk/release"
    file=app-release-unsigned
    tag=release
elif [ "$1" == "universal" ];then
    gradleTarget=packageReleaseUniversalApk
    target="apk_from_bundle/release"
    file=app-release-universal-unsigned
    tag=universal
fi
JAVA_HOME=/opt/android-studio/jbr/ ./gradlew $gradleTarget ${@:2}
