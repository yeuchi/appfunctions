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

/** Use case to check if the application has the privileged permission to execute app functions. */
class CheckAppFunctionPermissionUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * Executes the check.
         *
         * @return True if the permission is granted, false otherwise.
         */
        operator fun invoke(): Boolean {
            return context.checkSelfPermission(
                PERMISSION_EXECUTE_APP_FUNCTIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }

        companion object {
            private const val PERMISSION_EXECUTE_APP_FUNCTIONS =
                "android.permission.EXECUTE_APP_FUNCTIONS"
        }
    }
