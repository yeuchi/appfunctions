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
package com.example.appfunctions.agent.domain.troubleshoot

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CheckMainlineVersionUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * Executes the check.
         *
         * @return True if sufficient, false otherwise.
         */
        operator fun invoke(): Boolean {
            val pm = context.packageManager
            return try {
                val packageInfo =
                    pm.getPackageInfo(
                        APPSEARCH_MODULE_NAME,
                        PackageManager.PackageInfoFlags.of(PackageManager.MATCH_APEX.toLong()),
                    )
                val versionCode = packageInfo.longVersionCode
                versionCode > REQUIRED_VERSION
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        companion object {
            const val APPSEARCH_MODULE_NAME = "com.google.android.appsearch"
            const val REQUIRED_VERSION = 360743060L
        }
    }
