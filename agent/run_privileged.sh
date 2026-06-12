#!/bin/bash
#
# Copyright 2026 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

# run_privileged.sh
#
# This script builds, installs, and launches the AppFunction Testing Agent app
# with elevated privileges using ShellIdentityInstrumentation.
# This allows the app to access privileged AppFunctions APIs.

PACKAGE_NAME="com.example.appfunctions.agent"
INSTRUMENTATION_NAME="${PACKAGE_NAME}/.ShellIdentityInstrumentation"
BUILD_TYPE="debug"
FLAVOR="standard"
API_KEY=""
SERIAL=""

usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -b, --build        Force rebuild of the app before installing"
    echo "  -r, --release      Use release build instead of debug"
    echo "  -f, --flavor FLAVOR Specify build flavor (default: standard)"
    echo "  -k, --api-key KEY   Gemini API key (required for retail builds)"
    echo "  -s, --serial SERIAL ADB device serial number"
    echo "  -h, --help         Show this help message"
    echo ""
    echo "Description:"
    echo "  By default, this script will install the existing APK if found."
    echo "  If the APK is missing, it will build it automatically."
    echo "  Use -b or --build to force a rebuild."
}

REBUILD=false
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -b|--build) REBUILD=true ;;
        -r|--release) BUILD_TYPE="release" ;;
        -f|--flavor) FLAVOR="$2"; shift ;;
        -k|--api-key) API_KEY="$2"; shift ;;
        -s|--serial) SERIAL="$2"; shift ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown parameter: $1"; usage; exit 1 ;;
    esac
    shift
done

# Standardize names
FLAVOR_LOWER=$(echo "$FLAVOR" | tr '[:upper:]' '[:lower:]')
BUILD_TYPE_LOWER=$(echo "$BUILD_TYPE" | tr '[:upper:]' '[:lower:]')

FLAVOR_CAP=$(echo "${FLAVOR_LOWER:0:1}" | tr '[:lower:]' '[:upper:]')${FLAVOR_LOWER:1}
BUILD_TYPE_CAP=$(echo "${BUILD_TYPE_LOWER:0:1}" | tr '[:lower:]' '[:upper:]')${BUILD_TYPE_LOWER:1}

APK_PATH="app/build/outputs/apk/${FLAVOR_LOWER}/${BUILD_TYPE_LOWER}/app-${FLAVOR_LOWER}-${BUILD_TYPE_LOWER}.apk"
APK_LABEL="${FLAVOR_CAP} ${BUILD_TYPE_CAP}"

if [ "$REBUILD" = true ] || [ ! -f "$APK_PATH" ]; then
    echo "🔨 Building $APK_LABEL app..."
    TASK_NAME="assemble${FLAVOR_CAP}${BUILD_TYPE_CAP}"
    
    GRADLE_ARGS=("$TASK_NAME")
    if [ -n "$API_KEY" ]; then
        GRADLE_ARGS+=("-PGEMINI_API_KEY=$API_KEY")
    fi
    
    ./gradlew "${GRADLE_ARGS[@]}"
fi

# Device disambiguation for install
ADB_CMD="adb"
if [ -n "$SERIAL" ]; then
    ADB_CMD="adb -s $SERIAL"
fi

if [ -f "$APK_PATH" ]; then
    echo "📱 Installing $APK_PATH..."
    $ADB_CMD install -r "$APK_PATH"
else
    echo "❌ $APK_LABEL APK not found at $APK_PATH even after build attempt."
    exit 1
fi

# Call startAppFunction Testing Agent to handle the launch logic
START_ARGS=()
if [ -n "$SERIAL" ]; then
    START_ARGS+=("-s" "$SERIAL")
fi
./startAppFunctionTestingAgent "${START_ARGS[@]}"
