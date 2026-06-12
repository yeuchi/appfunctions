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
package com.example.appfunctions.agent.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.appfunctions.agent.data.LlmModel

@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey val threadId: String,
    val createdAt: Long,
    val llmModel: LlmModel,
    val latestInteractionId: String? = null,
)
