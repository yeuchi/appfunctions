# AppFunctions Samples

This repository contains Android sample code demonstrating the usage of [AppFunctions](https://developer.android.com/ai/appfunctions) to provide application functionality to system services and agents.

## Projects

This repository contains two primary projects. An API sample and a testing agent.
### 0. Exercise original

- Chat application launches fine.

  <img width="200" alt="Screenshot 2026-06-14 at 3 10 53 PM" src="https://github.com/user-attachments/assets/16cdcf07-b498-4321-a453-bdbe6afee181" />

- Agent requires the following. \
  a.  Manifest permission: uses-permission android:name="android.permission.EXECUTE_APP_FUNCTIONS" \
  b.  Your own Gemini API Key \
  c.  Launch via script: ./run_privileged.sh --build --flavor retail --api-key \
      Debug launch produces below error "The app does not have EXECUTE_APP_FUNCTIONS permission" \
  <img width="600" alt="Screenshot 2026-06-14 at 3 10 44 PM" src="https://github.com/user-attachments/assets/e93bd313-0cb4-468f-92c4-4e402e323e90" />



### 1. ChatApp sample

The **ChatApp** sample showcases how a communication app can provide its core features to the Android intelligence system. It serves as an educational reference for implementing AppFunctions.
It is a functional chat application built with [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Hilt](https://developer.android.com/training/dependency-injection/hilt-android), demonstrating AppFunctions usage across both mobile and Wear OS form factors.

- **Location**: [ChatApp](ChatApp/)
- **Structure**:
    - `app`: Main mobile chat application.
    - `wear`: Wear OS companion chat application.
    - `shared`: Common data repositories, ViewModels, and AppFunction service definitions.
- **Features**: Sending messages, searching contacts, and initiating calls via AppFunctions.

### 2. AppFunctions Testing Agent

The **AppFunctions Testing Agent** is a testing and debugging tool for Android AppFunctions. It is designed to help developers verify their integrations, troubleshoot execution issues, and evaluate AppFunctions using both manual deterministic inputs and an LLM-based agent.

- **Location**: [agent](agent/)
- **Documentation**: See the [Agent README](agent/README.md) for full building, running, and testing instructions.
- **Features**: Privileged AppFunction execution, manual debugging, test automation, and LLM agent evaluations.

## Setup Guide

Follow these steps to explore and run the samples:

1.  **Prerequisites**:
    - [Android Studio](https://developer.android.com/studio).
    - Android SDK 36+.
    - A device or emulator running Android 16 (API 36) or higher.
2.  **Clone the Repository**:
    ```bash
    git clone https://github.com/android/appfunctions.git
    ```
3.  **Open in Android Studio**:
    - Open Android Studio and select **Open**.
    - Navigate to the specific project directory (e.g., [`ChatApp`](ChatApp/) or [`agent`](agent/)) and click **OK**.
4.  **Sync and Build**: Wait for the Gradle sync to complete and build the project (**Build > Make Project**).
5.  **Run**: For `ChatApp`, click **Run > Run 'app'** to deploy the mobile sample to your phone/emulator, or **Run > Run 'wear'** to deploy the companion app to a Wear OS device/emulator. For `agent`, refer to the [Agent README](agent/README.md) for privileged launch instructions (`./run_privileged.sh`).

## Contributing

We'd love to accept your patches and contributions!

- **Contributor License Agreement (CLA)**: All contributors must sign the [Google CLA](https://cla.developers.google.com/).
- **Code Style**: We use [Spotless](https://github.com/diffplug/spotless) to maintain consistent formatting. Run `./gradlew spotlessApply` before committing.
- **Testing**: Ensure all new functionality is covered by unit or instrumentation tests.
- **Reviews**: All submissions are reviewed via GitHub Pull Requests.

See [CONTRIBUTING.md](CONTRIBUTING.md) for more detailed information.

## Resources

- **[AppFunctions Developer Guide](https://developer.android.com/ai/appfunctions)**: Comprehensive overview of the AppFunctions API and how to integrate it into your own apps.
- **[AppFunctions Skills](https://github.com/android/skills/tree/main/device-ai/appfunctions)**: Specialized skills unlocking AI driven AppFunction development.

---

License: Apache 2.0. See [LICENSE](LICENSE) for details.
