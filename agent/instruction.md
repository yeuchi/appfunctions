# AppFunction Testing Agent Instructions

This document explains how to use AppFunction Testing Agent as a testing agent.

## Purpose

The testing agent supports
* Troubleshooting - Help to identity common problems with AppFunction integration.
* Deterministic Debugging - Manually examine the AppFunction and invoke it with deterministic input.
* LLM-based Debugging - Connect to LLM as actual agent to test out AppFunctions.

## Prerequisites

1.  **Install the App**: You must manually install the debug APK on your device or emulator before running the launch script.
    ```bash
    adb install -r AppFunctionTestingAgent.apk
    ```
## How to Launch the App

To launch the app with the necessary privileges for testing, use the provided helper script:

1.  Ensure your device is connected and authorized via ADB.
2.  Run the `startAppFunctionTestingAgent` script:
    ```bash
    ./startAppFunctionTestingAgent
    ```
