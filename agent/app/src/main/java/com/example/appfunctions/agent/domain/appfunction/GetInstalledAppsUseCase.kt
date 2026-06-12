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
package com.example.appfunctions.agent.domain.appfunction

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Use case to get all installed apps on the device. */
class GetInstalledAppsUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * Executes the use case.
         *
         * @return A list of AppInfo for all installed apps, sorted by label.
         */
        operator fun invoke(): List<AppInfo> {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(0)
            return apps
                .map { appInfo ->
                    val label = pm.getApplicationLabel(appInfo)?.toString() ?: appInfo.packageName
                    val icon =
                        try {
                            pm.getApplicationIcon(appInfo)
                        } catch (e: Exception) {
                            null
                        }
                    AppInfo(appInfo.packageName, label, icon)
                }
                .sortedBy { it.label }
        }
    }
