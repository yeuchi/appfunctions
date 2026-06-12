/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appfunctions.agent.ui.screens.agentdemo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.appfunctions.agent.domain.appfunction.ConnectedAppInfo

/** Screenshot test for [ConnectedAppsScreen]. */
@PreviewTest
@Preview(showBackground = true)
@Composable
fun ConnectedAppsScreenScreenshotPreview() {
    ConnectedAppsScreenContent(
        uiState =
            ConnectedAppsUiState(
                connectedApps =
                    listOf(
                        ConnectedAppInfo(
                            "com.example.app1",
                            "App 1",
                            null,
                            true,
                            "This tool allows management of the Android launcher home screen.",
                        ),
                        ConnectedAppInfo(
                            "com.example.app2",
                            "App 2",
                            null,
                            false,
                            "Another app description.",
                        ),
                    ),
            ),
        onBack = {},
        onToggleApp = { _, _ -> },
    )
}
