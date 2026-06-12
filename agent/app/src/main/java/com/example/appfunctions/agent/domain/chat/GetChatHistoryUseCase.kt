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
package com.example.appfunctions.agent.domain.chat

import com.example.appfunctions.agent.data.ChatRepository
import com.example.appfunctions.agent.data.db.entities.MessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Use case to retrieve chat history for a specific thread. */
class GetChatHistoryUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        /**
         * Executes the use case.
         *
         * @param threadId The ID of the thread to retrieve history for.
         * @return A flow of messages for the thread.
         */
        operator fun invoke(threadId: String): Flow<List<MessageEntity>> {
            return chatRepository.getMessagesForThread(threadId)
        }
    }
