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
package com.example.appfunctions.agent.domain.pendingintent

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Use case to launch a PendingIntent with background activity start allowed. */
class LaunchPendingIntentUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * Launches the given [pendingIntent].
         *
         * @param pendingIntent The PendingIntent to launch.
         * @return A [Result] indicating success or failure.
         */
        operator fun invoke(pendingIntent: PendingIntent): Result<Unit> {
            val options = ActivityOptions.makeBasic()
            options.setPendingIntentBackgroundActivityStartMode(
                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED,
            )
            val bundle = options.toBundle()
            val fillInIntent = Intent().apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            return try {
                pendingIntent.send(context, 0, fillInIntent, null, null, null, bundle)
                Result.success(Unit)
            } catch (e: PendingIntent.CanceledException) {
                Result.failure(e)
            }
        }
    }
