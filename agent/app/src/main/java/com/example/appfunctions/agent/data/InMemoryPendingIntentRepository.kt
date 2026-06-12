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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** Singleton implementation mapping dynamic pendingIntentId to PendingIntent. */
@Singleton
class InMemoryPendingIntentRepository
    @Inject
    constructor() : PendingIntentRepository {
        private val actions = mutableMapOf<String, PendingIntent>()
        private val _pendingIntentIds = MutableStateFlow<Set<String>>(emptySet())

        override val pendingIntentIds: StateFlow<Set<String>> = _pendingIntentIds.asStateFlow()

        override fun savePendingIntent(
            pendingIntentId: String,
            pendingIntent: PendingIntent,
        ) {
            actions[pendingIntentId] = pendingIntent
            _pendingIntentIds.update { it + pendingIntentId }
        }

        override fun consumePendingIntent(pendingIntentId: String): PendingIntent? {
            val action = actions.remove(pendingIntentId)
            _pendingIntentIds.update { it - pendingIntentId }
            return action
        }
    }
