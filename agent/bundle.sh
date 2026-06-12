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

OUTPUT_DIR="."
CURRENT_DATETIME=$(date +"%Y-%m-%d_%H%M%S")
VERSION_NAME=$(grep "versionName" app/build.gradle.kts | head -n 1 | sed -E 's/.*versionName = "(.*)"/\1/')
BUILD_TYPE="debug"
FLAVOR="standard"
API_KEY=""

usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -o, --output-dir DIR  Directory to save the bundle (default: current directory)"
    echo "  -r, --release         Build release version"
    echo "  -f, --flavor FLAVOR   Specify build flavor (default: standard)"
    echo "  -k, --api-key KEY     Gemini API key (required for retail builds)"
    echo "  -h, --help            Show this help message"
}

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -o|--output-dir) OUTPUT_DIR="$2"; shift ;;
        -r|--release) BUILD_TYPE="release" ;;
        -f|--flavor) FLAVOR="$2"; shift ;;
        -k|--api-key) API_KEY="$2"; shift ;;
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

PACKAGE_NAME="AppFunctionTestingAgent_${FLAVOR_LOWER}_${VERSION_NAME}_${CURRENT_DATETIME}"
ZIP_NAME="${PACKAGE_NAME}.zip"

echo "🔨 Building $FLAVOR_CAP $BUILD_TYPE_CAP APK..."
TASK_NAME="assemble${FLAVOR_CAP}${BUILD_TYPE_CAP}"

GRADLE_ARGS=("$TASK_NAME")
if [ -n "$API_KEY" ]; then
    GRADLE_ARGS+=("-PGEMINI_API_KEY=$API_KEY")
fi

./gradlew "${GRADLE_ARGS[@]}"

APK_PATH="app/build/outputs/apk/${FLAVOR_LOWER}/${BUILD_TYPE_LOWER}/app-${FLAVOR_LOWER}-${BUILD_TYPE_LOWER}.apk"
APK_LABEL="${FLAVOR_CAP} ${BUILD_TYPE_CAP}"

if [ ! -f "$APK_PATH" ]; then
    echo "❌ $APK_LABEL APK not found at $APK_PATH"
    exit 1
fi

TEMP_DIR="./.tmp_test_suite"
SUITE_DIR="${TEMP_DIR}/${PACKAGE_NAME}"
mkdir -p "$SUITE_DIR"

echo "📂 Preparing test suite files..."
cp instruction.md "${SUITE_DIR}/README.md"
cp startAppFunctionTestingAgent "${SUITE_DIR}/"
cp "$APK_PATH" "${SUITE_DIR}/AppFunctionTestingAgent.apk"

# Make sure script is executable in the bundle
chmod +x "${SUITE_DIR}/startAppFunctionTestingAgent"

echo "🤐 Zipping test suite..."
# Resolve relative output dir to absolute to avoid issues when changing directory
mkdir -p "$OUTPUT_DIR"
ABSOLUTE_OUTPUT_DIR=$(cd "$OUTPUT_DIR" && pwd)
TARGET_ZIP="${ABSOLUTE_OUTPUT_DIR}/${ZIP_NAME}"

# Zip the content of the suite dir
(cd "$TEMP_DIR" && zip -r "$TARGET_ZIP" "$PACKAGE_NAME")

echo "🧹 Cleaning up..."
rm -rf "$TEMP_DIR"

echo "✅ Test suite created at ${TARGET_ZIP}"
