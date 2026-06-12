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
package com.example.appfunctions.agent.data

import android.app.PendingIntent
import kotlinx.coroutines.flow.StateFlow

/** Repository to manage transient actions returned by AppFunctions. */
interface PendingIntentRepository {
    /** Returns a flow of active pending action IDs. */
    val pendingIntentIds: StateFlow<Set<String>>

    /** Caches a dynamic PendingIntent transiently. */
    fun savePendingIntent(
        pendingIntentId: String,
        pendingIntent: PendingIntent,
    )

    /** Consumes a dynamic PendingIntent, making it no longer valid. */
    fun consumePendingIntent(pendingIntentId: String): PendingIntent?
}
